package com.xpro.rentalmain.rentalmain.controller;

import com.xpro.rentalmain.rentalmain.dto.RentalProfileCreateDTO;
import com.xpro.rentalmain.rentalmain.dto.RentalProfileResponseDTO;
import com.xpro.rentalmain.rentalmain.dto.RentalProfileUpdateDTO;
import com.xpro.rentalmain.rentalmain.service.RentalProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rental-profiles")
@RequiredArgsConstructor
public class RentalProfileController {

    private final RentalProfileService rentalProfileService;

    @PostMapping
    public RentalProfileResponseDTO createProfile(@RequestBody RentalProfileCreateDTO dto) {
        return rentalProfileService.createProfile(dto);
    }

    @GetMapping("/{id}")
    public RentalProfileResponseDTO getById(@PathVariable UUID id) {
        return rentalProfileService.getById(id);
    }

    @GetMapping
    public List<RentalProfileResponseDTO> getAll() {
        return rentalProfileService.getAllProfiles();
    }

    @GetMapping("/tenant/{tenantId}")
    public List<RentalProfileResponseDTO> getByTenant(@PathVariable UUID tenantId) {
        return rentalProfileService.getByTenantId(tenantId);
    }

    @PutMapping("/{id}")
    public RentalProfileResponseDTO update(@PathVariable UUID id, @RequestBody RentalProfileUpdateDTO dto) {
        return rentalProfileService.updateProfile(id, dto);
    }

    @PatchMapping("/{id}/terminate")
    public void terminateLease(@PathVariable UUID id) {
        rentalProfileService.terminateLease(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        rentalProfileService.deleteProfile(id);
    }
}