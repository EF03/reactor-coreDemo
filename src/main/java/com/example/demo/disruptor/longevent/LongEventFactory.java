package com.example.demo.disruptor.longevent;

import com.lmax.disruptor.EventFactory;

/**
 * @author IMI-Ron
 */
public class LongEventFactory implements EventFactory<LongEvent> {

    @Override
    public LongEvent newInstance()
    {
        return new LongEvent();
    }
}
