package com.pawconnect.care;

import com.pawconnect.care.CareDtos.MedicationRequest;
import com.pawconnect.care.CareDtos.VaccinationRequest;
import com.pawconnect.security.CurrentUser;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CareController {

    private final CareService care;

    public CareController(CareService care) {
        this.care = care;
    }

    @GetMapping("/dogs/{dogId}/vaccinations")
    public List<Map<String, Object>> vaccinations(Authentication auth, @PathVariable UUID dogId) {
        return care.vaccinations(CurrentUser.from(auth), dogId);
    }

    @PostMapping("/dogs/{dogId}/vaccinations")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, UUID> addVaccination(Authentication auth, @PathVariable UUID dogId,
                                            @Valid @RequestBody VaccinationRequest body) {
        return Map.of("id", care.addVaccination(CurrentUser.from(auth), dogId, body));
    }

    @DeleteMapping("/vaccinations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVaccination(Authentication auth, @PathVariable UUID id) {
        care.deleteVaccination(CurrentUser.from(auth), id);
    }

    @GetMapping("/dogs/{dogId}/medications")
    public List<Map<String, Object>> medications(Authentication auth, @PathVariable UUID dogId) {
        return care.medications(CurrentUser.from(auth), dogId);
    }

    @PostMapping("/dogs/{dogId}/medications")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, UUID> addMedication(Authentication auth, @PathVariable UUID dogId,
                                           @Valid @RequestBody MedicationRequest body) {
        return Map.of("id", care.addMedication(CurrentUser.from(auth), dogId, body));
    }

    @DeleteMapping("/medications/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMedication(Authentication auth, @PathVariable UUID id) {
        care.deleteMedication(CurrentUser.from(auth), id);
    }
}
