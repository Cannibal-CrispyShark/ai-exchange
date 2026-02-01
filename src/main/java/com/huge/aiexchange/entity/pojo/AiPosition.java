package com.huge.aiexchange.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI模型持仓实体类
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
     * 收益
     */
    private BigDecimal profit;

    /**
     * 收益率
     */
    private BigDecimal profitRate;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
