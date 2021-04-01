package com.example.demo.disruptor;

import com.example.demo.disruptor.transaction.TradeTransactionEventFactory;
import com.example.demo.disruptor.transaction.TradeTransaction;
import com.example.demo.disruptor.transaction.TradeTransactionInDbHandler;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.ProducerType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.nio.ByteBuffer;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author IMI-Ron
 */
public class LongEventMainJava8 {
    public static void main(String[] args) throws InterruptedException {
        // Specify the size of the ring buffer, must be power of 2.
        int bufferSize = 1024;

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数：线程池创建时候初始化的线程数
        executor.setCorePoolSize(10);
        // 最大线程数：线程池最大的线程数，只有在缓冲队列满了之后才会申请超过核心线程数的线程
        executor.setMaxPoolSize(10);
        // 缓冲队列：用来缓冲执行任务的队列
        executor.setQueueCapacity(500);
        // 允许线程的空闲时间60秒：当超过了核心线程之外的线程在空闲时间到达之后会被销毁
        executor.setKeepAliveSeconds(60);
        // 线程池名的前缀：设置好了之后可以方便我们定位处理任务所在的线程池
        executor.setThreadNamePrefix("DisruptorWorker - ");
        // 缓冲队列满了之后的拒绝策略：由调用线程处理（一般是主线程）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        executor.initialize();


        /**
         * BlockingWaitStrategy
         * Disruptor的默认策略是BlockingWaitStrategy。在BlockingWaitStrategy内部是使用锁和condition来控制线程的唤醒。BlockingWaitStrategy是最低效的策略，但其对CPU的消耗最小并且在各种不同部署环境中能提供更加一致的性能表现
         *
         * SleepingWaitStrategy
         * SleepingWaitStrategy 的性能表现跟 BlockingWaitStrategy 差不多，对 CPU 的消耗也类似，但其对生产者线程的影响最小，通过使用LockSupport.parkNanos(1)来实现循环等待。一般来说Linux系统会暂停一个线程约60µs，这样做的好处是，生产线程不需要采取任何其他行动就可以增加适当的计数器，也不需要花费时间信号通知条件变量。但是，在生产者线程和使用者线程之间移动事件的平均延迟会更高。它在不需要低延迟并且对生产线程的影响较小的情况最好。一个常见的用例是异步日志记录。
         *
         * YieldingWaitStrategy
         * YieldingWaitStrategy是可以使用在低延迟系统的策略之一。YieldingWaitStrategy将自旋以等待序列增加到适当的值。在循环体内，将调用Thread.yield（），以允许其他排队的线程运行。在要求极高性能且事件处理线数小于 CPU 逻辑核心数的场景中，推荐使用此策略；例如，CPU开启超线程的特性。
         *
         * BusySpinWaitStrategy
         * 性能最好，适合用于低延迟的系统。在要求极高性能且事件处理线程数小于CPU逻辑核心树的场景中，推荐使用此策略；例如，CPU开启超线程的特性。
         *
         * */
        // Construct the Disruptor
//        Disruptor<LongEvent> disruptor = new Disruptor<>(LongEvent::new, bufferSize, executor, ProducerType.MULTI, new BusySpinWaitStrategy());
//        LongEventHandler handler = new LongEventHandler();
        // Connect the handler
//        disruptor.handleEventsWith(handler);
        int excutorSize = 10;
        WorkHandler<TradeTransaction>[] workHandlers = new WorkHandler[excutorSize];
        for (int i = 0; i < excutorSize; i++) {
            WorkHandler<TradeTransaction> workHandler = new TradeTransactionInDbHandler();
            workHandlers[i] = workHandler;
        }


        // 设置消费者
//        RingBuffer<LongEvent> ringBuffer = RingBuffer.create(ProducerType.SINGLE, new LongEventFactory(), bufferSize,
//                new BusySpinWaitStrategy());
        RingBuffer<TradeTransaction> ringBuffer = RingBuffer.create(ProducerType.SINGLE, new TradeTransactionEventFactory(), bufferSize,
                new BusySpinWaitStrategy());
        SequenceBarrier barriers = ringBuffer.newBarrier();

        WorkerPool<TradeTransaction> workerPool = new WorkerPool<>(ringBuffer, barriers, new FatalExceptionHandler(), new TradeTransactionInDbHandler());
//        WorkerPool<TradeTransaction> workerPool = new WorkerPool<TradeTransaction>(ringBuffer, sequenceBarrier, new IgnoreExceptionHandler(), workHandlers);

//        workerPool.start(executor);


        ringBuffer.addGatingSequences(workerPool.getWorkerSequences());
        workerPool.start(executor);

        // Start the Disruptor, starts all threads running
//        disruptor.start();

        // Get the ring buffer from the Disruptor to be used for publishing.
//        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();
//
        ByteBuffer bb = ByteBuffer.allocate(8);
        for (long l = 0; l < 100000; l++) {
            bb.putLong(0, l);
            ringBuffer.publishEvent((event, sequence, buffer) -> event.setId(String.valueOf(buffer.getLong(0))), bb);
//            ringBuffer.publishEvent((event, sequence) -> event.set(bb.getLong(0)));
            System.out.println("thread = " + Thread.currentThread().getName() + " publishEvent");

//            disruptor.publishEvent((element, sequence) -> {
//                System.out.println("之前的數據" + element.toString() + "當前的sequence" + sequence);
////                element.setValue("我是第" + sequence + "個");
//        });

//            System.out.println("之前的數據" + element.getValue() + "當前的sequence" + sequence);
//            element.setValue("我是第" + sequence + "個");
//            Thread.sleep(1000);
        }


    }
}
