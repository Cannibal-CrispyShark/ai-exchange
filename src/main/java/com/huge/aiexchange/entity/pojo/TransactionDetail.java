package com.huge.aiexchange.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 交易记录表实体类
 */
@Data
@TableName("transaction_detail")
public class TransactionDetail {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 模型id
     */
    private Integer moduleId;

    /**
     * 模型名字
     */
    private String modelName;

    /**
     * 股票
     */
    private String stockCode;

    /**
     * 股票名字
     */
    private String stockName;

    /**
     * 交易量（美元），正为卖出，负为买入
     */
    private BigDecimal volume;

    /**
     * 交易日期，YYYY-MM-DD格式
     */
    private LocalDate date;


    /**
     *  交易当天价格
     */
    private BigDecimal price;
}
