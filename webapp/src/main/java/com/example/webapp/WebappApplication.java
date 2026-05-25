package com.example.webapp;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class WebappApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebappApplication.class, args);
	}

	@Bean
	public CommandLineRunner printAdminHash(PasswordEncoder passwordEncoder) {
		return args -> {
			String adminPassword = "admin";
			String hash = passwordEncoder.encode(adminPassword);
			System.out.println("===== ADMIN PASSWORD HASH =====");
			System.out.println("Password: " + adminPassword);
			System.out.println("Hash: " + hash);
			System.out.println("================================");
		};
	}
}
