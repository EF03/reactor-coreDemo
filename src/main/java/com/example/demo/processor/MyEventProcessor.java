package com.example.demo.processor;

import com.example.demo.listener.MyEventListener;

/**
 * @author Ron
 * @date 2021/3/22 下午 05:09
 */
public interface MyEventProcessor {
    void register(MyEventListener<String> eventListener);
    void dataChunk(String... values);
    void processComplete();
}
