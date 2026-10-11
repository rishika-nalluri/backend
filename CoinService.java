package com.pawconnect.reward;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoinService {

    private final CoinTransactionRepository coinTransactionRepository;
    private final CoinEarningRuleRepository coinEarningRuleRepository;

    @Transactional(readOnly = true)
    public int getBalance(UUID petParentId) {
        return coinTransactionRepository.findLatestByPetParentId(petParentId)
                .map(CoinTransaction::getBalanceAfter)
                .orElse(0);
    }

    @Transactional
    public CoinTransaction awardCoins(UUID petParentId, String activityType, UUID referenceId) {
        int coinsToAward = coinEarningRuleRepository.findByActivityTypeAndIsActiveTrue(activityType)
                .map(CoinEarningRule::getCoinsAwarded)
                .orElse(0); // an unknown activity earns nothing

        int currentBalance = getBalance(petParentId);
        int newBalance = currentBalance + coinsToAward;

        CoinTransaction tx = CoinTransaction.builder()
                .petParentId(petParentId)
                .type("EARN")
                .source(activityType)
                .amount(coinsToAward)
                .balanceAfter(newBalance)
                .referenceId(referenceId)
                .build();

        log.info("Awarded {} PawCoins to pet parent {} for {}", coinsToAward, petParentId, activityType);
        return coinTransactionRepository.save(tx);
    }

    @Transactional
    public CoinTransaction redeemCoins(UUID petParentId, int amount, String source, UUID referenceId) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Redemption amount must be greater than zero.");
        }

        int currentBalance = getBalance(petParentId);
        if (currentBalance < amount) {
            throw new IllegalStateException("Insufficient PawCoins balance. Current: " + currentBalance + ", requested: " + amount);
        }

        int newBalance = currentBalance - amount;

        CoinTransaction tx = CoinTransaction.builder()
                .petParentId(petParentId)
                .type("REDEEM")
                .source(source)
                .amount(amount)
                .balanceAfter(newBalance)
                .referenceId(referenceId)
                .build();

        log.info("Redeemed {} PawCoins from pet parent {} for {}", amount, petParentId, source);
        return coinTransactionRepository.save(tx);
    }

    @Transactional(readOnly = true)
    public List<CoinTransaction> getTransactions(UUID petParentId) {
        return coinTransactionRepository.findByPetParentIdOrderByCreatedAtDesc(petParentId);
    }

    @Transactional(readOnly = true)
    public List<CoinEarningRule> getActiveRules() {
        return coinEarningRuleRepository.findByIsActiveTrue();
    }
}
