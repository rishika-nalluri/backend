package com.pawconnect.care;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public final class CareDtos {

    private CareDtos() {
    }

    public record VaccinationRequest(
            @NotBlank @Size(max = 120) String vaccineName,
            @PastOrPresent LocalDate dateGiven,
            LocalDate nextDueDate) {
    }

    public record MedicationRequest(
            @NotBlank @Size(max = 120) String medicationName,
            @Size(max = 120) String dosage,
            @Size(max = 200) String schedule,
            LocalDate startDate,
            LocalDate endDate) {
    }
}
