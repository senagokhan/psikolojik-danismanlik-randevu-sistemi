package com.psikolojikdanismanlik.randevusistemi.controller;

import com.psikolojikdanismanlik.randevusistemi.dto.request.AvailabilityRequest;
import com.psikolojikdanismanlik.randevusistemi.dto.request.TherapistRequest;
import com.psikolojikdanismanlik.randevusistemi.dto.request.TherapistUpdateRequest;
import com.psikolojikdanismanlik.randevusistemi.dto.response.AppointmentResponseDto;
import com.psikolojikdanismanlik.randevusistemi.dto.response.ClientResponseDto;
import com.psikolojikdanismanlik.randevusistemi.dto.response.TherapistResponseDto;
import com.psikolojikdanismanlik.randevusistemi.entity.Availability;
import com.psikolojikdanismanlik.randevusistemi.entity.Therapist;
import com.psikolojikdanismanlik.randevusistemi.service.AppointmentService;
import com.psikolojikdanismanlik.randevusistemi.service.AvailabilityService;
import com.psikolojikdanismanlik.randevusistemi.service.TherapistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/therapists")
@RequiredArgsConstructor
public class TherapistController {

    private final AvailabilityService availabilityService;
    private final TherapistService therapistService;
    private final AppointmentService appointmentService;

    @PostMapping("/{id}/availabilities")
    public ResponseEntity<Availability> addAvailability(@PathVariable Long id, @RequestBody AvailabilityRequest request) {
        Availability availability = availabilityService.addAvailability(id, request);
        return new ResponseEntity<>(availability, HttpStatus.CREATED);
    }

    @PostMapping
    public ResponseEntity<Therapist> createTherapist(@RequestBody TherapistRequest request) {
        Therapist therapist = therapistService.createTherapist(request);
        return new ResponseEntity<>(therapist, HttpStatus.CREATED);
    }


    @GetMapping("/my-appointments")
    public ResponseEntity<Page<AppointmentResponseDto>> getAppointmentsByLoggedInTherapist(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<AppointmentResponseDto> response = appointmentService
                .getAppointmentsByTherapistEmail(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(response);
    }


    @PutMapping("/{id}")
    public ResponseEntity<Therapist> updateTherapist(@PathVariable Long id, @RequestBody TherapistUpdateRequest request, @AuthenticationPrincipal UserDetails userDetails
    ) throws AccessDeniedException {
        Therapist updated = therapistService.updateTherapist(id, request, userDetails.getUsername());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTherapist(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails
    ) throws AccessDeniedException {
        therapistService.deleteTherapist(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<TherapistResponseDto>> getAllTherapists() {
        List<TherapistResponseDto> therapists = therapistService.getAllTherapists();
        return ResponseEntity.ok(therapists);
    }

    @GetMapping("/clients")
    public ResponseEntity<List<ClientResponseDto>> getClientsOfTherapist(@AuthenticationPrincipal UserDetails userDetails) {
        List<ClientResponseDto> clients = therapistService.getClientsOfTherapist(userDetails.getUsername());
        return ResponseEntity.ok(clients);
    }

}
