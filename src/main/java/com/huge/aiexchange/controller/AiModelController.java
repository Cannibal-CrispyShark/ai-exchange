package com.huge.aiexchange.controller;

import com.huge.aiexchange.entity.pojo.AiModelInfo;
import com.huge.aiexchange.entity.pojo.Response;
import com.huge.aiexchange.mapper.AiModelInfoMapper;
import com.huge.aiexchange.service.inter.AiTradeAssistant;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * AI模型管理Controller
 */
@RestController
@RequestMapping("/api/ai-model")
public class AiModelController {

    @Resource
    private AiModelInfoMapper aiModelInfoMapper;

    @Resource
    private AiTradeAssistant aiTradeAssistant;

    /**
     * 创建AI模型
     * @param modelName 模型名称
     * @param temperature 随机度
     * @param deposit 初始余额
     * @return 创建的模型信息
     */
    @PostMapping("/create")
    public Response createAiModel(@RequestParam String modelName,
                                   @RequestParam(defaultValue = "0.7") Float temperature,
                                   @RequestParam(defaultValue = "10000") BigDecimal deposit) {
        try {
            // 检查模型名称是否已存在
            AiModelInfo existingModel = aiModelInfoMapper.selectByModelName(modelName);
            if (existingModel != null) {
                return Response.fail("模型名称已存在");
            }

            // 创建新模型
            AiModelInfo aiModelInfo = new AiModelInfo();
            aiModelInfo.setModelName(modelName);
            aiModelInfo.setTemperature(temperature);
            aiModelInfo.setDeposit(deposit);

            aiModelInfoMapper.insert(aiModelInfo);

            return Response.success(aiModelInfo);
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 获取AI模型信息
     * @param modelId 模型ID
     * @return 模型信息
     */
     @GetMapping("/{modelId}")
    public Response getAiModel(@PathVariable Integer modelId) {
        try {
            AiModelInfo aiModelInfo = aiModelInfoMapper.selectById(modelId);
            if (aiModelInfo == null) {
                return Response.fail("模型不存在");
            }
            return Response.success(aiModelInfo);
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }
    }

    /**
     * AI交易对话
     * @param modelId 模型ID
     * @param message 用户消息
     * @return AI回答
     */
    @PostMapping("/{modelId}/chat")
    public Response chatWithAi(@PathVariable Integer modelId, @RequestBody String message) {
        try {
            // 检查模型是否存在
            AiModelInfo aiModelInfo = aiModelInfoMapper.selectById(modelId);
            if (aiModelInfo == null) {
                return Response.fail("模型不存在");
            }

            // 调用AI进行交易对话
            String answer = aiTradeAssistant.getAnswer(modelId, message);

            return Response.success(answer);
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 为AI模型充值
     * @param modelId 模型ID
     * @param amount 充值金额
     * @return 充值结果
     */
    @PostMapping("/{modelId}/recharge")
    public Response recharge(@PathVariable Integer modelId, @RequestParam BigDecimal amount) {
        try {
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return Response.fail("充值金额必须大于0");
            }

            // 这里可以实现充值逻辑，更新deposit字段
            // 暂时返回成功
            return Response.success("充值成功");
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }
    }

}
