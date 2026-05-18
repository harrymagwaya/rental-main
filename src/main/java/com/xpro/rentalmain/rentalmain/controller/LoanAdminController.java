package com.xpro.rentalmain.rentalmain.controller;

import com.xpro.rentalmain.rentalmain.dto.LoanAdminResponseDTO;
import com.xpro.rentalmain.rentalmain.dto.LoanAdminUpdateDTO;
import com.xpro.rentalmain.rentalmain.service.LoanAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/loan-admins")
@RequiredArgsConstructor
public class LoanAdminController {

    private final LoanAdminService loanAdminService;

    /**
     * Get or Initialize the Admin profile for a specific User ID.
     * Use this when an admin first logs in to ensure their profile exists.
     */
    @GetMapping("/profile/{userId}")
    public LoanAdminResponseDTO getProfile(@PathVariable UUID userId) {
        return loanAdminService.getAdminProfile(userId);
    }

    @GetMapping("/{id}")
    public LoanAdminResponseDTO getById(@PathVariable UUID id) {
        return loanAdminService.getById(id);
    }

    @GetMapping
    public List<LoanAdminResponseDTO> getAll() {
        return loanAdminService.findAll();
    }

    @PatchMapping("/{userId}")
    public LoanAdminResponseDTO update(@PathVariable UUID userId, @RequestBody LoanAdminUpdateDTO dto) {
        return loanAdminService.updateAdmin(userId, dto);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID userId) {
        loanAdminService.deleteProfile(userId);
    }
}