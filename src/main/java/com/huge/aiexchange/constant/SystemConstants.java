package com.huge.aiexchange.constant;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 系统常量类
 */
public class SystemConstants {

    /**
     * 今天往前推5天的日期
     * 用于判断数据是否需要重新计算
     */
    public static LocalDate TODAY_MINUS_5 = LocalDate.now().minusDays(5);

    /**
     * 初始资金
     * 用于计算收益率
     */
    public static final BigDecimal INITIAL_FUND = new BigDecimal("100000");

}
