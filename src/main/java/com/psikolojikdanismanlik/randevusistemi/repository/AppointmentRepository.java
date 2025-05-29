package com.psikolojikdanismanlik.randevusistemi.repository;

import com.psikolojikdanismanlik.randevusistemi.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByClientId(Long clientId);
    List<Appointment> findByTherapistId(Long therapistId);
    boolean existsByClientIdAndStartTime(Long clientId, LocalDateTime startTime);
    void deleteAllByTherapistId(Long therapistId);


}
