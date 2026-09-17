package com.expensetracker.BuckSave.Controller;

import com.expensetracker.BuckSave.dto.DailySummaryResponse;
import com.expensetracker.BuckSave.dto.DashboardResponse;
import com.expensetracker.BuckSave.dto.MonthlySummaryResponse;
import com.expensetracker.BuckSave.dto.WeeklySummaryResponse;
import com.expensetracker.BuckSave.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard() {

        return ResponseEntity.ok(
                dashboardService.getDashboard()
        );
    }
    @GetMapping("/monthly")
    public ResponseEntity<MonthlySummaryResponse> getMonthlySummary(
            @RequestParam int month,
            @RequestParam int year) {

        return ResponseEntity.ok(
                dashboardService.getMonthlySummary(month, year)
        );
    }
    @GetMapping("/daily")
    public ResponseEntity<DailySummaryResponse> getDailySummary(
            @RequestParam LocalDate date) {

        return ResponseEntity.ok(
                dashboardService.getDailySummary(date)
        );
    }

    @GetMapping("/weekly")
    public ResponseEntity<WeeklySummaryResponse> getWeeklySummary(
            @RequestParam LocalDate date) {

        return ResponseEntity.ok(
                dashboardService.getWeeklySummary(date)
        );
    }

}