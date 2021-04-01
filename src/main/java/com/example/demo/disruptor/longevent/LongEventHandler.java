package com.example.demo.disruptor.longevent;

import com.lmax.disruptor.EventHandler;

/**
 * @author IMI-Ron
 */
public class LongEventHandler implements EventHandler<LongEvent> {
    @Override
    public void onEvent(LongEvent event, long sequence, boolean endOfBatch) {
        System.out.println("Thread = " + Thread.currentThread().getName() + " LongEventHandler Event 处理事件: " + event + " event getValue = " + event.getValue());


        // Failing to call clear here will result in the
        // object associated with the event to live until
        // it is overwritten once the ring buffer has wrapped
        // around to the beginning.
        event.clear();
    }
}
