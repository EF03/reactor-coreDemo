package com.example.demo.utils;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.RejectedExecutionHandler;

/**
 * @author Ron
 * @date 2020/12/23 上午 11:31
 */
public class ThreadPoolUtil {

    /*
     * ThreadPoolExecutor.AbortPolicy:丢弃任务并抛出RejectedExecutionException异常。
     * ThreadPoolExecutor.DiscardPolicy：也是丢弃任务，但是不抛出异常。
     * ThreadPoolExecutor.DiscardOldestPolicy：丢弃队列最前面的任务，然后重新尝试执行任务（重复此过程）
     * ThreadPoolExecutor.CallerRunsPolicy：由调用线程处理该任务
     *
     */

    public static ThreadPoolTaskExecutor buildThreadPoolTaskExecutor(int corePoolSize, int maxPoolSize, String threadNamePrefix, RejectedExecutionHandler discardPolicy) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数：线程池创建时候初始化的线程数
        executor.setCorePoolSize(corePoolSize);
        // 最大线程数：线程池最大的线程数，只有在缓冲队列满了之后才会申请超过核心线程数的线程
        executor.setMaxPoolSize(maxPoolSize);
        // 缓冲队列：用来缓冲执行任务的队列
        executor.setQueueCapacity(1000);
        // 允许线程的空闲时间60秒：当超过了核心线程之外的线程在空闲时间到达之后会被销毁
        executor.setKeepAliveSeconds(60);
        // 线程池名的前缀：设置好了之后可以方便我们定位处理任务所在的线程池
        executor.setThreadNamePrefix(threadNamePrefix);
        // 缓冲队列满了之后的拒绝策略：由调用线程处理（一般是主线程）
        // executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        executor.setRejectedExecutionHandler(discardPolicy);
        executor.initialize();
        return executor;
    }
}
