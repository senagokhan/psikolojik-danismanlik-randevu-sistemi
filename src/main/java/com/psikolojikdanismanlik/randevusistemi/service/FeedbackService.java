package com.psikolojikdanismanlik.randevusistemi.service;

import com.psikolojikdanismanlik.randevusistemi.dto.request.FeedbackRequestDto;
import com.psikolojikdanismanlik.randevusistemi.dto.response.FeedbackResponseDto;
import com.psikolojikdanismanlik.randevusistemi.entity.Appointment;
import com.psikolojikdanismanlik.randevusistemi.entity.Feedback;
import com.psikolojikdanismanlik.randevusistemi.entity.User;
import com.psikolojikdanismanlik.randevusistemi.enums.Role;
import com.psikolojikdanismanlik.randevusistemi.enums.Status;
import com.psikolojikdanismanlik.randevusistemi.repository.AppointmentRepository;
import com.psikolojikdanismanlik.randevusistemi.repository.FeedbackRepository;
import com.psikolojikdanismanlik.randevusistemi.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;

@Service
public class FeedbackService {

    private final AppointmentRepository appointmentRepository;
    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

    public FeedbackService(AppointmentRepository appointmentRepository, FeedbackRepository feedbackRepository, UserRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
        this.feedbackRepository = feedbackRepository;
        this.userRepository = userRepository;
    }

    public FeedbackResponseDto addFeedback(Long appointmentId, FeedbackRequestDto request, String clientEmail) throws AccessDeniedException {
        try {
            Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow(() -> new RuntimeException("Randevu bulunamadı."));
            String appointmentClientEmail = appointment.getClient().getUser().getEmail();
            if (!appointmentClientEmail.equals(clientEmail)) {
                throw new AccessDeniedException("Bu randevuya yorum yapmaya yetkiniz yok.");
            }
            if (appointment.getStatus() != Status.COMPLETED) {
                throw new RuntimeException("Yorum yapmak için randevunun tamamlanmış olması gerekir.");
            }
            if (appointment.getFeedback() != null) {
                throw new RuntimeException("Bu randevu için zaten yorum yapılmış.");
            }
            Feedback feedback = new Feedback();
            feedback.setComment(request.getComment());
            feedback.setRating(request.getRating());
            feedback.setCreatedAt(LocalDateTime.now());
            feedback.setClient(appointment.getClient());
            feedback.setAppointment(appointment);
            appointment.setFeedback(feedback);
            appointmentRepository.save(appointment);
            return mapToResponse(feedback);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new RuntimeException("Yorum eklenirken hata oluştu: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Beklenmeyen bir hata oluştu: " + e.getMessage());
        }
    }

    public void deleteFeedback(Long appointmentId, String requesterEmail) throws AccessDeniedException {
        try {
            Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow(() -> new RuntimeException("Randevu bulunamadı."));
            Feedback feedback = appointment.getFeedback();

            if (feedback == null) {
                throw new RuntimeException("Silinecek yorum bulunamadı.");
            }

            String feedbackOwnerEmail = feedback.getClient().getUser().getEmail();
            User requester = userRepository.findByEmail(requesterEmail).orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));
            boolean isFeedbackOwner = feedbackOwnerEmail.equals(requesterEmail);
            boolean isAdmin = requester.getRole() == Role.ADMIN;
            if (!isFeedbackOwner && !isAdmin) {
                throw new AccessDeniedException("Bu yorumu silmeye yetkiniz yok.");
            }
            appointment.setFeedback(null);
            appointmentRepository.save(appointment);

        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new RuntimeException("Yorum silinirken hata oluştu: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Beklenmeyen bir hata oluştu: " + e.getMessage());
        }
    }

    public FeedbackResponseDto updateFeedback(Long appointmentId, FeedbackRequestDto request, String clientEmail) throws AccessDeniedException {
        try {
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

        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new RuntimeException("Yorum güncellenirken hata oluştu: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Beklenmeyen bir hata oluştu: " + e.getMessage());
        }
    }

    private Appointment getAuthorizedCompletedAppointment(Long appointmentId, String clientEmail) throws AccessDeniedException {
        try {
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

        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new RuntimeException("Randevu kontrolü sırasında hata oluştu: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Beklenmeyen bir hata oluştu: " + e.getMessage());
        }
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
        try {
            Double avg = feedbackRepository.findAverageRatingByTherapistId(therapistId);
            return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
        } catch (RuntimeException e) {
            throw new RuntimeException("Ortalama puan hesaplanırken hata oluştu: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Beklenmeyen bir hata oluştu: " + e.getMessage());
        }
    }


}
