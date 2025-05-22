package com.psikolojikdanismanlik.randevusistemi.service;

import com.psikolojikdanismanlik.randevusistemi.dto.request.AvailabilityRequest;
import com.psikolojikdanismanlik.randevusistemi.entity.Availability;
import com.psikolojikdanismanlik.randevusistemi.entity.Therapist;
import com.psikolojikdanismanlik.randevusistemi.repository.AvailabilityRepository;
import com.psikolojikdanismanlik.randevusistemi.repository.TherapistRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class AvailabilityService {

    public final AvailabilityRepository availabilityRepository;
    public final TherapistRepository therapistRepository;


    public AvailabilityService(AvailabilityRepository availabilityRepository, TherapistRepository therapistRepository, ModelMapper modelMapper) {
        this.availabilityRepository = availabilityRepository;
        this.therapistRepository = therapistRepository;
    }

    public Availability addAvailability(Long therapistId, AvailabilityRequest request) {
        Therapist therapist = therapistRepository.findById(therapistId)
                .orElseThrow(() -> new RuntimeException("Terapist bulunamadı"));

        Availability availability = new Availability();
        availability.setTherapist(therapist);
        availability.setStartTime(request.getStartTime());
        availability.setEndTime(request.getEndTime());

        return availabilityRepository.save(availability);
    }
}
