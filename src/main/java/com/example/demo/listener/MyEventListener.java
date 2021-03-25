package com.example.demo.listener;

import java.util.List;

/**
 * @author Ron
 * @date 2021/3/22 下午 05:06
 */
public interface MyEventListener<T> {

    void onDataChunk(List<T> chunk);

    void processComplete();
}
