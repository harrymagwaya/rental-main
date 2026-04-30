package com.xpro.rentalmain.rentalmain.controller;

import com.xpro.rentalmain.rentalmain.dto.PropertyRequest;
import com.xpro.rentalmain.rentalmain.dto.PropertyResponse;
import com.xpro.rentalmain.rentalmain.dto.PropertyUpdateRequest;
import com.xpro.rentalmain.rentalmain.service.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PropertyResponse createProperty(@RequestBody PropertyRequest request) {
        return propertyService.createProperty(request);
    }

    @GetMapping("/{id}")
    public PropertyResponse getProperty(@PathVariable UUID id) {
        return propertyService.getPropertyById(id);
    }

    @PutMapping("/{id}")
    public PropertyResponse updateProperty(@PathVariable UUID id, @RequestBody PropertyUpdateRequest request) {
        return propertyService.updateProperty(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProperty(@PathVariable UUID id) {
        propertyService.deleteProperty(id);
    }

    @GetMapping("/landlord/{landlordId}")
    public List<PropertyResponse> getByLandlord(@PathVariable UUID landlordId) {
        return propertyService.getPropertiesByLandlord(landlordId);
    }
}