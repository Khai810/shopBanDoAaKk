package com.projectshopbando.shopbandoapi.config;

import com.projectshopbando.shopbandoapi.entities.Account;
import com.projectshopbando.shopbandoapi.entities.Staff;
import com.projectshopbando.shopbandoapi.enums.Roles;
import com.projectshopbando.shopbandoapi.repositories.AccountRepository;
import com.projectshopbando.shopbandoapi.repositories.StaffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ApplicationInitConfig {
    private final PasswordEncoder passwordEncoder;
    @Bean
    public ApplicationRunner applicationRunner(AccountRepository accountRepository, StaffRepository staffRepository) {
        return args -> {
            if (accountRepository.findByEmail("admin").isEmpty()) {
                var roles = new HashSet<Roles>();
                roles.add(Roles.ADMIN);
                roles.add(Roles.STAFF);
                roles.add(Roles.CUSTOMER);

                Staff staff = Staff.builder()
                        .fullName("KhaiPham ADMIN")
                        .phone("0999999999")
                        .build();
                staff = staffRepository.save(staff);

                Account account = Account.builder()
                        .password(passwordEncoder.encode("admin"))
                        .role(roles)
                        .customer(null)
                        .staff(staff)
                        .build();
                accountRepository.save(account);
                log.warn("default admin user has been created with default password admin");
            }
        };
    }
}
