package com.capston.matching_app.security;

import com.capston.matching_app.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {
    private final Integer userId;
    private final String email;
    private final String password;
    private final List<SimpleGrantedAuthority> authorities;

    //DB조회용 생성자
    public CustomUserDetails(User user) {
        this.userId = user.getUserId();
        this.email = user.getEmail();
        this.password = user.getPasswordHash();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    //JWT 인증용 생성자
    public CustomUserDetails(Integer userId, String email, String role) {
        this.userId = userId;
        this.email = email;
        this.password = null;
        this.authorities = List.of(new SimpleGrantedAuthority(role));
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
    public Integer getUserId() { return userId; }  // ★ 중요
}
