package com.huge.aiexchange.entity.pojo;


import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AiIncome {

    class EarnInStock {
        private Double income;
        private String stockCode;
    }
    class EarnInDay{
        private Double income;
        private LocalDate day;
    }

    private Double incomeTotal;
    private List<EarnInStock> earnInStocks;
    private List<EarnInDay> earnInDays;

}
