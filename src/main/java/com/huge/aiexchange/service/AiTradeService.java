package com.huge.aiexchange.service;

import com.huge.aiexchange.constant.SystemConstants;
import com.huge.aiexchange.entity.pojo.AiPosition;
import com.huge.aiexchange.entity.pojo.TransactionDetail;
import com.huge.aiexchange.mapper.AiModelInfoMapper;
import com.huge.aiexchange.mapper.AiPositionMapper;
import com.huge.aiexchange.mapper.StockBaseMapper;
import com.huge.aiexchange.mapper.TransactionDetailMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI交易服务类，实现买卖操作的业务逻辑
 * 使用平均成本法计算持仓和收益
 */
@Service
public class AiTradeService {

    @Resource
    private TransactionDetailMapper transactionDetailMapper;

    @Resource
    private StockBaseMapper stockBaseMapper;

    @Resource
    private AiModelInfoMapper aiModelInfoMapper;

    @Resource
    private AiPositionMapper aiPositionMapper;

    /**
     * 买入股票
     * @param moduleId 模型ID
     * @param modelName 模型名称
     * @param stockName 股票名称
     * @param stockCode 股票代码
     * @param amount 买入数量
     * @return 操作结果
     */
    @Transactional
    public String buyStock(Integer moduleId, String modelName, String stockName, String stockCode, int amount) {
        // 从stock_base表获取TODAY_MINUS_5日期的close价格
        BigDecimal price = stockBaseMapper.selectClosePriceByStockCodeAndDate(stockCode, SystemConstants.TODAY_MINUS_5);
        if (price == null) {
            return "无法获取股票" + stockCode + "在" + SystemConstants.TODAY_MINUS_5 + "的收盘价";
        }

        // 计算买入金额
        BigDecimal totalCost = price.multiply(new BigDecimal(amount));

        // 检查余额是否充足
        BigDecimal currentBalance = aiModelInfoMapper.selectBalanceById(moduleId);
        if (currentBalance == null || currentBalance.compareTo(totalCost) < 0) {
            return "余额不足，无法买入股票";
        }

        // 1. 创建交易记录（买入金额为负数）
        TransactionDetail transactionDetail = new TransactionDetail();
        transactionDetail.setModuleId(moduleId);
        transactionDetail.setModelName(modelName);
        transactionDetail.setStockCode(stockCode);
        transactionDetail.setStockName(stockName);
        transactionDetail.setVolume(totalCost.negate());
        transactionDetail.setPrice(price);
        transactionDetail.setDate(SystemConstants.TODAY_MINUS_5);
        
        int insertResult = transactionDetailMapper.insert(transactionDetail);
        if (insertResult <= 0) {
            return "创建交易记录失败";
        }

        // 2. 扣款操作
        int deductResult = aiModelInfoMapper.deductBalance(moduleId, totalCost);
        if (deductResult <= 0) {
            throw new RuntimeException("扣款失败");
        }

        // 3. 更新ai_position表（买入：更新平均成本）
        updateAiPositionBuy(moduleId, stockCode, stockName, amount, price);

        return "买入成功：" + stockName + "(" + stockCode + ") " + amount + "股，单价$" + price;
    }

    /**
     * 卖出股票
     * @param moduleId 模型ID
     * @param modelName 模型名称
     * @param stockName 股票名称
     * @param stockCode 股票代码
     * @param amount 卖出数量
     * @return 操作结果
     */
    @Transactional
    public String sellStock(Integer moduleId, String modelName, String stockName, String stockCode, int amount) {
        // 从stock_base表获取TODAY_MINUS_5日期的close价格
        BigDecimal price = stockBaseMapper.selectClosePriceByStockCodeAndDate(stockCode, SystemConstants.TODAY_MINUS_5);
        if (price == null) {
            return "无法获取股票" + stockCode + "在" + SystemConstants.TODAY_MINUS_5 + "的收盘价";
        }

        // 计算卖出金额
        BigDecimal totalRevenue = price.multiply(new BigDecimal(amount));

        // 1. 创建交易记录（卖出金额为正数）
        TransactionDetail transactionDetail = new TransactionDetail();
        transactionDetail.setModuleId(moduleId);
        transactionDetail.setModelName(modelName);
        transactionDetail.setStockCode(stockCode);
        transactionDetail.setStockName(stockName);
        transactionDetail.setVolume(totalRevenue);
        transactionDetail.setPrice(price);
        transactionDetail.setDate(SystemConstants.TODAY_MINUS_5);
        
        int insertResult = transactionDetailMapper.insert(transactionDetail);
        if (insertResult <= 0) {
            return "创建交易记录失败";
        }

        // 2. 增加余额（卖出增加余额，所以传入负数）
        int addResult = aiModelInfoMapper.deductBalance(moduleId, totalRevenue.negate());
        if (addResult <= 0) {
            throw new RuntimeException("增加余额失败");
        }

        // 3. 更新ai_position表（卖出：减少position，累加已实现收益）
        updateAiPositionSell(moduleId, stockCode, amount, price);

        return "卖出成功：" + stockName + "(" + stockCode + ") " + amount + "股，单价$" + price;
    }

    /**
     * 更新AI持仓表（买入操作）
     * 买入时更新平均成本：averageCost = (原持仓成本 + 新买入成本) / 总持仓数量
     * @param modelId 模型ID
     * @param stockCode 股票代码
     * @param stockName 股票名称
     * @param amount 买入数量
     * @param price 买入单价
     */
    private void updateAiPositionBuy(Integer modelId, String stockCode, String stockName, int amount, BigDecimal price) {
        // 查询现有持仓
        AiPosition existingPosition = aiPositionMapper.selectByModelIdAndStockCode(modelId, stockCode);
        
        if (existingPosition == null) {
            // 新建持仓记录，平均成本就是买入价格
            AiPosition newPosition = new AiPosition();
            newPosition.setModelId(modelId);
            newPosition.setStockCode(stockCode);
            newPosition.setStockName(stockName);
            newPosition.setPosition(amount);
            newPosition.setAverageCost(price);
            newPosition.setProfit(BigDecimal.ZERO);
            newPosition.setUpdateTime(LocalDateTime.now());
            aiPositionMapper.insert(newPosition);
        } else {
            // 更新平均成本
            // 原持仓成本 = 原持仓数量 * 原平均成本
            BigDecimal originalCost = existingPosition.getAverageCost()
                    .multiply(new BigDecimal(existingPosition.getPosition()));
            // 新买入成本 = 买入数量 * 买入价格
            BigDecimal newCost = price.multiply(new BigDecimal(amount));
            // 新总持仓数量
            int newPosition = existingPosition.getPosition() + amount;
            // 新平均成本 = (原持仓成本 + 新买入成本) / 新总持仓数量
            BigDecimal newAverageCost = originalCost.add(newCost)
                    .divide(new BigDecimal(newPosition));
            
            existingPosition.setPosition(newPosition);
            existingPosition.setAverageCost(newAverageCost);
            existingPosition.setUpdateTime(LocalDateTime.now());
            aiPositionMapper.updateById(existingPosition);
        }
    }

    /**
     * 更新AI持仓表（卖出操作）
     * 卖出时：减少position，累加已实现收益
     * 已实现收益 = (卖出价格 - 平均成本) * 卖出数量
     * @param modelId 模型ID
     * @param stockCode 股票代码
     * @param amount 卖出数量
     * @param price 卖出单价
     */
    private void updateAiPositionSell(Integer modelId, String stockCode, int amount, BigDecimal price) {
        // 查询现有持仓
        AiPosition existingPosition = aiPositionMapper.selectByModelIdAndStockCode(modelId, stockCode);
        
        if (existingPosition == null) {
            throw new RuntimeException("未持有股票" + stockCode + "，无法卖出");
        }
        
        int currentPosition = existingPosition.getPosition();
        if (currentPosition < amount) {
            throw new RuntimeException("持仓不足，当前持仓" + currentPosition + "股，尝试卖出" + amount + "股");
        }
        
        // 计算本次卖出实现的收益：(卖出价格 - 平均成本) * 卖出数量
        BigDecimal sellProfit = price.subtract(existingPosition.getAverageCost())
                .multiply(new BigDecimal(amount));
        
        // 累加已实现收益
        BigDecimal newProfit = existingPosition.getProfit().add(sellProfit);
        
        int newPosition = currentPosition - amount;
        
        // 更新持仓：减少position，累加已实现收益
        // 注意：平均成本保持不变，即使持仓为0也保留记录
        existingPosition.setPosition(newPosition);
        existingPosition.setProfit(newProfit);
        existingPosition.setUpdateTime(LocalDateTime.now());
        aiPositionMapper.updateById(existingPosition);
    }

}
