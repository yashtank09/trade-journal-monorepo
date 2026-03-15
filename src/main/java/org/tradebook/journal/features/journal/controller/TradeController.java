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
import org.tradebook.journal.features.journal.dto.response.TradeResponse;
import org.tradebook.journal.features.journal.service.TradeService;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/trades")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    // TODO: Get userId from Security Context
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<TradeResponse> createTrade(@Parameter(hidden = true) Principal principal, @RequestBody CreateTradeRequest request) {
        Long userId = getUserId(principal);
        TradeResponse response = tradeService.createTrade(userId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PatchMapping("/{tradeId}")
    public ResponseEntity<TradeResponse> updateTrade(@Parameter(hidden = true) Principal principal, @PathVariable Long tradeId, @RequestBody UpdateTradeRequest request) {
        Long userId = getUserId(principal);
        TradeResponse response = tradeService.updateTrade(tradeId, userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{tradeId}")
    public ResponseEntity<TradeResponse> getTrade(@Parameter(hidden = true) Principal principal, @PathVariable Long tradeId) {
        Long userId = getUserId(principal);
        TradeResponse response = tradeService.getTrade(tradeId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TradeResponse>> getTrades(@Parameter(hidden = true) Principal principal, @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate, @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long userId = getUserId(principal);
        List<TradeResponse> response = tradeService.getTrades(userId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    private Long getUserId(Principal principal) {
        return userRepository.findByEmail(principal.getName()).orElseThrow(() -> new org.tradebook.journal.common.exception.TradeBookException("User not found")).getId();
    }
}
