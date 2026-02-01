package com.huge.aiexchange.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huge.aiexchange.entity.pojo.AiPosition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * AI模型持仓 Mapper 接口
 */
@Mapper
public interface AiPositionMapper extends BaseMapper<AiPosition> {

    /**
     * 根据AI模型ID和股票代码查询持仓信息
     * @param modelId AI模型ID
     * @param stockCode 股票代码
     * @return 持仓信息
     */
    @Select("SELECT * FROM ai_position WHERE modelId = #{modelId} AND stockCode = #{stockCode}")
    AiPosition selectByModelIdAndStockCode(@Param("modelId") Integer modelId, @Param("stockCode") String stockCode);

    /**
     * 根据AI模型ID查询所有持仓
     * @param modelId AI模型ID
     * @return 持仓列表
     */
    @Select("SELECT * FROM ai_position WHERE modelId = #{modelId}")
    List<AiPosition> selectByModelId(@Param("modelId") Integer modelId);

    /**
     * 根据AI模型ID查询持仓数量
     * @param modelId AI模型ID
     * @return 持仓数量
     */
    @Select("SELECT COUNT(*) FROM ai_position WHERE modelId = #{modelId}")
    Integer selectPositionCountByModelId(@Param("modelId") Integer modelId);

    /**
     * 根据AI模型ID查询盈利持仓数量
     * @param modelId AI模型ID
     * @return 盈利持仓数量
     */
    @Select("SELECT COUNT(*) FROM ai_position WHERE modelId = #{modelId} AND profit > 0")
    Integer selectProfitCountByModelId(@Param("modelId") Integer modelId);

    /**
     * 根据AI模型ID查询总收益
     * @param modelId AI模型ID
     * @return 总收益
     */
    @Select("SELECT SUM(profit) FROM ai_position WHERE modelId = #{modelId}")
    java.math.BigDecimal selectTotalProfitByModelId(@Param("modelId") Integer modelId);

}
