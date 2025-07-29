package com.usermanagement;

import com.usermanagement.model.ERole;
import com.usermanagement.model.Role;
import com.usermanagement.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initializeRoles();
    }

    private void initializeRoles() {
        for (ERole x : ERole.values()) {
            if (roleRepository.findByName(x).isEmpty()) {
                roleRepository.save(new Role(x));
                log.info("Created role {}", x);
            }
        }
    }
}
