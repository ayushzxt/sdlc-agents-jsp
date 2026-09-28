package com.example.leavemanagement.security;

import com.example.leavemanagement.entity.User;
import com.example.leavemanagement.exception.ResourceNotFoundException;
import com.example.leavemanagement.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    private final UserRepository userRepository;
    public CurrentUserService(UserRepository userRepository) { this.userRepository = userRepository; }
    public User requireUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) throw new ResourceNotFoundException("User not found");
        return userRepository.findByUsername(authentication.getName()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}