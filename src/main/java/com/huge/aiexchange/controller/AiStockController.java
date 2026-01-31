package com.huge.aiexchange.controller;

import com.huge.aiexchange.entity.pojo.AiIncome;
import com.huge.aiexchange.entity.pojo.Response;
import com.huge.aiexchange.entity.vo.AiIncomeVO;
import com.huge.aiexchange.service.AiStockService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/ai")
public class AiStockController {

    @Resource
    AiStockService aiStockService;


    @GetMapping("/{moduleId}/{aiCode}/position")
    public Response getPositionInfo(@PathVariable("moduleId") Integer moduleId, @PathVariable("aiCode") String aiCode){
        try{
            return Response.success(aiStockService.getPositionInfo(moduleId, aiCode));
        }catch (Exception e){
            return Response.fail(e.getMessage());
        }
    }

}
