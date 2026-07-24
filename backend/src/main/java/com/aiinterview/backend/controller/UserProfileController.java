package com.aiinterview.backend.controller;

import com.aiinterview.backend.entity.UserProfile;
import com.aiinterview.backend.security.JwtUtil;
import com.aiinterview.backend.service.UserProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.aiinterview.backend.model.UserProfileRequest;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/profile")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(
            UserProfileService userProfileService) {

        this.userProfileService = userProfileService;
    }

    @GetMapping
    public UserProfile getProfile(
            @RequestHeader("Authorization") String token) {

        token = token.replace("Bearer ", "");

        String email = JwtUtil.extractEmail(token);

        return userProfileService.getProfile(email);
    }


    @PutMapping
public UserProfile updateProfile(
        @RequestHeader("Authorization") String token,
        @RequestBody UserProfileRequest request) {

    token = token.replace(
            "Bearer ",
            "");

    String email =
            JwtUtil.extractEmail(token);

    return userProfileService.saveProfile(
            email,
            request);

}

}