package org.example.websitesalephone.config.init;

import lombok.RequiredArgsConstructor;
import org.example.websitesalephone.entity.Role;
import org.example.websitesalephone.enums.RoleEnums;
import org.example.websitesalephone.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleDataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        for (RoleEnums roleEnum : RoleEnums.values()) {
            if (roleRepository.findById(roleEnum.getId()).isEmpty()) {
                Role role = new Role();
                role.setId(roleEnum.getId());
                role.setRoleEnums(roleEnum);
                role.setStatus(1);
                roleRepository.save(role);
            }
        }
    }
}
