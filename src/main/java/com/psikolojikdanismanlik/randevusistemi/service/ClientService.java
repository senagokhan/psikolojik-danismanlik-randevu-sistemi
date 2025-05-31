package com.psikolojikdanismanlik.randevusistemi.service;

import com.psikolojikdanismanlik.randevusistemi.dto.request.ClientRequest;
import com.psikolojikdanismanlik.randevusistemi.dto.request.ClientUpdateRequest;
import com.psikolojikdanismanlik.randevusistemi.entity.Client;
import com.psikolojikdanismanlik.randevusistemi.entity.User;
import com.psikolojikdanismanlik.randevusistemi.repository.ClientRepository;
import com.psikolojikdanismanlik.randevusistemi.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.nio.file.AccessDeniedException;


@Service
public class ClientService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    public ClientService(UserRepository userRepository, ClientRepository clientRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
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

    public Client updateClient(Long clientId, ClientUpdateRequest request, String currentEmail) throws AccessDeniedException {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client bulunamadı."));

        User user = client.getUser();

        if (!user.getEmail().equals(currentEmail) && !isAdmin(currentEmail)) {
            throw new AccessDeniedException("Bu işlemi yapmaya yetkiniz yok.");
        }

        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        userRepository.save(user);
        return client;
    }
}
