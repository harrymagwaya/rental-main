package com.xpro.rentalmain.rentalmain.controller;

import com.xpro.rentalmain.rentalmain.dto.AddressRequest;
import com.xpro.rentalmain.rentalmain.dto.AddressResponse;
import com.xpro.rentalmain.rentalmain.dto.AddressUpdateDTO;
import com.xpro.rentalmain.rentalmain.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AddressResponse create(@RequestBody AddressRequest request) {
        return addressService.createAddress(request);
    }

    @GetMapping("/{id}")
    public AddressResponse getById(@PathVariable UUID id) {
        return addressService.getById(id);
    }

    @GetMapping
    public List<AddressResponse> getAll() {
        return addressService.findAll();
    }

    @PatchMapping("/{id}")
    public AddressResponse update(@PathVariable UUID id, @RequestBody AddressUpdateDTO dto) {
        return addressService.updateAddress(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        addressService.deleteAddress(id);
    }
}