package org.tradebook.journal.features.journal.controller;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tradebook.journal.features.auth.repository.UserRepository;
import org.tradebook.journal.features.ingestion.entity.TradeSummary;
import org.tradebook.journal.features.ingestion.service.TradeSummaryService;
import org.tradebook.journal.features.journal.dto.response.TradeDetailResponse;
import org.tradebook.journal.features.journal.dto.response.TradeSummaryResponse;
import org.tradebook.journal.features.journal.service.TradeService;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/trade-summaries")
@RequiredArgsConstructor
public class TradeSummaryController {

    private final TradeService tradeService;
    private final UserRepository userRepository;
    private final TradeSummaryService tradeSummaryService;

    @GetMapping("/open")
    public ResponseEntity<List<TradeSummaryResponse>> getOpenPositions(@Parameter(hidden = true) Principal principal) {
        Long userId = getUserId(principal);
        List<TradeSummaryResponse> openTrades = tradeService.getOpenTrades(userId);
        return ResponseEntity.ok(openTrades);
    }

    @GetMapping("/trade")
    public ResponseEntity<TradeDetailResponse> getTrade(@Parameter(hidden = true) Principal principal, @RequestParam Long tradeId) {
        Long userId = getUserId(principal);
        TradeDetailResponse response = tradeService.getTrade(tradeId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TradeSummary>> getTradeSummaries(@Parameter(hidden = true) Principal principal) {
        Long userId = getUserId(principal);
        List<TradeSummary> summaries = tradeSummaryService.getTradeSummaries(userId);
        return ResponseEntity.ok(summaries);
    }

    private Long getUserId(Principal principal) {
        return userRepository.findByEmail(principal.getName()).orElseThrow(() -> new org.tradebook.journal.common.exception.TradeBookException("User not found")).getId();
    }
}
