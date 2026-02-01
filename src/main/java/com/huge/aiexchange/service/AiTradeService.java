package com.huge.aiexchange.service;

import com.huge.aiexchange.constant.SystemConstants;
import com.huge.aiexchange.entity.pojo.TransactionDetail;
import com.huge.aiexchange.mapper.AiModelInfoMapper;
import com.huge.aiexchange.mapper.StockBaseMapper;
import com.huge.aiexchange.mapper.TransactionDetailMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * AI交易服务类，实现买卖操作的业务逻辑
 * 买入卖出操作记录到transaction_detail表，并在ai_model_info表中扣款
 */
@Service
public class AiTradeService {

    @Resource
    private TransactionDetailMapper transactionDetailMapper;

    @Resource
    private StockBaseMapper stockBaseMapper;

    @Resource
    private AiModelInfoMapper aiModelInfoMapper;

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
    public boolean buyStock(Integer moduleId, String modelName, String stockName, String stockCode, int amount) {
        // 从stock_base表获取TODAY_MINUS_5日期的close价格
        BigDecimal price = stockBaseMapper.selectClosePriceByStockCodeAndDate(stockCode, SystemConstants.TODAY_MINUS_5);
        if (price == null) {
            throw new RuntimeException("无法获取股票" + stockCode + "在" + SystemConstants.TODAY_MINUS_5 + "的收盘价");
        }

        // 计算买入金额（负数表示买入）
        BigDecimal volume = price.multiply(new BigDecimal(amount)).negate();

        // 检查余额是否充足（买入需要扣款，所以检查余额 >= 买入金额）
        BigDecimal currentBalance = aiModelInfoMapper.selectBalanceById(moduleId);
        if (currentBalance == null || currentBalance.compareTo(price.multiply(new BigDecimal(amount))) < 0) {
            throw new RuntimeException("余额不足，无法买入股票");
        }

        // 扣款操作
        int deductResult = aiModelInfoMapper.deductBalance(moduleId, price.multiply(new BigDecimal(amount)));
        if (deductResult <= 0) {
            throw new RuntimeException("扣款失败，余额不足或模型不存在");
        }

        // 创建交易记录
        TransactionDetail transactionDetail = new TransactionDetail();
        transactionDetail.setModuleId(moduleId);
        transactionDetail.setModelName(modelName);
        transactionDetail.setStockCode(stockCode);
        transactionDetail.setStockName(stockName);
        transactionDetail.setVolume(volume);
        transactionDetail.setPrice(price);
        transactionDetail.setDate(SystemConstants.TODAY_MINUS_5);

        return transactionDetailMapper.insert(transactionDetail) > 0;
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
    public boolean sellStock(Integer moduleId, String modelName, String stockName, String stockCode, int amount) {
        // 从stock_base表获取TODAY_MINUS_5日期的close价格
        BigDecimal price = stockBaseMapper.selectClosePriceByStockCodeAndDate(stockCode, SystemConstants.TODAY_MINUS_5);
        if (price == null) {
            throw new RuntimeException("无法获取股票" + stockCode + "在" + SystemConstants.TODAY_MINUS_5 + "的收盘价");
        }

        // 计算卖出金额（正数表示卖出）
        BigDecimal volume = price.multiply(new BigDecimal(amount));

        // 创建交易记录
        TransactionDetail transactionDetail = new TransactionDetail();
        transactionDetail.setModuleId(moduleId);
        transactionDetail.setModelName(modelName);
        transactionDetail.setStockCode(stockCode);
        transactionDetail.setStockName(stockName);
        transactionDetail.setVolume(volume);
        transactionDetail.setPrice(price);
        transactionDetail.setDate(SystemConstants.TODAY_MINUS_5);

        // 卖出增加余额
        int addResult = aiModelInfoMapper.deductBalance(moduleId, volume.negate());
        if (addResult <= 0) {
            throw new RuntimeException("增加余额失败");
        }

        return transactionDetailMapper.insert(transactionDetail) > 0;
    }

}
