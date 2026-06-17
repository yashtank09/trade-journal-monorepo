package org.tradebook.journal.features.ingestion.automations.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tradebook.journal.features.ingestion.dto.AggregatedTradeBookDataDto;
import org.tradebook.journal.features.ingestion.entity.TradeBookMaster;
import org.tradebook.journal.common.enums.TradeStatus;
import org.tradebook.journal.features.ingestion.repository.TradeBookDataRepository;
import org.tradebook.journal.features.ingestion.repository.TradeBookMasterRepository;
import org.tradebook.journal.common.util.TradeCalculationUtils;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeMasterIngestionService {

    private final TradeBookMasterRepository tradeBookMasterRepository;
    private final TradeBookDataRepository tradeBookDataRepository;

    public void processTradeBookData(List<AggregatedTradeBookDataDto> tradeBookDataList) {
        log.info("Starting trade book data.");
        try {
            List<TradeBookMaster> tradeBookMasterList = tradeBookDataList.stream().map(this::mapToEntity).toList();
            tradeBookMasterRepository.saveAll(tradeBookMasterList);

            for (AggregatedTradeBookDataDto dataDto : tradeBookDataList) {
                UUID recordBatchId = UUID.randomUUID();
                tradeBookDataRepository.updateProcessedRecords(recordBatchId, dataDto.getSymbol(),
                        dataDto.getTradeType(), dataDto.getOrderId());
            }
        } catch (Exception ex) {
            log.error("Failed to ingest trade book master data", ex);
            throw ex;
        }
        log.info("Trade book data processed successfully.");
    }

    private TradeBookMaster mapToEntity(AggregatedTradeBookDataDto dataDto) {
        TradeBookMaster masterData = new TradeBookMaster();
        masterData.setUserId(dataDto.getUserId());
        masterData.setExchange(dataDto.getExchange());
        masterData.setSegment(dataDto.getSegment());
        masterData.setExpiryDate(dataDto.getExpiryDate());
        masterData.setAveragePrice(dataDto.getAveragePrice());
        masterData.setOrderId(dataDto.getOrderId());
        masterData.setTradeType(dataDto.getTradeType());
        masterData.setTotalQuantity(dataDto.getTotalQuantity());
        masterData.setOrderExecutionTime(dataDto.getOrderExecutionTime());
        masterData.setSymbol(dataDto.getSymbol());
        masterData.setTradeDate(dataDto.getTradeDate());
        masterData.setVwap(dataDto.getVwap());
        // extracting option type from symbol
        masterData.setOptionType(TradeCalculationUtils.extractOptionType(dataDto.getSymbol()));
        masterData.setStatus(TradeStatus.CLOSED);
        // update processed records
        return masterData;
    }
}
