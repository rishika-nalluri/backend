package com.pawconnect.profile;

import com.pawconnect.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Records where an uploaded verification document lives. Files are uploaded straight to Supabase Storage. */
@RestController
public class VerificationDocumentController {

    /** Must sit in the caller's own folder: <user id>/<file name>. */
    private static final Pattern DOCUMENT_PATH = Pattern.compile("^[0-9a-fA-F-]{36}/[A-Za-z0-9._-]{1,200}$");

    public record DocumentRequest(@NotBlank @Size(max = 300) String path) {
    }

    private final JdbcTemplate jdbc;

    public VerificationDocumentController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostMapping("/profiles/me/verification-document")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void attach(Authentication auth, @Valid @RequestBody DocumentRequest body) {
        CurrentUser user = CurrentUser.from(auth);
        String path = body.path();
        if (!DOCUMENT_PATH.matcher(path).matches() || !path.startsWith(user.id() + "/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid document path");
        }
        int updated;
        if ("VET".equals(user.role())) {
            updated = jdbc.update(
                    "update public.vet_profiles set verification_doc_url = ?, verification_status = "
                            + "case when verification_status = 'REJECTED' then 'PENDING'::public.verification_status "
                            + "else verification_status end where profile_id = ?",
                    path, user.id());
        } else if ("NGO".equals(user.role())) {
            updated = jdbc.update(
                    "update public.ngo_profiles set registration_cert_url = ?, verification_status = "
                            + "case when verification_status = 'REJECTED' then 'PENDING'::public.verification_status "
                            + "else verification_status end where profile_id = ?",
                    path, user.id());
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only veterinarians and NGOs upload verification documents");
        }
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found");
        }
    }
}
