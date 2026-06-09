package com.loanlens.service;

import com.loanlens.model.response.LoanDecisionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ClaudeExplanationService {

    @Value("${claude.api.key}")
    private String apiKey;

    @Value("${claude.api.url}")
    private String apiUrl;

    @Value("${claude.model}")
    private String model;

    public String generateExplanation(LoanDecisionResponse decision) {
        log.info("generating explanation for {} decision {}",
                decision.getApplicationId(), decision.getDecision());
        String prompt = buildPrompt(decision);
        log.info("prompt ready, {} chars", prompt.length());
        return generateMockExplanation(decision);
    }

    private String buildPrompt(LoanDecisionResponse decision) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("loan decision: ").append(decision.getDecision()).append("\n");
        prompt.append("score: ").append(decision.getScore()).append("/100\n");
        prompt.append("risk: ").append(decision.getRiskCategory()).append("\n\n");

        if (!decision.getPassedChecks().isEmpty()) {
            prompt.append("passed:\n");
            for (String check : decision.getPassedChecks()) {
                prompt.append("- ").append(check).append("\n");
            }
        }

        if (!decision.getFailedChecks().isEmpty()) {
            prompt.append("failed:\n");
            for (String check : decision.getFailedChecks()) {
                prompt.append("- ").append(check).append("\n");
            }
        }

        prompt.append("\nexplain this decision in 2-3 sentences. suggest one fix if rejected.");
        return prompt.toString();
    }

    private String generateMockExplanation(LoanDecisionResponse decision) {
        List<String> failed = decision.getFailedChecks();

        if ("APPROVED".equals(decision.getDecision())) {
            return "application approved. strong credit score and healthy dti ratio. funds disbursed within 3-5 working days.";
        }

        if ("REJECTED".equals(decision.getDecision())) {
            String reason = failed.isEmpty() ? "multiple risk factors" : failed.get(0);
            return "application rejected. primary issue: " + reason + ". reduce existing debt and reapply after 3 months with a credit score above 700.";
        }

        String concern = failed.isEmpty() ? "borderline profile" : failed.get(0);
        return "application under review. concern: " + concern + ". loan officer will follow up in 2 working days.";
    }
}