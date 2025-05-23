package com.psikolojikdanismanlik.randevusistemi.dto.request;
import java.time.LocalDateTime;


public class AppointmentRequest {
    private Long clientId;
    private Long therapistId;
    private LocalDateTime startTime;

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getTherapistId() {
        return therapistId;
    }

    public void setTherapistId(Long therapistId) {
        this.therapistId = therapistId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
}
