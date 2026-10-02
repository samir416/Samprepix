package com.aiinterview.backend.controller;

import com.aiinterview.backend.entity.Role;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.entity.UserProfile;
import com.aiinterview.backend.repository.UserRepository;
import com.aiinterview.backend.security.JwtUtil;
import com.aiinterview.backend.service.UserProfileService;
import com.aiinterview.backend.service.UserService;
import com.aiinterview.backend.model.UserProfileRequest;
import com.aiinterview.backend.model.UserProfileResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final UserRepository userRepository;
    private final UserService userService;

    public UserProfileController(
            UserProfileService userProfileService,
            UserRepository userRepository,
            UserService userService) {

        this.userProfileService = userProfileService;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile(
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String tokenHeader) {

        String email = resolveEmail(authentication, tokenHeader);
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(userProfileService.getProfile(email));
    }

    @PutMapping
    public ResponseEntity<UserProfileResponse> updateProfile(
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @RequestBody UserProfileRequest request) {

        String email = resolveEmail(authentication, tokenHeader);
        if (email == null || email.isBlank()) {
            org.slf4j.LoggerFactory.getLogger(UserProfileController.class)
                    .warn("PUT /api/profile rejected with 401: unable to resolve email from auth [{}] or header [{}]",
                            authentication != null ? authentication.getName() : "null",
                            tokenHeader != null ? "present" : "missing");
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }

        userProfileService.saveProfile(email, request);
        return ResponseEntity.ok(userProfileService.getProfile(email));
    }

    private String resolveEmail(Authentication authentication, String tokenHeader) {
        if (authentication != null && authentication.isAuthenticated() && authentication.getName() != null && !authentication.getName().isBlank()) {
            return authentication.getName().trim().toLowerCase();
        }
        if (tokenHeader != null && tokenHeader.toLowerCase().startsWith("bearer ")) {
            String rawToken = tokenHeader.substring(7).trim();
            while (rawToken.toLowerCase().startsWith("bearer ")) {
                rawToken = rawToken.substring(7).trim();
            }
            String token = rawToken.replace("\"", "").trim();
            if (JwtUtil.validateToken(token)) {
                String extracted = JwtUtil.extractEmail(token);
                if (extracted != null && !extracted.isBlank()) {
                    return extracted.trim().toLowerCase();
                }
            }
        }
        return null;
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


@GetMapping("/skills/suggestions")
public ResponseEntity<List<String>> getSkillSuggestions(

        @RequestParam String role,

        @RequestParam String query

) {

    return ResponseEntity.ok(

            userProfileService.getSkillSuggestions(

                    role,

                    query

            )

    );

}

    @DeleteMapping(value = {"", "/account"})
    public ResponseEntity<?> deleteMyAccount(
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String tokenHeader) {

        String email = resolveEmail(authentication, tokenHeader);
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Unauthorized"));
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }

        if (user.getRole() == Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Admin accounts cannot be deleted."));
        }

        userService.deleteUser(user.getId());

        return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));
    }

}