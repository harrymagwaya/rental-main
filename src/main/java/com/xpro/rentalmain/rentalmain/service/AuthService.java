package com.xpro.rentalmain.rentalmain.service;

import com.xpro.rentalmain.rentalmain.config.UserPrincipal;
import com.xpro.rentalmain.rentalmain.dto.PasswordChangeRequest;
import com.xpro.rentalmain.rentalmain.entity.User;
import com.xpro.rentalmain.rentalmain.model.RentalApp; // Replacement for HospusAPP
import com.xpro.rentalmain.rentalmain.model.UserType; // Your Enum
import com.xpro.rentalmain.rentalmain.dto.AuthResponse;
import com.xpro.rentalmain.rentalmain.dto.SecurityResetRequest;
import com.xpro.rentalmain.rentalmain.dto.LoginRequest;
import com.xpro.rentalmain.rentalmain.repository.UserRepository;
import com.xpro.rentalmain.rentalmain.security.TokenProvider;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
public class AuthService {

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private OtpService otpService;

    @Autowired
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    public AuthResponse login(LoginRequest request, RentalApp app) {
        // 1. Fetch Principal
        UserPrincipal principal = (UserPrincipal) userDetailsService.loadUserByUsername(request.getEmail());

        // 2. Verify Password
        if (!passwordEncoder.matches(request.getPassword(), principal.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        // 3. Check App Access (Landlord app vs Tenant app vs Admin panel)
        validateAppAccess(principal.getUserRole(), app);

        // 4. Create Authentication
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );

        // 5. Generate Token
        String token = tokenProvider.generateToken(auth);

        return new AuthResponse(
                token,
                principal.getId(),
                tokenProvider.getExpiration(token),
                principal.getUserRole()
        );
    }

    public void logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        log.info("User logged out successfully.");
    }

    private void validateAppAccess(UserType role, RentalApp appSource) {
        boolean isAuthorized = switch (appSource) {
            // Admin Portal allows System Admins and Loan Admins
            case XPRO_ADMIN_PORTAL -> (role == UserType.SYSTEM_ADMIN );

            case XPRO_LOAN_WEB_APP ->  role == UserType.LOAN_ADMIN;

            // Landlord Web App allows Landlords and System Admins
            case XPRO_LANDLORD_WEB_APP -> (role == UserType.LANDLORD || role == UserType.SYSTEM_ADMIN);

            // Mobile app is primarily for Tenants
            case XPRO_RENTAL_MOBILE_APP -> role == UserType.TENANT;

            // General Web App (Public/Shared)
            case XPRO_WEBB_APP -> true;

            // Internal services
            case XPRO_STORAGE_SERVICE -> (role == UserType.SYSTEM_ADMIN);
        };

        if (!isAuthorized) {
            log.warn("Access Denied: {} tried to access {}", role, appSource.getValue());
            throw new AccessDeniedException("Your account type (" + role + ") is not permitted to access " + appSource.getValue());
        }
    }

    // --- PASSWORD MANAGEMENT (Stateless OTP Logic) ---

    @Transactional(readOnly = true)
    public void initiateReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("No user found with this email"));

        // Generate OTP based on current password hash (Stateless)
        String userSecret = user.getPassword() + user.getEmail();
        String otp = otpService.generateStatelessOtp(userSecret);

        // Publish event for email service to pick up
        // eventPublisher.publishEvent(new PasswordResetEvent(email, otp));
        log.info("Reset OTP generated for rental user: {}", email);
    }

    @Transactional(readOnly = true)
    public boolean verifyOtp(String email, String otpCode) {
        return userRepository.findByEmail(email)
                .map(user -> {
                    String userSecret = user.getPassword() + user.getEmail();
                    return otpService.isOtpValid(userSecret, otpCode);
                })
                .orElse(false);
    }

    @Transactional
    public void completeReset(SecurityResetRequest request) {
        if (!verifyOtp(request.getEmail(), request.getOtpCode())) {
            throw new IllegalArgumentException("Invalid or expired OTP code.");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password successfully reset for: {}", request.getEmail());
    }

    @Transactional
    public void changePassword(UUID userId, PasswordChangeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Confirmation password does not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}