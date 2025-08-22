package com.psikolojikdanismanlik.randevusistemi.service;

import com.psikolojikdanismanlik.randevusistemi.dto.request.ClientRequest;
import com.psikolojikdanismanlik.randevusistemi.dto.request.ClientUpdateRequest;
import com.psikolojikdanismanlik.randevusistemi.dto.response.ClientResponseDto;
import com.psikolojikdanismanlik.randevusistemi.entity.Client;
import com.psikolojikdanismanlik.randevusistemi.entity.User;
import com.psikolojikdanismanlik.randevusistemi.repository.ClientRepository;
import com.psikolojikdanismanlik.randevusistemi.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.nio.file.AccessDeniedException;


@Service
public class ClientService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final ModelMapper modelMapper;

    public ClientService(UserRepository userRepository, ClientRepository clientRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.modelMapper = modelMapper;
    }

    public ClientResponseDto createClient(ClientRequest request, String currentEmail) throws AccessDeniedException {
        try {
            User user = userRepository.findById(request.getUserId()).orElseThrow(() -> new RuntimeException("User not found."));

            boolean isOwner = user.getEmail().equals(currentEmail);
            boolean isAdmin = isAdmin(currentEmail);

            if (!isOwner && !isAdmin) {
                throw new AccessDeniedException("You are not authorized to perform this operation.");
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
            throw new RuntimeException("An error occurred while creating the client:" + e.getMessage());
        }
    }

    public void deleteClientById(Long clientId, String currentEmail) throws AccessDeniedException {
        try {
            Client client = clientRepository.findById(clientId)
                    .orElseThrow(() -> new RuntimeException("Client not found."));

            User clientUser = client.getUser();

            boolean isOwner = clientUser.getEmail().equals(currentEmail);
            boolean isAdmin = isAdmin(currentEmail);

            if (!isOwner && !isAdmin) {
                throw new AccessDeniedException("You are not authorized to perform this operation.");
            }

            userRepository.delete(clientUser);
            clientRepository.delete(client);

        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while deleting the client: " + e.getMessage());
        }
    }

    private boolean isAdmin(String email) {
        return userRepository.findByEmail(email)
                .map(user -> user.getRole().name().equals("ADMIN"))
                .orElse(false);
    }

    public ClientResponseDto getClientByUserId(Long userId) {
        try {
            Client client = clientRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Client not found."));
            return modelMapper.map(client, ClientResponseDto.class);
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while retrieving client information: " + e.getMessage());
        }
    }
}
