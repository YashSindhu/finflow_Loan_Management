package com.example.adminservice.repository;

import com.example.adminservice.entity.Decision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DecisionRepository extends JpaRepository<Decision, Long> {
    Optional<Decision> findByApplicationId(Long applicationId);
    List<Decision> findByDecisionType(Decision.DecisionType decisionType);
}
