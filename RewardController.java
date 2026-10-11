package com.pawconnect.reward;

import com.pawconnect.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rewards")
@RequiredArgsConstructor
public class RewardController {

    private final CoinService coinService;

    @GetMapping("/balance")
    public ResponseEntity<Map<String, Object>> getMyBalance(@AuthenticationPrincipal UserPrincipal principal) {
        int balance = coinService.getBalance(principal.getId());
        Map<String, Object> response = new HashMap<>();
        response.put("balance", balance);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<CoinTransaction>> getTransactionHistory(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(coinService.getTransactions(principal.getId()));
    }

    @GetMapping("/rules")
    public ResponseEntity<List<CoinEarningRule>> getEarningRules() {
        return ResponseEntity.ok(coinService.getActiveRules());
    }
}
