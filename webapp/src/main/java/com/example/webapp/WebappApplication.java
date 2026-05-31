package com.example.webapp;

import com.example.webapp.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WebappApplication {
//http://localhost:8080/
    public static void main(String[] args) {
        SpringApplication.run(WebappApplication.class, args);
    }

    @Bean
    public CommandLineRunner seedDemoAdmin(UserService userService) {
        return args -> {
            userService.createAdminIfMissing("admin@gmail.com", "admin");
            System.out.println("Demo admin account: admin@gmail.com / admin");
        };
    }
}