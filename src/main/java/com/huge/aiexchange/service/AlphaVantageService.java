package com.huge.aiexchange.service;

import com.crazzyghost.alphavantage.AlphaVantage;
import com.crazzyghost.alphavantage.AlphaVantageException;
import com.crazzyghost.alphavantage.parameters.OutputSize;
import com.crazzyghost.alphavantage.timeseries.response.StockUnit;
import com.crazzyghost.alphavantage.timeseries.response.TimeSeriesResponse;
import com.huge.aiexchange.Utility.StockFeatureCalculator;
import com.huge.aiexchange.constant.SystemConstants;
import com.huge.aiexchange.entity.pojo.Response;
import com.huge.aiexchange.entity.pojo.StockBase;
import com.huge.aiexchange.entity.vo.StockInfoVO;
import com.huge.aiexchange.mapper.StockBaseMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.Thread.sleep;

@Service
public class AlphaVantageService {

    @Resource
    private StockBaseMapper stockBaseMapper;

    public Response<StockInfoVO> getBaseByAlpha(String stockCode) {

        List<StockBase> stockBases;
        StockFeatureCalculator.StockFuture stockFuture;
        
        // 先从DB获取最近5天的数据，用于判断是否有最新数据
        List<StockBase> recentData = stockBaseMapper.selectByStockCodeAndDateAfter(stockCode, SystemConstants.TODAY_MINUS_5);
        
        // 如果DB中有最近5天的数据，拉取该股票的所有历史数据
        if (recentData != null && !recentData.isEmpty()) {
            stockBases = stockBaseMapper.selectAllByStockCode(stockCode);
            Collections.reverse(stockBases);
            stockFuture = StockFeatureCalculator.calculateFeaturesForDate(stockBases, LocalDate.now().minusDays(10));
            return Response.success(new StockInfoVO(stockBases, stockFuture, null));
        }

        // DB中没有最近5天的数据，从API获取
        TimeSeriesResponse response = AlphaVantage.api()
                .timeSeries()
                .daily()
                .forSymbol(stockCode)
                .outputSize(OutputSize.COMPACT)
                .fetchSync();

        if (response.getErrorMessage() != null) {
            return Response.fail(response.getErrorMessage());
        }

        stockBases = StockUnits2StockBase(response.getStockUnits(), stockCode);
        Collections.reverse(stockBases);
        stockFuture = StockFeatureCalculator.calculateFeaturesForDate(stockBases, LocalDate.now().minusDays(10));

        // 保存数据到DB
        saveStockBasesToDb(stockBases);

        return Response.success(new StockInfoVO(stockBases, stockFuture, response.getMetaData()));

    }

    /**
     * 保存股票数据到数据库
     * @param stockBases 股票数据列表
     */
    private void saveStockBasesToDb(List<StockBase> stockBases) {
        if (stockBases == null || stockBases.isEmpty()) {
            return;
        }
        
        // 批量插入数据，忽略重复（使用一个SQL语句）
        try {
            stockBaseMapper.batchInsert(stockBases);
        } catch (Exception e) {
            // 记录日志，但不影响主流程
            System.err.println("保存股票数据到DB失败: " + e.getMessage());
        }
    }

    public List<StockBase> StockUnits2StockBase(List<StockUnit> stockUnits, String stockCode) {
        List<StockBase> stockBases = new ArrayList<>();
        for (StockUnit stockUnit : stockUnits) {
            StockBase stockBase = new StockBase();
            stockBase.setStockCode(stockCode);
            stockBase.setTime(LocalDate.parse(stockUnit.getDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            stockBase.setClose(new java.math.BigDecimal(stockUnit.getClose()));
            stockBase.setOpen(new java.math.BigDecimal(stockUnit.getOpen()));
            stockBase.setHigh(new java.math.BigDecimal(stockUnit.getHigh()));
            stockBase.setLow(new java.math.BigDecimal(stockUnit.getLow()));
            stockBase.setVolume(stockUnit.getVolume());
            stockBases.add(stockBase);
        }
        return stockBases;
    }

}
