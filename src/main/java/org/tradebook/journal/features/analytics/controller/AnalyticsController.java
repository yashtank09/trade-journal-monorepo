package org.tradebook.journal.features.analytics.controller;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tradebook.journal.features.analytics.dto.*;
import org.tradebook.journal.features.analytics.service.AnalyticsService;
import org.tradebook.journal.features.auth.repository.UserRepository;
import org.tradebook.journal.common.exception.TradeBookException;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final UserRepository userRepository;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDTO> getSummary(
            @Parameter(hidden = true) Principal principal,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(analyticsService.getSummary(getUserId(principal), getOrDefaultStartDate(startDate), getOrDefaultEndDate(endDate)));
    }

    @GetMapping("/equity-curve")
    public ResponseEntity<List<ChartPointDTO>> getEquityCurve(
            @Parameter(hidden = true) Principal principal,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(analyticsService.getEquityCurve(getUserId(principal), getOrDefaultStartDate(startDate), getOrDefaultEndDate(endDate)));
    }

    @GetMapping("/breakdown/asset")
    public ResponseEntity<List<BreakdownDTO>> getAssetBreakdown(
            @Parameter(hidden = true) Principal principal,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(analyticsService.getAssetBreakdown(getUserId(principal), getOrDefaultStartDate(startDate), getOrDefaultEndDate(endDate)));
    }

    @GetMapping("/breakdown/strategy")
    public ResponseEntity<List<BreakdownDTO>> getStrategyBreakdown(
            @Parameter(hidden = true) Principal principal,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(analyticsService.getStrategyBreakdown(getUserId(principal), getOrDefaultStartDate(startDate), getOrDefaultEndDate(endDate)));
    }

    @GetMapping("/breakdown/time")
    public ResponseEntity<List<BreakdownDTO>> getTimeBreakdown(
            @Parameter(hidden = true) Principal principal,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(analyticsService.getTimeBreakdown(getUserId(principal), getOrDefaultStartDate(startDate), getOrDefaultEndDate(endDate)));
    }

    @GetMapping("/risk-metrics")
    public ResponseEntity<RiskMetricsDTO> getRiskMetrics(
            @Parameter(hidden = true) Principal principal,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(analyticsService.getRiskMetrics(getUserId(principal), getOrDefaultStartDate(startDate), getOrDefaultEndDate(endDate)));
    }

    @GetMapping("/mistakes")
    public ResponseEntity<List<BreakdownDTO>> getMistakes(
            @Parameter(hidden = true) Principal principal,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(analyticsService.getMistakes(getUserId(principal), getOrDefaultStartDate(startDate), getOrDefaultEndDate(endDate)));
    }

    private Long getUserId(Principal principal) {
        if (principal == null || principal.getName() == null) {
             throw new TradeBookException("User is not authenticated");
        }
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new TradeBookException("User not found"))
                .getId();
    }

    private LocalDate getOrDefaultStartDate(LocalDate startDate) {
        return startDate != null ? startDate : LocalDate.now().minusYears(1); // Default to last 1 year
    }

    private LocalDate getOrDefaultEndDate(LocalDate endDate) {
        return endDate != null ? endDate : LocalDate.now();
    }
}
