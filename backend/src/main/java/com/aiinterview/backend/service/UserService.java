package com.aiinterview.backend.service;

import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {

        Optional<User> user =
                userRepository.findById(id);

        return user.orElse(null);
    }

    public User updateUser(
            Long id,
            User updatedUser) {

        Optional<User> existingUser =
                userRepository.findById(id);

        if(existingUser.isPresent()) {

            User user =
                    existingUser.get();

            user.setName(
                    updatedUser.getName());

            user.setEmail(
                    updatedUser.getEmail());

            user.setPassword(
                    updatedUser.getPassword());

            return userRepository.save(user);
        }

        return null;
    }

    public String deleteUser(Long id) {

        if(userRepository.existsById(id)) {

            userRepository.deleteById(id);

            return "User deleted successfully!";
        }

        return "User not found!";
    }
}