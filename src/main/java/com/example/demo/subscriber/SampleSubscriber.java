package com.example.demo.subscriber;

import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;

/**
 * @author Ron
 * @date 2021/3/22 下午 03:17
 */
public class SampleSubscriber <T> extends BaseSubscriber<T> {
    public void hookOnSubscribe(Subscription subscription) {
        System.out.println("Subscribed");
        request(3);
    }

    public void hookOnNext(T value) {
        System.out.println(value);
        request(1);
    }
}
