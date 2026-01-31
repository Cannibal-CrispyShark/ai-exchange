package com.huge.aiexchange.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 股票特征数据实体类
 * 对应stockFuture表
 */
@Data
@TableName("stock_future")
public class StockFuture {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 股票代码
     */
    private String stockCode;

    /**
     * 日期
     */
    private LocalDate date;

    /**
     * 收盘价
     */
    private BigDecimal close;

    /**
     * 20日移动平均线
     */
    private BigDecimal ma20d;

    /**
     * 60日移动平均线
     */
    private BigDecimal ma60d;

    /**
     * 趋势位置（收盘价/60日MA）
     */
    private BigDecimal trendPosition;

    /**
     * 5日收益率
     */
    private BigDecimal return5d;

    /**
     * 20日收益率
     */
    private BigDecimal return20d;

    /**
     * 20日波动率
     */
    private BigDecimal volatility20d;

    /**
     * 250日价格分位数
     */
    private BigDecimal pricePercentile250d;

    /**
     * 100日价格分位数
     */
    private BigDecimal pricePercentile100d;

    /**
     * 20日内最高价
     */
    private BigDecimal highest20d;

    /**
     * 是否创20日新高 (1:是, 0:否)
     */
    private Boolean isNew20dHigh;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
