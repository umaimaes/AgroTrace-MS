package com.agritech.app.farm;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FarmService {
    private final FarmRepository repository;

    public FarmService(FarmRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Farm create(Farm farm) {
        return repository.save(farm);
    }

    @Transactional(readOnly = true)
    public Farm get(Long id) {
        return repository.findById(id).orElseThrow(() -> new FarmService.NotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Farm> list() {
        return repository.findAll();
    }

    @Transactional
    public Farm update(Long id, Farm update) {
        Farm existing = get(id);
        if (update.getName() != null) existing.setName(update.getName());
        if (update.getLocation() != null) existing.setLocation(update.getLocation());
        if (update.getOwnerId() != null) existing.setOwnerId(update.getOwnerId());
        return repository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(Long id) {
            super("Farm not found: " + id);
        }
    }
}

