package com.aiinterview.backend.service;

import com.aiinterview.backend.entity.PasswordResetToken;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.PasswordResetTokenRepository;
import com.aiinterview.backend.repository.UserRepository;
import com.aiinterview.backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public UserService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            EmailService emailService) {

        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
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

        userRepository.save(user);

        return "User saved successfully!";
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

        Optional<User> existingUser =
                userRepository.findById(id);

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

        Optional<User> optionalUser =
                userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return "User not found!";
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(
                password,
                user.getPassword())) {

            return "Invalid password!";
        }

        return JwtUtil.generateToken(
                user.getEmail());
    }

        public String forgotPassword(String email) {

        Optional<User> optionalUser =
                userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return "Email not found!";
        }

        User user = optionalUser.get();

        tokenRepository.findByUser(user)
                .ifPresent(existingToken ->
                        tokenRepository.delete(existingToken));

        PasswordResetToken token =
                new PasswordResetToken();

        token.setUser(user);

        token.setToken(
                UUID.randomUUID().toString());

        token.setExpiryTime(
                LocalDateTime.now().plusMinutes(30));

        token.setUsed(false);

        tokenRepository.save(token);

        String resetLink =
                frontendUrl
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

}