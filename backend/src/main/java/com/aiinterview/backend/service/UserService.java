package com.aiinterview.backend.service;

import com.aiinterview.backend.entity.PasswordResetToken;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.model.UserResponse;
import com.aiinterview.backend.repository.PasswordResetTokenRepository;
import com.aiinterview.backend.repository.UserProfileRepository;
import com.aiinterview.backend.repository.UserRepository;
import com.aiinterview.backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.aiinterview.backend.entity.AccountStatus;
import com.aiinterview.backend.entity.EmailVerificationToken;
import com.aiinterview.backend.repository.EmailVerificationTokenRepository;
import com.aiinterview.backend.entity.UserProfile;
import java.util.Random;

@Service
public class UserService {

        private final UserRepository userRepository;
        private final UserProfileRepository userProfileRepository;
        private final PasswordResetTokenRepository tokenRepository;
        private final EmailVerificationTokenRepository emailVerificationTokenRepository;
        private final EmailService emailService;

        private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        @Value("${app.frontend.url}")
        private String frontendUrl;

        public UserService(
                        UserRepository userRepository,
                        UserProfileRepository userProfileRepository,
                        PasswordResetTokenRepository tokenRepository,
                        EmailService emailService,
                        EmailVerificationTokenRepository emailVerificationTokenRepository) {

                this.userRepository = userRepository;
                this.userProfileRepository = userProfileRepository;
                this.tokenRepository = tokenRepository;
                this.emailService = emailService;
                this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        }

        public String saveUser(User user) {

                if (userRepository.existsByEmail(user.getEmail())) {
                        return "Email already exists!";
                }

                if (userRepository.existsByUsername(user.getUsername())) {
                        return "Username already exists!";
                }

                user.setPassword(
                                passwordEncoder.encode(user.getPassword()));

                user.setEmailVerified(false);
                user.setAccountStatus(AccountStatus.PENDING);

                userRepository.save(user);

                UserProfile profile = new UserProfile();

                profile.setUser(user);

                user.setProfile(profile);

                userProfileRepository.save(profile);

                emailVerificationTokenRepository.findByUser(user)
                                .ifPresent(emailVerificationTokenRepository::delete);

                EmailVerificationToken verificationToken = new EmailVerificationToken();

                verificationToken.setUser(user);
                verificationToken.setOtp(generateOtp());
                verificationToken.setExpiryTime(
                                LocalDateTime.now().plusMinutes(10));
                verificationToken.setUsed(false);

                emailVerificationTokenRepository.save(
                                verificationToken);

                emailService.sendOtpEmail(
                                user.getEmail(),
                                user.getUsername(),
                                verificationToken.getOtp());

                return "OTP sent successfully!";
        }

        public List<User> getAllUsers() {

                return userRepository.findAll();
        }

        public User getUserById(Long id) {

                return userRepository
                                .findById(id)
                                .orElse(null);
        }

        public User updateUser(
                        Long id,
                        User updatedUser) {

                Optional<User> existingUser = userRepository.findById(id);

                if (existingUser.isEmpty()) {
                        return null;
                }

                User user = existingUser.get();

                if (!user.getUsername().equals(updatedUser.getUsername())
                                && userRepository.existsByUsername(updatedUser.getUsername())) {

                        throw new RuntimeException(
                                        "Username already exists!");
                }

                if (!user.getEmail().equals(updatedUser.getEmail())
                                && userRepository.existsByEmail(updatedUser.getEmail())) {

                        throw new RuntimeException(
                                        "Email already exists!");
                }

                user.setUsername(updatedUser.getUsername());
                user.setEmail(updatedUser.getEmail());

                if (updatedUser.getPassword() != null
                                && !updatedUser.getPassword().isBlank()) {

                        user.setPassword(
                                        passwordEncoder.encode(
                                                        updatedUser.getPassword()));
                }

                return userRepository.save(user);
        }

        public String deleteUser(Long id) {

                if (!userRepository.existsById(id)) {
                        return "User not found!";
                }

                userRepository.deleteById(id);

                return "User deleted successfully!";
        }

        public String login(
                        String email,
                        String password) {

                Optional<User> optionalUser = userRepository.findByEmail(email);

                if (optionalUser.isEmpty()) {
                        return "User not found!";
                }

                User user = optionalUser.get();

                if (!passwordEncoder.matches(
                                password,
                                user.getPassword())) {

                        return "Invalid password!";
                }

                if (!user.isEmailVerified()) {
                        return "Please verify your email first!";
                }

                if (user.getAccountStatus() != AccountStatus.ACTIVE) {
                        return "Account is not active!";
                }

                return JwtUtil.generateToken(
                                user.getEmail());
        }

        public String forgotPassword(String email) {

                Optional<User> optionalUser = userRepository.findByEmail(email);

                if (optionalUser.isEmpty()) {
                        return "Email not found!";
                }

                User user = optionalUser.get();

                tokenRepository.findByUser(user)
                                .ifPresent(existingToken -> tokenRepository.delete(existingToken));

                PasswordResetToken token = new PasswordResetToken();

                token.setUser(user);

                token.setToken(
                                UUID.randomUUID().toString());

                token.setExpiryTime(
                                LocalDateTime.now().plusMinutes(30));

                token.setUsed(false);

                tokenRepository.save(token);

                String resetLink = frontendUrl
                                + "/reset-password?token="
                                + token.getToken();

                emailService.sendPasswordResetEmail(
                                user.getEmail(),
                                user.getUsername(),
                                resetLink);

                return "Reset link sent successfully!";
        }

        public String resetPassword(
                        String token,
                        String newPassword) {

                Optional<PasswordResetToken> optionalToken = tokenRepository.findByToken(token);

                if (optionalToken.isEmpty()) {
                        return "Invalid reset token!";
                }

                PasswordResetToken resetToken = optionalToken.get();

                if (resetToken.isUsed()) {
                        return "Reset token has already been used!";
                }

                if (resetToken.getExpiryTime().isBefore(
                                LocalDateTime.now())) {

                        return "Reset token has expired!";
                }

                User user = resetToken.getUser();

                user.setPassword(
                                passwordEncoder.encode(newPassword));

                userRepository.save(user);
                resetToken.setUsed(true);

                tokenRepository.save(resetToken);

                return "Password reset successfully!";
        }

        private String generateOtp() {

                return String.format(
                                "%04d",
                                new Random().nextInt(10000));
        }

        public String verifyOtp(
                        String email,
                        String otp) {

                Optional<User> optionalUser = userRepository.findByEmail(email);

                if (optionalUser.isEmpty()) {
                        return "User not found!";
                }

                User user = optionalUser.get();

                Optional<EmailVerificationToken> optionalToken = emailVerificationTokenRepository.findByUser(user);

                if (optionalToken.isEmpty()) {
                        return "OTP not found!";
                }

                EmailVerificationToken token = optionalToken.get();

                if (token.isUsed()) {
                        return "OTP has already been used!";
                }

                if (token.getExpiryTime().isBefore(
                                LocalDateTime.now())) {

                        return "OTP has expired!";
                }

                if (!token.getOtp().equals(otp)) {
                        return "Invalid OTP!";
                }

                user.setEmailVerified(true);
                user.setAccountStatus(AccountStatus.ACTIVE);

                userRepository.save(user);

                token.setUsed(true);

                emailVerificationTokenRepository.delete(token);

                return JwtUtil.generateToken(user.getEmail());
        }

        public String resendOtp(String email) {

                Optional<User> optionalUser = userRepository.findByEmail(email);

                if (optionalUser.isEmpty()) {
                        return "User not found!";
                }

                User user = optionalUser.get();

                if (user.isEmailVerified()) {
                        return "Email is already verified!";
                }

                emailVerificationTokenRepository.findByUser(user)
                                .ifPresent(emailVerificationTokenRepository::delete);

                EmailVerificationToken token = new EmailVerificationToken();

                token.setUser(user);
                token.setOtp(generateOtp());
                token.setExpiryTime(LocalDateTime.now().plusMinutes(10));
                token.setUsed(false);

                emailVerificationTokenRepository.save(token);

                emailService.sendOtpEmail(
                                user.getEmail(),
                                user.getUsername(),
                                token.getOtp());

                return "OTP sent successfully!";
        }

        public UserResponse getCurrentUser(String email) {

                User user = userRepository.findByEmail(email).orElse(null);

                if (user == null) {
                        return null;
                }

                return new UserResponse(
                                user.getId(),
                                user.getUsername(),
                                user.getName(),
                                user.getEmail(),
                                user.getProfilePicture(),
                                user.getProfile() != null &&
                                                user.getProfile().isProfileCompleted());
        }

}