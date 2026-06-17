package org.tradebook.journal.features.ingestion.automations.core;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.tradebook.journal.features.ingestion.automations.service.TradeDataIngestionService;
import org.tradebook.journal.features.ingestion.entity.FileProcessor;
import org.tradebook.journal.features.ingestion.repository.FileJobProcessRepository;
import org.tradebook.journal.common.enums.JobStatus;

import java.util.List;

@Component
public class TradeJobProcessor implements Runnable {

    private final FileJobProcessRepository fileJobProcessRepository;
    private final TradeDataIngestionService tradeDataIngestionService;

    public TradeJobProcessor(FileJobProcessRepository fileJobProcessRepository, TradeDataIngestionService tradeDataIngestionService) {
        this.fileJobProcessRepository = fileJobProcessRepository;
        this.tradeDataIngestionService = tradeDataIngestionService;
    }

    @Override
    @Transactional
    public void run() {
        List<FileProcessor> pendingFileJobs = fileJobProcessRepository.findPendingFiles(JobStatus.PENDING, PageRequest.of(0, 10));
        for (FileProcessor job: pendingFileJobs) {
            tradeDataIngestionService.processFile(job);
        }
    }
}
