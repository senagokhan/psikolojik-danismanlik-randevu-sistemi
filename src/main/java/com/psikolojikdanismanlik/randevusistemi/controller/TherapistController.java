package com.psikolojikdanismanlik.randevusistemi.controller;


import com.psikolojikdanismanlik.randevusistemi.dto.request.AvailabilityRequest;
import com.psikolojikdanismanlik.randevusistemi.dto.request.TherapistRequest;
import com.psikolojikdanismanlik.randevusistemi.entity.Availability;
import com.psikolojikdanismanlik.randevusistemi.entity.Therapist;
import com.psikolojikdanismanlik.randevusistemi.service.AvailabilityService;
import com.psikolojikdanismanlik.randevusistemi.service.TherapistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/therapists")
@RequiredArgsConstructor
public class TherapistController {

    private final AvailabilityService availabilityService;
    private final TherapistService therapistService;

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

}
