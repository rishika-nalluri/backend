package com.pawconnect.ai;

import com.pawconnect.health.AIRiskService;
import com.pawconnect.health.AIRiskService.AIRiskResponse;
import com.pawconnect.health.AIRiskService.DogHealthRiskInput;
import com.pawconnect.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Browser-facing AI endpoints. The browser never calls the AI service directly. */
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    public record ChatTurn(@NotBlank @Size(max = 16) String role, @NotBlank @Size(max = 2000) String content) {
    }

    public record ChatRequest(@NotBlank List<ChatTurn> messages, Map<String, Object> userContext) {
    }

    private final AIRiskService risk;
    private final AiGatewayClient gateway;

    @PostMapping("/risk")
    public AIRiskResponse screen(Authentication auth, @RequestBody DogHealthRiskInput input) {
        CurrentUser.from(auth);
        return risk.preview(input);
    }

    /** Sends the latest user message to the AI service. Its own daily limit applies per user. */
    @PostMapping("/chat")
    public Map<String, Object> chat(Authentication auth, @Valid @RequestBody ChatRequest request) {
        CurrentUser user = CurrentUser.from(auth);
        String last = null;
        for (int i = request.messages().size() - 1; i >= 0; i--) {
            ChatTurn turn = request.messages().get(i);
            if ("user".equalsIgnoreCase(turn.role())) {
                last = turn.content();
                break;
            }
        }
        if (last == null) {
            return Map.of("reply", "Ask me something about your dog's care.");
        }
        String reply;
        try {
            reply = gateway.chat(user.id().toString(), last, null);
        } catch (RuntimeException unavailable) {
            reply = null;
        }
        return Map.of("reply", reply == null
                ? "The pet-care assistant is unavailable right now. Please book a veterinarian for health questions."
                : reply);
    }
}
