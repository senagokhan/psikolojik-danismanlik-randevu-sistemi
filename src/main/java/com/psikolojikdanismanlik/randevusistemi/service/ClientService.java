package com.psikolojikdanismanlik.randevusistemi.service;

import com.psikolojikdanismanlik.randevusistemi.dto.request.ClientRequest;
import com.psikolojikdanismanlik.randevusistemi.dto.request.ClientUpdateRequest;
import com.psikolojikdanismanlik.randevusistemi.dto.response.ClientResponseDto;
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

    public ClientResponseDto createClient(ClientRequest request, String currentEmail) throws AccessDeniedException {
        try {
            User user = userRepository.findById(request.getUserId()).orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

            boolean isOwner = user.getEmail().equals(currentEmail);
            boolean isAdmin = isAdmin(currentEmail);

            if (!isOwner && !isAdmin) {
                throw new AccessDeniedException("Bu işlemi yapmaya yetkiniz yok.");
            }

            Client client = new Client();
            client.setUser(user);
            Client saved = clientRepository.save(client);

            ClientResponseDto dto = new ClientResponseDto();
            dto.setId(saved.getId());
            dto.setFullName(user.getFullName());
            dto.setEmail(user.getEmail());
            dto.setPhoneNumber(user.getPhoneNumber());

            return dto;
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Danışan oluşturulurken hata oluştu: " + e.getMessage());
        }
    }

    public void deleteClientById(Long clientId, String currentEmail) throws AccessDeniedException {
        try {
            Client client = clientRepository.findById(clientId)
                    .orElseThrow(() -> new RuntimeException("Client bulunamadı"));

            User clientUser = client.getUser();

            boolean isOwner = clientUser.getEmail().equals(currentEmail);
            boolean isAdmin = isAdmin(currentEmail);

            if (!isOwner && !isAdmin) {
                throw new AccessDeniedException("Bu işlemi yapmaya yetkiniz yok.");
            }

            userRepository.delete(clientUser);
            clientRepository.delete(client);

        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Danışan silinirken bir hata oluştu: " + e.getMessage());
        }
    }

    private boolean isAdmin(String email) {
        return userRepository.findByEmail(email)
                .map(user -> user.getRole().name().equals("ADMIN"))
                .orElse(false);
    }

    public ClientResponseDto updateClient(Long clientId, ClientUpdateRequest request, String currentEmail) throws AccessDeniedException {
        try {
            Client client = clientRepository.findById(clientId).orElseThrow(() -> new RuntimeException("Client bulunamadı."));

            User user = client.getUser();
            boolean isOwner = user.getEmail().equals(currentEmail);
            boolean isAdmin = isAdmin(currentEmail);

            if (!isOwner && !isAdmin) {
                throw new AccessDeniedException("Bu işlemi yapmaya yetkiniz yok.");
            }
            user.setFullName(request.getFullName());
            user.setPhoneNumber(request.getPhoneNumber());
            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
            }
            userRepository.save(user);
            ClientResponseDto dto = new ClientResponseDto();
            dto.setId(client.getId());
            dto.setFullName(user.getFullName());
            dto.setEmail(user.getEmail());
            dto.setPhoneNumber(user.getPhoneNumber());
            return dto;

        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Danışan güncellenirken hata oluştu: " + e.getMessage());
        }
    }
}
