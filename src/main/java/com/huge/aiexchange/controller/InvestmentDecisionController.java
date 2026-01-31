package com.huge.aiexchange.controller;

import com.huge.aiexchange.entity.pojo.Response;
import com.huge.aiexchange.service.AiInvestmentService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * AI投资决策Controller
 * 提供AI投资决策的API接口
 */
@RestController
@RequestMapping("/api/investment")
public class InvestmentDecisionController {

    @Resource
    private AiInvestmentService aiInvestmentService;

    /**
     * 执行AI投资决策
     * @param modelId AI模型ID
     * @return 投资决策结果
     */
    @GetMapping("/{modelId}/decide")
    public Response makeInvestmentDecision(@PathVariable Integer modelId) {
        AiInvestmentService.InvestmentDecisionResult result = 
                aiInvestmentService.makeInvestmentDecision(modelId);
        
        if (result.isSuccess()) {
            return Response.success(result.getDecisionData());
        } else {
            return Response.fail(result.getMessage());
        }
    }

}
