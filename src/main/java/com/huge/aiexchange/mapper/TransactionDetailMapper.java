package com.huge.aiexchange.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huge.aiexchange.entity.pojo.TransactionDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 交易记录 Mapper 接口
 */
@Mapper
public interface TransactionDetailMapper extends BaseMapper<TransactionDetail> {

    /**
     * 查询AI模型的所有交易记录
     * @param moduleId 模型ID
     * @return 交易记录列表
     */
    @Select("SELECT * FROM transaction_detail WHERE moduleId = #{moduleId} ORDER BY date DESC")
    List<TransactionDetail> selectByModuleId(@Param("moduleId") Integer moduleId);

    /**
     * 查询AI模型在指定日期之后的交易记录
     * @param moduleId 模型ID
     * @param startDate 开始日期
     * @return 交易记录列表
     */
    @Select("SELECT * FROM transaction_detail WHERE moduleId = #{moduleId} AND date >= #{startDate} ORDER BY date DESC")
    List<TransactionDetail> selectByModuleIdAndDateAfter(@Param("moduleId") Integer moduleId, @Param("startDate") LocalDate startDate);

    /**
     * 查询AI模型在指定日期范围的交易记录
     * @param moduleId 模型ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 交易记录列表
     */
    @Select("SELECT * FROM transaction_detail WHERE moduleId = #{moduleId} AND date >= #{startDate} AND date <= #{endDate} ORDER BY date DESC")
    List<TransactionDetail> selectByModuleIdAndDateRange(@Param("moduleId") Integer moduleId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 查询AI模型的最新交易日期
     * @param moduleId 模型ID
     * @return 最新交易日期
     */
    @Select("SELECT MAX(date) FROM transaction_detail WHERE moduleId = #{moduleId}")
    LocalDate selectLatestDateByModuleId(@Param("moduleId") Integer moduleId);

    /**
     * 统计AI模型在指定日期范围的总收益
     * @param moduleId 模型ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 总收益
     */
    @Select("SELECT SUM(volume) FROM transaction_detail WHERE moduleId = #{moduleId} AND date >= #{startDate} AND date <= #{endDate}")
    BigDecimal selectTotalProfitByModuleIdAndDateRange(@Param("moduleId") Integer moduleId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 统计AI模型在指定日期范围每支股票的收益
     * @param moduleId 模型ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 股票收益列表
     */
    @Select("SELECT stockId, stockName, SUM(volume) as totalProfit, COUNT(*) as tradeCount " +
            "FROM transaction_detail WHERE moduleId = #{moduleId} AND date >= #{startDate} AND date <= #{endDate} " +
            "GROUP BY stockId, stockName")
    List<StockProfitVO> selectStockProfitByModuleIdAndDateRange(@Param("moduleId") Integer moduleId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 股票收益VO
     */
    class StockProfitVO {
        private Integer stockId;
        private String stockName;
        private BigDecimal totalProfit;
        private Integer tradeCount;

        public Integer getStockId() { return stockId; }
        public void setStockId(Integer stockId) { this.stockId = stockId; }
        public String getStockName() { return stockName; }
        public void setStockName(String stockName) { this.stockName = stockName; }
        public BigDecimal getTotalProfit() { return totalProfit; }
        public void setTotalProfit(BigDecimal totalProfit) { this.totalProfit = totalProfit; }
        public Integer getTradeCount() { return tradeCount; }
        public void setTradeCount(Integer tradeCount) { this.tradeCount = tradeCount; }
    }

}
