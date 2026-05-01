package com.xpro.rentalmain.rentalmain.security;

import com.xpro.rentalmain.rentalmain.config.UserPrincipal;
import com.xpro.rentalmain.rentalmain.entity.User;
import com.xpro.rentalmain.rentalmain.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userIdentityService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            // This returns the User entity or throws EntityNotFoundException
            User user = userIdentityService.getByEmail(email);
            return UserPrincipal.createUser(user);
        } catch (EntityNotFoundException e) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
    }

    public UserPrincipal loadUserById(UUID id) {
        // Calls the service method directly
        User user = userIdentityService.getById(id);
        return UserPrincipal.createUser(user);
    }
}