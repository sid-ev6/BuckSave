package com.expensetracker.BuckSave.Controller;

import com.expensetracker.BuckSave.dto.SpendingAnalysisResponse;
import com.expensetracker.BuckSave.dto.SpendingLimitRequest;
import com.expensetracker.BuckSave.dto.SpendingLimitResponse;
import com.expensetracker.BuckSave.service.SpendingLimitService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/spending-limits")
public class SpendingLimitController {

    private final SpendingLimitService spendingLimitService;

    public SpendingLimitController(
            SpendingLimitService spendingLimitService) {

        this.spendingLimitService = spendingLimitService;
    }


    // CREATE
    @PostMapping
    public ResponseEntity<SpendingLimitResponse> createSpendingLimit(
            @Valid @RequestBody SpendingLimitRequest request) {

        return ResponseEntity.ok(
                spendingLimitService.createSpendingLimit(request)
        );
    }


    // GET ALL
    @GetMapping
    public ResponseEntity<List<SpendingLimitResponse>>
    getMySpendingLimits() {

        return ResponseEntity.ok(
                spendingLimitService
                        .getMySpendingLimits()
        );
    }


    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<SpendingLimitResponse>
    getSpendingLimitById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                spendingLimitService
                        .getSpendingLimitById(id)
        );
    }


    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<SpendingLimitResponse> updateSpendingLimit(
            @PathVariable Long id,
            @Valid @RequestBody SpendingLimitRequest request) {

        return ResponseEntity.ok(
                spendingLimitService.updateSpendingLimit(id, request)
        );
    }


    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void>
    deleteSpendingLimit(
            @PathVariable Long id) {

        spendingLimitService.deleteSpendingLimit(id);

        return ResponseEntity.noContent().build();
    }
    @GetMapping("/{id}/analysis")
    public ResponseEntity<SpendingAnalysisResponse>
    getSpendingAnalysis(@PathVariable Long id) {

        return ResponseEntity.ok(
                spendingLimitService
                        .getSpendingAnalysis(id)
        );
    }
    @GetMapping("/budget-status")
    public ResponseEntity<Map<String, Object>> getBudgetStatus() {

        return ResponseEntity.ok(
                spendingLimitService.getBudgetStatus()
        );
    }

}