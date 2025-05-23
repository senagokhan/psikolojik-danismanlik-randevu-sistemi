package com.psikolojikdanismanlik.randevusistemi.service;

import com.psikolojikdanismanlik.randevusistemi.dto.request.FeedbackRequestDto;
import com.psikolojikdanismanlik.randevusistemi.dto.response.FeedbackResponseDto;
import com.psikolojikdanismanlik.randevusistemi.entity.Appointment;
import com.psikolojikdanismanlik.randevusistemi.entity.Feedback;
import com.psikolojikdanismanlik.randevusistemi.enums.Status;
import com.psikolojikdanismanlik.randevusistemi.repository.AppointmentRepository;
import com.psikolojikdanismanlik.randevusistemi.repository.FeedbackRepository;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;

@Service
public class FeedbackService {

    private final AppointmentRepository appointmentRepository;

    public FeedbackService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public FeedbackResponseDto addFeedback(Long appointmentId, FeedbackRequestDto request, String clientEmail) throws AccessDeniedException {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Randevu bulunamadı"));

        String email = appointment.getClient().getUser().getEmail();
        if (!email.equals(clientEmail)) {
            throw new AccessDeniedException("Bu randevuya yorum yapamazsınız.");
        }

        if (appointment.getStatus() != Status.COMPLETED) {
            throw new RuntimeException("Sadece tamamlanmış randevulara yorum yapılabilir.");
        }

        if (appointment.getFeedback() != null) {
            throw new RuntimeException("Bu randevu için zaten yorum yapılmış.");
        }

        Feedback feedback = new Feedback();
        feedback.setComment(request.getComment());
        feedback.setRating((int) request.getRating());
        feedback.setCreatedAt(LocalDateTime.now());
        feedback.setClient(appointment.getClient());

        appointment.setFeedback(feedback);
        appointmentRepository.save(appointment);

        FeedbackResponseDto response = new FeedbackResponseDto();
        response.setId(feedback.getId());
        response.setComment(feedback.getComment());
        response.setRating(feedback.getRating());
        response.setCreatedAt(feedback.getCreatedAt());

        return response;
    }

}
