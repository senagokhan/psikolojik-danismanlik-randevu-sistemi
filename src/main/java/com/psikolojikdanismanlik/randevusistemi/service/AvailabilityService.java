package com.psikolojikdanismanlik.randevusistemi.service;

import com.psikolojikdanismanlik.randevusistemi.dto.request.AvailabilityRequest;
import com.psikolojikdanismanlik.randevusistemi.dto.response.AvailabilityResponseDto;
import com.psikolojikdanismanlik.randevusistemi.entity.Availability;
import com.psikolojikdanismanlik.randevusistemi.entity.Therapist;
import com.psikolojikdanismanlik.randevusistemi.entity.User;
import com.psikolojikdanismanlik.randevusistemi.enums.Role;
import com.psikolojikdanismanlik.randevusistemi.repository.AvailabilityRepository;
import com.psikolojikdanismanlik.randevusistemi.repository.TherapistRepository;
import com.psikolojikdanismanlik.randevusistemi.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;

@Service
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final TherapistRepository therapistRepository;
    private final UserRepository userRepository;


    public AvailabilityService(AvailabilityRepository availabilityRepository, TherapistRepository therapistRepository, UserRepository userRepository) {
        this.availabilityRepository = availabilityRepository;
        this.therapistRepository = therapistRepository;
        this.userRepository = userRepository;
    }

    public Availability addAvailability(Long therapistId, AvailabilityRequest request, String requesterEmail) throws AccessDeniedException {
        User user = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));

        if (user.getRole() != Role.ADMIN && user.getRole() != Role.THERAPIST) {
            throw new AccessDeniedException("Sadece terapist ya da admin müsaitlik ekleyebilir.");
        }

        Therapist therapist = therapistRepository.findById(therapistId)
                .orElseThrow(() -> new RuntimeException("Terapist bulunamadı."));

        Availability availability = new Availability();
        availability.setTherapist(therapist);
        availability.setStartTime(request.getStartTime());
        availability.setEndTime(request.getEndTime());

        return availabilityRepository.save(availability);
    }

    public void deleteAvailability(Long therapistId, Long availabilityId, String therapistEmail) throws AccessDeniedException {
        Therapist therapist = therapistRepository.findById(therapistId)
                .orElseThrow(() -> new RuntimeException("Terapist bulunamadı"));

        String emailFromDb = therapist.getUser().getEmail();

        if (!emailFromDb.equals(therapistEmail)) {
            throw new AccessDeniedException("Bu müsaitliği silmeye yetkiniz yok.");
        }

        Availability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new RuntimeException("Müsaitlik bulunamadı"));

        if (!availability.getTherapist().getId().equals(therapistId)) {
            throw new AccessDeniedException("Bu müsaitliği silmeye yetkiniz yok.");
        }

        availabilityRepository.delete(availability);
    }

    public AvailabilityResponseDto updateAvailability(Long therapistId, Long availabilityId, AvailabilityRequest request) throws AccessDeniedException {
        Therapist therapist = therapistRepository.findById(therapistId)
                .orElseThrow(() -> new RuntimeException("Terapist bulunamadı"));

        Availability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new RuntimeException("Müsaitlik bulunamadı"));

        if (!availability.getTherapist().getId().equals(therapist.getId())) {
            throw new AccessDeniedException("Bu terapiste ait olmayan bir müsaitlik güncellenemez.");
        }

        availability.setStartTime(request.getStartTime());
        availability.setEndTime(request.getEndTime());

        Availability updated = availabilityRepository.save(availability);

        AvailabilityResponseDto response = new AvailabilityResponseDto();
        response.setId(updated.getId());
        response.setStartTime(updated.getStartTime());
        response.setEndTime(updated.getEndTime());
        response.setTherapistId(therapist.getId());

        return response;
    }

    public boolean isTherapistAvailableOn(Long therapistId, LocalDateTime desiredTime) {
        return availabilityRepository.existsByTherapistIdAndStartTime(therapistId, desiredTime);
    }

}
