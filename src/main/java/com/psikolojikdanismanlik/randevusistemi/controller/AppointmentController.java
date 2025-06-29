package com.psikolojikdanismanlik.randevusistemi.controller;

import com.psikolojikdanismanlik.randevusistemi.dto.request.AppointmentRequest;
import com.psikolojikdanismanlik.randevusistemi.dto.request.AppointmentStatusUpdateRequest;
import com.psikolojikdanismanlik.randevusistemi.dto.request.RescheduleRequestDto;
import com.psikolojikdanismanlik.randevusistemi.dto.response.AppointmentResponseDto;
import com.psikolojikdanismanlik.randevusistemi.service.AppointmentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<AppointmentResponseDto> createAppointment(
            @RequestBody AppointmentRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        AppointmentResponseDto appointment = appointmentService.createAppointment(request, userDetails.getUsername());
        return new ResponseEntity<>(appointment, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<AppointmentResponseDto> updateStatus(
            @PathVariable Long id,
            @RequestBody AppointmentStatusUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws AccessDeniedException {
        String therapistEmail = userDetails.getUsername();
        AppointmentResponseDto response = appointmentService.updateStatus(id, request.getStatus(), therapistEmail);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/reschedule")
    public ResponseEntity<AppointmentResponseDto> requestRescheduleByClient(
            @PathVariable Long id,
            @RequestBody RescheduleRequestDto request,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws AccessDeniedException {
        String email = userDetails.getUsername();
        AppointmentResponseDto response = appointmentService.requestRescheduleByClient(id, request, email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Page<AppointmentResponseDto>> getAppointmentsByClientId(
            @PathVariable Long id,
            @PageableDefault(size = 5, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Page<AppointmentResponseDto> appointments = appointmentService.getAppointmentsByClientId(id, pageable, userDetails.getUsername());
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/clients/{clientId}/future")
    public ResponseEntity<List<AppointmentResponseDto>> getFutureAppointmentsByClientId(@PathVariable Long clientId) {
        List<AppointmentResponseDto> response = appointmentService.getFutureAppointmentsByClientId(clientId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/clients/{clientId}/past")
    public ResponseEntity<List<AppointmentResponseDto>> getPastAppointmentsByClientId(@PathVariable Long clientId) {
        List<AppointmentResponseDto> response = appointmentService.getPastAppointmentsByClientId(clientId);
        return ResponseEntity.ok(response);
    }
}
