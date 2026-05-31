package com.example.webapp.web;

import com.example.webapp.model.User;
import com.example.webapp.model.UserRole;
import com.example.webapp.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session) {
        return userService.authenticate(email, password)
                .map(user -> {
                    session.setAttribute("currentUserId", user.getId());
                    session.setAttribute("currentUserFullName", user.getFullName());
                    session.setAttribute("currentUserEmail", user.getEmail());
                    session.setAttribute("currentUserRole", user.getRole());
                    if (user.getRole() == UserRole.ADMIN) {
                        return "redirect:/admin";
                    }
                    return "redirect:/dashboard";
                })
                .orElse("redirect:/login?error=invalid");
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String fullname,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirm_password) {

        if (!password.equals(confirm_password)) {
            return "redirect:/register?error=password_mismatch";
        }

        try {
            userService.registerCustomer(fullname, email, password);
        } catch (IllegalArgumentException e) {
            return "redirect:/register?error=email_exists";
        }

        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
