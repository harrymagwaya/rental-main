package com.xpro.rentalmain.rentalmain.controller;

import com.xpro.rentalmain.rentalmain.dto.TenantResponseDTO;
import com.xpro.rentalmain.rentalmain.dto.TenantUpdateDTO;
import com.xpro.rentalmain.rentalmain.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    /**
     * GET: Retrieve or initialize a tenant profile by the User ID.
     * This is the main endpoint Harold will call when opening his profile.
     */
    @GetMapping("/{userId}")
    public TenantResponseDTO getTenantProfile(@PathVariable UUID userId) {
        return tenantService.getTenantProfile(userId);
    }

    /**
     * PATCH: Partial update for the tenant profile.
     * Use this to add the National ID (NIN) or update specific fields.
     */
    @PatchMapping("/{userId}")
    public TenantResponseDTO updateTenantProfile(
            @PathVariable UUID userId,
            @RequestBody TenantUpdateDTO updateDto) {
        return tenantService.updateTenant(userId, updateDto);
    }

    /**
     * GET: Admin endpoint to list all tenant profiles in the pilot.
     */
    @GetMapping
    public List<TenantResponseDTO> getAllTenants() {
        return tenantService.findAll();
    }



    /**
     * DELETE: Remove the tenant profile extension.
     * Note: This removes the profile, but usually keeps the master User identity.
     */
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTenantProfile(@PathVariable UUID userId) {
        tenantService.deleteProfile(userId);
    }
}