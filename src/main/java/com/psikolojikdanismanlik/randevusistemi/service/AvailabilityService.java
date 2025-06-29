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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        try {
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

        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new RuntimeException("Müsaitlik eklenirken hata oluştu: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Beklenmeyen bir hata oluştu: " + e.getMessage());
        }
    }


    public void deleteAvailability(Long therapistId, Long availabilityId, String therapistEmail) throws AccessDeniedException {
        try {
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

        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new RuntimeException("Müsaitlik silinirken hata oluştu: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Beklenmeyen bir hata oluştu: " + e.getMessage());
        }
    }

    public boolean isTherapistAvailableOn(Long therapistId, LocalDateTime desiredTime) {
        try {
            return availabilityRepository.existsByTherapistIdAndStartTime(therapistId, desiredTime);
        } catch (Exception e) {
            System.err.println("Terapist uygunluğu kontrol edilirken hata oluştu: " + e.getMessage());
            return false;
        }
    }

    public Page<AvailabilityResponseDto> getAvailabilitiesForTherapist(Long therapistId, Pageable pageable) {
        try {
            Page<Availability> availabilities = availabilityRepository
                    .findByTherapistIdOrderByStartTimeAsc(therapistId, pageable);

            return availabilities.map(av -> {
                AvailabilityResponseDto dto = new AvailabilityResponseDto();
                dto.setId(av.getId());
                dto.setStartTime(av.getStartTime());
                dto.setEndTime(av.getEndTime());
                dto.setTherapistId(av.getTherapist().getId());
                dto.setBooked(av.isBooked());
                return dto;
            });
        } catch (Exception e) {
            throw new RuntimeException("Terapistin müsaitlikleri alınırken hata oluştu: " + e.getMessage());
        }
    }
}
