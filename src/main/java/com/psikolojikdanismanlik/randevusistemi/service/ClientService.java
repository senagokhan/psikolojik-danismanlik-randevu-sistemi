package com.psikolojikdanismanlik.randevusistemi.service;

import com.psikolojikdanismanlik.randevusistemi.dto.request.ClientRequest;
import com.psikolojikdanismanlik.randevusistemi.dto.response.AppointmentResponseDto;
import com.psikolojikdanismanlik.randevusistemi.entity.Client;
import com.psikolojikdanismanlik.randevusistemi.entity.User;
import com.psikolojikdanismanlik.randevusistemi.repository.AppointmentRepository;
import com.psikolojikdanismanlik.randevusistemi.repository.ClientRepository;
import com.psikolojikdanismanlik.randevusistemi.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
public class ClientService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    public ClientService(UserRepository userRepository, ClientRepository clientRepository) {
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
    }

    public Client createClient(ClientRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        Client client = new Client();
        client.setUser(user);

        return clientRepository.save(client);
    }

    public void deleteClientById(Long clientId, String currentEmail) throws AccessDeniedException {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client bulunamadı"));

        User clientUser = client.getUser();

        if (!clientUser.getEmail().equals(currentEmail) && !isAdmin(currentEmail)) {
            throw new AccessDeniedException("Bu işlemi yapmaya yetkiniz yok.");
        }
        userRepository.delete(clientUser);
        clientRepository.delete(client);
    }

    private boolean isAdmin(String email) {
        return userRepository.findByEmail(email)
                .map(user -> user.getRole().name().equals("ADMIN"))
                .orElse(false);
    }




}
