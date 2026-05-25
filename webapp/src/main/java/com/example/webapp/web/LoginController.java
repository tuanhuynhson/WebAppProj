package com.example.webapp.web; 

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    // to login.html
    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; 
    }

    // to register.html
    @GetMapping("/register")
    public String showRegisterPage() {
        return "register"; 
    }
}