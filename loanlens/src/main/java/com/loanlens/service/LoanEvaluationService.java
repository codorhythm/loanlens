package com.loanlens.service;

import com.loanlens.engine.LoanEligibilityEngine;
import com.loanlens.model.entity.LoanApplication;
import com.loanlens.model.request.BorrowerRequest;
import com.loanlens.model.response.LoanDecisionResponse;
import com.loanlens.repository.LoanApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanEvaluationService {

    private final LoanEligibilityEngine engine;
    private final LoanApplicationRepository repository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ClaudeExplanationService claudeExplanationService;

    private static final String CACHE_PREFIX = "loanlens:evaluation:";
    private static final long CACHE_TTL_HOURS = 24;

    public LoanDecisionResponse evaluate(BorrowerRequest request) {

        String cacheKey = buildCacheKey(request);

        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.info("cache hit for key {}", cacheKey);
            return (LoanDecisionResponse) cached;
        }

        log.info("cache miss for key {}", cacheKey);

        LoanEligibilityEngine.EngineResult result = engine.evaluate(request);

        String passedJoined = String.join(",", result.passedChecks());
        String failedJoined = String.join(",", result.failedChecks());

        LoanApplication application = LoanApplication.builder()
                .fullName(request.getFullName())
                .age(request.getAge())
                .monthlyIncome(request.getMonthlyIncome())
                .monthlyDebt(request.getMonthlyDebt())
                .creditScore(request.getCreditScore())
                .requestedLoanAmount(request.getRequestedLoanAmount())
                .loanTenureMonths(request.getLoanTenureMonths())
                .employmentType(request.getEmploymentType())
                .decision(result.decision())
                .score(result.score())
                .riskCategory(result.riskCategory())
                .monthlyEmi(result.monthlyEmi())
                .passedChecks(passedJoined)
                .failedChecks(failedJoined)
                .evaluatedAt(LocalDateTime.now())
                .build();

        LoanApplication saved = repository.save(application);

        LoanDecisionResponse response = LoanDecisionResponse.builder()
                .applicationId(saved.getId())
                .fullName(saved.getFullName())
                .decision(saved.getDecision())
                .score(saved.getScore())
                .riskCategory(saved.getRiskCategory())
                .requestedLoanAmount(saved.getRequestedLoanAmount())
                .monthlyEmi(saved.getMonthlyEmi())
                .passedChecks(result.passedChecks())
                .failedChecks(result.failedChecks())
                .aiExplanation(null)
                .evaluatedAt(saved.getEvaluatedAt())
                .build();

        String explanation = claudeExplanationService.generateExplanation(response);
        response.setAiExplanation(explanation);

        saved.setAiExplanation(explanation);
        repository.save(saved);

        redisTemplate.opsForValue().set(cacheKey, response, CACHE_TTL_HOURS, TimeUnit.HOURS);
        log.info("cached response for key {} with ttl {} hours", cacheKey, CACHE_TTL_HOURS);

        return response;
    }

    private String buildCacheKey(BorrowerRequest request) {
        return CACHE_PREFIX
                + request.getCreditScore() + ":"
                + request.getMonthlyIncome() + ":"
                + request.getMonthlyDebt() + ":"
                + request.getRequestedLoanAmount() + ":"
                + request.getLoanTenureMonths() + ":"
                + request.getEmploymentType().toUpperCase();
    }
}