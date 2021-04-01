package com.example.demo.disruptor.transaction;

import lombok.Getter;
import lombok.Setter;

/**
 * @author IMI-Ron
 */
@Getter
@Setter
public class TradeTransaction {
    /**
     * 交易ID
     */
    private String id;
    /**
     * 交易金额
     */
    private double price;
}
