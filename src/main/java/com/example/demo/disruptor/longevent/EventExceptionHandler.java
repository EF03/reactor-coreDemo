package com.example.demo.disruptor.longevent;

import com.lmax.disruptor.ExceptionHandler;

/**
 * @author IMI-Ron
 */
public class EventExceptionHandler implements ExceptionHandler<LongEvent> {

    @Override
    public void handleEventException(Throwable ex, long sequence, LongEvent event) {
        System.out.println("handleEventException：" + ex);
    }

    @Override
    public void handleOnShutdownException(Throwable ex) {
        System.out.println("handleEventException：" + ex);
    }

    @Override
    public void handleOnStartException(Throwable ex) {
        System.out.println("handleOnStartException：" + ex);
    }
}
