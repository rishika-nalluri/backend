package com.pawconnect.dog;

import com.pawconnect.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/dogs")
@RequiredArgsConstructor
public class DogController {

    private final DogService dogService;

    @GetMapping
    public ResponseEntity<List<Dog>> getMyDogs(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(dogService.getMyDogs(principal.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Dog> getDogById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(dogService.getDogDetails(id, principal.getId(), principal.getRole()));
    }

    @PostMapping
    public ResponseEntity<Dog> createDog(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Dog dog
    ) {
        return ResponseEntity.ok(dogService.createDog(principal.getId(), dog));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Dog> updateDog(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Dog update
    ) {
        return ResponseEntity.ok(dogService.updateDog(id, principal.getId(), update));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDog(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        dogService.deleteDog(id, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
