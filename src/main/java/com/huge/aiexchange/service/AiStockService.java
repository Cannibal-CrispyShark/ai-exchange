package com.huge.aiexchange.service;

import com.huge.aiexchange.constant.SystemConstants;
import com.huge.aiexchange.entity.pojo.AiPosition;
import com.huge.aiexchange.entity.vo.AiIncomeVO;
import com.huge.aiexchange.mapper.AiPositionMapper;
import com.huge.aiexchange.mapper.TransactionDetailMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class AiStockService {

    @Resource
    private AiPositionMapper aiPositionMapper;

    @Resource
    private TransactionDetailMapper transactionDetailMapper;

    /**
     * 获取AI持仓信息
     * @param aiCode AI代码
     * @return AI持仓信息
     */
    public AiIncomeVO getPositionInfo(Integer moduleId, String aiCode){
        // 创建AI持仓信息对象
        AiIncomeVO aiIncomeVO = new AiIncomeVO();

        // 查询ai_position表中的最新更新日期
        LocalDate latestUpdateDate = getLatestUpdateDateFromAiPosition(aiCode);

        // 如果最近5天内有更新，直接读取ai_position表
        if (latestUpdateDate != null && !latestUpdateDate.isBefore(SystemConstants.TODAY_MINUS_5)) {
            return getPositionInfoFromDb(aiCode);
        }

        // 需要重新计算：从上一次更新日期到today-5的交易收益
        LocalDate startDate = latestUpdateDate != null ? latestUpdateDate.plusDays(1) : LocalDate.MIN;
        LocalDate endDate = SystemConstants.TODAY_MINUS_5;

        // 从transaction_detail表统计收益
        recalculateAndUpdatePosition(moduleId, aiCode, startDate, endDate);

        // 重新从ai_position表读取
        return getPositionInfoFromDb(aiCode);
    }

    /**
     * 从ai_position表获取最新更新日期
     * @param aiCode AI代码
     * @return 最新更新日期
     */
    private LocalDate getLatestUpdateDateFromAiPosition(String aiCode) {
        // 查询该AI模型的所有持仓记录，获取最新的update_time
        List<AiPosition> positions = aiPositionMapper.selectByAiCode(aiCode);
        if (positions == null || positions.isEmpty()) {
            return null;
        }
        return positions.stream()
                .map(AiPosition::getUpdateTime)
                .map(java.time.LocalDateTime::toLocalDate)
                .max(LocalDate::compareTo)
                .orElse(null);
    }

    /**
     * 从ai_position表直接获取持仓信息
     * @param aiCode AI代码
     * @return AI持仓信息
     */
    private AiIncomeVO getPositionInfoFromDb(String aiCode) {
        AiIncomeVO aiIncomeVO = new AiIncomeVO();

        // 从数据库中获取数据
        Integer positionCount = aiPositionMapper.selectPositionCountByAiCode(aiCode);
        Integer profitCount = aiPositionMapper.selectProfitCountByAiCode(aiCode);
        BigDecimal totalProfit = aiPositionMapper.selectTotalProfitByAiCode(aiCode);
        List<AiPosition> positions = aiPositionMapper.selectByAiCode(aiCode);

        // 设置数据
        aiIncomeVO.setIncome(totalProfit != null ? totalProfit.intValue() : 0); // 总收益
        aiIncomeVO.setPositionCount(positionCount != null ? positionCount : 0); // 持仓数量
        aiIncomeVO.setProfitCount(profitCount != null ? profitCount : 0); // 盈利数量

        // 计算收益率：总收益 / 初始资金
        double yieldRate = totalProfit != null ? 
                totalProfit.doubleValue() / SystemConstants.INITIAL_FUND.doubleValue() : 0.0;
        aiIncomeVO.setYieldRate(yieldRate); // 收益率

        // 转换持仓明细
        List<AiIncomeVO.StockPositionVO> positionDetails = new ArrayList<>();
        if (positions != null) {
            for (AiPosition position : positions) {
                AiIncomeVO.StockPositionVO stockPositionVO = new AiIncomeVO.StockPositionVO();
                stockPositionVO.setStockCode(position.getStockCode());
                stockPositionVO.setStockName(position.getStockName());
                stockPositionVO.setPosition(position.getPosition());
                stockPositionVO.setProfit(position.getProfit().doubleValue());
                stockPositionVO.setProfitRate(position.getProfitRate().doubleValue());
                positionDetails.add(stockPositionVO);
            }
        }

        // 设置持仓明细
        aiIncomeVO.setPositionDetails(positionDetails);

        return aiIncomeVO;
    }

    /**
     * 重新计算并更新持仓信息
     * @param moduleId 模型ID
     * @param aiCode AI代码
     * @param startDate 开始日期
     * @param endDate 结束日期
     */
    private void recalculateAndUpdatePosition(Integer moduleId, String aiCode, LocalDate startDate, LocalDate endDate) {
        // 从transaction_detail表统计每支股票的收益
        List<TransactionDetailMapper.StockProfitVO> stockProfits = 
                transactionDetailMapper.selectStockProfitByModuleIdAndDateRange(moduleId, startDate, endDate);

        if (stockProfits == null || stockProfits.isEmpty()) {
            return;
        }

        // 更新或插入ai_position表
        for (TransactionDetailMapper.StockProfitVO stockProfit : stockProfits) {
            String stockCode = String.valueOf(stockProfit.getStockId());
            String stockName = stockProfit.getStockName();
            BigDecimal profit = stockProfit.getTotalProfit();
            Integer tradeCount = stockProfit.getTradeCount();

            // 查询是否已有持仓记录
            AiPosition existingPosition = aiPositionMapper.selectByAiCodeAndStockCode(aiCode, stockCode);

            if (existingPosition != null) {
                // 更新已有持仓
                BigDecimal newProfit = existingPosition.getProfit().add(profit);
                // 计算收益率：总收益 / 初始资金 * 100%
                BigDecimal newProfitRate = newProfit.divide(SystemConstants.INITIAL_FUND, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));

                existingPosition.setProfit(newProfit);
                existingPosition.setProfitRate(newProfitRate);
                // 更新持仓数量（根据交易次数估算）
                existingPosition.setPosition(existingPosition.getPosition() + tradeCount);

                aiPositionMapper.updateById(existingPosition);
            } else {
                // 创建新持仓
                AiPosition newPosition = new AiPosition();
                newPosition.setAiCode(aiCode);
                newPosition.setStockCode(stockCode);
                newPosition.setStockName(stockName);
                newPosition.setPosition(tradeCount);
                newPosition.setProfit(profit);
                // 计算收益率：总收益 / 初始资金 * 100%
                BigDecimal profitRate = profit.divide(SystemConstants.INITIAL_FUND, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                newPosition.setProfitRate(profitRate);

                aiPositionMapper.insert(newPosition);
            }
        }
    }

}
