package org.tradebook.journal.features.journal.controller;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tradebook.journal.features.auth.repository.UserRepository;
import org.tradebook.journal.features.journal.dto.request.CreateTradeRequest;
import org.tradebook.journal.features.journal.dto.request.UpdateTradeRequest;
import org.tradebook.journal.features.journal.dto.response.TradeDetailResponse;
import org.tradebook.journal.features.journal.dto.response.TradeSummaryResponse;
import org.tradebook.journal.features.journal.service.TradeService;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/trades")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<TradeDetailResponse> createTrade(@Parameter(hidden = true) Principal principal, @RequestBody CreateTradeRequest request) {
        Long userId = getUserId(principal);
        TradeDetailResponse response = tradeService.createTrade(userId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<TradeDetailResponse> updateTrade(@Parameter(hidden = true) Principal principal, @RequestBody UpdateTradeRequest request) {
        Long userId = getUserId(principal);
        // Assuming tradeId is now in the request body
        TradeDetailResponse response = tradeService.updateTrade(request.getTradeId(), userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TradeSummaryResponse>> getTrades(@Parameter(hidden = true) Principal principal, @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate, @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long userId = getUserId(principal);

        // Default to a reasonable range if not provided
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        List<TradeSummaryResponse> response = tradeService.getTrades(userId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    private Long getUserId(Principal principal) {
        return userRepository.findByEmail(principal.getName()).orElseThrow(() -> new org.tradebook.journal.common.exception.TradeBookException("User not found")).getId();
    }
}
