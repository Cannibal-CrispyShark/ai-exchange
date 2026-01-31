-- 股票特征数据表
-- 对应 StockFeatureCalculator.StockFuture 类
CREATE TABLE stockFuture (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID' PRIMARY KEY,
    stockCode VARCHAR(20) NOT NULL COMMENT '股票代码',
    date DATE NOT NULL COMMENT '日期',
    close DECIMAL(10, 4) NOT NULL COMMENT '收盘价',
    ma20d DECIMAL(10, 4) NULL COMMENT '20日移动平均线',
    ma60d DECIMAL(10, 4) NULL COMMENT '60日移动平均线',
    trendPosition DECIMAL(10, 6) NULL COMMENT '趋势位置（收盘价/60日MA）',
    return5d DECIMAL(10, 6) NULL COMMENT '5日收益率',
    return20d DECIMAL(10, 6) NULL COMMENT '20日收益率',
    volatility20d DECIMAL(10, 6) NULL COMMENT '20日波动率',
    pricePercentile250d DECIMAL(10, 6) NULL COMMENT '250日价格分位数',
    pricePercentile100d DECIMAL(10, 6) NULL COMMENT '100日价格分位数',
    highest20d DECIMAL(10, 4) NULL COMMENT '20日内最高价',
    isNew20dHigh TINYINT(1) NULL COMMENT '是否创20日新高 (1:是, 0:否)',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 唯一索引：股票代码+日期
    UNIQUE KEY ukStockDate (stockCode, date),
    -- 普通索引
    INDEX idxDate (date),
    INDEX idxStockCode (stockCode)
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
COMMENT='股票特征数据表';
