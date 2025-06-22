package com.psikolojikdanismanlik.randevusistemi.repository;

import com.psikolojikdanismanlik.randevusistemi.entity.Availability;
import com.psikolojikdanismanlik.randevusistemi.entity.Therapist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    boolean existsByTherapistAndStartTime(Therapist therapist, LocalDateTime startTime);
    boolean existsByTherapistIdAndStartTime(Long therapistId, LocalDateTime startTime);
    Page<Availability> findByTherapistIdOrderByStartTimeAsc(Long therapistId, Pageable pageable);

}
