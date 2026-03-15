package org.tradebook.journal.features.journal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tradebook.journal.features.journal.entity.TradePlan;
import org.tradebook.journal.features.journal.repository.TradePlanRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TradePlanServiceImpl implements TradePlanService {

    private final TradePlanRepository tradePlanRepository;

    @Override
    public TradePlan save(TradePlan tradePlan) {
        return tradePlanRepository.save(tradePlan);
    }

    @Override
    public List<TradePlan> findByTradeId(Long tradeId) {
        return tradePlanRepository.findByTradeId(tradeId);
    }
}
