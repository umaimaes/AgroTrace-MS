package com.agritech.app.farm.dto;

import com.agritech.app.farm.Farm;

import java.time.OffsetDateTime;

public class FarmResponse {
    private Long id;
    private Long ownerId;
    private String name;
    private String location;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static FarmResponse from(Farm farm) {
        FarmResponse r = new FarmResponse();
        r.id = farm.getId();
        r.ownerId = farm.getOwnerId();
        r.name = farm.getName();
        r.location = farm.getLocation();
        r.createdAt = farm.getCreatedAt();
        r.updatedAt = farm.getUpdatedAt();
        return r;
    }

    public Long getId() {
        return id;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}

