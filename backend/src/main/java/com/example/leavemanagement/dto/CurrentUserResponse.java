package com.example.leavemanagement.dto;

import java.util.Set;

public record CurrentUserResponse(String username, Set<String> roles, String csrfToken) { }