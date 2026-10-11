package com.pawconnect.reward;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CoinEarningRuleRepository extends JpaRepository<CoinEarningRule, UUID> {
    Optional<CoinEarningRule> findByActivityTypeAndIsActiveTrue(String activityType);
    List<CoinEarningRule> findByIsActiveTrue();
}
