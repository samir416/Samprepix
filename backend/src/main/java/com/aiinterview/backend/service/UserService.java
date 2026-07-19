package com.aiinterview.backend.service;

import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.UserRepository;
import com.aiinterview.backend.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

   public String saveUser(User user) {

    if (userRepository.existsByEmail(user.getEmail())) {
        return "Email already exists!";
    }

    if (userRepository.existsByUsername(user.getUsername())) {
        return "Username already exists!";
    }

    user.setPassword(
            passwordEncoder.encode(user.getPassword())
    );

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

    public User updateUser(Long id, User updatedUser) {

        Optional<User> existingUser =
                userRepository.findById(id);

        if (existingUser.isEmpty()) {
            return null;
        }

        User user = existingUser.get();

        if (!user.getUsername().equals(updatedUser.getUsername())
                && userRepository.existsByUsername(updatedUser.getUsername())) {

            throw new RuntimeException("Username already exists!");
        }

        if (!user.getEmail().equals(updatedUser.getEmail())
                && userRepository.existsByEmail(updatedUser.getEmail())) {

            throw new RuntimeException("Email already exists!");
        }

        user.setUsername(updatedUser.getUsername());
        user.setEmail(updatedUser.getEmail());

        if (updatedUser.getPassword() != null
                && !updatedUser.getPassword().isBlank()) {

            user.setPassword(
                    passwordEncoder.encode(updatedUser.getPassword())
            );
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

    public String login(String email, String password) {

        Optional<User> optionalUser =
                userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return "User not found!";
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return "Invalid password!";
        }

        return JwtUtil.generateToken(user.getEmail());
    }
}