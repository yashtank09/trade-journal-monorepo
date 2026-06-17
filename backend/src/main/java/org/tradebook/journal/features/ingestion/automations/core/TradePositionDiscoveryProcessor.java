package org.tradebook.journal.features.ingestion.automations.core;


import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.tradebook.journal.features.ingestion.automations.service.TradePositionDiscoveryService;
import org.tradebook.journal.features.ingestion.entity.TradeBookMaster;
import org.tradebook.journal.features.ingestion.repository.TradeBookMasterRepository;

import java.util.List;

@Slf4j
@Component
public class TradePositionDiscoveryProcessor implements Runnable {

    private final TradeBookMasterRepository tradeBookMasterRepository;
    private final TradePositionDiscoveryService tradePositionDiscoveryService;

    public TradePositionDiscoveryProcessor(TradeBookMasterRepository tradeMasterJobProcessor, TradePositionDiscoveryService tradePositionDiscoveryService) {
        this.tradeBookMasterRepository = tradeMasterJobProcessor;
        this.tradePositionDiscoveryService = tradePositionDiscoveryService;
    }


    @Override
    @Transactional
    public void run() {
        List<TradeBookMaster> pendingTrades = tradeBookMasterRepository.findPendingTrades(PageRequest.of(0, 50));

        if (pendingTrades.isEmpty()) {
            log.info("No pending trades found");
            return;
        }

        log.info("Found {} pending trades", pendingTrades.size());
        for (TradeBookMaster tradeBookMaster : pendingTrades) {
            try {
                tradePositionDiscoveryService.processTrade(tradeBookMaster);
            } catch (Exception e) {
                log.error("Error processing trade book master", e);
            }
        }

    }
}
