package com.aiinterview.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OAuthController {

    @GetMapping("/oauth2/login/google")
    public String googleLogin() {

        return "redirect:/oauth2/authorize/google";
    }

    @GetMapping("/oauth2/login/github")
    public String githubLogin() {

        return "redirect:/oauth2/authorize/github";
    }

    @GetMapping("/oauth2/register/google")
    public String googleRegister() {

        return "redirect:/oauth2/authorize/google";
    }

    @GetMapping("/oauth2/register/github")
    public String githubRegister() {

        return "redirect:/oauth2/authorize/github";
    }

}