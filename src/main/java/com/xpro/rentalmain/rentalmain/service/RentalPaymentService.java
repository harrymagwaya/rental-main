package com.xpro.rentalmain.rentalmain.service;

import com.xpro.rentalmain.rentalmain.dto.RentalPaymentEventResponse;
import com.xpro.rentalmain.rentalmain.entity.FinancialRecord;
import com.xpro.rentalmain.rentalmain.entity.RentalPaymentEvent;
import com.xpro.rentalmain.rentalmain.entity.RentalProfile;
import com.xpro.rentalmain.rentalmain.repository.RentalPaymentEventRepository;
import com.xpro.rentalmain.rentalmain.repository.RentalProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RentalPaymentService {

    private final RentalProfileRepository profileRepo;
    private final RentalPaymentEventRepository eventRepo;

    @Transactional
    public RentalPaymentEventResponse recordBehavioralEvent(FinancialRecord payment) {
        RentalProfile profile = profileRepo.findActiveByTenantId(payment.getTenantId())
                .orElseThrow(() -> new RuntimeException("No active lease found for tenant"));

        // Logic: Standardized due date on the 5th
        LocalDateTime dueDate = payment.getTransactionDate()
                .withDayOfMonth(profile.getRentDueDay());

        long daysLate = java.time.Duration.between(dueDate, payment.getTransactionDate()).toDays();
        if (daysLate < 0) daysLate = 0;

        RentalPaymentEvent event = RentalPaymentEvent.builder()
                .rentalProfile(profile)
                .dueDate(dueDate)
                .paymentDate(payment.getTransactionDate())
                .expectedAmount(profile.getUnit().getRentAmount())
                .amountPaid(payment.getAmount())
                .daysLate((int) daysLate)
                .status(payment.getStatus())
                .transactionReference(payment.getTxnId())
                .build();

        RentalPaymentEvent saved = eventRepo.save(event);
        log.info("Ledger Updated: Tenant [{}], Days Late: {}", payment.getTenantId(), daysLate);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public RentalPaymentEventResponse getById(UUID eventId) {
        return eventRepo.findById(eventId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new EntityNotFoundException("Payment event not found: " + eventId));
    }

    @Transactional(readOnly = true)
    public List<RentalPaymentEventResponse> getAll() {
        return eventRepo.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RentalPaymentEventResponse> getHistoryByTenant(UUID tenantId) {
        return eventRepo.findByRentalProfileTenantIdOrderByPaymentDateDesc(tenantId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RentalPaymentEventResponse> getHistoryByProperty(UUID propertyId) {
        return eventRepo.findAllByPropertyId(propertyId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public RentalPaymentEventResponse updateEvent(UUID eventId, BigDecimal actualPaid, Integer adjustedDaysLate) {
        RentalPaymentEvent event = eventRepo.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        event.setAmountPaid(actualPaid);
        event.setDaysLate(adjustedDaysLate);

        return mapToResponse(eventRepo.save(event));
    }

    @Transactional
    public void deleteEvent(UUID eventId) {
        eventRepo.deleteById(eventId);
    }

    // =========================
    // PRIVATE MAPPING LOGIC
    // =========================

    private RentalPaymentEventResponse mapToResponse(RentalPaymentEvent entity) {
        return new RentalPaymentEventResponse(
                entity.getId(),
                entity.getRentalProfile().getId(),
                entity.getRentalProfile().getUnit().getUnitNumber(), // Flattened
                entity.getDueDate(),
                entity.getPaymentDate(),
                entity.getExpectedAmount(),
                entity.getAmountPaid(),
                entity.getDaysLate(),
                entity.getStatus(),
                entity.getMethod(),
                entity.getTransactionReference()
        );
    }
}