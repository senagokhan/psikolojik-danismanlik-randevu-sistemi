package com.psikolojikdanismanlik.randevusistemi.controller;

import com.psikolojikdanismanlik.randevusistemi.dto.request.AppointmentRequest;
import com.psikolojikdanismanlik.randevusistemi.dto.request.AppointmentStatusUpdateRequest;
import com.psikolojikdanismanlik.randevusistemi.dto.request.RescheduleRequestDto;
import com.psikolojikdanismanlik.randevusistemi.dto.response.AppointmentResponseDto;
import com.psikolojikdanismanlik.randevusistemi.service.AppointmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<AppointmentResponseDto> createAppointment(@RequestBody AppointmentRequest request) {
        AppointmentResponseDto appointment = appointmentService.createAppointment(request);
        return new ResponseEntity<>(appointment, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponseDto> updateStatus(
            @PathVariable Long id,
            @RequestBody AppointmentStatusUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws AccessDeniedException {
        String therapistEmail = userDetails.getUsername();

        AppointmentResponseDto response = appointmentService.updateStatusAsTherapist(id, request.getStatus(), therapistEmail);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancel-request")
    public ResponseEntity<AppointmentResponseDto> requestCancelByClient(
            @PathVariable Long id,
            @RequestBody AppointmentStatusUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws AccessDeniedException {
        String email = userDetails.getUsername();
        AppointmentResponseDto response = appointmentService.requestCancelByClient(id, request.getStatus(), email);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/reschedule")
    public ResponseEntity<AppointmentResponseDto> requestReschedule(
            @PathVariable Long id,
            @RequestBody RescheduleRequestDto request,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws AccessDeniedException {
        String email = userDetails.getUsername();
        AppointmentResponseDto response = appointmentService.requestRescheduleByClient(id, request, email);
        return ResponseEntity.ok(response);
    }

}
