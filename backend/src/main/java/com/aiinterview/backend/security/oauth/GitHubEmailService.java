package com.aiinterview.backend.security.oauth;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class GitHubEmailService {

    private final WebClient webClient;

    public GitHubEmailService(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    @SuppressWarnings("unchecked")
    public String getPrimaryEmail(String accessToken) {

        System.out.println("========== GITHUB EMAIL DEBUG ==========");
        System.out.println("Access Token Present : " + (accessToken != null));

        try {

            List<Map<String, Object>> emails =
                    webClient.get()
                            .uri("https://api.github.com/user/emails")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                            .header(HttpHeaders.ACCEPT, "application/vnd.github+json")
                            .accept(MediaType.APPLICATION_JSON)
                            .retrieve()
                            .bodyToMono(List.class)
                            .block();

            System.out.println("GitHub API Response : " + emails);

            if (emails == null || emails.isEmpty()) {
                System.out.println("No emails returned from GitHub.");
                return null;
            }

            for (Map<String, Object> email : emails) {

                System.out.println("Email Object : " + email);

                Boolean primary = (Boolean) email.get("primary");
                Boolean verified = (Boolean) email.get("verified");

                if (Boolean.TRUE.equals(primary)
                        && Boolean.TRUE.equals(verified)) {

                    String result = (String) email.get("email");

                    System.out.println("Primary Email Found : " + result);
                    System.out.println("========================================");

                    return result;
                }
            }

            System.out.println("No verified primary email found.");

        } catch (Exception ex) {

            System.out.println("GitHub Email API ERROR");
            ex.printStackTrace();
        }

        System.out.println("========================================");

        return null;
    }
}