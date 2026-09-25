package com.example.leavemanagement.controller;

import com.example.leavemanagement.dto.CurrentUserResponse;
import com.example.leavemanagement.dto.LoginRequest;
import com.example.leavemanagement.exception.LoginThrottledException;
import com.example.leavemanagement.security.LoginRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final LoginRateLimiter loginRateLimiter;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
    public AuthController(AuthenticationManager authenticationManager, LoginRateLimiter loginRateLimiter) { this.authenticationManager = authenticationManager; this.loginRateLimiter = loginRateLimiter; }

    @PostMapping("/login")
    public CurrentUserResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse, CsrfToken csrfToken) {
        String source = httpRequest.getRemoteAddr();
        if (loginRateLimiter.isBlocked(request.username(), source)) throw new LoginThrottledException();
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
            httpRequest.changeSessionId();
            loginRateLimiter.reset(request.username(), source);
            SecurityContext context = SecurityContextHolder.createEmptyContext(); context.setAuthentication(authentication); SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, httpRequest, httpResponse); return current(authentication, csrfToken);
        } catch (BadCredentialsException exception) {
            if (loginRateLimiter.recordFailure(request.username(), source)) throw new LoginThrottledException();
            throw exception;
        }
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) { SecurityContextHolder.clearContext(); HttpSession session = request.getSession(false); if (session != null) session.invalidate(); }

    @GetMapping("/csrf")
    public CurrentUserResponse csrf(CsrfToken token) { return new CurrentUserResponse(null, Set.of(), token.getToken()); }

    @GetMapping("/me")
    public CurrentUserResponse me(Authentication authentication, CsrfToken csrfToken) { return current(authentication, csrfToken); }

    private CurrentUserResponse current(Authentication authentication, CsrfToken csrfToken) { return new CurrentUserResponse(authentication.getName(), authentication.getAuthorities().stream().map(a -> a.getAuthority().replace("ROLE_", "")).collect(java.util.stream.Collectors.toSet()), csrfToken.getToken()); }
}