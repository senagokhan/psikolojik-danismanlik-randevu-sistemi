package com.psikolojikdanismanlik.randevusistemi.repository;

import com.psikolojikdanismanlik.randevusistemi.entity.Availability;
import com.psikolojikdanismanlik.randevusistemi.entity.Therapist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    boolean existsByTherapistAndStartTime(Therapist therapist, LocalDateTime startTime);
}
