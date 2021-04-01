package com.example.demo.disruptor.longevent;

import com.lmax.disruptor.WorkHandler;

/**
 * @author IMI-Ron
 * <p>
 * LongEvent事件消息者，消息LongEvent事件
 */
public class LongEventConsumer implements WorkHandler<LongEvent> {
    @Override
    public void onEvent(LongEvent event) throws Exception {
        System.out.println("LongEventConsumer 处理事件 consumer:" + Thread.currentThread().getName() + " Event: value=" + event.getValue());
    }
}
