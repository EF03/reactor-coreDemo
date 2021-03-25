package com.example.demo.processor;

import com.example.demo.listener.MyEventListener;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.System.out;

/**
 * @author Ron
 * @date 2021/3/22 下午 05:16
 */
public class MyEventProcessorImpl implements MyEventProcessor {
    private MyEventListener<String> eventListener;
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void register(MyEventListener<String> eventListener) {
        this.eventListener = eventListener;
        out.println("register");
    }

    @Override
    public void dataChunk(String... values) {
        executor.schedule(() -> {
                    eventListener.onDataChunk(Arrays.asList(values));
                    out.println("dataChunk");
                },
                500, TimeUnit.MILLISECONDS);
    }

    @Override
    public void processComplete() {
        executor.schedule(() -> {
                    eventListener.processComplete();
                    out.println("processComplete");
                },
                500, TimeUnit.MILLISECONDS);
    }
}
