package com.xpro.rentalmain.rentalmain.controller;

import com.xpro.rentalmain.rentalmain.dto.FinancialRecordRequest;
import com.xpro.rentalmain.rentalmain.dto.FinancialRecordResponse;
import com.xpro.rentalmain.rentalmain.model.PaymentStatus;
import com.xpro.rentalmain.rentalmain.service.FinancialRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/financial-records")
@RequiredArgsConstructor
public class FinancialRecordController {

    private final FinancialRecordService financialRecordService;

    /**
     * POST: Attach a new manual record
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FinancialRecordResponse createRecord(@RequestBody FinancialRecordRequest request) {
        return financialRecordService.attachRecordToTenant(request);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<FinancialRecordResponse> getAllRecords() {
        return financialRecordService.getAllRecords();
    }

    /**
     * GET: Retrieve a specific record
     */
    @GetMapping("/{recordId}")
    public FinancialRecordResponse getRecord(@PathVariable UUID recordId) {
        return financialRecordService.getRecordById(recordId);
    }

    /**
     * GET: History for a tenant with optional timeframe (7days, 30days, etc.)
     */
    @GetMapping("/tenant/{tenantId}")
    public List<FinancialRecordResponse> getTenantHistory(
            @PathVariable UUID tenantId,
            @RequestParam(required = false) String timeframe) {

        if (timeframe != null) {
            return financialRecordService.getTenantHistoryByTimeframe(tenantId, timeframe);
        }
        return financialRecordService.getTenantHistory(tenantId);
    }

    /**
     * GET: Archive for a specific year and month
     */
    @GetMapping("/tenant/{tenantId}/archive")
    public List<FinancialRecordResponse> getMonthlyArchive(
            @PathVariable UUID tenantId,
            @RequestParam int year,
            @RequestParam int month) {
        return financialRecordService.getHistoryForSpecificMonth(tenantId, year, month);
    }

    /**
     * PUT: Update record details (Only if PENDING)
     */
    @PutMapping("/{recordId}")
    public FinancialRecordResponse updateRecord(
            @PathVariable UUID recordId,
            @RequestBody FinancialRecordRequest request) {
        return financialRecordService.updateRecord(recordId, request);
    }

    /**
     * PATCH: Verify status (Confirm/Reject/Late etc.)
     */
    @PatchMapping("/{recordId}/status")
    public FinancialRecordResponse updateStatus(
            @PathVariable UUID recordId,
            @RequestParam PaymentStatus status) {
        return financialRecordService.updateStatus(recordId, status);
    }

    /**
     * DELETE: Remove record
     */
    @DeleteMapping("/{recordId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRecord(@PathVariable UUID recordId) {
        financialRecordService.deleteRecord(recordId);
    }
}