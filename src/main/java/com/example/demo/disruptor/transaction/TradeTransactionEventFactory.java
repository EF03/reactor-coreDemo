package com.example.demo.disruptor.transaction;

import com.lmax.disruptor.EventFactory;

/**
 * @author IMI-Ron
 */
public class TradeTransactionEventFactory implements EventFactory<TradeTransaction> {

    @Override
    public TradeTransaction newInstance()
    {
        return new TradeTransaction();
    }
}
