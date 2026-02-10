package com.huge.aiexchange.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI模型持仓实体类
 * 使用平均成本法计算收益
 */
@Data
@TableName("ai_position")
public class AiPosition {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * AI模型ID
     */
    private Integer modelId;

    /**
     * 股票代码
     */
    private String stockCode;

    /**
     * 股票名称
     */
    private String stockName;

    /**
     * 持仓数量
     */
    private Integer position;

    /**
     * 平均成本（买入时更新）
     */
    private BigDecimal averageCost;

    /**
     * 已实现收益（卖出时累加）
     */
    private BigDecimal profit;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
