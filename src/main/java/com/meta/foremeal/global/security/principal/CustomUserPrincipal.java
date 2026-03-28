package com.meta.foremeal.global.security.principal;

import com.meta.foremeal.user.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserPrincipal implements UserDetails {

    private final Long userId;
    private final String email;
    private final String password;
    private final String role;

    public CustomUserPrincipal(Long userId, String email, String password, String role) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public static CustomUserPrincipal from(User user) {
        return new CustomUserPrincipal(
                user.getUserId(),
                user.getEmail(),
                user.getPassword(),
                user.getRole().name()
        );
    }

    public Long getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }
}