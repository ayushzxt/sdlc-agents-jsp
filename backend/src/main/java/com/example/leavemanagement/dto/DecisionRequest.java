package com.example.leavemanagement.dto;

import jakarta.validation.constraints.Size;

public record DecisionRequest(@Size(max = 1000) String comment) { }