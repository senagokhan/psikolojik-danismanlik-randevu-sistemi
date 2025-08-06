package com.psikolojikdanismanlik.randevusistemi.service;

import com.psikolojikdanismanlik.randevusistemi.dto.request.TherapistRequest;
import com.psikolojikdanismanlik.randevusistemi.dto.request.TherapistUpdateRequest;
import com.psikolojikdanismanlik.randevusistemi.dto.response.AppointmentResponseDto;
import com.psikolojikdanismanlik.randevusistemi.dto.response.ClientResponseDto;
import com.psikolojikdanismanlik.randevusistemi.dto.response.TherapistResponseDto;
import com.psikolojikdanismanlik.randevusistemi.entity.Appointment;
import com.psikolojikdanismanlik.randevusistemi.entity.Client;
import com.psikolojikdanismanlik.randevusistemi.entity.Therapist;
import com.psikolojikdanismanlik.randevusistemi.entity.User;
import com.psikolojikdanismanlik.randevusistemi.enums.Role;
import com.psikolojikdanismanlik.randevusistemi.repository.AppointmentRepository;
import com.psikolojikdanismanlik.randevusistemi.repository.TherapistRepository;
import com.psikolojikdanismanlik.randevusistemi.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.nio.file.AccessDeniedException;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TherapistService {

    private final TherapistRepository therapistRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppointmentRepository appointmentRepository;
    private final ModelMapper modelMapper;


    public TherapistService(TherapistRepository therapistRepository, UserRepository userRepository, PasswordEncoder passwordEncoder, AppointmentRepository appointmentRepository, ModelMapper modelMapper) {
        this.therapistRepository = therapistRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.appointmentRepository = appointmentRepository;
        this.modelMapper = modelMapper;
    }

    public Therapist createTherapist(TherapistRequest request, String requesterEmail) {
        try {
            User requester = userRepository.findByEmail(requesterEmail)
                    .orElseThrow(() -> new RuntimeException("Giriş yapan kullanıcı bulunamadı"));

            boolean isAdmin = requester.getRole() == Role.ADMIN;

            if (!isAdmin && !Objects.equals(request.getUserId(), requester.getId())) {
                throw new AccessDeniedException("Bu işlemi yapmaya yetkiniz yok.");
            }
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("İlgili kullanıcı bulunamadı"));

            Therapist therapist = new Therapist();
            therapist.setUser(user);
            therapist.setSpecialization(request.getSpecialization());
            therapist.setExperience(request.getExperience());
            therapist.setAbout(request.getAbout());

            return therapistRepository.save(therapist);

        } catch (AccessDeniedException e) {
            throw new RuntimeException("Yetkisiz işlem: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Terapist oluşturulurken bir hata oluştu: " + e.getMessage());
        }
    }

    public Therapist updateTherapist(Long therapistId, TherapistUpdateRequest request, String currentEmail) {
        try {
            Therapist therapist = therapistRepository.findById(therapistId).orElseThrow(() -> new RuntimeException("Terapist bulunamadı"));
            User user = therapist.getUser();
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
            therapist.setSpecialization(request.getSpecialization());
            therapist.setExperience(request.getExperience());
            therapist.setAbout(request.getAbout());

            return therapistRepository.save(therapist);

        } catch (AccessDeniedException e) {
            throw new RuntimeException("Yetkisiz erişim: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Terapist güncellenirken hata oluştu: " + e.getMessage());
        }
    }

    private boolean isAdmin(String email) {
        return userRepository.findByEmail(email)
                .map(user -> user.getRole().name().equals("ADMIN"))
                .orElse(false);
    }

    public Page<AppointmentResponseDto> getAppointmentsByTherapistEmail(String email, Pageable pageable) {
        try {
            Therapist therapist = therapistRepository.findByUserEmail(email)
                    .orElseThrow(() -> new RuntimeException("Terapist bulunamadı"));

            Page<Appointment> page = appointmentRepository.findByTherapistId(therapist.getId(), pageable);

            return page.map(this::mapToDto);

        } catch (Exception e) {
            throw new RuntimeException("Terapistin randevuları getirilirken hata oluştu: " + e.getMessage());
        }
    }

    private AppointmentResponseDto mapToDto(Appointment appointment) {
        AppointmentResponseDto dto = new AppointmentResponseDto();
        dto.setId(appointment.getId());
        dto.setTherapistId(appointment.getTherapist().getId());
        dto.setClientId(appointment.getClient().getId());
        dto.setStartTime(appointment.getStartTime());
        dto.setCreatedDate(appointment.getCreatedDate());
        dto.setStatus(appointment.getStatus().toString());

        if (appointment.getClient() != null && appointment.getClient().getUser() != null) {
            dto.setClientName(appointment.getClient().getUser().getFullName());
        } else {
            dto.setClientName("Bilinmiyor");
        }

        if (appointment.getStartTime() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            dto.setFormattedStartTime(appointment.getStartTime().format(formatter));
        } else {
            dto.setFormattedStartTime("Bilinmiyor");
        }

        return dto;
    }

    public Page<TherapistResponseDto> getAllTherapists(Pageable pageable) {
        Page<Therapist> therapists = therapistRepository.findAll(pageable);

        return therapists.map(t -> {
            TherapistResponseDto dto = new TherapistResponseDto();
            dto.setId(t.getId());
            dto.setFullName(t.getUser().getFullName());
            dto.setSpecialization(t.getSpecialization());
            dto.setExperience(t.getExperience());
            return dto;
        });
    }

    public List<ClientResponseDto> getClientsOfTherapist(String currentEmail) throws AccessDeniedException {
        try {
            Therapist therapist = therapistRepository.findByUserEmail(currentEmail)
                    .orElseThrow(() -> new RuntimeException("Terapist bulunamadı."));

            boolean isOwner = therapist.getUser().getEmail().equals(currentEmail);
            boolean isAdmin = isAdmin(currentEmail);
            if (!isOwner && !isAdmin) {
                throw new AccessDeniedException("Bu işlemi yapmaya yetkiniz yok.");
            }
            List<Appointment> appointments = appointmentRepository.findByTherapistId(therapist.getId());
            Set<Client> uniqueClients = appointments.stream().map(Appointment::getClient).collect(Collectors.toSet());

            return uniqueClients.stream()
                    .map(client -> {
                        ClientResponseDto dto = new ClientResponseDto();
                        dto.setId(client.getId());
                        dto.setFullName(client.getUser().getFullName());
                        dto.setEmail(client.getUser().getEmail());
                        dto.setPhoneNumber(client.getUser().getPhoneNumber());
                        return dto;
                    })
                    .sorted(Comparator.comparing(ClientResponseDto::getFullName))
                    .collect(Collectors.toList());

        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Danışan listesi getirilirken hata oluştu: " + e.getMessage());
        }
    }

    public TherapistResponseDto getTherapistById(Long id) {
        try {
            Therapist therapist = therapistRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Terapist bulunamadı"));

            TherapistResponseDto dto = modelMapper.map(therapist, TherapistResponseDto.class);
            dto.setFullName(therapist.getUser().getFullName());

            return dto;
        } catch (Exception e) {
            throw new RuntimeException("Terapist getirilirken bir hata oluştu: " + e.getMessage());
        }
    }

    public TherapistResponseDto getTherapistByEmail(String email) {
        try {
            Therapist therapist = therapistRepository.findByUserEmail(email)
                    .orElseThrow(() -> new RuntimeException("Bu e-posta ile kayıtlı terapist bulunamadı: " + email));

            return modelMapper.map(therapist, TherapistResponseDto.class);
        } catch (Exception e) {
            System.err.println("Terapist bilgisi alınamadı: " + e.getMessage());
            throw new RuntimeException("Terapist bilgisi alınırken bir hata oluştu.");
        }
    }

    public List<TherapistResponseDto> searchTherapistsByName(String name) {
        List<Therapist> therapists = therapistRepository.searchByFullName(name);

        return therapists.stream().map(therapist -> {
            TherapistResponseDto dto = new TherapistResponseDto();
            dto.setId(therapist.getId());
            dto.setFullName(therapist.getUser().getFullName());
            dto.setSpecialization(therapist.getSpecialization());
            dto.setExperience(therapist.getExperience());
            return dto;
        }).collect(Collectors.toList());
    }

}
