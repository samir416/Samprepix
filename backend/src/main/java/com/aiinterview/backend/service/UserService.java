package com.aiinterview.backend.service;

import com.aiinterview.backend.entity.AccountStatus;
import com.aiinterview.backend.entity.EmailVerificationToken;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.entity.UserProfile;
import com.aiinterview.backend.model.RegisterResponse;
import com.aiinterview.backend.model.UserResponse;
import com.aiinterview.backend.repository.EmailVerificationTokenRepository;
import com.aiinterview.backend.repository.PasswordResetTokenRepository;
import com.aiinterview.backend.repository.UserProfileRepository;
import com.aiinterview.backend.repository.UserRepository;
import com.aiinterview.backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final EmailService emailService;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    private final SecureRandom secureRandom =
            new SecureRandom();

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public UserService(
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            PasswordResetTokenRepository tokenRepository,
            EmailVerificationTokenRepository emailVerificationTokenRepository,
            EmailService emailService) {

        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.tokenRepository = tokenRepository;
        this.emailVerificationTokenRepository =
                emailVerificationTokenRepository;
        this.emailService = emailService;
    }

    @Transactional
    public RegisterResponse saveUser(User user) {

        if (user == null) {
            return new RegisterResponse(
                    false,
                    "Invalid registration request!",
                    null,
                    null
            );
        }

        String email = normalizeEmail(user.getEmail());
        String username = normalizeUsername(user.getUsername());
        String name = normalizeName(user.getName());
        String password = user.getPassword();

        if (!isValidEmail(email)) {
            return new RegisterResponse(
                    false,
                    "Invalid email!",
                    null,
                    null
            );
        }

        if (username == null
                || username.length() < 3
                || username.length() > 50
                || !username.matches("[A-Za-z0-9_.-]+")) {

            return new RegisterResponse(
                    false,
                    "Invalid username!",
                    null,
                    null
            );
        }

        if (password == null
                || password.length() < 8
                || password.length() > 72) {

            return new RegisterResponse(
                    false,
                    "Password must contain 8 to 72 characters!",
                    null,
                    null
            );
        }

        if (name != null && name.length() > 100) {
            return new RegisterResponse(
                    false,
                    "Invalid name!",
                    null,
                    null
            );
        }

        if (userRepository.existsByEmail(email)) {
            return new RegisterResponse(
                    false,
                    "Email already exists!",
                    null,
                    null
            );
        }

        if (userRepository.existsByUsername(username)) {
            return new RegisterResponse(
                    false,
                    "Username already exists!",
                    null,
                    null
            );
        }

        user.setEmail(email);
        user.setUsername(username);
        user.setName(name);
        user.setPassword(
                passwordEncoder.encode(password)
        );
        user.setEmailVerified(true);
        user.setAccountStatus(AccountStatus.ACTIVE);

        userRepository.saveAndFlush(user);

        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setProfileCompleted(false);

        user.setProfile(profile);
        userProfileRepository.saveAndFlush(profile);

        String token =
                JwtUtil.generateToken(user.getEmail());

        return new RegisterResponse(
                true,
                "Account created successfully!",
                user.getEmail(),
                token
        );
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {

        if (id == null || id <= 0) {
            return null;
        }

        return userRepository
                .findById(id)
                .orElse(null);
    }

    @Transactional
    public User updateUser(
            Long id,
            User updatedUser) {

        if (id == null
                || id <= 0
                || updatedUser == null) {

            return null;
        }

        Optional<User> existingUser =
                userRepository.findById(id);

        if (existingUser.isEmpty()) {
            return null;
        }

        User user = existingUser.get();

        if (updatedUser.getUsername() != null
                && !updatedUser.getUsername().isBlank()) {

            String username =
                    normalizeUsername(
                            updatedUser.getUsername()
                    );

            if (username == null
                    || username.length() < 3
                    || username.length() > 50
                    || !username.matches(
                            "[A-Za-z0-9_.-]+"
                    )) {

                throw new IllegalArgumentException(
                        "Invalid username!"
                );
            }

            if (!username.equals(user.getUsername())
                    && userRepository
                    .existsByUsername(username)) {

                throw new IllegalArgumentException(
                        "Username already exists!"
                );
            }

            user.setUsername(username);
        }

        if (updatedUser.getEmail() != null
                && !updatedUser.getEmail().isBlank()) {

            String email =
                    normalizeEmail(
                            updatedUser.getEmail()
                    );

            if (!isValidEmail(email)) {
                throw new IllegalArgumentException(
                        "Invalid email!"
                );
            }

            if (!email.equalsIgnoreCase(user.getEmail())
                    && userRepository.existsByEmail(email)) {

                throw new IllegalArgumentException(
                        "Email already exists!"
                );
            }

            user.setEmail(email);
        }

        if (updatedUser.getName() != null) {

            String name =
                    normalizeName(
                            updatedUser.getName()
                    );

            if (name != null && name.length() > 100) {
                throw new IllegalArgumentException(
                        "Invalid name!"
                );
            }

            user.setName(name);
        }

        if (updatedUser.getPassword() != null
                && !updatedUser.getPassword().isBlank()) {

            if (updatedUser.getPassword().length() < 8
                    || updatedUser.getPassword().length() > 72) {

                throw new IllegalArgumentException(
                        "Password must contain 8 to 72 characters!"
                );
            }

            user.setPassword(
                    passwordEncoder.encode(
                            updatedUser.getPassword()
                    )
            );
        }

        return userRepository.save(user);
    }

    @Transactional
    public String deleteUser(Long id) {

        if (id == null || id <= 0) {
            return "Invalid user ID!";
        }

        Optional<User> userOpt =
                userRepository.findById(id);

        if (userOpt.isEmpty()) {
            return "User not found!";
        }

        userRepository.deleteById(id);

        return "User deleted successfully!";
    }

    public String login(
            String email,
            String password) {

        String normalizedEmail =
                normalizeEmail(email);

        if (!isValidEmail(normalizedEmail)
                || password == null
                || password.isBlank()) {

            return "Invalid credentials!";
        }

        Optional<User> optionalUser =
                userRepository.findByEmail(
                        normalizedEmail
                );

        if (optionalUser.isEmpty()) {
            return "Invalid credentials!";
        }

        User user = optionalUser.get();

        if (user.getPassword() == null
                || !passwordEncoder.matches(
                        password,
                        user.getPassword()
                )) {

            return "Invalid credentials!";
        }

        if (!user.isEmailVerified()) {
            return "Please verify your email first!";
        }

        if (user.getAccountStatus()
                != AccountStatus.ACTIVE) {

            return "Account is not active!";
        }

        return JwtUtil.generateToken(
                user.getEmail()
        );
    }

    @Transactional
    public String forgotPassword(String email) {

        String normalizedEmail =
                normalizeEmail(email);

        if (!isValidEmail(normalizedEmail)) {
            return "If the account exists, a reset link has been sent.";
        }

        Optional<User> optionalUser =
                userRepository.findByEmail(
                        normalizedEmail
                );

        if (optionalUser.isEmpty()) {
            return "If the account exists, a reset link has been sent.";
        }

        User user = optionalUser.get();

        tokenRepository
                .findByUser(user)
                .ifPresent(tokenRepository::delete);

        PasswordResetToken token =
                new PasswordResetToken();

        token.setUser(user);
        token.setToken(
                UUID.randomUUID().toString()
        );
        token.setExpiryTime(
                LocalDateTime.now().plusMinutes(30)
        );
        token.setUsed(false);

        tokenRepository.save(token);

        String resetLink =
                frontendUrl
                        + "/reset-password?token="
                        + token.getToken();

        emailService.sendPasswordResetEmail(
                user.getEmail(),
                user.getUsername(),
                resetLink
        );

        return "If the account exists, a reset link has been sent.";
    }

    @Transactional
    public String resetPassword(
            String token,
            String newPassword) {

        if (token == null
                || token.isBlank()
                || token.length() > 255) {

            return "Invalid reset token!";
        }

        if (newPassword == null
                || newPassword.length() < 8
                || newPassword.length() > 72) {

            return "Password must contain 8 to 72 characters!";
        }

        Optional<PasswordResetToken> optionalToken =
                tokenRepository.findByToken(token);

        if (optionalToken.isEmpty()) {
            return "Invalid reset token!";
        }

        PasswordResetToken resetToken =
                optionalToken.get();

        if (resetToken.isUsed()) {
            return "Reset token has already been used!";
        }

        if (resetToken.getExpiryTime() == null
                || resetToken.getExpiryTime()
                .isBefore(LocalDateTime.now())) {

            return "Reset token has expired!";
        }

        User user = resetToken.getUser();

        if (user == null) {
            return "Invalid reset token!";
        }

        user.setPassword(
                passwordEncoder.encode(newPassword)
        );

        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        return "Password reset successfully!";
    }

    private String generateOtp() {

        return String.format(
                "%04d",
                secureRandom.nextInt(10000)
        );
    }

    @Transactional
    public String verifyOtp(
            String email,
            String otp) {

        String normalizedEmail =
                normalizeEmail(email);

        if (!isValidEmail(normalizedEmail)) {
            return "Invalid OTP!";
        }

        Optional<User> optionalUser =
                userRepository.findByEmail(
                        normalizedEmail
                );

        if (optionalUser.isEmpty()) {
            return "Invalid OTP!";
        }

        User user = optionalUser.get();

        Optional<EmailVerificationToken> optionalToken =
                emailVerificationTokenRepository
                        .findByUser(user);

        if (optionalToken.isEmpty()) {
            return "OTP not found!";
        }

        EmailVerificationToken token =
                optionalToken.get();

        if (token.isUsed()) {
            return "OTP has already been used!";
        }

        if (token.getExpiryTime() == null
                || token.getExpiryTime()
                .isBefore(LocalDateTime.now())) {

            return "OTP has expired!";
        }

        if (otp == null
                || !otp.matches("\\d{4}")
                || !otp.equals(token.getOtp())) {

            return "Invalid OTP!";
        }

        user.setEmailVerified(true);
        user.setAccountStatus(
                AccountStatus.ACTIVE
        );

        userRepository.save(user);

        token.setUsed(true);
        emailVerificationTokenRepository.delete(token);

        return JwtUtil.generateToken(
                user.getEmail()
        );
    }

    @Transactional
    public String resendOtp(String email) {

        String normalizedEmail =
                normalizeEmail(email);

        if (!isValidEmail(normalizedEmail)) {
            return "Invalid email!";
        }

        Optional<User> optionalUser =
                userRepository.findByEmail(
                        normalizedEmail
                );

        if (optionalUser.isEmpty()) {
            return "User not found!";
        }

        User user = optionalUser.get();

        if (user.isEmailVerified()) {
            return "Email is already verified!";
        }

        emailVerificationTokenRepository
                .findByUser(user)
                .ifPresent(
                        emailVerificationTokenRepository::delete
                );

        EmailVerificationToken token =
                new EmailVerificationToken();

        token.setUser(user);
        token.setOtp(generateOtp());
        token.setExpiryTime(
                LocalDateTime.now().plusMinutes(10)
        );
        token.setUsed(false);

        emailVerificationTokenRepository.save(token);

        emailService.sendOtpEmail(
                user.getEmail(),
                user.getUsername(),
                token.getOtp()
        );

        return "OTP sent successfully!";
    }

    public UserResponse getCurrentUser(
            String email) {

        String normalizedEmail =
                normalizeEmail(email);

        if (!isValidEmail(normalizedEmail)) {
            return null;
        }

        User user =
                userRepository
                        .findByEmail(normalizedEmail)
                        .orElse(null);

        if (user == null) {
            return null;
        }

        String roleStr =
                user.getRole() != null
                        ? user.getRole().name()
                        : "USER";

        UserResponse response =
                new UserResponse();

        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setProfilePicture(
                user.getProfilePicture()
        );

        UserProfile profile =
                user.getProfile();

        if (profile == null) {
            profile =
                    userProfileRepository
                            .findByUser(user)
                            .orElse(null);
        }

        boolean isCompleted =
                profile != null
                        && (
                        profile.isProfileCompleted()
                                || (
                                profile.getJourneyType() != null
                                        && profile.getTargetRole() != null
                                        && !profile.getTargetRole().isBlank()
                                        && profile.getCareerGoal() != null
                        )
                );

        response.setProfileCompleted(
                isCompleted
        );

        response.setRole(roleStr);
        response.setPlan(null);

        return response;
    }

    private String normalizeEmail(String email) {

        if (email == null) {
            return "";
        }

        return email.trim().toLowerCase();
    }

    private String normalizeUsername(
            String username) {

        if (username == null) {
            return null;
        }

        return username.trim();
    }

    private String normalizeName(String name) {

        if (name == null) {
            return null;
        }

        String normalized = name.trim();

        return normalized.isBlank()
                ? null
                : normalized;
    }

    private boolean isValidEmail(String email) {

        if (email == null
                || email.isBlank()
                || email.length() > 254
                || email.contains("\r")
                || email.contains("\n")) {

            return false;
        }

        return email.matches(
                "^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@"
                        + "[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}"
                        + "[A-Za-z0-9])?"
                        + "(?:\\.[A-Za-z0-9]"
                        + "(?:[A-Za-z0-9-]{0,61}"
                        + "[A-Za-z0-9])?)+$"
        );
    }
}