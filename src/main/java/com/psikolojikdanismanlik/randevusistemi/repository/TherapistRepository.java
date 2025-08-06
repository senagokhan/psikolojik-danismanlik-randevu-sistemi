package com.psikolojikdanismanlik.randevusistemi.repository;

import com.psikolojikdanismanlik.randevusistemi.entity.Therapist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import com.psikolojikdanismanlik.randevusistemi.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TherapistRepository extends JpaRepository<Therapist, Long> {
    Optional<Therapist> findByUserEmail(String email);
    Page<Therapist> findAll(Pageable pageable);
    Optional<Therapist> findByUser(User user);
    @Query("SELECT t FROM Therapist t WHERE LOWER(t.user.fullName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Therapist> searchByFullName(@Param("name") String name);
}
