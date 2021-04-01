package com.example.demo.disruptor.transaction;

import com.lmax.disruptor.WorkHandler;

import java.util.UUID;

/**
 * @author IMI-Ron
 */
public class TradeTransactionInDbHandler implements WorkHandler<TradeTransaction> {

    @Override
    public void onEvent(TradeTransaction event) throws Exception {
        event.setId(UUID.randomUUID().toString());
        System.out.println("TradeTransactionInDBHandler thread =  " + Thread.currentThread().getName() + "  event.id = " + event.getId());

    }

}
