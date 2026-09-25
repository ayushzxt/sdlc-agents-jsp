package com.example.leavemanagement.security;

import com.example.leavemanagement.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.http.MediaType;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    UserDetailsService userDetailsService(UserRepository repository) {
        return username -> repository.findByUsername(username).map(user -> org.springframework.security.core.userdetails.User
            .withUsername(user.getUsername()).password(user.getPasswordHash()).roles(user.getRole().name()).disabled(!user.isEnabled()).build())
            .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
    }

    @Bean
    DaoAuthenticationProvider authenticationProvider(UserDetailsService details, org.springframework.security.crypto.password.PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(); provider.setUserDetailsService(details); provider.setPasswordEncoder(encoder); return provider;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception { return configuration.getAuthenticationManager(); }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.csrfTokenRepository(new HttpSessionCsrfTokenRepository()))
            .authorizeHttpRequests(auth -> auth.requestMatchers("/", "/index", "/js/**", "/css/**", "/api/auth/csrf", "/api/auth/login").permitAll()
                .requestMatchers("/api/approvals/**").hasRole("APPROVER")
                .requestMatchers("/api/**").authenticated().anyRequest().permitAll())
            .formLogin(form -> form.disable()).httpBasic(basic -> basic.disable())
            .sessionManagement(session -> session.sessionFixation(fixation -> fixation.changeSessionId()))
            .logout(logout -> logout.disable())
            .exceptionHandling(errors -> errors
                .authenticationEntryPoint((request, response, exception) -> { response.setStatus(401); response.setContentType(MediaType.APPLICATION_JSON_VALUE); response.getWriter().write("{\"code\":\"UNAUTHORIZED\",\"message\":\"Authentication required\",\"fieldErrors\":[]}"); })
                .accessDeniedHandler((request, response, exception) -> { response.setStatus(403); response.setContentType(MediaType.APPLICATION_JSON_VALUE); response.getWriter().write("{\"code\":\"FORBIDDEN\",\"message\":\"Access denied\",\"fieldErrors\":[]}"); }));
        return http.build();
    }
}