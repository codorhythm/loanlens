package com.loanlens.controller;

import com.loanlens.model.request.BorrowerRequest;
import com.loanlens.model.response.LoanDecisionResponse;
import com.loanlens.service.LoanEvaluationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LoanController {

    private final LoanEvaluationService evaluationService;

    @PostMapping("/evaluate")
    public ResponseEntity<LoanDecisionResponse> evaluate(@Valid @RequestBody BorrowerRequest request) {
        LoanDecisionResponse response = evaluationService.evaluate(request);
        return ResponseEntity.ok(response);
    }
}