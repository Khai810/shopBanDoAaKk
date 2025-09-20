package com.projectshopbando.shopbandoapi.config;

import com.projectshopbando.shopbandoapi.entities.Account;
import com.projectshopbando.shopbandoapi.entities.Customer;
import com.projectshopbando.shopbandoapi.enums.Roles;
import com.projectshopbando.shopbandoapi.repositories.AccountRepository;
import com.projectshopbando.shopbandoapi.repositories.CustomerRepository;
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
    private final CustomerRepository customerRepository;
    @Bean
    public ApplicationRunner applicationRunner(AccountRepository accountRepository) {
        return args -> {
            if (accountRepository.findByCustomer_Phone("0999999999").isEmpty()) {
                var roles = new HashSet<Roles>();
                roles.add(Roles.ADMIN);
                roles.add(Roles.USER);

                Customer customer = Customer.builder()
                        .fullName("KhaiPham ADMIN")
                        .phone("0999999999")
                        .build();
                customer = customerRepository.save(customer);
                Account account = Account.builder()
                        .password(passwordEncoder.encode("admin"))
                        .role(roles)
                        .customer(customer)
                        .build();
                accountRepository.save(account);
                log.warn("default admin user has been created with default password admin");
            }
        };
    }
}
