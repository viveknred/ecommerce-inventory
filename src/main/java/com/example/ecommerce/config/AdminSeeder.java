package com.example.ecommerce.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.ecommerce.entity.Role;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.repository.UserRepository;

@Configuration
public class AdminSeeder {

    @Bean
    CommandLineRunner createAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            String adminEmail = "admin@ecommerce.com";

            if (userRepository.findByEmail(adminEmail).isEmpty()) {

                User admin = new User();

                admin.setName("Admin");
                admin.setEmail(adminEmail);
                admin.setPassword(
                        passwordEncoder.encode("Admin@123")
                );
                admin.setRole(Role.ADMIN);

                userRepository.save(admin);
            }
        };
    }
}