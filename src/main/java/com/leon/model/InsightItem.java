package com.leon.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsightItem {
    private String name;
    private double orderBuy;
    private double executedBuy;
    private double orderSell;
    private double executedSell;
}


