package com.huge.aiexchange.Utility;

import com.huge.aiexchange.entity.pojo.StockBase;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class StockFeatureCalculator {

    private static Boolean isStockBaseValid(List<StockBase> stockBases, int index) {

        for (int i = index - 59; i <= index; i++) {
            if (i<0 || stockBases.get(i) == null ||
                    stockBases.get(i).close == null ||
                    stockBases.get(i).date == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * 计算指定日期的特征指标，返回StockData对象
     *
     * @param stockBases 按日期升序排列的完整历史数据列表
     * @param targetDate 需要计算特征的交易日
     * @return 包含该日期所有特征的StockData对象。如果数据不足或日期不存在，返回null。
     */
    public static StockFuture calculateFeaturesForDate(List<StockBase> stockBases, LocalDate targetDate) {
        // 1. 找到目标日期在列表中的索引
        int targetIndex = -1;
        List<LocalDate> dates = stockBases.stream().map(d -> d.date).collect(Collectors.toList());
        List<Double> closes = stockBases.stream().map(d -> d.close).collect(Collectors.toList());

        for (int i = 0; i < dates.size(); i++) {
            if (dates.get(i).equals(targetDate)) {
                targetIndex = i;
                break;
            }
        }

        // 检查数据是否完整
        if (!isStockBaseValid(stockBases, targetIndex)){
            throw new IllegalArgumentException("数据不完整");
        }

        if (targetIndex == -1) {
            System.out.println("Warning: 目标日期不在数据集中。");
            return null;
        }

        // 2. 创建结果对象，并设置基础信息
        StockFuture result = new StockFuture();
        StockBase targetDayData = stockBases.get(targetIndex);
        result.date = targetDayData.date;
        result.close = targetDayData.close;
        double currentClose = result.close;

        // 3. 计算移动平均线 (MA)
        // 3.1 计算20日MA (需要目标日前至少19天数据)
        if (targetIndex >= 19) {
            double sum20 = 0;
            for (int j = targetIndex - 19; j <= targetIndex; j++) {
                sum20 += closes.get(j);
            }
            result.ma20d = sum20 / 20.0;
        }

        // 3.2 计算60日MA (需要目标日前至少59天数据)
        if (targetIndex >= 59) {
            double sum60 = 0;
            for (int j = targetIndex - 59; j <= targetIndex; j++) {
                sum60 += closes.get(j);
            }
            result.ma60d = sum60 / 60.0;

            // 计算趋势位置: 收盘价 / 60日MA
            if (result.ma60d != 0) {
                result.trendPosition = currentClose / result.ma60d;
            }
        }

        // 4. 计算滚动收益率
        // 4.1 5日收益率
        if (targetIndex >= 5) {
            double price5DaysAgo = closes.get(targetIndex - 5);
            if (price5DaysAgo != 0) {
                result.return5d = (currentClose - price5DaysAgo) / price5DaysAgo;
            }
        }

        // 4.2 20日收益率
        if (targetIndex >= 20) {
            double price20DaysAgo = closes.get(targetIndex - 20);
            if (price20DaysAgo != 0) {
                result.return20d = (currentClose - price20DaysAgo) / price20DaysAgo;
            }
        }

        // 5. 计算20日波动率 (收益率的标准差)
        if (targetIndex >= 20) {
            List<Double> dailyReturns = new ArrayList<>();
            // 计算目标日前20个交易日的日收益率
            for (int j = targetIndex - 19; j <= targetIndex; j++) {
                if (j > 0) { // 确保有前一天数据
                    double dailyReturn = (closes.get(j) - closes.get(j - 1)) / closes.get(j - 1);
                    dailyReturns.add(dailyReturn);
                }
            }
            if (!dailyReturns.isEmpty()) {
                result.volatility20d = calculateStdDev(dailyReturns);
            }
        }

        // 6. 计算250日价格分位数
        if (targetIndex >= 250) {
            double currentPrice = currentClose;
            int lowerCount = 0;
            // 遍历目标日前250个交易日
            for (int j = targetIndex - 249; j <= targetIndex; j++) {
                if (!closes.get(j).isNaN() && closes.get(j) < currentPrice) {
                    lowerCount++;
                }
            }
            result.pricePercentile250d = (double) lowerCount / 250;
        }

        // 7. 生成20日突破信号 (判断是否创20日新高)
        if (targetIndex >= 20) {
            // 找出目标日之前20个交易日（含）的最高价
            double max20d = Double.MIN_VALUE;
            for (int j = targetIndex - 19; j <= targetIndex; j++) {
                if (closes.get(j) > max20d) {
                    max20d = closes.get(j);
                }
            }
            result.highest20d = max20d;

            // 判断当前收盘价是否高于前一日计算的20日最高价
            if (targetIndex > 20) {
                double prevMax20d = Double.MIN_VALUE;
                for (int j = targetIndex - 20; j <= targetIndex - 1; j++) {
                    if (closes.get(j) > prevMax20d) {
                        prevMax20d = closes.get(j);
                    }
                }
                result.isNew20dHigh = currentClose > prevMax20d;
            }
        }

        return result;
    }

    /**
     * 计算标准差（辅助方法）
     */
    private static double calculateStdDev(List<Double> values) {
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average().orElse(0.0);
        return Math.sqrt(variance);
    }


    // 股票数据点类（保持原有字段）
    public static class StockFuture {
        public LocalDate date;
        public double close;
        public Double ma20d;             // 20日移动平均线
        public Double ma60d;             // 60日移动平均线
        public Double trendPosition;     // 趋势位置（收盘价/60日MA）
        public Double return5d;          // 5日收益率
        public Double return20d;         // 20日收益率
        public Double volatility20d;     // 20日波动率
        public Double pricePercentile250d; // 250日价格分位数
        public Double highest20d;        // 20日内最高价
        public Boolean isNew20dHigh;     // 是否创20日新高

        // 为了方便使用，添加一个构造方法
        public StockFuture() {
        }

        public StockFuture(LocalDate date, double close) {
            this.date = date;
            this.close = close;
        }

        // 可以添加toString方法方便查看
        @Override
        public String toString() {
            return String.format(
                    "StockData{date=%s, close=%.2f, ma20d=%.2f, ma60d=%.2f, trendPos=%.3f, " +
                            "return20d=%.3f, vol20d=%.3f, percentile250d=%.3f, isNewHigh=%s}",
                    date, close,
                    ma20d != null ? ma20d : 0.0,
                    ma60d != null ? ma60d : 0.0,
                    trendPosition != null ? trendPosition : 0.0,
                    return20d != null ? return20d : 0.0,
                    volatility20d != null ? volatility20d : 0.0,
                    pricePercentile250d != null ? pricePercentile250d : 0.0,
                    isNew20dHigh != null ? isNew20dHigh : false
            );
        }
    }
}