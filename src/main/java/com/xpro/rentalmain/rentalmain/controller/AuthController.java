package com.xpro.rentalmain.rentalmain.controller;

import com.xpro.rentalmain.rentalmain.dto.*;
import com.xpro.rentalmain.rentalmain.model.RentalApp;
import com.xpro.rentalmain.rentalmain.service.AuthService;
import com.xpro.rentalmain.rentalmain.util.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request,
            @RequestHeader(Constants.APP_NAME) RentalApp app) { // Auto-converts header string to Enum

        return authService.login(request, app);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        authService.logout(request);
    }

    @PostMapping("/password-reset/initiate")
    @ResponseStatus(HttpStatus.OK)
    public void initiateReset(@RequestParam String email) {
        authService.initiateReset(email);
    }

    @PostMapping("/password-reset/complete")
    @ResponseStatus(HttpStatus.OK)
    public void completeReset(@Valid @RequestBody SecurityResetRequest request) {
        authService.completeReset(request);
    }
}