package com.example.leavemanagement.dto;

import java.util.List;

public record ApiError(String code, String message, List<FieldErrorResponse> fieldErrors) { }