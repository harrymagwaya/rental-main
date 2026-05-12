package com.xpro.rentalmain.rentalmain.controller;

import com.xpro.rentalmain.rentalmain.dto.RentalPaymentEventResponse;
import com.xpro.rentalmain.rentalmain.service.RentalPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ledger")
@RequiredArgsConstructor
public class RentalLedgerController {

    private final RentalPaymentService ledgerService;

    /**
     * RENTER VIEW: Get the specific payment behavioral history for a tenant.
     */
    @GetMapping("/tenant/{tenantId}")
    public List<RentalPaymentEventResponse> getTenantLedger(@PathVariable UUID tenantId) {
        return ledgerService.getHistoryByTenant(tenantId);
    }

    /**
     * LANDLORD VIEW: Get the rent roll (all unit statuses) for a specific property.
     */
    @GetMapping("/property/{propertyId}")
    public List<RentalPaymentEventResponse> getPropertyRentRoll(@PathVariable UUID propertyId) {
        return ledgerService.getHistoryByProperty(propertyId);
    }

    /**
     * ADMIN/LANDLORD ACTION: Manually correct a behavioral event.
     */
    @PatchMapping("/event/{eventId}/adjust")
    public RentalPaymentEventResponse manualAdjustment(
            @PathVariable UUID eventId,
            @RequestParam BigDecimal actualPaid,
            @RequestParam Integer daysLate) {
        return ledgerService.updateEvent(eventId, actualPaid, daysLate);
    }
}