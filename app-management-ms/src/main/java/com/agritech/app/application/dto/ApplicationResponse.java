package com.agritech.app.application.dto;

import com.agritech.app.application.Application;
import com.agritech.app.application.ApplicationStatus;

import java.time.OffsetDateTime;

public class ApplicationResponse {
    private Long id;
    private Long applicantId;
    private Long parcelId;
    private String title;
    private String type;
    private String details;
    private ApplicationStatus status;
    private String statusReason;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static ApplicationResponse from(Application application) {
        ApplicationResponse r = new ApplicationResponse();
        r.id = application.getId();
        r.applicantId = application.getApplicantId();
        r.parcelId = application.getParcel() != null ? application.getParcel().getId() : null;
        r.title = application.getTitle();
        r.type = application.getType();
        r.details = application.getDetails();
        r.status = application.getStatus();
        r.statusReason = application.getStatusReason();
        r.createdAt = application.getCreatedAt();
        r.updatedAt = application.getUpdatedAt();
        return r;
    }

    public Long getId() {
        return id;
    }

    public Long getApplicantId() {
        return applicantId;
    }

    public Long getParcelId() {
        return parcelId;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public String getDetails() {
        return details;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public String getStatusReason() {
        return statusReason;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
