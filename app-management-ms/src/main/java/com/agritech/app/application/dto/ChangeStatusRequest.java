package com.agritech.app.application.dto;

import com.agritech.app.application.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ChangeStatusRequest {
    @NotNull
    private ApplicationStatus status;

    @Size(max = 500)
    private String reason;

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}

