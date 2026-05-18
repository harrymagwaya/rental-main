package com.xpro.rentalmain.rentalmain.controller;

import com.xpro.rentalmain.rentalmain.dto.UserRequest;
import com.xpro.rentalmain.rentalmain.dto.UserResponse;
import com.xpro.rentalmain.rentalmain.dto.UserUpdateDTO;
// Ensure this package is correct or updated
import com.xpro.rentalmain.rentalmain.model.UserStatus;
import com.xpro.rentalmain.rentalmain.service.UserService;
import com.xpro.rentalmain.rentalmain.util.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@RequestBody UserRequest request,
                                 @RequestHeader(value = Constants.ACTOR_ID, required = false) UUID actorId) {
        return userService.registerUser(request, actorId);
    }

    @GetMapping
    public Page<UserResponse> getAll(@RequestParam(required = false) UserStatus status,
                                     Pageable pageable) {
        return userService.getAllUsers(status, pageable);
    }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable UUID id) {
        return userService.findById(id);
    }

    @PatchMapping("/{id}")
    public UserResponse update(@PathVariable UUID id, @RequestBody UserUpdateDTO dto) {
        return userService.updateUser(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        userService.deleteUser(id);
    }
}