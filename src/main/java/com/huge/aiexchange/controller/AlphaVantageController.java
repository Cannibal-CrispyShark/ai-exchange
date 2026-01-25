package com.huge.aiexchange.controller;


import com.huge.aiexchange.entity.pojo.Response;
import com.huge.aiexchange.entity.pojo.StockBase;
import com.huge.aiexchange.entity.vo.StockInfoVO;
import com.huge.aiexchange.service.AlphaVantageService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stocks")
public class AlphaVantageController {

    @Resource
    private AlphaVantageService alphaVantageService;


    @GetMapping("/{stockCode}/get")
    public Response<StockInfoVO> getStockBase(@PathVariable String stockCode){
        return alphaVantageService.getBaseByAlpha(stockCode);
    }

    @GetMapping("/test")
    public Response<String> test(){
        return Response.success("hello world");
    }


}
