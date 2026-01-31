package com.huge.aiexchange.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huge.aiexchange.entity.pojo.StockFuture;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 股票特征数据 Mapper 接口
 */
@Mapper
public interface StockFutureMapper extends BaseMapper<StockFuture> {

    /**
     * 根据股票代码和日期查询特征数据
     * @param stockCode 股票代码
     * @param date 日期
     * @return 特征数据
     */
    @Select("SELECT * FROM stock_future WHERE stockCode = #{stockCode} AND date = #{date}")
    StockFuture selectByStockCodeAndDate(@Param("stockCode") String stockCode, @Param("date") LocalDate date);

    /**
     * 根据股票代码查询所有特征数据
     * @param stockCode 股票代码
     * @return 特征数据列表
     */
    @Select("SELECT * FROM stock_future WHERE stockCode = #{stockCode} ORDER BY date DESC")
    List<StockFuture> selectByStockCode(@Param("stockCode") String stockCode);

    /**
     * 根据股票代码查询最近N天的特征数据
     * @param stockCode 股票代码
     * @param limit 限制条数
     * @return 特征数据列表
     */
    @Select("SELECT * FROM stock_future WHERE stockCode = #{stockCode} ORDER BY date DESC LIMIT #{limit}")
    List<StockFuture> selectRecentByStockCode(@Param("stockCode") String stockCode, @Param("limit") int limit);

}
