package com.psikolojikdanismanlik.randevusistemi.service;

import com.psikolojikdanismanlik.randevusistemi.dto.request.ClientUpdateRequest;
import com.psikolojikdanismanlik.randevusistemi.dto.request.TherapistRequest;
import com.psikolojikdanismanlik.randevusistemi.dto.request.TherapistUpdateRequest;
import com.psikolojikdanismanlik.randevusistemi.dto.response.TherapistResponseDto;
import com.psikolojikdanismanlik.randevusistemi.entity.Client;
import com.psikolojikdanismanlik.randevusistemi.entity.Therapist;
import com.psikolojikdanismanlik.randevusistemi.entity.User;
import com.psikolojikdanismanlik.randevusistemi.repository.AppointmentRepository;
import com.psikolojikdanismanlik.randevusistemi.repository.ClientRepository;
import com.psikolojikdanismanlik.randevusistemi.repository.TherapistRepository;
import com.psikolojikdanismanlik.randevusistemi.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.nio.file.AccessDeniedException;
import java.util.List;

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

    public Therapist createTherapist(TherapistRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        Therapist therapist = new Therapist();
        therapist.setUser(user);
        therapist.setSpecialization(request.getSpecialization());
        therapist.setExperience(request.getExperience());
        therapist.setAbout(request.getAbout());

        return therapistRepository.save(therapist);
    }

    public Therapist updateTherapist(Long therapistId, TherapistUpdateRequest request, String currentEmail) throws AccessDeniedException {
        Therapist therapist = therapistRepository.findById(therapistId)
                .orElseThrow(() -> new RuntimeException("Terapist bulunamadı"));

        User user = therapist.getUser();

        if (!user.getEmail().equals(currentEmail) && !isAdmin(currentEmail)) {
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
    }


    private boolean isAdmin(String email) {
        return userRepository.findByEmail(email)
                .map(user -> user.getRole().name().equals("ADMIN"))
                .orElse(false);
    }

    public void deleteTherapist(Long therapistId, String currentEmail) throws AccessDeniedException {
        Therapist therapist = therapistRepository.findById(therapistId)
                .orElseThrow(() -> new RuntimeException("Terapist bulunamadı"));

        User user = therapist.getUser();

        if (!user.getEmail().equals(currentEmail) && !isAdmin(currentEmail)) {
            throw new AccessDeniedException("Bu işlemi yapmaya yetkiniz yok.");
        }
        appointmentRepository.deleteAllByTherapistId(therapistId);

        therapistRepository.delete(therapist);
        userRepository.delete(user);
    }

    public List<TherapistResponseDto> getAllTherapists() {
        List<Therapist> therapists = therapistRepository.findAll();
        return therapists.stream()
                .map(therapist -> modelMapper.map(therapist, TherapistResponseDto.class))
                .toList();
    }



}
