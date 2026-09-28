package com.example.leavemanagement.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.leavemanagement.entity.Role;
import com.example.leavemanagement.entity.User;
import com.example.leavemanagement.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class SecurityConfigTest {
    @Test
    void userDetailsUsesBcryptHashAndRoleAuthority() {
        UserRepository repository = org.mockito.Mockito.mock(UserRepository.class);
        String hash = new BCryptPasswordEncoder().encode("correct-password");
        User user = new User("employee@example.com", hash, Role.EMPLOYEE);
        when(repository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        UserDetails details = new SecurityConfig().userDetailsService(repository).loadUserByUsername(user.getUsername());

        assertThat(details.getPassword()).isEqualTo(hash);
        assertThat(details.getAuthorities()).extracting("authority").containsExactly("ROLE_EMPLOYEE");
        assertThat(new BCryptPasswordEncoder().matches("correct-password", details.getPassword())).isTrue();
        assertThat(new BCryptPasswordEncoder().matches("wrong-password", details.getPassword())).isFalse();
    }
}