package com.psikolojikdanismanlik.randevusistemi.controller;

import com.psikolojikdanismanlik.randevusistemi.dto.request.AvailabilityRequest;
import com.psikolojikdanismanlik.randevusistemi.dto.response.AvailabilityResponseDto;
import com.psikolojikdanismanlik.randevusistemi.entity.Availability;
import com.psikolojikdanismanlik.randevusistemi.service.AvailabilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/therapists/{therapistId}/availabilities")
public class AvailabilityController {

    private final AvailabilityService availabilityService;
    private static final Logger logger = LoggerFactory.getLogger(AvailabilityController.class);


    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @PostMapping
    public ResponseEntity<Availability> addAvailability(
            @PathVariable Long therapistId,
            @RequestBody AvailabilityRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws AccessDeniedException {
        Availability availability = availabilityService.addAvailability(therapistId, request, userDetails.getUsername());
        return new ResponseEntity<>(availability, HttpStatus.CREATED);
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

    @GetMapping("/available-at")
    public ResponseEntity<Boolean> isTherapistAvailable(
            @PathVariable Long therapistId,
            @RequestParam("time") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime time,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String currentEmail = userDetails.getUsername();
        System.out.println("Kullanıcı sorguladı: " + currentEmail);

        boolean available = availabilityService.isTherapistAvailableOn(therapistId, time);
        return ResponseEntity.ok(available);
    }

    @GetMapping
    public ResponseEntity<Page<AvailabilityResponseDto>> getAvailabilitiesForTherapist(
            @PathVariable Long therapistId,
            @PageableDefault(sort = "startTime", direction = Sort.Direction.ASC) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        logger.info("Kullanıcı [{}] terapistin [{}] müsaitliklerini sorguladı.", userDetails.getUsername(), therapistId);

        Page<AvailabilityResponseDto> availabilities = availabilityService.getAvailabilitiesForTherapist(therapistId, pageable);
        return ResponseEntity.ok(availabilities);
    }
}


