package com.huge.aiexchange.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * AI模型信息实体类
 * 对应ai_model_info表
 */
@Data
@TableName("ai_model_info")
public class AiModelInfo {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 随机度
     */
    private Float temperature;

    /**
     * 余额
     */
    private BigDecimal deposit;

}
