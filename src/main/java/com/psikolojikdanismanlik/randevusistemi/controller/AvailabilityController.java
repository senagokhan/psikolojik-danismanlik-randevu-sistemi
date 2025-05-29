package com.psikolojikdanismanlik.randevusistemi.controller;

import com.psikolojikdanismanlik.randevusistemi.dto.request.AvailabilityRequest;
import com.psikolojikdanismanlik.randevusistemi.dto.response.AvailabilityResponseDto;
import com.psikolojikdanismanlik.randevusistemi.service.AvailabilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/therapists/{therapistId}/availabilities")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @DeleteMapping("/{availabilityId}")
    public ResponseEntity<Void> deleteAvailability(
            @PathVariable Long therapistId,
            @PathVariable Long availabilityId,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws AccessDeniedException {
        availabilityService.deleteAvailability(therapistId, availabilityId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{availabilityId}")
    public ResponseEntity<AvailabilityResponseDto> updateAvailability(
            @PathVariable Long therapistId,
            @PathVariable Long availabilityId,
            @RequestBody AvailabilityRequest request
    ) throws AccessDeniedException {
        AvailabilityResponseDto updated = availabilityService.updateAvailability(therapistId, availabilityId, request);
        return ResponseEntity.ok(updated);
    }
}


