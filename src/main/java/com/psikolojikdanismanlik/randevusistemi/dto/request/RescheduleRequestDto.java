package com.psikolojikdanismanlik.randevusistemi.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class RescheduleRequestDto {
    @JsonProperty("newDateTime")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime newTime;

    public LocalDateTime getNewTime() {
        return newTime;
    }

    public void setNewTime(LocalDateTime newTime) {
        this.newTime = newTime;
    }
}
