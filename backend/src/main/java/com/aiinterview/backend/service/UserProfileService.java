package com.aiinterview.backend.service;

import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.entity.UserProfile;
import com.aiinterview.backend.repository.UserProfileRepository;
import com.aiinterview.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import com.aiinterview.backend.model.UserProfileRequest;

import java.util.Optional;

@Service
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    public UserProfileService(
            UserRepository userRepository,
            UserProfileRepository userProfileRepository) {

        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
    }

    public UserProfile getProfile(String email) {

        Optional<User> optionalUser =
                userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return null;
        }

        return userProfileRepository
                .findByUser(optionalUser.get())
                .orElse(null);
    }

    public UserProfile saveProfile(
        String email,
        UserProfileRequest request) {

    User user = userRepository
            .findByEmail(email)
            .orElseThrow();

   UserProfile profile = userProfileRepository
        .findByUser(user)
        .orElseGet(() -> {
            UserProfile newProfile = new UserProfile();
            newProfile.setUser(user);
            return newProfile;
        });

    profile.setJourneyType(
            request.getJourneyType());

    profile.setTargetRole(
            request.getTargetRole());

    profile.setExperienceLevel(
        request.getExperienceLevel());

    profile.setYearsOfExperience(
            request.getYearsOfExperience());

    profile.setCareerGoal(
            request.getCareerGoal());

    profile.setCollege(
            request.getCollege());

    profile.setCourse(
            request.getCourse());

    profile.setGraduationYear(
            request.getGraduationYear());

    profile.setCurrentRole(
            request.getCurrentRole());

    profile.setCurrentCompany(
            request.getCurrentCompany());

    profile.setPhone(
            request.getPhone());

    profile.setGender(
            request.getGender());

    profile.setGithubUrl(
            request.getGithubUrl());

    profile.setLinkedinUrl(
            request.getLinkedinUrl());

    profile.setPortfolioUrl(
            request.getPortfolioUrl());

    profile.setProfileCompleted(true);

    return userProfileRepository.save(profile);

}

}