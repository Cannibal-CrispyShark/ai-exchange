package com.huge.aiexchange.service;

import com.crazzyghost.alphavantage.AlphaVantage;
import com.crazzyghost.alphavantage.AlphaVantageException;
import com.crazzyghost.alphavantage.parameters.OutputSize;
import com.crazzyghost.alphavantage.timeseries.response.StockUnit;
import com.crazzyghost.alphavantage.timeseries.response.TimeSeriesResponse;
import com.huge.aiexchange.Utility.StockFeatureCalculator;
import com.huge.aiexchange.entity.pojo.Response;
import com.huge.aiexchange.entity.pojo.StockBase;
import com.huge.aiexchange.entity.vo.StockInfoVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.Thread.sleep;

@Service
public class AlphaVantageService {

    public Response<StockInfoVO> getBaseByAlpha(String stockCode) {

        List<StockBase> stockBases;
        StockFeatureCalculator.StockFuture stockFuture;

        //todo 先从db获取，如果没有再从api获取
        //从api获取
        TimeSeriesResponse response = AlphaVantage.api()
                .timeSeries()
                .daily()
                .forSymbol(stockCode)
                .outputSize(OutputSize.COMPACT)
                .fetchSync();

        if (response.getErrorMessage() != null) {
            return Response.fail(response.getErrorMessage());
        }

        stockBases = StockUnits2StockBase(response.getStockUnits());
        Collections.reverse(stockBases);
        stockFuture = StockFeatureCalculator.calculateFeaturesForDate(stockBases, LocalDate.now().minusDays(10));

        //todo 存base进db，feature进redis


        return Response.success(new StockInfoVO(stockBases,stockFuture,response.getMetaData()));

    }

    public List<StockBase> StockUnits2StockBase(List<StockUnit> stockUnits) {
        List<StockBase> stockBases = new ArrayList<>();
        for (StockUnit stockUnit : stockUnits) {
            StockBase stockBase = new StockBase();
            stockBase.setDate(LocalDate.parse(stockUnit.getDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            stockBase.setClose(stockUnit.getClose());
            stockBases.add(stockBase);
        }
        return stockBases;
    }

    public static void handleSuccess(TimeSeriesResponse response) {
        // 修复：更正变量名并添加简单的打印逻辑替代plotGraph
        List<StockUnit> stockUnits = response.getStockUnits();
        System.out.println("Retrieved " + stockUnits.size() + " stock units");
        // 如果需要绘图功能，请添加相应的绘图库依赖和实现
        for (StockUnit stockUnit : stockUnits) {
            System.out.println("Date: " + stockUnit.getDate());
            System.out.println("Open: " + stockUnit.getOpen());
            System.out.println("High: " + stockUnit.getHigh());
            System.out.println("Low: " + stockUnit.getLow());
            System.out.println("Close: " + stockUnit.getClose());
            System.out.println("Adj Close: " + stockUnit.getAdjustedClose());
            System.out.println("Volume: " + stockUnit.getVolume());
            System.out.println("Dividend Amount: " + stockUnit.getDividendAmount());
            System.out.println("Split Coefficient: " + stockUnit.getSplitCoefficient());
            System.out.println();
        }
    }

    public static void handleFailure(AlphaVantageException error) {
        System.out.println("API request failed: " + error.getMessage());
    }
}
