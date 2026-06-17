package org.tradebook.journal.features.analytics.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tradebook.journal.features.analytics.dto.*;
import org.tradebook.journal.features.analytics.repository.AnalyticsRepository;
import org.tradebook.journal.features.ingestion.entity.TradeSummary;
import org.tradebook.journal.features.journal.entity.TradePlan;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;

    private LocalDateTime startOfDay(LocalDate date) {
        return date != null ? date.atStartOfDay() : null;
    }
    
    private LocalDateTime endOfDay(LocalDate date) {
        return date != null ? date.atTime(LocalTime.MAX) : null;
    }

    public DashboardSummaryDTO getSummary(Long userId, LocalDate startDate, LocalDate endDate) {
        List<TradeSummary> trades = analyticsRepository.findTradeSummariesForAnalytics(userId, startOfDay(startDate), endOfDay(endDate));
        if (trades.isEmpty()) {
            return DashboardSummaryDTO.builder()
                    .totalTrades(0)
                    .winRate(0)
                    .totalNetPnl(BigDecimal.ZERO)
                    .profitFactor(0)
                    .averageWin(BigDecimal.ZERO)
                    .averageLoss(BigDecimal.ZERO)
                    .maxWin(BigDecimal.ZERO)
                    .maxLoss(BigDecimal.ZERO)
                    .build();
        }

        int totalTrades = trades.size();
        long winningTrades = trades.stream().filter(t -> t.getRealizedPnl() != null && t.getRealizedPnl().compareTo(BigDecimal.ZERO) > 0).count();
        double winRate = (double) winningTrades / totalTrades;

        BigDecimal totalNetPnl = trades.stream()
                .map(t -> t.getRealizedPnl() != null ? t.getRealizedPnl() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal grossProfit = trades.stream()
                .filter(t -> t.getRealizedPnl() != null && t.getRealizedPnl().compareTo(BigDecimal.ZERO) > 0)
                .map(TradeSummary::getRealizedPnl)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal grossLoss = trades.stream()
                .filter(t -> t.getRealizedPnl() != null && t.getRealizedPnl().compareTo(BigDecimal.ZERO) < 0)
                .map(TradeSummary::getRealizedPnl)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal maxWin = trades.stream()
                .filter(t -> t.getRealizedPnl() != null)
                .map(TradeSummary::getRealizedPnl)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal maxLoss = trades.stream()
                .filter(t -> t.getRealizedPnl() != null)
                .map(TradeSummary::getRealizedPnl)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        double profitFactor = 0;
        if (grossLoss.compareTo(BigDecimal.ZERO) < 0) {
            profitFactor = grossProfit.divide(grossLoss.abs(), 2, RoundingMode.HALF_UP).doubleValue();
        } else if (grossProfit.compareTo(BigDecimal.ZERO) > 0) {
            profitFactor = 999.99; // Represents infinite/all wins
        }

        long losingTrades = totalTrades - winningTrades;
        BigDecimal avgWin = winningTrades > 0 ? grossProfit.divide(BigDecimal.valueOf(winningTrades), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal avgLoss = losingTrades > 0 ? grossLoss.divide(BigDecimal.valueOf(losingTrades), 2, RoundingMode.HALF_UP).abs() : BigDecimal.ZERO;

        return DashboardSummaryDTO.builder()
                .totalTrades(totalTrades)
                .winRate(winRate * 100)
                .totalNetPnl(totalNetPnl)
                .profitFactor(profitFactor)
                .averageWin(avgWin)
                .averageLoss(avgLoss)
                .maxWin(maxWin.compareTo(BigDecimal.ZERO) > 0 ? maxWin : BigDecimal.ZERO)
                .maxLoss(maxLoss.compareTo(BigDecimal.ZERO) < 0 ? maxLoss : BigDecimal.ZERO)
                .build();
    }

    public List<ChartPointDTO> getEquityCurve(Long userId, LocalDate startDate, LocalDate endDate) {
        List<TradeSummary> trades = analyticsRepository.findTradeSummariesForAnalytics(userId, startOfDay(startDate), endOfDay(endDate));
        List<ChartPointDTO> curve = new ArrayList<>();
        BigDecimal cumulativePnL = BigDecimal.ZERO;
        
        curve.add(new ChartPointDTO(startDate.toString(), cumulativePnL));

        for (TradeSummary t : trades) {
            if (t.getRealizedPnl() != null) {
                cumulativePnL = cumulativePnL.add(t.getRealizedPnl());
            }
            String label = t.getClosedAt() != null ? t.getClosedAt().toString() : (t.getOpenedAt() != null ? t.getOpenedAt().toString() : "");
            curve.add(new ChartPointDTO(label, cumulativePnL));
        }
        
        return curve;
    }

    public List<BreakdownDTO> getAssetBreakdown(Long userId, LocalDate startDate, LocalDate endDate) {
        return calculateWinRates(analyticsRepository.getAssetBreakdown(userId, startOfDay(startDate), endOfDay(endDate)), userId, startOfDay(startDate), endOfDay(endDate), "ASSET");
    }

    public List<BreakdownDTO> getStrategyBreakdown(Long userId, LocalDate startDate, LocalDate endDate) {
        return calculateWinRates(analyticsRepository.getStrategyBreakdown(userId, startOfDay(startDate), endOfDay(endDate)), userId, startOfDay(startDate), endOfDay(endDate), "STRATEGY");
    }

    public List<BreakdownDTO> getTimeBreakdown(Long userId, LocalDate startDate, LocalDate endDate) {
        List<TradeSummary> trades = analyticsRepository.findTradeSummariesForAnalytics(userId, startOfDay(startDate), endOfDay(endDate));
        
        Map<String, List<TradeSummary>> byDayOfWeek = trades.stream()
                .filter(t -> t.getOpenedAt() != null)
                .collect(Collectors.groupingBy(t -> t.getOpenedAt().getDayOfWeek().name()));

        List<BreakdownDTO> result = new ArrayList<>();
        for (Map.Entry<String, List<TradeSummary>> entry : byDayOfWeek.entrySet()) {
            List<TradeSummary> dayTrades = entry.getValue();
            int count = dayTrades.size();
            BigDecimal netPnl = dayTrades.stream()
                    .map(t -> t.getRealizedPnl() != null ? t.getRealizedPnl() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            long wins = dayTrades.stream().filter(t -> t.getRealizedPnl() != null && t.getRealizedPnl().compareTo(BigDecimal.ZERO) > 0).count();
            double winRate = (count > 0) ? ((double) wins / count) * 100 : 0;
            
            result.add(new BreakdownDTO(entry.getKey(), netPnl, count, winRate));
        }

        List<String> daysOrder = Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY");
        result.sort(Comparator.comparingInt(b -> daysOrder.indexOf(b.getCategory())));
        
        return result;
    }

    public RiskMetricsDTO getRiskMetrics(Long userId, LocalDate startDate, LocalDate endDate) {
        List<TradePlan> plans = analyticsRepository.findTradePlansForAnalytics(userId, startOfDay(startDate), endOfDay(endDate));
        
        BigDecimal totalRisk = BigDecimal.ZERO;
        int riskCount = 0;
        double totalRMultiple = 0;
        int rMultipleCount = 0;
        
        for (TradePlan tp : plans) {
            if (tp.getRiskAmount() != null && tp.getRiskAmount().compareTo(BigDecimal.ZERO) > 0) {
                totalRisk = totalRisk.add(tp.getRiskAmount());
                riskCount++;
                
                if (tp.getTradeSummary() != null && tp.getTradeSummary().getRealizedPnl() != null) {
                    double rMulti = tp.getTradeSummary().getRealizedPnl().divide(tp.getRiskAmount(), 2, RoundingMode.HALF_UP).doubleValue();
                    totalRMultiple += rMulti;
                    rMultipleCount++;
                }
            }
        }
        
        BigDecimal avgRisk = riskCount > 0 ? totalRisk.divide(BigDecimal.valueOf(riskCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        double avgRMulti = rMultipleCount > 0 ? totalRMultiple / rMultipleCount : 0.0;
        
        List<ChartPointDTO> equityCurve = getEquityCurve(userId, startDate, endDate);
        BigDecimal maxDrawdown = BigDecimal.ZERO;
        BigDecimal peak = BigDecimal.ZERO;
        
        for (ChartPointDTO point : equityCurve) {
            BigDecimal equity = point.getValue();
            if (equity.compareTo(peak) > 0) {
                peak = equity;
            }
            BigDecimal drawdown = peak.subtract(equity);
            if (drawdown.compareTo(maxDrawdown) > 0) {
                maxDrawdown = drawdown;
            }
        }

        DashboardSummaryDTO summary = getSummary(userId, startDate, endDate);
        double winRateDec = summary.getWinRate() / 100.0;
        double lossRateDec = 1.0 - winRateDec;
        double expectancy = (winRateDec * summary.getAverageWin().doubleValue()) - (lossRateDec * summary.getAverageLoss().doubleValue());
        
        return RiskMetricsDTO.builder()
                .averageRiskAmount(avgRisk)
                .averageRMultiple(avgRMulti)
                .maxDrawdown(maxDrawdown)
                .expectancy(expectancy)
                .build();
    }

    public List<BreakdownDTO> getMistakes(Long userId, LocalDate startDate, LocalDate endDate) {
        List<TradePlan> plans = analyticsRepository.findTradePlansForAnalytics(userId, startOfDay(startDate), endOfDay(endDate));
        
        Map<String, List<TradePlan>> byMistake = new HashMap<>();
        
        for (TradePlan tp : plans) {
            if (tp.getMistakes() != null && !tp.getMistakes().trim().isEmpty()) {
                String[] mistakes = tp.getMistakes().split(",");
                for (String mistake : mistakes) {
                    String cleanMistake = mistake.trim();
                    if (!cleanMistake.isEmpty()) {
                        byMistake.computeIfAbsent(cleanMistake, k -> new ArrayList<>()).add(tp);
                    }
                }
            }
        }
        
        List<BreakdownDTO> result = new ArrayList<>();
        for (Map.Entry<String, List<TradePlan>> entry : byMistake.entrySet()) {
            List<TradePlan> mistakePlans = entry.getValue();
            int count = mistakePlans.size();
            
            BigDecimal netPnl = BigDecimal.ZERO;
            long wins = 0;
            
            for (TradePlan tp : mistakePlans) {
                if (tp.getTradeSummary() != null && tp.getTradeSummary().getRealizedPnl() != null) {
                    netPnl = netPnl.add(tp.getTradeSummary().getRealizedPnl());
                    if (tp.getTradeSummary().getRealizedPnl().compareTo(BigDecimal.ZERO) > 0) {
                        wins++;
                    }
                }
            }
            
            double winRate = (count > 0) ? ((double) wins / count) * 100 : 0;
            result.add(new BreakdownDTO(entry.getKey(), netPnl, count, winRate));
        }

        result.sort((a, b) -> Integer.compare(b.getTradeCount(), a.getTradeCount()));
        
        return result;
    }

    private List<BreakdownDTO> calculateWinRates(List<BreakdownDTO> baseList, Long userId, LocalDateTime startDate, LocalDateTime endDate, String type) {
        if (baseList.isEmpty()) return baseList;

        if (type.equals("ASSET")) {
            List<TradeSummary> trades = analyticsRepository.findTradeSummariesForAnalytics(userId, startDate, endDate);
            for (BreakdownDTO dto : baseList) {
                List<TradeSummary> bucket = trades.stream().filter(t -> t.getSegment() != null && dto.getCategory().equals(t.getSegment())).collect(Collectors.toList());
                if (!bucket.isEmpty()) {
                     long wins = bucket.stream().filter(t -> t.getRealizedPnl() != null && t.getRealizedPnl().compareTo(BigDecimal.ZERO) > 0).count();
                     dto.setWinRate(((double) wins / bucket.size()) * 100);
                }
            }
        } else if (type.equals("STRATEGY")) {
             List<TradePlan> plans = analyticsRepository.findTradePlansForAnalytics(userId, startDate, endDate);
             for (BreakdownDTO dto : baseList) {
                List<TradePlan> bucket = plans.stream().filter(p -> p.getStrategy() != null ? dto.getCategory().equals(p.getStrategy()) : dto.getCategory().equals("None")).collect(Collectors.toList());
                if (!bucket.isEmpty()) {
                     long wins = bucket.stream().filter(p -> p.getTradeSummary() != null && p.getTradeSummary().getRealizedPnl() != null && p.getTradeSummary().getRealizedPnl().compareTo(BigDecimal.ZERO) > 0).count();
                     dto.setWinRate(((double) wins / bucket.size()) * 100);
                }
             }
        }
        
        return baseList;
    }
}
