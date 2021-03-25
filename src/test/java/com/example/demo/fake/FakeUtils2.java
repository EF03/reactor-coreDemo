package com.example.demo.fake;

import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

import java.util.function.Function;

/**
 * @author Ron
 * @date 2021/3/23 上午 09:40
 */
public class FakeUtils2 {
    public static final Function<? super Flux<String>, Flux<Tuple2<Long, String>>> enrichUser =
            Flux::elapsed;
}
