package com.psikolojikdanismanlik.randevusistemi.dto.request;

import com.psikolojikdanismanlik.randevusistemi.enums.Status;

public class AppointmentStatusUpdateRequest {
    private Status status;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
