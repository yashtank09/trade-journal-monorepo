package org.tradebook.journal.features.journal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tradebook.journal.features.journal.entity.Instrument;
import org.tradebook.journal.features.journal.entity.TradePlan;
import org.tradebook.journal.features.journal.service.InstrumentService;
import org.tradebook.journal.features.journal.service.TradePlanService;

import java.util.List;

@RestController
@RequestMapping("/trade-book-master")
@RequiredArgsConstructor
public class TradeBookMasterController {

    private final InstrumentService instrumentService;
    private final TradePlanService tradePlanService;

    @PostMapping("/instruments")
    public ResponseEntity<Instrument> createInstrument(@RequestBody Instrument instrument) {
        Instrument savedInstrument = instrumentService.save(instrument);
        return ResponseEntity.ok(savedInstrument);
    }

    @GetMapping("/instruments")
    public ResponseEntity<List<Instrument>> getAllInstruments() {
        List<Instrument> instruments = instrumentService.findAll();
        return ResponseEntity.ok(instruments);
    }

    @PostMapping("/trade-plans")
    public ResponseEntity<TradePlan> createTradePlan(@RequestBody TradePlan tradePlan) {
        TradePlan savedTradePlan = tradePlanService.save(tradePlan);
        return ResponseEntity.ok(savedTradePlan);
    }

    @GetMapping("/trade-plans")
    public ResponseEntity<List<TradePlan>> getTradePlans(@RequestParam Long tradeSummaryId) {
        List<TradePlan> tradePlans = tradePlanService.findByTradeSummaryId(tradeSummaryId);
        return ResponseEntity.ok(tradePlans);
    }
}
