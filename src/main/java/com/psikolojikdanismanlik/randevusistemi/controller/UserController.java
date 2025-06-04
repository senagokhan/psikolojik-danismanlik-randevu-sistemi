package com.psikolojikdanismanlik.randevusistemi.controller;

import com.psikolojikdanismanlik.randevusistemi.dto.request.UserUpdateRequest;
import com.psikolojikdanismanlik.randevusistemi.dto.response.UserResponseDto;
import com.psikolojikdanismanlik.randevusistemi.enums.Role;
import com.psikolojikdanismanlik.randevusistemi.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        UserResponseDto response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponseDto> updateProfile(@RequestBody UserUpdateRequest request) {
        UserResponseDto response = userService.updateCurrentUser(request, request.getEmail());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws AccessDeniedException {
        userService.deleteUser(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/list")
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable
    ) throws AccessDeniedException {
        if (!userService.isAdmin(userDetails.getUsername())) {
            throw new AccessDeniedException("Yetkisiz erişim.");
        }

        Page<UserResponseDto> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }


    @GetMapping("/role")
    public ResponseEntity<List<UserResponseDto>> getUsersByRole(
            @RequestParam Role role,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws AccessDeniedException {
        List<UserResponseDto> users = userService.getUsersByRole(role, userDetails.getUsername());
        return ResponseEntity.ok(users);
    }

    @PutMapping("/admin/{userId}/role")
    public ResponseEntity<UserResponseDto> updateUserRole(
            @PathVariable Long userId,
            @RequestParam Role newRole,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws AccessDeniedException {
        UserResponseDto updatedUser = userService.updateUserRole(userId, newRole, userDetails.getUsername());
        return ResponseEntity.ok(updatedUser);
    }




}
