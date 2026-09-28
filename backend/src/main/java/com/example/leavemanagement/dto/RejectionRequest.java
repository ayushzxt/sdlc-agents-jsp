package com.example.leavemanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectionRequest(@NotBlank @Size(max = 1000) String comment) { }