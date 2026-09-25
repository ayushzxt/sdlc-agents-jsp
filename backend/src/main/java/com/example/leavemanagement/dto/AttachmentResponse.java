package com.example.leavemanagement.dto;

import java.util.UUID;

public record AttachmentResponse(UUID id, String originalFilename, String contentType, long size) { }