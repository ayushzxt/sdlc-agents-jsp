package com.example.leavemanagement.security;

import java.util.UUID;

public record SessionUser(UUID id, String username, String role) { }