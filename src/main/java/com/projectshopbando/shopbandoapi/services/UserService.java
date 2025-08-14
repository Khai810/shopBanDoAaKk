package com.projectshopbando.shopbandoapi.services;

import com.projectshopbando.shopbandoapi.dtos.request.ChangePasswordReq;
import com.projectshopbando.shopbandoapi.dtos.request.CreateUserReq;
import com.projectshopbando.shopbandoapi.dtos.request.UpdateUserReq;
import com.projectshopbando.shopbandoapi.entities.User;
import com.projectshopbando.shopbandoapi.enums.Roles;
import com.projectshopbando.shopbandoapi.exception.NotFoundException;
import com.projectshopbando.shopbandoapi.mappers.UserMapper;
import com.projectshopbando.shopbandoapi.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public User createUser (CreateUserReq request){
        if(userRepository.existsByUsername(request.getUsername())){
            try {
                throw new BadRequestException("Username already exists");
            } catch (BadRequestException e) {
                throw new RuntimeException(e);
            }
        }

        User user = userMapper.toUser(request);

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Set<Roles> roles = new HashSet<>();
        roles.add(Roles.USER);
        user.setRole(roles);

        return userRepository.save(user);
    }

    public User getUserById(String id){
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found with id: " + id));
    }

    public Page<User> getAllUsers(int page, int size, String search){
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findByUsernameContainingOrPhoneContainingOrderByCreatedAtDesc(search, search, pageable);
    }

    public boolean changePassword(ChangePasswordReq request, String id){
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        if(!passwordEncoder.matches(request.getOldPassword(), user.getPassword())){
            return false;
        }
        if(!request.getNewPassword().equals(request.getConfirmPassword())){
            return false;
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return true;
    }

    public User updateUser(UpdateUserReq request, String id){
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        userMapper.toUpdateUser(user, request);
        return userRepository.save(user);
    }
}
