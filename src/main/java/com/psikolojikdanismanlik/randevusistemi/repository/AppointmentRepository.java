package com.psikolojikdanismanlik.randevusistemi.repository;

import com.psikolojikdanismanlik.randevusistemi.entity.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    Page<Appointment> findByClientId(Long clientId, Pageable pageable);
    List<Appointment> findByTherapistId(Long therapistId);
    boolean existsByClientIdAndStartTime(Long clientId, LocalDateTime startTime);
    void deleteAllByTherapistId(Long therapistId);
    Page<Appointment> findByTherapistId(Long therapistId, Pageable pageable);


}
