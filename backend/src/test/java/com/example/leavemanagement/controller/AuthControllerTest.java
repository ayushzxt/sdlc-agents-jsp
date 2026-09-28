package com.example.leavemanagement.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.leavemanagement.security.LoginRateLimiter;
import com.example.leavemanagement.repository.UserRepository;
import com.example.leavemanagement.security.PasswordConfig;
import com.example.leavemanagement.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc
@Import({ApiExceptionHandler.class, SecurityConfig.class, PasswordConfig.class})
class AuthControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private AuthenticationManager authenticationManager;
    @MockBean private LoginRateLimiter loginRateLimiter;
    @MockBean private UserRepository userRepository;

    @Test
    void rotatesSessionOnSuccessfulCustomLogin() throws Exception {
        Authentication authentication = new UsernamePasswordAuthenticationToken("employee@example.com", null,
            java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_EMPLOYEE")));
        when(authenticationManager.authenticate(org.mockito.ArgumentMatchers.any())).thenReturn(authentication);

        var session = new org.springframework.mock.web.MockHttpSession();
        mockMvc.perform(post("/api/auth/login").session(session).with(csrf())
                .contentType("application/json").content("{\"username\":\"employee@example.com\",\"password\":\"secret\"}"))
            .andExpect(status().isOk());

        verify(loginRateLimiter).reset("employee@example.com", "127.0.0.1");
    }

    @Test
    void returnsGeneric429AtThrottleBoundary() throws Exception {
        when(authenticationManager.authenticate(org.mockito.ArgumentMatchers.any())).thenThrow(new BadCredentialsException("bad"));
        when(loginRateLimiter.recordFailure("employee@example.com", "127.0.0.1")).thenReturn(true);

        mockMvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json")
                .content("{\"username\":\"employee@example.com\",\"password\":\"wrong\"}"))
            .andExpect(status().isTooManyRequests());
    }
}