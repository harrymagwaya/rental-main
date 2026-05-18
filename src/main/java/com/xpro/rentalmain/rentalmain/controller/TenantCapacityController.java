package com.xpro.rentalmain.rentalmain.controller;

import com.xpro.rentalmain.rentalmain.dto.TenantCapacityRequestDTO;
import com.xpro.rentalmain.rentalmain.entity.TenantCapacity;
import com.xpro.rentalmain.rentalmain.service.TenantCapacityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenant-capacities")
@RequiredArgsConstructor
public class TenantCapacityController {

    private final TenantCapacityService capacityService;

    @PostMapping("/upsert")
    @ResponseStatus(HttpStatus.OK)
    public TenantCapacity upsertCapacity(@RequestBody TenantCapacityRequestDTO dto) {
        return capacityService.createOrUpdate(dto);
    }

    @GetMapping
    public List<TenantCapacity> getAll() {
        return capacityService.getAll();
    }

    @GetMapping("/{tenantId}")
    public TenantCapacity getByTenant(@PathVariable UUID tenantId) {
        return capacityService.getByTenant(tenantId);
    }

    @GetMapping("/record/{id}")
    public TenantCapacity getById(@PathVariable UUID id) {
        return capacityService.getById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        capacityService.delete(id);
    }
}