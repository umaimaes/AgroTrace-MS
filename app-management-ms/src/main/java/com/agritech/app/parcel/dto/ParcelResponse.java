package com.agritech.app.parcel.dto;

import com.agritech.app.parcel.Parcel;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class ParcelResponse {
    private Long id;
    private Long farmId;
    private String name;
    private BigDecimal areaHa;
    private String cropType;
    private String geometry;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static ParcelResponse from(Parcel parcel) {
        ParcelResponse r = new ParcelResponse();
        r.id = parcel.getId();
        r.farmId = parcel.getFarm() != null ? parcel.getFarm().getId() : null;
        r.name = parcel.getName();
        r.areaHa = parcel.getAreaHa();
        r.cropType = parcel.getCropType();
        r.geometry = parcel.getGeometry();
        r.createdAt = parcel.getCreatedAt();
        r.updatedAt = parcel.getUpdatedAt();
        return r;
    }

    public Long getId() {
        return id;
    }

    public Long getFarmId() {
        return farmId;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getAreaHa() {
        return areaHa;
    }

    public String getCropType() {
        return cropType;
    }

    public String getGeometry() {
        return geometry;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}

