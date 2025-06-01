package com.psikolojikdanismanlik.randevusistemi.controller;

import com.psikolojikdanismanlik.randevusistemi.dto.request.UserUpdateRequest;
import com.psikolojikdanismanlik.randevusistemi.dto.response.UserResponseDto;
import com.psikolojikdanismanlik.randevusistemi.service.UserService;
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
    public ResponseEntity<List<UserResponseDto>> getAllUsers(@AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        if (!userService.isAdmin(userDetails.getUsername())) {
            throw new AccessDeniedException("Yetkisiz erişim.");
        }

        List<UserResponseDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }


}
