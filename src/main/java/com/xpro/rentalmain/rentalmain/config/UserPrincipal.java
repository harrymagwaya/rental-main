package com.xpro.rentalmain.rentalmain.config;

import com.xpro.rentalmain.rentalmain.model.UserStatus;
import com.xpro.rentalmain.rentalmain.model.UserType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


public class UserPrincipal implements UserDetails {

    private final UUID id;
    private final String username;
    private final String password;
    private final String email;
    private final UserType userType;
    private final UserStatus status;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(UUID id, String username, String email, String password, UserType type,UserStatus status, Collection<? extends GrantedAuthority> authorities){
        this.id = id;
        this.username = username;
        this.password  = password;
        this.email = email;
        this.userType = type;
        this.authorities = authorities;
        this.status = status;

    }


    public static UserPrincipal createUser(User user){
        List <GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getType()));
        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getType(),
                user.getStatus(),
                authorities
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public UUID getId(){
        return id;
    }

    public String getEmail(){
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public UserType getUserType(){
        return userType;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public UserStatus getStatus() {
        return status;
    }
}
