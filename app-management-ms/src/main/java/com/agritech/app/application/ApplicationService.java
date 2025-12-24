package com.agritech.app.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ApplicationService {
    private final ApplicationRepository repository;

    public ApplicationService(ApplicationRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Application create(Application application) {
        application.setStatus(ApplicationStatus.PENDING);
        return repository.save(application);
    }

    @Transactional(readOnly = true)
    public Application get(Long id) {
        return repository.findById(id).orElseThrow(() -> new ApplicationService.NotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Application> list(Long applicantId, ApplicationStatus status) {
        if (applicantId != null && status != null) {
            return repository.findAll((root, query, cb) -> cb.and(
                    cb.equal(root.get("applicantId"), applicantId),
                    cb.equal(root.get("status"), status)
            ));
        } else if (applicantId != null) {
            return repository.findByApplicantId(applicantId);
        } else if (status != null) {
            return repository.findByStatus(status);
        }
        return repository.findAll();
    }

    @Transactional
    public Application update(Long id, Application update) {
        Application existing = get(id);
        if (update.getTitle() != null) existing.setTitle(update.getTitle());
        if (update.getType() != null) existing.setType(update.getType());
        if (update.getDetails() != null) existing.setDetails(update.getDetails());
        if (update.getApplicantId() != null) existing.setApplicantId(update.getApplicantId());
        return repository.save(existing);
    }

    @Transactional
    public Application changeStatus(Long id, ApplicationStatus status, String reason) {
        Application existing = get(id);
        existing.setStatus(status);
        existing.setStatusReason(reason);
        return repository.save(existing);
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(Long id) {
            super("Application not found: " + id);
        }
    }
}

