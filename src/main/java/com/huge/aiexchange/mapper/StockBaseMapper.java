package com.huge.aiexchange.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huge.aiexchange.entity.pojo.StockBase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 股票基础数据 Mapper 接口
 */
@Mapper
public interface StockBaseMapper extends BaseMapper<StockBase> {

    /**
     * 查询股票在指定日期之后的数据
     * @param stockCode 股票代码
     * @param startDate 开始日期
     * @return 股票数据列表
     */
    @Select("SELECT * FROM stock_base WHERE stockCode = #{stockCode} AND time >= #{startDate} ORDER BY time DESC")
    List<StockBase> selectByStockCodeAndDateAfter(@Param("stockCode") String stockCode, @Param("startDate") LocalDate startDate);

    /**
     * 查询股票最近N天的数据
     * @param stockCode 股票代码
     * @param days 天数
     * @return 股票数据列表
     */
    @Select("SELECT * FROM stock_base WHERE stockCode = #{stockCode} AND time >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) ORDER BY time DESC")
    List<StockBase> selectRecentDaysData(@Param("stockCode") String stockCode, @Param("days") int days);

    /**
     * 查询股票最新数据日期
     * @param stockCode 股票代码
     * @return 最新数据日期
     */
    @Select("SELECT MAX(time) FROM stock_base WHERE stockCode = #{stockCode}")
    LocalDate selectLatestDateByStockCode(@Param("stockCode") String stockCode);

    /**
     * 批量插入股票数据（忽略重复）
     * @param stockBases 股票数据列表
     * @return 插入数量
     */
    int batchInsert(@Param("list") List<StockBase> stockBases);

    /**
     * 查询股票所有数据
     * @param stockCode 股票代码
     * @return 股票数据列表
     */
    @Select("SELECT * FROM stock_base WHERE stockCode = #{stockCode} ORDER BY time DESC")
    List<StockBase> selectAllByStockCode(@Param("stockCode") String stockCode);

    /**
     * 查询指定日期股票的收盘价
     * @param stockCode 股票代码
     * @param date 日期
     * @return 收盘价
     */
    @Select("SELECT close FROM stock_base WHERE stockCode = #{stockCode} AND time = #{date}")
    java.math.BigDecimal selectClosePriceByStockCodeAndDate(@Param("stockCode") String stockCode, @Param("date") LocalDate date);

}
