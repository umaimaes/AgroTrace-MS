package com.agritech.app.application;

import com.agritech.app.application.dto.*;
import jakarta.validation.Valid;
import com.agritech.app.parcel.ParcelRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {
    private final ApplicationService service;
    private final ParcelRepository parcelRepository;

    public ApplicationController(ApplicationService service, ParcelRepository parcelRepository) {
        this.service = service;
        this.parcelRepository = parcelRepository;
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> create(@Valid @RequestBody CreateApplicationRequest request) {
        Application application = new Application();
        application.setApplicantId(request.getApplicantId());
        if (request.getParcelId() != null) {
            application.setParcel(parcelRepository.findById(request.getParcelId()).orElse(null));
        }
        application.setTitle(request.getTitle());
        application.setType(request.getType());
        application.setDetails(request.getDetails());
        Application saved = service.create(application);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApplicationResponse.from(saved));
    }

    @GetMapping("/{id}")
    public ApplicationResponse get(@PathVariable Long id) {
        return ApplicationResponse.from(service.get(id));
    }

    @GetMapping
    public List<ApplicationResponse> list(
            @RequestParam(required = false) Long applicantId,
            @RequestParam(required = false) ApplicationStatus status
    ) {
        return service.list(applicantId, status).stream()
                .map(ApplicationResponse::from)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    public ApplicationResponse update(@PathVariable Long id, @Valid @RequestBody UpdateApplicationRequest request) {
        Application update = new Application();
        update.setApplicantId(request.getApplicantId());
        if (request.getParcelId() != null) {
            update.setParcel(parcelRepository.findById(request.getParcelId()).orElse(null));
        }
        update.setTitle(request.getTitle());
        update.setType(request.getType());
        update.setDetails(request.getDetails());
        return ApplicationResponse.from(service.update(id, update));
    }

    @PostMapping("/{id}/status")
    public ApplicationResponse changeStatus(@PathVariable Long id, @Valid @RequestBody ChangeStatusRequest request) {
        return ApplicationResponse.from(service.changeStatus(id, request.getStatus(), request.getReason()));
    }
}
