package com.huge.aiexchange.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huge.aiexchange.entity.pojo.AiModelInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

/**
 * AI模型信息 Mapper 接口
 */
@Mapper
public interface AiModelInfoMapper extends BaseMapper<AiModelInfo> {

    /**
     * 根据模型名称查询模型信息
     * @param modelName 模型名称
     * @return 模型信息
     */
    @Select("SELECT * FROM ai_model_info WHERE modelName = #{modelName}")
    AiModelInfo selectByModelName(@Param("modelName") String modelName);

    /**
     * 扣款操作
     * @param modelId 模型ID
     * @param amount 扣款金额
     * @return 更新行数
     */
    @Update("UPDATE ai_model_info SET deposit = deposit - #{amount} WHERE id = #{modelId} AND deposit >= #{amount}")
    int deductBalance(@Param("modelId") Integer modelId, @Param("amount") BigDecimal amount);

    /**
     * 查询模型余额
     * @param modelId 模型ID
     * @return 余额
     */
    @Select("SELECT deposit FROM ai_model_info WHERE id = #{modelId}")
    BigDecimal selectBalanceById(@Param("modelId") Integer modelId);

}
