package com.xpro.rentalmain.rentalmain.security;

import com.xpro.rentalmain.rentalmain.config.UserPrincipal;
import com.xpro.rentalmain.rentalmain.entity.User;
import com.xpro.rentalmain.rentalmain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.info("User with email {} not found during login attempt", email);
                    return new UsernameNotFoundException("User with email" + email +" not found");
                });
        log.info("Found user with email{} ", email);
        return UserPrincipal.createUser(user);
    }

    public UserPrincipal loadUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.info("User with id {} not found during login attempt", id);
                    return new UsernameNotFoundException("User not found");
                });

        log.info("Found User with id {}", id);
        return UserPrincipal.createUser(user);
    }
}
