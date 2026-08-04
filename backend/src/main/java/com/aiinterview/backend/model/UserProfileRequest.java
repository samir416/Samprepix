package com.aiinterview.backend.model;

import com.aiinterview.backend.entity.CareerGoal;
import com.aiinterview.backend.entity.JourneyType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileRequest {

    private JourneyType journeyType;

    private String targetRole;

    private String experienceLevel;

    private Double yearsOfExperience;

    private CareerGoal careerGoal;

    private String college;

    private String course;

    private String graduationYear;

    private String currentCompany;

    private String phone;

    private String gender;

    private String githubUrl;

    private String linkedinUrl;

    private String portfolioUrl;

    private String personalWebsite;

    private String university;

    private String designation;

    private String employmentType;

    private String dateOfBirth;

    private java.util.List<String> skills;

    
}