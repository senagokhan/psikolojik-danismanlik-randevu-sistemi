package com.psikolojikdanismanlik.randevusistemi.service;

import com.psikolojikdanismanlik.randevusistemi.dto.request.*;
import com.psikolojikdanismanlik.randevusistemi.dto.response.LoginResponseDto;
import com.psikolojikdanismanlik.randevusistemi.dto.response.UserProfileResponseDto;
import com.psikolojikdanismanlik.randevusistemi.dto.response.UserResponseDto;
import com.psikolojikdanismanlik.randevusistemi.entity.Therapist;
import com.psikolojikdanismanlik.randevusistemi.entity.User;
import com.psikolojikdanismanlik.randevusistemi.enums.Role;
import com.psikolojikdanismanlik.randevusistemi.repository.ClientRepository;
import com.psikolojikdanismanlik.randevusistemi.repository.TherapistRepository;
import com.psikolojikdanismanlik.randevusistemi.repository.UserRepository;
import com.psikolojikdanismanlik.randevusistemi.util.JwtUtil;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final TherapistRepository therapistRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final TherapistService therapistService;
    private final ClientService clientService;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, ClientRepository clientRepository, TherapistRepository therapistRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder, TherapistService therapistService, ClientService clientService, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.therapistRepository = therapistRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.therapistService = therapistService;
        this.clientService = clientService;
        this.jwtUtil = jwtUtil;
    }

    public UserResponseDto register(UserRegisterRequest request) {
        try {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Bu e-posta adresi zaten kayıtlı.");
            }
            User user = modelMapper.map(request, User.class);
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            userRepository.save(user);

            if (user.getRole() == Role.THERAPIST) {
                TherapistRequest therapistRequest = new TherapistRequest();
                therapistRequest.setUserId(user.getId());
                therapistRequest.setSpecialization("Belirtilmedi");
                therapistRequest.setExperience("Belirtilmedi");
                therapistRequest.setAbout("Profil henüz oluşturulmadı.");
                therapistService.createTherapist(therapistRequest, user.getEmail());
            } else if (user.getRole() == Role.CLIENT) {
                ClientRequest clientRequest = new ClientRequest();
                clientRequest.setUserId(user.getId());

                clientService.createClient(clientRequest, user.getEmail());
            }
            return modelMapper.map(user, UserResponseDto.class);

        } catch (Exception e) {
            throw new RuntimeException("Kayıt işlemi sırasında bir hata oluştu: " + e.getMessage());
        }
    }


    public LoginResponseDto login(UserLoginRequest request) {
        try {
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("E-posta veya şifre hatalı."));

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new RuntimeException("E-posta veya şifre hatalı.");
            }
            String token = jwtUtil.generateToken(user);
            LoginResponseDto response = new LoginResponseDto();
            response.setToken(token);
            response.setUserId(user.getId());
            response.setRole(Role.valueOf(user.getRole().name()));

            if (user.getRole() == Role.CLIENT) {
                clientRepository.findByUserId(user.getId())
                        .ifPresent(client -> response.setClientId(client.getId()));
            }
            if (user.getRole() == Role.THERAPIST) {
                therapistRepository.findByUser(user)
                        .ifPresent(therapist -> response.setTherapistId(therapist.getId()));
            }
            return response;

        } catch (Exception e) {
            throw new RuntimeException("Giriş işlemi sırasında bir hata oluştu: " + e.getMessage());
        }
    }

    public UserProfileResponseDto getCurrentUser(String email) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

            UserProfileResponseDto dto = modelMapper.map(user, UserProfileResponseDto.class);
            dto.setUserId(user.getId());
            dto.setRole(user.getRole());

            if (user.getRole() == Role.THERAPIST) {
                Therapist therapist = therapistRepository.findByUser(user).orElse(null);
                if (therapist != null) {
                    dto.setTherapistId(therapist.getId());
                    dto.setSpecialization(therapist.getSpecialization());
                    dto.setExperience(therapist.getExperience());
                    dto.setAbout(therapist.getAbout());
                }
            }
            return dto;
        } catch (Exception e) {
            throw new RuntimeException("Kullanıcı bilgileri alınırken bir hata oluştu: " + e.getMessage());
        }
    }

    public UserProfileResponseDto updateCurrentUser(UserProfileUpdateRequestDto request, String email) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));

            if (request.getFullName() != null) user.setFullName(request.getFullName());
            if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
            }
            userRepository.save(user);
            if (user.getRole() == Role.THERAPIST) {
                Therapist therapist = therapistRepository.findByUser(user)
                        .orElseThrow(() -> new RuntimeException("Terapist bilgisi bulunamadı"));

                if (request.getSpecialization() != null)
                    therapist.setSpecialization(request.getSpecialization());
                if (request.getExperience() != null)
                    therapist.setExperience(request.getExperience());
                if (request.getAbout() != null)
                    therapist.setAbout(request.getAbout());

                therapistRepository.save(therapist);
            }
            return getCurrentUser(email);
        } catch (Exception e) {
            throw new RuntimeException("Profil güncellenirken hata oluştu: " + e.getMessage());
        }
    }

    public void deleteUser(Long userId, String currentEmail) {
        try {
            User userToDelete = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));

            User currentUser = userRepository.findByEmail(currentEmail)
                    .orElseThrow(() -> new RuntimeException("Geçerli kullanıcı bulunamadı."));

            boolean isOwner = userToDelete.getEmail().equals(currentEmail);
            boolean isAdmin = currentUser.getRole().name().equals("ADMIN");

            if (!isOwner && !isAdmin) {
                throw new AccessDeniedException("Bu işlemi yapmaya yetkiniz yok.");
            }

            if (userToDelete.getClient() != null) {
                clientRepository.deleteById(userToDelete.getClient().getId());
            }

            if (userToDelete.getTherapist() != null) {
                therapistRepository.deleteById(userToDelete.getTherapist().getId());
            }

            userRepository.deleteById(userId);
        } catch (Exception e) {
            throw new RuntimeException("Kullanıcı silinirken bir hata oluştu: " + e.getMessage());
        }
    }

    public UserResponseDto getUserById(Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));
            return modelMapper.map(user, UserResponseDto.class);
        } catch (Exception e) {
            throw new RuntimeException("Kullanıcı getirilirken hata oluştu: " + e.getMessage());
        }
    }

    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        try {
            return userRepository.findAll(pageable)
                    .map(user -> modelMapper.map(user, UserResponseDto.class));
        } catch (Exception e) {
            throw new RuntimeException("Kullanıcılar getirilirken bir hata oluştu: " + e.getMessage());
        }
    }

    public boolean isAdmin(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        return user.getRole() == Role.ADMIN;
    }

    public List<UserResponseDto> getUsersByRole(Role role, String requesterEmail) throws AccessDeniedException {
        try {
            User requester = userRepository.findByEmail(requesterEmail)
                    .orElseThrow(() -> new RuntimeException("İstek yapan kullanıcı bulunamadı."));

            if (requester.getRole() != Role.ADMIN) {
                throw new AccessDeniedException("Sadece admin kullanıcılar bu işlemi yapabilir.");
            }

            List<User> users = userRepository.findByRole(role);
            return users.stream()
                    .map(user -> modelMapper.map(user, UserResponseDto.class))
                    .collect(Collectors.toList());
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Kullanıcılar role göre getirilirken bir hata oluştu: " + e.getMessage());
        }
    }

    public UserResponseDto updateUserRole(Long userId, UserRoleUpdateRequest request, String requesterEmail) {
        try {
            if (!isAdmin(requesterEmail)) {
                throw new AccessDeniedException("Sadece admin rol güncellemesi yapabilir.");
            }
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

            user.setRole(request.getNewRole());
            userRepository.save(user);
            return modelMapper.map(user, UserResponseDto.class);

        } catch (AccessDeniedException e) {
            throw new RuntimeException("Yetkisiz erişim: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Rol güncellenemedi: " + e.getMessage());
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + email));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}

