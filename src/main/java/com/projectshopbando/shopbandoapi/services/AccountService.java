package com.projectshopbando.shopbandoapi.services;

import com.projectshopbando.shopbandoapi.dtos.request.ChangePasswordReq;
import com.projectshopbando.shopbandoapi.dtos.request.CreateAccountReq;
import com.projectshopbando.shopbandoapi.entities.Account;
import com.projectshopbando.shopbandoapi.entities.Customer;
import com.projectshopbando.shopbandoapi.entities.Staff;
import com.projectshopbando.shopbandoapi.enums.Roles;
import com.projectshopbando.shopbandoapi.exception.NotFoundException;
import com.projectshopbando.shopbandoapi.mappers.AccountMapper;
import com.projectshopbando.shopbandoapi.repositories.AccountRepository;
import com.projectshopbando.shopbandoapi.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final PasswordEncoder passwordEncoder;

    public Account getAccountByEmail(String email) {
        return accountRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Account not found"));
    }

    // Create account when create customer / staff
    // Default role is CUSTOMER for customer, STAFF for staff
    // Account is not saved to database, it will be saved when customer / staff is saved
    public Account createAccount (CreateAccountReq request, Customer customer, Staff staff) {
            Account account = accountMapper.toAccount(request);
            account.setPassword(passwordEncoder.encode(request.getPassword()));
            Set<Roles> roles = new HashSet<>();
            if(staff != null) {
                account.setStaff(staff);
                roles.add(Roles.STAFF);
                roles.add(Roles.ADMIN);
                return account;
            }
            account.setCustomer(customer);
            roles.add(Roles.CUSTOMER);
            account.setRole(roles);
            return account;
    }

    public Account getAccountById(String id){
        return accountRepository.findById(id).orElseThrow(() -> new NotFoundException("Account not found with id: " + id));
    }

    public Page<Account> getAllAccounts(int page, int size, String search){
        Pageable pageable = PageRequest.of(page, size);
        return accountRepository.findByCustomer_FullNameContainingOrCustomer_PhoneContainingOrderByCustomer_CreatedAtDesc(search, search, pageable);
    }

    public boolean changePassword(ChangePasswordReq request, String customerId){
        Account account = accountRepository.findByCustomer_Id(customerId)
                .orElseThrow(() -> new NotFoundException("Account not found with id: " + customerId));
        if(!passwordEncoder.matches(request.getOldPassword(), account.getPassword())){
            return false;
        }
        if(!request.getNewPassword().equals(request.getConfirmPassword())){
            return false;
        }
        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);
        return true;
    }
//
//    public Account updateAccount(UpdateAccountReq request, String id){
//        Account user = accountRepository.findById(id).orElseThrow(() -> new NotFoundException("Account not found with id: " + id));
//        accountMapper.toUpdateAccount(user, request);
//        return accountRepository.save(user);
//    }
}
