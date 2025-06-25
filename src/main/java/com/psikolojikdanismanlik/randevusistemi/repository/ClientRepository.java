package com.psikolojikdanismanlik.randevusistemi.repository;

import com.psikolojikdanismanlik.randevusistemi.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByUserId(Long userId);
}
