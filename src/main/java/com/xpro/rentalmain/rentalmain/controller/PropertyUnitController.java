package com.xpro.rentalmain.rentalmain.controller;

import com.xpro.rentalmain.rentalmain.dto.PropertyUnitRequest;
import com.xpro.rentalmain.rentalmain.dto.PropertyUnitResponse;
import com.xpro.rentalmain.rentalmain.dto.PropertyUnitUpdateRequest;
import com.xpro.rentalmain.rentalmain.service.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/units")
@RequiredArgsConstructor
public class PropertyUnitController {

    private final PropertyService propertyService;

    /**
     * POST: Add a single unit to a property
     */
    @PostMapping("/property/{propertyId}")
    @ResponseStatus(HttpStatus.CREATED)
    public PropertyUnitResponse addUnit(@PathVariable UUID propertyId, @RequestBody PropertyUnitRequest request) {
        return propertyService.addSingleUnit(propertyId, request);
    }

    @GetMapping("/{id}")
    public PropertyUnitResponse getUnit(@PathVariable UUID id) {
        return propertyService.getUnitById(id);
    }

    @PatchMapping("/{id}")
    public PropertyUnitResponse updateUnit(@PathVariable UUID id, @RequestBody PropertyUnitUpdateRequest request) {
        return propertyService.updateUnit(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUnit(@PathVariable UUID id) {
        propertyService.deleteUnit(id);
    }

    /**
     * GET: List all units for a specific building
     */
    @GetMapping("/property/{propertyId}")
    public List<PropertyUnitResponse> getUnitsByProperty(@PathVariable UUID propertyId) {
        return propertyService.getUnitsByProperty(propertyId);
    }

    /**
     * GET: Find the unit assigned to a specific tenant
     */
    @GetMapping("/tenant/{tenantId}")
    public PropertyUnitResponse getUnitByTenant(@PathVariable UUID tenantId) {
        return propertyService.getUnitByTenant(tenantId);
    }
}