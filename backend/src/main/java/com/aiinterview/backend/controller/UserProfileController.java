package com.aiinterview.backend.controller;

import com.aiinterview.backend.entity.UserProfile;
import com.aiinterview.backend.security.JwtUtil;
import com.aiinterview.backend.service.UserProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.aiinterview.backend.model.UserProfileRequest;
import com.aiinterview.backend.model.UserProfileResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.DeleteMapping;

@RestController
@RequestMapping("/api/profile")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(
            UserProfileService userProfileService) {

        this.userProfileService = userProfileService;
    }

   @GetMapping
public UserProfileResponse getProfile(
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

@PostMapping("/upload-photo")
public ResponseEntity<String> uploadProfilePicture(
        Authentication authentication,
        @RequestParam("file") MultipartFile file) throws Exception {

    String imageUrl = userProfileService.uploadProfilePicture(
            authentication.getName(),
            file);

    return ResponseEntity.ok(imageUrl);
}

@DeleteMapping("/remove-photo")
public ResponseEntity<Void> removeProfilePicture(
        Authentication authentication) throws Exception {

    userProfileService.removeProfilePicture(
            authentication.getName());

    return ResponseEntity.noContent().build();
}

}