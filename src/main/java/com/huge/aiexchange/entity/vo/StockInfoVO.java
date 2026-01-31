package com.huge.aiexchange.entity.vo;


import com.crazzyghost.alphavantage.timeseries.response.MetaData;
import com.huge.aiexchange.entity.pojo.StockBase;
import com.huge.aiexchange.entity.pojo.StockFuture;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class StockInfoVO {

    public List<StockBase> stockBase;
    public StockFuture stockFuture;
    public MetaData metaData;

}
