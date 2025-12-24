package com.agritech.app.farm;

import com.agritech.app.farm.dto.FarmResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/farms")
public class FarmController {
    private final FarmService service;

    public FarmController(FarmService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<FarmResponse> create(@Valid @RequestBody Farm farm) {
        return ResponseEntity.status(HttpStatus.CREATED).body(FarmResponse.from(service.create(farm)));
    }

    @GetMapping("/{id}")
    public FarmResponse get(@PathVariable Long id) {
        return FarmResponse.from(service.get(id));
    }

    @GetMapping
    public List<FarmResponse> list() {
        return service.list().stream().map(FarmResponse::from).collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    public FarmResponse update(@PathVariable Long id, @Valid @RequestBody Farm update) {
        return FarmResponse.from(service.update(id, update));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
