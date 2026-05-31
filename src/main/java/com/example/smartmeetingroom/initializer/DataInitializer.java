package com.example.smartmeetingroom.initializer;

import com.example.smartmeetingroom.entity.Role;
import com.example.smartmeetingroom.entity.User;
import com.example.smartmeetingroom.repository.RoleRepository;
import com.example.smartmeetingroom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    @Value("${app.super-admin.email}")
    private String SUPER_ADMIN_EMAIL;
    @Value("${app.super-admin.password}")
    private String SUPER_ADMIN_PASSWORD;
    @Value("${app.super-admin.first-name}")
    private String SUPER_ADMIN_FIRST_NAME;
    @Value("${app.super-admin.last-name}")
    private String SUPER_ADMIN_LAST_NAME;
    @Value("${app.roles}")
    private final List<String> roles;

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    public void run(String... args) throws Exception {

        roles.forEach(r -> {
                    var isExists = roleRepository.existsByRoleName(r.toUpperCase());
                    if (!isExists) {
                        var role = new Role();
                        role.setRoleName(r.toUpperCase());
                        roleRepository.save(role);
                    }
        });

        var superAdminRole = roleRepository.findByRoleName("SUPER_ADMIN").orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
        boolean userExists = userRepository.existsByEmail(SUPER_ADMIN_EMAIL);

        if (!userExists) {

            var user = new User();

            user.setFirstName(SUPER_ADMIN_FIRST_NAME);
            user.setLastName(SUPER_ADMIN_LAST_NAME);
            user.setEmail(SUPER_ADMIN_EMAIL);
            user.setPassword(passwordEncoder.encode(SUPER_ADMIN_PASSWORD));
            user.setRoles(superAdminRole);

            userRepository.save(user);

           log.info("SUPER ADMIN CREATED");
        }
    }
}
