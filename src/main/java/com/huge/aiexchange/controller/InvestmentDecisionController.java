package com.huge.aiexchange.controller;

import com.huge.aiexchange.entity.pojo.Response;
import com.huge.aiexchange.service.AiInvestmentService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @PostMapping("/{modelId}/decide")
    public Response makeInvestmentDecision(@PathVariable Integer modelId) {
        try {
            AiInvestmentService.InvestmentDecisionResult result = 
                    aiInvestmentService.makeInvestmentDecision(modelId);
            return Response.success(result);
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }
    }

}
