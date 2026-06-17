package org.tradebook.journal.features.ingestion.automations.service;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tradebook.journal.config.FileProcessingProperties;
import org.tradebook.journal.features.ingestion.dto.TradeBookCsvDto;
import org.tradebook.journal.features.ingestion.entity.FileProcessor;
import org.tradebook.journal.features.ingestion.entity.TradeBookData;
import org.tradebook.journal.common.enums.JobStatus;
import org.tradebook.journal.features.ingestion.repository.FileJobProcessRepository;
import org.tradebook.journal.features.ingestion.repository.TradeBookDataRepository;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeDataIngestionService {

    private final FileJobProcessRepository fileJobProcessRepository;
    private final TradeBookDataRepository tradeBookDataRepository;
    private final FileProcessingProperties fileProcessingProperties;

    @Transactional
    public void processFile(FileProcessor fileJob) {
        log.info("Start file processing. JobId={}", fileJob.getFileJobId());
        log.info("Processing trade book data: {}", fileJob.getStoredPath());
        try {
            updateJobStatus(fileJob, JobStatus.PROCESSING);
            switch (fileJob.getFileCategory()) {
                case TRADE_BOOK -> processTradeBook(fileJob);
                case HOLDINGS -> log.warn("Holdings processing not implemented yet");
                case BANK_STATEMENT -> log.warn("Bank statement processing not implemented yet");
                case TAX_REPORT -> log.warn("Tax report processing not implemented yet");
                default -> throw new IllegalArgumentException("Unsupported file category");
            }
            updateJobStatus(fileJob, JobStatus.COMPLETED);
            log.info("Completed file processing. JobId={}", fileJob.getFileJobId());
        } catch (Exception ex) {
            log.error("Failed to process file. JobId={}", fileJob.getFileJobId(), ex);
            updateJobStatus(fileJob, JobStatus.FAILED);
        }
    }

    private void processTradeBook(FileProcessor fileJob) {
        final Integer batchSize = fileProcessingProperties.getBatchSize();
        Path filePath = Path.of(fileJob.getStoredPath());
        List<TradeBookData> batch = new ArrayList<>(batchSize);
        Set<Long> batchTradeIds = new HashSet<>();
        try (Reader reader = Files.newBufferedReader(filePath)) {
            CsvToBean<TradeBookCsvDto> csvToBean = new CsvToBeanBuilder<TradeBookCsvDto>(reader).withType(TradeBookCsvDto.class).withIgnoreLeadingWhiteSpace(true).build();

            for (TradeBookCsvDto tradeBookCsvDto : csvToBean) {
                try {

                    TradeBookData entity = mapToEntity(tradeBookCsvDto, fileJob.getFileJobId(), fileJob.getUserId());

                    batch.add(entity);
                    batchTradeIds.add(tradeBookCsvDto.getTradeId());

                    if (batch.size() >= batchSize) {
                        saveBatchSafely(batch, batchTradeIds);
                        batch.clear();
                        batchTradeIds.clear();
                    }
                } catch (Exception e) {
                    log.error("Skipping invalid row, TradeId={}", tradeBookCsvDto.getTradeId(), e);
                }
            }

            if (!batch.isEmpty()) {
                saveBatchSafely(batch, batchTradeIds);
            }
        } catch (Exception ex) {
            updateJobStatus(fileJob, JobStatus.FAILED);
            log.error("Failed to process trade book. JobId={}", fileJob.getFileJobId(), ex);
        }
    }


    private TradeBookData mapToEntity(TradeBookCsvDto tradeBookCsvDto, Long fileJobId, Long userId) {
        try {
            TradeBookData entity = new TradeBookData();
            entity.setUserId(userId);
            entity.setSymbol(tradeBookCsvDto.getSymbol());
            entity.setIsin(tradeBookCsvDto.getIsin());
            entity.setTradeDate(tradeBookCsvDto.getTradeDate());
            entity.setExchange(tradeBookCsvDto.getExchange());
            entity.setSegment(tradeBookCsvDto.getSegment());
            entity.setSeries(tradeBookCsvDto.getSeries());
            entity.setTradeType(tradeBookCsvDto.getTradeType());
            entity.setAuction(tradeBookCsvDto.getAuction());
            entity.setQuantity(tradeBookCsvDto.getQuantity().intValue());
            entity.setPrice(tradeBookCsvDto.getPrice());
            entity.setTradeId(tradeBookCsvDto.getTradeId());
            entity.setOrderId(tradeBookCsvDto.getOrderId());
            entity.setOrderExecutionTime(tradeBookCsvDto.getOrderExecutionTime());
            entity.setExpiryDate(tradeBookCsvDto.getExpiryDate());
            entity.setSourceFileId(fileJobId);
            return entity;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to map TradeBookCsvDto to TradeBookData", e);
        }
    }

    private void saveBatchSafely(List<TradeBookData> batch, Set<Long> batchTradeIds) {
        // Fetch duplicates in ONE DB call
        Set<Long> existingTradeIds = tradeBookDataRepository.findExistingTradeIds(batchTradeIds);

        List<TradeBookData> toInsert = batch.stream().filter(t -> !existingTradeIds.contains(t.getTradeId())).toList();

        if (!toInsert.isEmpty()) {
            tradeBookDataRepository.saveAll(toInsert);
        }

        if (!existingTradeIds.isEmpty()) {
            log.info("Skipped {} duplicate trades.", existingTradeIds.size());
        }
    }

    private void updateJobStatus(FileProcessor job, JobStatus status) {
        job.setStatus(status);
        fileJobProcessRepository.save(job);
    }
}
