package com.psikolojikdanismanlik.randevusistemi.dto.request;

import java.time.LocalDateTime;

public class RescheduleRequestDto {
    private LocalDateTime newTime;

    public LocalDateTime getNewTime() {
        return newTime;
    }

    public void setNewTime(LocalDateTime newTime) {
        this.newTime = newTime;
    }
}
