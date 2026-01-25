package com.huge.aiexchange.entity.pojo;

import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Data
public class StockBase {

    public LocalDate date;
    public Double close;

}
