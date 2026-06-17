package org.tradebook.journal.features.ingestion.automations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tradebook.journal.features.ingestion.automations.core.TradeJobProcessor;
import org.tradebook.journal.features.ingestion.automations.core.TradeMasterJobProcessor;
import org.tradebook.journal.features.ingestion.automations.core.TradePositionDiscoveryProcessor;

@Slf4j
@Component
@EnableScheduling
public class TradeJobExecutor {

    private final TaskExecutor taskExecutor;
    private final TradeJobProcessor tradeJobProcessor;
    private final TradeMasterJobProcessor tradeMasterJobProcessor;
    private final TradePositionDiscoveryProcessor tradePositionDiscoveryProcessor;


    public TradeJobExecutor(@Qualifier("applicationTaskExecutor") TaskExecutor taskExecutor, TradeJobProcessor tradeJobProcessor, TradeMasterJobProcessor tradeMasterJobProcessor, TradePositionDiscoveryProcessor tradePositionDiscoveryProcessor) {
        this.taskExecutor = taskExecutor;
        this.tradeJobProcessor = tradeJobProcessor;
        this.tradeMasterJobProcessor = tradeMasterJobProcessor;
        this.tradePositionDiscoveryProcessor = tradePositionDiscoveryProcessor;
    }

    @Scheduled(fixedDelayString = "${file.processing.interval-ms}")
    public void triggerProcessingJobs() {
        taskExecutor.execute(tradeJobProcessor);
    }

    @Scheduled(fixedDelayString = "${trade.book.master.processing.interval-ms}")
    public void processTradeBookMaster() {
        taskExecutor.execute(tradeMasterJobProcessor);
    }

    @Scheduled(fixedDelayString = "${trade.book.position.processing.interval-ms}")
    public void processTradePosition() {
        taskExecutor.execute(tradePositionDiscoveryProcessor);
    }
}
