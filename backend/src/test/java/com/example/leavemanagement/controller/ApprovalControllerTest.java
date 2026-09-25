package com.example.leavemanagement.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.leavemanagement.dto.LeaveRequestResponse;
import com.example.leavemanagement.entity.LeaveRequestStatus;
import com.example.leavemanagement.service.LeaveRequestService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ApprovalController.class)
@AutoConfigureMockMvc
@Import(ApiExceptionHandler.class)
class ApprovalControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LeaveRequestService service;

    @Test
    void approvesWithOptionalCommentAtHttpBoundary() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.approve("approver@example.com", id, "Coverage confirmed")).thenReturn(response(id, LeaveRequestStatus.APPROVED));

        mockMvc.perform(post("/api/approvals/leave-requests/{id}/approve", id)
                .with(authentication(new TestingAuthenticationToken("approver@example.com", null, "ROLE_APPROVER")))
                .with(csrf())
                .contentType("application/json")
                .content("{\"comment\":\"Coverage confirmed\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(service).approve("approver@example.com", id, "Coverage confirmed");
    }

    @Test
    void rejectsMissingOrBlankCommentBeforeCallingService() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(post("/api/approvals/leave-requests/{id}/reject", id)
                .with(authentication(new TestingAuthenticationToken("approver@example.com", null, "ROLE_APPROVER")))
                .with(csrf())
                .contentType("application/json")
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.fieldErrors[0].field").value("comment"));

        mockMvc.perform(post("/api/approvals/leave-requests/{id}/reject", id)
                .with(authentication(new TestingAuthenticationToken("approver@example.com", null, "ROLE_APPROVER")))
                .with(csrf())
                .contentType("application/json")
                .content("{\"comment\":\" \"}"))
            .andExpect(status().isBadRequest());

        verify(service, never()).reject(eq("approver@example.com"), eq(id), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectsUnauthenticatedDecisionAndMapsStateConflict() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(post("/api/approvals/leave-requests/{id}/approve", id).with(csrf()))
            .andExpect(status().isUnauthorized());

        when(service.approve("approver@example.com", id, null))
            .thenThrow(new com.example.leavemanagement.exception.ConflictException("Request has already been decided"));
        mockMvc.perform(post("/api/approvals/leave-requests/{id}/approve", id)
                .with(authentication(new TestingAuthenticationToken("approver@example.com", null, "ROLE_APPROVER"))).with(csrf()))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    private LeaveRequestResponse response(UUID id, LeaveRequestStatus status) {
        return new LeaveRequestResponse(id, "employee@example.com", "approver@example.com", "Annual", null, null, "Rest", null, null,
            status, null, null, null, null, null);
    }
}