package com.agritech.app.application.dto;

import jakarta.validation.constraints.Size;

public class UpdateApplicationRequest {
    private Long applicantId;
    private Long parcelId;

    @Size(max = 255)
    private String title;

    @Size(max = 100)
    private String type;

    @Size(max = 5000)
    private String details;

    public Long getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(Long applicantId) {
        this.applicantId = applicantId;
    }

    public Long getParcelId() {
        return parcelId;
    }

    public void setParcelId(Long parcelId) {
        this.parcelId = parcelId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
