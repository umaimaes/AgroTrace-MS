package com.agritech.app.parcel;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParcelRepository extends JpaRepository<Parcel, Long> {
    List<Parcel> findByFarm_Id(Long farmId);
}

