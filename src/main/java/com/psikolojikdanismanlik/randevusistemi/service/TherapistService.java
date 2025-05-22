package com.psikolojikdanismanlik.randevusistemi.service;

import com.psikolojikdanismanlik.randevusistemi.dto.request.TherapistRequest;
import com.psikolojikdanismanlik.randevusistemi.entity.Therapist;
import com.psikolojikdanismanlik.randevusistemi.entity.User;
import com.psikolojikdanismanlik.randevusistemi.repository.TherapistRepository;
import com.psikolojikdanismanlik.randevusistemi.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class TherapistService {

    private final TherapistRepository therapistRepository;
    private final UserRepository userRepository;

    public TherapistService(TherapistRepository therapistRepository, UserRepository userRepository) {
        this.therapistRepository = therapistRepository;
        this.userRepository = userRepository;
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


}
