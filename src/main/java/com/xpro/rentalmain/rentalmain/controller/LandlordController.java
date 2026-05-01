package com.xpro.rentalmain.rentalmain.controller;

import com.xpro.rentalmain.rentalmain.dto.LandlordResponseDTO;
import com.xpro.rentalmain.rentalmain.dto.LandlordUpdateDTO;
import com.xpro.rentalmain.rentalmain.service.LandlordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/landlords")
@RequiredArgsConstructor
public class LandlordController {

    private final LandlordService landlordService;

    /**
     * GET: Retrieve or initialize a specific landlord profile.
     */
    @GetMapping("/{userId}")
    public LandlordResponseDTO getProfile(@PathVariable UUID userId) {
        return landlordService.getLandlordProfile(userId);
    }

    /**
     * GET: Fetch all landlords (Admin view).
     */
    @GetMapping
    public List<LandlordResponseDTO> getAllLandlords() {
        return landlordService.findAll();
    }

    /**
     * PATCH: Null-safe partial update of profile details.
     */
    @PatchMapping("/{userId}")
    public LandlordResponseDTO updateProfile(
            @PathVariable UUID userId,
            @RequestBody LandlordUpdateDTO updateDto) {
        return landlordService.updateLandlord(userId, updateDto);
    }

    /**
     * DELETE: Remove the landlord profile extension.
     */
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfile(@PathVariable UUID userId) {
        landlordService.deleteLandlord(userId);
    }
}