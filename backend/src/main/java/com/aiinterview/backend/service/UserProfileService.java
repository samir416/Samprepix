package com.aiinterview.backend.service;

import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.entity.UserProfile;
import com.aiinterview.backend.repository.UserProfileRepository;
import com.aiinterview.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import com.aiinterview.backend.model.UserProfileRequest;
import com.aiinterview.backend.model.UserProfileResponse;
import com.aiinterview.backend.service.gemini.GeminiService;
import org.springframework.web.multipart.MultipartFile;
import com.aiinterview.backend.entity.JourneyType;
import java.time.LocalDate;
import java.util.List;

@Service
public class UserProfileService {

        private final UserRepository userRepository;
        private final FileStorageService fileStorageService;
        private final GeminiService geminiService;
        private final UserProfileRepository userProfileRepository;

        public UserProfileService(
                        UserRepository userRepository,
                        UserProfileRepository userProfileRepository,
                        FileStorageService fileStorageService,

                        GeminiService geminiService) {

                this.userRepository = userRepository;
                this.userProfileRepository = userProfileRepository;
                this.fileStorageService = fileStorageService;
                this.geminiService = geminiService;
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

                response.setPersonalWebsite(profile.getPersonalWebsite());

                response.setUniversity(profile.getUniversity());

                response.setDesignation(profile.getDesignation());

                response.setEmploymentType(profile.getEmploymentType());

                response.setDateOfBirth(
                                profile.getDob() != null
                                                ? profile.getDob().toString()
                                                : null);

                response.setSkills(

                                profile.getSkills()

                );

                return response;
        }

        public UserProfile saveProfile(
                        String email,
                        UserProfileRequest request) {

                User user = userRepository
                                .findByEmail(email)
                                .orElseThrow();
                user.setName(request.getName());

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

                profile.setPersonalWebsite(
                                request.getPersonalWebsite());

                profile.setUniversity(
                                request.getUniversity());

                profile.setDesignation(
                                request.getDesignation());

                profile.setEmploymentType(
                                request.getEmploymentType());

                profile.setSkills(

                                request.getSkills()

                );

                if (request.getDateOfBirth() != null &&
                                !request.getDateOfBirth().isBlank()) {

                        profile.setDob(
                                        LocalDate.parse(request.getDateOfBirth()));

                } else {

                        profile.setDob(null);

                }

                userRepository.save(user);

                updateProfileCompletion(user, profile);

                return userProfileRepository.save(profile);

        }

        private void updateProfileCompletion(User user, UserProfile profile) {

                boolean profileCompleted =

                                profile.getJourneyType() != null &&

                                                profile.getTargetRole() != null &&
                                                !profile.getTargetRole().isBlank() &&

                                                profile.getExperienceLevel() != null &&
                                                !profile.getExperienceLevel().isBlank() &&

                                                profile.getPhone() != null &&
                                                !profile.getPhone().isBlank() &&

                                                profile.getGithubUrl() != null &&
                                                !profile.getGithubUrl().isBlank() &&

                                                profile.getLinkedinUrl() != null &&
                                                !profile.getLinkedinUrl().isBlank() &&

                                                profile.getDob() != null &&

                                                user.getProfilePicture() != null &&
                                                !user.getProfilePicture().isBlank();

                if (profile.getJourneyType() == JourneyType.STUDENT) {

                        profileCompleted = profileCompleted &&

                                        profile.getCollege() != null &&
                                        !profile.getCollege().isBlank() &&

                                        profile.getCourse() != null &&
                                        !profile.getCourse().isBlank() &&

                                        profile.getGraduationYear() != null &&
                                        !profile.getGraduationYear().isBlank() &&

                                        profile.getUniversity() != null &&
                                        !profile.getUniversity().isBlank();

                }

                if (profile.getJourneyType() == JourneyType.WORKING_PROFESSIONAL) {

                        profileCompleted = profileCompleted &&

                                        profile.getCurrentCompany() != null &&
                                        !profile.getCurrentCompany().isBlank() &&

                                        profile.getDesignation() != null &&
                                        !profile.getDesignation().isBlank() &&

                                        profile.getEmploymentType() != null &&
                                        !profile.getEmploymentType().isBlank() &&

                                        profile.getYearsOfExperience() != null;

                }

                profile.setProfileCompleted(profileCompleted);

                userProfileRepository.save(profile);

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

                String imageUrl = fileStorageService.saveProfilePicture(file);

                user.setProfilePicture(imageUrl);

                userRepository.save(user);

                UserProfile profile = userProfileRepository
                                .findByUser(user)
                                .orElse(null);

                if (profile != null) {

                        updateProfileCompletion(user, profile);

                }

                return imageUrl;
        }

        public void removeProfilePicture(String email) throws Exception {

                User user = userRepository
                                .findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                if (user.getProfilePicture() != null &&
                                !user.getProfilePicture().isBlank()) {

                        fileStorageService.deleteProfilePicture(
                                        user.getProfilePicture());

                        user.setProfilePicture(null);

                        userRepository.save(user);

                        UserProfile profile = userProfileRepository
                                        .findByUser(user)
                                        .orElse(null);

                        if (profile != null) {

                                updateProfileCompletion(user, profile);

                        }
                }

        }

        public List<String> getSkillSuggestions(

                        String role,

                        String query

        ) {

                try {

                        return geminiService.generateSkillSuggestions(

                                        role,

                                        query

                        );

                }

                catch (Exception exception) {

                        exception.printStackTrace();

                        return java.util.Collections.emptyList();

                }

        }

}
