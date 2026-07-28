package com.aiinterview.backend.service;

import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.entity.UserProfile;
import com.aiinterview.backend.repository.UserProfileRepository;
import com.aiinterview.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import com.aiinterview.backend.model.UserProfileRequest;
import com.aiinterview.backend.model.UserProfileResponse;
import com.aiinterview.backend.service.FileStorageService;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
public class UserProfileService {

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final UserProfileRepository userProfileRepository;

   public UserProfileService(
        UserRepository userRepository,
        UserProfileRepository userProfileRepository,
        FileStorageService fileStorageService) {

    this.userRepository = userRepository;
    this.userProfileRepository = userProfileRepository;
    this.fileStorageService = fileStorageService;
}

   public UserProfileResponse getProfile(String email) {

    User user = userRepository
            .findByEmail(email)
            .orElse(null);

    if (user == null) {
        return null;
    }

    UserProfile profile = userProfileRepository
            .findByUser(user)
            .orElse(new UserProfile());

    UserProfileResponse response = new UserProfileResponse();

    response.setName(user.getName());
    response.setUsername(user.getUsername());
    response.setEmail(user.getEmail());
    response.setProfilePicture(user.getProfilePicture());

    response.setJourneyType(profile.getJourneyType());
    response.setTargetRole(profile.getTargetRole());
    response.setCurrentRole(profile.getCurrentRole());
    response.setExperienceLevel(profile.getExperienceLevel());
    response.setYearsOfExperience(profile.getYearsOfExperience());
    response.setCareerGoal(profile.getCareerGoal());

    response.setCollege(profile.getCollege());
    response.setCourse(profile.getCourse());
    response.setGraduationYear(profile.getGraduationYear());
    response.setCurrentCompany(profile.getCurrentCompany());

    response.setPhone(profile.getPhone());
    response.setGender(profile.getGender());

    response.setGithubUrl(profile.getGithubUrl());
    response.setLinkedinUrl(profile.getLinkedinUrl());
    response.setPortfolioUrl(profile.getPortfolioUrl());

    return response;
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

public String uploadProfilePicture(String email, MultipartFile file) throws Exception {

    User user = userRepository
            .findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

    if (user.getProfilePicture() != null &&
            !user.getProfilePicture().isBlank()) {

        fileStorageService.deleteProfilePicture(
                user.getProfilePicture());
    }

    String imageUrl =
            fileStorageService.saveProfilePicture(file);

    user.setProfilePicture(imageUrl);

    userRepository.save(user);

    return imageUrl;
}

}

