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
    private final FeedbackRepository feedbackRepository;

    public FeedbackService(AppointmentRepository appointmentRepository, FeedbackRepository feedbackRepository) {
        this.appointmentRepository = appointmentRepository;
        this.feedbackRepository = feedbackRepository;
    }

    public FeedbackResponseDto addFeedback(Long appointmentId, FeedbackRequestDto request, String clientEmail) throws AccessDeniedException {
        Appointment appointment = getAuthorizedCompletedAppointment(appointmentId, clientEmail);

        if (appointment.getFeedback() != null) {
            throw new RuntimeException("Bu randevu için zaten yorum yapılmış.");
        }

        Feedback feedback = new Feedback();
        feedback.setComment(request.getComment());
        feedback.setRating((int) request.getRating());
        feedback.setCreatedAt(LocalDateTime.now());
        feedback.setClient(appointment.getClient());
        feedback.setAppointment(appointment);

        appointment.setFeedback(feedback);
        appointmentRepository.save(appointment);

        return mapToResponse(feedback);
    }

    public FeedbackResponseDto updateFeedback(Long appointmentId, FeedbackRequestDto request, String clientEmail) throws AccessDeniedException {
        Appointment appointment = getAuthorizedCompletedAppointment(appointmentId, clientEmail);

        Feedback feedback = appointment.getFeedback();
        if (feedback == null) {
            throw new RuntimeException("Bu randevuda henüz yorum yok, önce oluşturmalısınız.");
        }

        feedback.setComment(request.getComment());
        feedback.setRating((int) request.getRating());
        feedback.setCreatedAt(LocalDateTime.now());

        appointmentRepository.save(appointment);
        return mapToResponse(feedback);
    }

    public void deleteFeedback(Long appointmentId, String clientEmail) throws AccessDeniedException {
        Appointment appointment = getAuthorizedCompletedAppointment(appointmentId, clientEmail);

        if (appointment.getFeedback() == null) {
            throw new RuntimeException("Silinecek yorum bulunamadı.");
        }

        appointment.setFeedback(null);
        appointmentRepository.save(appointment);
    }

    private Appointment getAuthorizedCompletedAppointment(Long appointmentId, String clientEmail) throws AccessDeniedException {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Randevu bulunamadı"));

        String email = appointment.getClient().getUser().getEmail();
        if (!email.equals(clientEmail)) {
            throw new AccessDeniedException("Bu randevuya erişim yetkiniz yok.");
        }

        if (appointment.getStatus() != Status.COMPLETED) {
            throw new RuntimeException("Yalnızca tamamlanmış randevulara yorum yapılabilir.");
        }

        return appointment;
    }

    private FeedbackResponseDto mapToResponse(Feedback feedback) {
        FeedbackResponseDto response = new FeedbackResponseDto();
        response.setId(feedback.getId());
        response.setComment(feedback.getComment());
        response.setRating(feedback.getRating());
        response.setCreatedAt(feedback.getCreatedAt());
        return response;
    }

    public Double getAverageRatingForTherapist(Long therapistId) {
        Double avg = feedbackRepository.findAverageRatingByTherapistId(therapistId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;  // 1 ondalık yuvarlama
    }


}
