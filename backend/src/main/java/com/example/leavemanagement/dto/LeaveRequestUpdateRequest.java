package com.example.leavemanagement.dto;

import com.example.leavemanagement.entity.PartialDay;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record LeaveRequestUpdateRequest(@NotBlank @Size(max = 50) String leaveType, @NotNull LocalDate startDate,
                                        @NotNull LocalDate endDate, @NotBlank @Size(max = 1000) String reason,
                                        PartialDay partialDay) { }