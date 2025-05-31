package com.psikolojikdanismanlik.randevusistemi.controller;

import com.psikolojikdanismanlik.randevusistemi.dto.request.ClientRequest;
import com.psikolojikdanismanlik.randevusistemi.dto.response.AppointmentResponseDto;
import com.psikolojikdanismanlik.randevusistemi.entity.Client;
import com.psikolojikdanismanlik.randevusistemi.service.AppointmentService;
import com.psikolojikdanismanlik.randevusistemi.service.ClientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {
    private final ClientService clientService;
    private final AppointmentService appointmentService;
    public ClientController(ClientService clientService, AppointmentService appointmentService) {
        this.clientService = clientService;
        this.appointmentService = appointmentService;
    }
    @PostMapping
    public ResponseEntity<Client> createClient(@RequestBody ClientRequest request) {
        Client client = clientService.createClient(request);
        return new ResponseEntity<>(client, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/appointments")
    public ResponseEntity<List<AppointmentResponseDto>> getAppointmentsByClientId(@PathVariable Long id) {
        List<AppointmentResponseDto> appointments = appointmentService.getAppointmentsByClientId(id);
        return ResponseEntity.ok(appointments);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws AccessDeniedException {
        clientService.deleteClientById(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

}
