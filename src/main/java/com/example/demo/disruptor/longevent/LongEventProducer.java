package com.example.demo.disruptor.longevent;

import com.lmax.disruptor.RingBuffer;

import java.nio.ByteBuffer;

/**
 * @author IMI-Ron
 * <p>
 * LongEvent事件生产者，生产LongEvent事件
 */
public class LongEventProducer {
    private final RingBuffer<LongEvent> ringBuffer;

    public LongEventProducer(RingBuffer<LongEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    public void onData(ByteBuffer bb) {
        // Grab the next sequence 获得下一个Event槽的下标
        long sequence = ringBuffer.next();
        try {
            // Get the entry in the Disruptor 给Event填充数据
            LongEvent event = ringBuffer.get(sequence);
            // for the sequence  // Fill with data
            event.set(bb.getLong(0));
//            System.out.println("LongEventProducer 生产任务传递事件消费者 thread = " + Thread.currentThread().getName());
        } finally {
            // 发布Event，激活观察者去消费， 将sequence传递给该消费者
            // 注意，最后的 ringBuffer.publish() 方法必须包含在 finally 中以确保必须得到调用；如果某个请求的 sequence 未被提交，将会堵塞后续的发布操作或者其它的 producer。
            ringBuffer.publish(sequence);
        }
    }

    public void produceData(long value) {
        long sequence = ringBuffer.next(); // 获得下一个Event槽的下标
        try {
            // 给Event填充数据
            LongEvent event = ringBuffer.get(sequence);
            event.set(value);
//            System.out.println("LongEventProducer 生产任务传递事件消费者 thread = " + Thread.currentThread().getName());

        } finally {
            // 发布Event，激活观察者去消费， 将sequence传递给该消费者
            // 注意，最后的 ringBuffer.publish() 方法必须包含在 finally 中以确保必须得到调用；如果某个请求的 sequence 未被提交，将会堵塞后续的发布操作或者其它的 producer。
            ringBuffer.publish(sequence);
        }
    }
}
