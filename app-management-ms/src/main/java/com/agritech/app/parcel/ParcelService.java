package com.agritech.app.parcel;

import com.agritech.app.farm.FarmService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ParcelService {
    private final ParcelRepository repository;
    private final FarmService farmService;

    public ParcelService(ParcelRepository repository, FarmService farmService) {
        this.repository = repository;
        this.farmService = farmService;
    }

    @Transactional
    public Parcel create(Long farmId, Parcel parcel) {
        parcel.setFarm(farmService.get(farmId));
        return repository.save(parcel);
    }

    @Transactional(readOnly = true)
    public Parcel get(Long id) {
        return repository.findById(id).orElseThrow(() -> new ParcelService.NotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Parcel> list(Long farmId) {
        if (farmId != null) {
            return repository.findByFarm_Id(farmId);
        }
        return repository.findAll();
    }

    @Transactional
    public Parcel update(Long id, Parcel update) {
        Parcel existing = get(id);
        if (update.getName() != null) existing.setName(update.getName());
        if (update.getAreaHa() != null) existing.setAreaHa(update.getAreaHa());
        if (update.getCropType() != null) existing.setCropType(update.getCropType());
        if (update.getGeometry() != null) existing.setGeometry(update.getGeometry());
        return repository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(Long id) {
            super("Parcel not found: " + id);
        }
    }
}

