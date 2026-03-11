package com.xpro.rentalmain.rentalmain.security;

import com.xpro.rentalmain.rentalmain.config.UserPrincipal;
import com.xpro.rentalmain.rentalmain.entity.UserIdentity;
import com.xpro.rentalmain.rentalmain.service.UserIdentityService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserIdentityService userIdentityService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userIdentityService.findByEmail(email)
                .map(UserPrincipal::createUser)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    public UserPrincipal loadUserById(UUID id) {
        // Calls the service method directly
        UserIdentity user = userIdentityService.getUserById(id);
        return UserPrincipal.createUser(user);
    }
}