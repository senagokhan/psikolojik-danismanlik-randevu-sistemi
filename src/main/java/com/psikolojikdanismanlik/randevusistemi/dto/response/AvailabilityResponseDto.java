package com.psikolojikdanismanlik.randevusistemi.dto.response;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityResponseDto {
    private Long id;
    private Long therapistId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
