package com.event.EventManage.config;

import com.event.EventManage.model.Role;
import com.event.EventManage.model.User;
import com.event.EventManage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByEmail("admin@eventdeco.com").isEmpty()) {
            User admin = User.builder()
                    .firstName("Super")
                    .lastName("Admin")
                    .email("admin@eventdeco.com")
                    .passwordHash(passwordEncoder.encode("Admin@123"))
                    .phone("1234567890")
                    .role(Role.ADMIN)
                    .build();
            
            userRepository.save(admin);
            log.info("Default Admin user created! Email: admin@eventdeco.com | Password: Admin@123");
        }
    }
}
