package com.xpro.rentalmain.rentalmain.controller;

import com.xpro.rentalmain.rentalmain.dto.PropertyUnitRequest;
import com.xpro.rentalmain.rentalmain.dto.PropertyUnitResponse;
import com.xpro.rentalmain.rentalmain.dto.PropertyUnitUpdateRequest;
import com.xpro.rentalmain.rentalmain.service.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/units")
@RequiredArgsConstructor
public class PropertyUnitController {

    private final PropertyService propertyService;

    /**
     * CREATE: Add a unit to a specific property
     */
    @PostMapping("/property/{propertyId}")
    public PropertyUnitResponse addUnit(
            @PathVariable UUID propertyId,
            @RequestBody PropertyUnitRequest request) {
        return propertyService.addSingleUnit(propertyId, request);
    }

    /**
     * READ: Get unit details by ID
     */
    @GetMapping("/{id}")
    public PropertyUnitResponse getById(@PathVariable UUID id) {
        return propertyService.getUnitById(id);
    }

    /**
     * READ: Find which unit a tenant is currently living in
     */
    @GetMapping("/tenant/{tenantId}")
    public PropertyUnitResponse getByTenant(@PathVariable UUID tenantId) {
        return propertyService.getUnitByTenant(tenantId);
    }

    /**
     * UPDATE: Update unit details (rent, unit number, or status)
     */
    @PutMapping("/{id}")
    public PropertyUnitResponse update(
            @PathVariable UUID id,
            @RequestBody PropertyUnitUpdateRequest request) {
        // Ensure you have an updateUnit method in PropertyService matching your Service logic
        return propertyService.updateUnit(id, request);
    }

    @GetMapping("/{propertyId}/units")
    public List<PropertyUnitResponse> getUnitsByProperty(@PathVariable UUID propertyId) {
        return propertyService.getUnitsByProperty(propertyId);
    }

    /**
     * DELETE: Remove a unit (Service handles check for active tenants)
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        propertyService.deleteUnit(id);
    }
}