package com.xpro.rentalmain.rentalmain.service;

import com.xpro.rentalmain.rentalmain.dto.AddressRequest;
import com.xpro.rentalmain.rentalmain.dto.AddressResponse;
import com.xpro.rentalmain.rentalmain.dto.AddressUpdateDTO;
import com.xpro.rentalmain.rentalmain.entity.Address;
import com.xpro.rentalmain.rentalmain.repository.AddressRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepo;

    @Transactional
    public AddressResponse createAddress(AddressRequest request) {
        log.info("Creating address for city: {}", request.city());
        Address address = Address.builder()
                .street(request.street())
                .city(request.city())
                .country(request.country())
                .zipCode(request.zipCode())
                .postalCode(request.postalCode())
                .build();

        return mapToResponse(addressRepo.save(address));
    }

    @Transactional(readOnly = true)
    public AddressResponse getById(UUID id) {
        return addressRepo.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new EntityNotFoundException("Address not found with ID: " + id));
    }

    @Transactional
    public AddressResponse updateAddress(UUID id, AddressUpdateDTO dto) {
        Address existing = addressRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Address not found"));

        // Partial Update Logic
        if (dto.street() != null) existing.setStreet(dto.street());
        if (dto.city() != null) existing.setCity(dto.city());
        if (dto.country() != null) existing.setCountry(dto.country());
        if (dto.zipCode() != null) existing.setZipCode(dto.zipCode());
        if (dto.postalCode() != null) existing.setPostalCode(dto.postalCode());

        return mapToResponse(addressRepo.save(existing));
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> findAll() {
        return addressRepo.findAll().stream().map(this::mapToResponse).toList();
    }

    @Transactional
    public void deleteAddress(UUID id) {
        if (!addressRepo.existsById(id)) {
            throw new EntityNotFoundException("Address not found");
        }
        addressRepo.deleteById(id);
    }

    // Inside AddressService.java

    // Use this for internal service-to-service communication
    public Address getAddressEntity(UUID id) {
        return addressRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Address not found: " + id));
    }

    private AddressResponse mapToResponse(Address a) {
        return new AddressResponse(
                a.getId(),
                a.getStreet(),
                a.getCity(),
                a.getCountry(),
                a.getZipCode(),
                a.getPostalCode()
        );
    }
}