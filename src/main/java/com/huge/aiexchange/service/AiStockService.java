package com.huge.aiexchange.service;

import com.huge.aiexchange.constant.SystemConstants;
import com.huge.aiexchange.entity.pojo.AiPosition;
import com.huge.aiexchange.entity.vo.AiIncomeVO;
import com.huge.aiexchange.mapper.AiPositionMapper;
import com.huge.aiexchange.mapper.StockBaseMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class AiStockService {

    @Resource
    private AiPositionMapper aiPositionMapper;

    @Resource
    private StockBaseMapper stockBaseMapper;

    /**
     * 获取AI持仓信息
     * 使用平均成本法计算收益
     *
     * @param moduleId 模型ID
     * @return AI持仓信息
     */
    public AiIncomeVO getPositionInfo(Integer moduleId) {
        AiIncomeVO aiIncomeVO = new AiIncomeVO();

        // 从数据库中获取持仓数据
        List<AiPosition> positions = aiPositionMapper.selectByModelId(moduleId);

        // 计算汇总数据
        int totalPositionCount = 0;  // 持仓股票种类数
        int totalStockCount = 0;     // 总持仓股数
        BigDecimal totalAverageCost = BigDecimal.ZERO;  // 总平均成本（用于计算）
        BigDecimal totalRealizedProfit = BigDecimal.ZERO;  // 总已实现收益
        BigDecimal totalUnrealizedProfit = BigDecimal.ZERO;  // 总未实现收益

        // 转换持仓明细
        List<AiIncomeVO.StockPositionVO> positionDetails = new ArrayList<>();
        if (positions != null) {
            for (AiPosition position : positions) {
                totalPositionCount++;
                totalStockCount += position.getPosition();

                // 获取当前股票价格
                BigDecimal currentPrice = stockBaseMapper.selectClosePriceByStockCodeAndDate(
                        position.getStockCode(), SystemConstants.TODAY_MINUS_5);
                if (currentPrice == null) {
                    currentPrice = position.getAverageCost(); // 如果获取不到价格，使用平均成本
                }

                // 计算未实现收益：(当前价格 - 平均成本) * 持仓数量
                BigDecimal unrealizedProfit = BigDecimal.ZERO;
                if (position.getPosition() > 0) {
                    unrealizedProfit = currentPrice.subtract(position.getAverageCost())
                            .multiply(new BigDecimal(position.getPosition()));
                }

                // 累加已实现收益
                BigDecimal realizedProfit = position.getProfit() != null ? position.getProfit() : BigDecimal.ZERO;
                totalRealizedProfit = totalRealizedProfit.add(realizedProfit);

                // 累加未实现收益
                totalUnrealizedProfit = totalUnrealizedProfit.add(unrealizedProfit);

                // 累加平均成本（用于计算整体收益率）
                // 使用 totallyCost 字段作为总成本
                BigDecimal positionCost = position.getTotallyCost() != null ?
                        position.getTotallyCost() :
                        position.getAverageCost().multiply(new BigDecimal(position.getPosition()));
                totalAverageCost = totalAverageCost.add(positionCost);

                // 计算收益率：总收益 / 总成本
                // 总收益 = 已实现收益 + 未实现收益
                // 总成本 = totallyCost（数据库中存储的总成本）
                double returnRate = 0.0;
                BigDecimal totalProfit = realizedProfit.add(unrealizedProfit);

                if (totalProfit == null || totalProfit.compareTo(BigDecimal.ZERO) == 0) {
                    returnRate = 0.0;
                } else if (positionCost != null && positionCost.compareTo(BigDecimal.ZERO) > 0) {
                    returnRate = totalProfit
                            .divide(positionCost, 4, BigDecimal.ROUND_HALF_UP)
                            .multiply(new BigDecimal("100"))
                            .doubleValue();
                }

                // 构建持仓明细
                AiIncomeVO.StockPositionVO stockPositionVO = new AiIncomeVO.StockPositionVO();
                stockPositionVO.setStockCode(position.getStockCode());
                stockPositionVO.setStockName(position.getStockName());
                stockPositionVO.setPosition(position.getPosition());
                stockPositionVO.setAverageCost(position.getAverageCost().doubleValue());
                stockPositionVO.setCurrentPrice(currentPrice.doubleValue());
                stockPositionVO.setReturnRate(returnRate); // 收益率
                stockPositionVO.setRealizedProfit(realizedProfit.doubleValue());
                stockPositionVO.setUnrealizedProfit(unrealizedProfit.doubleValue());
                stockPositionVO.setTotalProfit(realizedProfit.add(unrealizedProfit).doubleValue());
                positionDetails.add(stockPositionVO);
            }
        }

        // 计算总收益 = 已实现收益 + 未实现收益
        BigDecimal totalProfit = totalRealizedProfit.add(totalUnrealizedProfit);

        // 设置汇总数据
        aiIncomeVO.setPositionCount(totalPositionCount);  // 持仓股票种类数
        aiIncomeVO.setStockCount(totalStockCount);        // 总持仓股数
        aiIncomeVO.setTotalCost(totalAverageCost.doubleValue()); // 总成本（平均成本 * 持仓数量）
        aiIncomeVO.setRealizedProfit(totalRealizedProfit.doubleValue()); // 总已实现收益
        aiIncomeVO.setUnrealizedProfit(totalUnrealizedProfit.doubleValue()); // 总未实现收益
        aiIncomeVO.setIncome(totalProfit.intValue());     // 总收益（取整）

        // 计算整体收益率：总收益 / 总成本
        double yieldRate = 0.0;
        if (totalAverageCost.compareTo(BigDecimal.ZERO) > 0) {
            yieldRate = totalProfit.divide(totalAverageCost, 4, BigDecimal.ROUND_HALF_UP).doubleValue();
        }
        aiIncomeVO.setYieldRate(yieldRate); // 整体收益率

        // 设置持仓明细
        aiIncomeVO.setPositionDetails(positionDetails);

        return aiIncomeVO;
    }

}
