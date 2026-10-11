package com.pawconnect.admin;

import com.pawconnect.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/verifications")
public class AdminController {

    public record DecisionRequest(@NotBlank @Pattern(regexp = "VERIFIED|REJECTED") String decision) {
    }

    private final AdminService admin;

    public AdminController(AdminService admin) {
        this.admin = admin;
    }

    @GetMapping
    public List<Map<String, Object>> pending(Authentication auth) {
        return admin.pendingVerifications(CurrentUser.from(auth));
    }

    @GetMapping("/{profileId}/document-url")
    public Map<String, String> document(Authentication auth, @PathVariable UUID profileId) {
        return Map.of("url", admin.documentUrl(CurrentUser.from(auth), profileId));
    }

    @PostMapping("/{profileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void decide(Authentication auth, @PathVariable UUID profileId,
                       @Valid @RequestBody DecisionRequest body) {
        admin.decide(CurrentUser.from(auth), profileId, body.decision());
    }
}
