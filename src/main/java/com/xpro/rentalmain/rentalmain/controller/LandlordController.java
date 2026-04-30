package com.xpro.rentalmain.rentalmain.controller;

import com.xpro.rentalmain.rentalmain.dto.LandlordResponseDTO;
import com.xpro.rentalmain.rentalmain.dto.LandlordUpdateDTO;
import com.xpro.rentalmain.rentalmain.service.LandlordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/landlords")
@RequiredArgsConstructor
public class LandlordController {

    private final LandlordService landlordService;

    @GetMapping("/{userId}")
    public LandlordResponseDTO getProfile(@PathVariable UUID userId) {
        return landlordService.getLandlordProfile(userId);
    }

    @PatchMapping("/{userId}")
    public LandlordResponseDTO updateProfile(
            @PathVariable UUID userId,
            @RequestBody LandlordUpdateDTO updateDto) {
        return landlordService.updateLandlord(userId, updateDto);
    }
}