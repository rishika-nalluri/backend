package com.pawconnect.care;

import com.pawconnect.care.CareDtos.MedicationRequest;
import com.pawconnect.care.CareDtos.VaccinationRequest;
import com.pawconnect.dog.DogService;
import com.pawconnect.reward.CoinService;
import com.pawconnect.security.CurrentUser;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Owner-only vaccination and medication records. Vets see these through the consent-gated record view.
 * A vaccination earns coins once per (dog, vaccine, date given), so re-entering the same record pays nothing.
 */
@Service
public class CareService {

    private final JdbcTemplate jdbc;
    private final DogService dogs;
    private final CoinService coins;

    public CareService(JdbcTemplate jdbc, DogService dogs, CoinService coins) {
        this.jdbc = jdbc;
        this.dogs = dogs;
        this.coins = coins;
    }

    public List<Map<String, Object>> vaccinations(CurrentUser user, UUID dogId) {
        ownerOf(user, dogId);
        return jdbc.queryForList(
                "select id, vaccine_name, date_given, next_due_date from public.vaccination_records "
                        + "where dog_id = ? order by next_due_date nulls last, created_at",
                dogId);
    }

    public UUID addVaccination(CurrentUser user, UUID dogId, VaccinationRequest r) {
        ownerOf(user, dogId);
        if (r.dateGiven() != null && r.nextDueDate() != null && r.nextDueDate().isBefore(r.dateGiven())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Next due date cannot be before the date given");
        }
        String name = r.vaccineName().trim();
        Integer duplicates = jdbc.queryForObject(
                "select count(*) from public.vaccination_records where dog_id = ? "
                        + "and lower(vaccine_name) = lower(?) and date_given is not distinct from cast(? as date)",
                Integer.class, dogId, name, r.dateGiven());
        UUID id = jdbc.queryForObject(
                "insert into public.vaccination_records (dog_id, vaccine_name, date_given, next_due_date) "
                        + "values (?, ?, ?, ?) returning id",
                UUID.class, dogId, name, r.dateGiven(), r.nextDueDate());
        if (duplicates == null || duplicates == 0) {
            coins.awardCoins(user.id(), "VACCINATION_LOG", id);
        }
        return id;
    }

    public void deleteVaccination(CurrentUser user, UUID id) {
        user.requireRole("PET_PARENT");
        int n = jdbc.update(
                "delete from public.vaccination_records v using public.dogs d "
                        + "where v.id = ? and d.id = v.dog_id and d.pet_parent_id = ?",
                id, user.id());
        if (n == 0) {
            throw notFound();
        }
    }

    public List<Map<String, Object>> medications(CurrentUser user, UUID dogId) {
        ownerOf(user, dogId);
        return jdbc.queryForList(
                "select id, medication_name, dosage, schedule, start_date, end_date from public.medication_records "
                        + "where dog_id = ? order by start_date desc nulls last, created_at desc",
                dogId);
    }

    public UUID addMedication(CurrentUser user, UUID dogId, MedicationRequest r) {
        ownerOf(user, dogId);
        if (r.startDate() != null && r.endDate() != null && r.endDate().isBefore(r.startDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date cannot be before the start date");
        }
        return jdbc.queryForObject(
                "insert into public.medication_records (dog_id, medication_name, dosage, schedule, start_date, end_date) "
                        + "values (?, ?, ?, ?, ?, ?) returning id",
                UUID.class, dogId, r.medicationName().trim(), blank(r.dosage()), blank(r.schedule()),
                r.startDate(), r.endDate());
    }

    public void deleteMedication(CurrentUser user, UUID id) {
        user.requireRole("PET_PARENT");
        int n = jdbc.update(
                "delete from public.medication_records m using public.dogs d "
                        + "where m.id = ? and d.id = m.dog_id and d.pet_parent_id = ?",
                id, user.id());
        if (n == 0) {
            throw notFound();
        }
    }

    private void ownerOf(CurrentUser user, UUID dogId) {
        user.requireRole("PET_PARENT");
        dogs.assertOwned(dogId, user.id());
    }

    private static String blank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found");
    }
}
