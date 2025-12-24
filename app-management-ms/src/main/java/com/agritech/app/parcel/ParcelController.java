package com.agritech.app.parcel;

import com.agritech.app.parcel.dto.ParcelResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/parcels")
public class ParcelController {
    private final ParcelService service;

    public ParcelController(ParcelService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ParcelResponse> create(@RequestParam Long farmId, @Valid @RequestBody Parcel parcel) {
        Parcel saved = service.create(farmId, parcel);
        return ResponseEntity.status(HttpStatus.CREATED).body(ParcelResponse.from(saved));
    }

    @GetMapping("/{id}")
    public ParcelResponse get(@PathVariable Long id) {
        return ParcelResponse.from(service.get(id));
    }

    @GetMapping
    public List<ParcelResponse> list(@RequestParam(required = false) Long farmId) {
        return service.list(farmId).stream().map(ParcelResponse::from).collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    public ParcelResponse update(@PathVariable Long id, @Valid @RequestBody Parcel update) {
        return ParcelResponse.from(service.update(id, update));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
