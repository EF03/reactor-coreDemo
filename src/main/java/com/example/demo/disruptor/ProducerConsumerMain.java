package com.example.demo.disruptor;

import com.example.demo.disruptor.longevent.LongEventFactory;
import com.example.demo.disruptor.longevent.EventExceptionHandler;
import com.example.demo.disruptor.longevent.LongEvent;
import com.example.demo.disruptor.longevent.LongEventConsumer;
import com.example.demo.disruptor.longevent.LongEventProducer;
import com.example.demo.utils.ThreadPoolUtil;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.ProducerType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.nio.ByteBuffer;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author IMI-Ron
 * <p>
 * Disruptor多个消费者不重复处理生产者发送过来的消息
 */
public class ProducerConsumerMain {

    public static void main(String[] args) throws InterruptedException {
        Long time = System.currentTimeMillis();
        int produceDataNum = 999999999;
        // 指定 ring buffer字节大小，必需为2的N次方(能将求模运算转为位运算提高效率 )，否则影响性能
        int bufferSize = 1024 * 1024;
        //固定线程数
        int nThreads = 10;
        ThreadPoolTaskExecutor executor = ThreadPoolUtil.buildThreadPoolTaskExecutor(nThreads, nThreads, "event thread - ", new ThreadPoolExecutor.DiscardOldestPolicy());
        // 创建ringBuffer
        RingBuffer<LongEvent> ringBuffer = RingBuffer.create(ProducerType.MULTI, new LongEventFactory(), bufferSize, new YieldingWaitStrategy());
        SequenceBarrier barriers = ringBuffer.newBarrier();
        // 创建10个消费者来处理同一个生产者发送过来的消息(这10个消费者不重复消费消息)
        LongEventConsumer[] consumers = new LongEventConsumer[nThreads];
        for (int i = 0; i < consumers.length; i++) {
            consumers[i] = new LongEventConsumer();
        }
        WorkerPool<LongEvent> workerPool = new WorkerPool<>(ringBuffer, barriers, new EventExceptionHandler(), consumers);
        ringBuffer.addGatingSequences(workerPool.getWorkerSequences());
        workerPool.start(executor);

        LongEventProducer producer = new LongEventProducer(ringBuffer);
        ByteBuffer bb = ByteBuffer.allocate(8);
        for (int i = 0; i < produceDataNum; i++) {
            producer.produceData(i);
//            bb.putLong(0, i);
//            producer.onData(bb);
        }

        //等上1秒，等消费都处理完成
        Thread.sleep(1500);
        //通知事件(或者说消息)处理器 可以结束了（并不是马上结束!!!）
        workerPool.halt();
        executor.shutdown();
        System.out.println("总共耗时(单位毫秒) :" + (System.currentTimeMillis() - time));
    }


}
