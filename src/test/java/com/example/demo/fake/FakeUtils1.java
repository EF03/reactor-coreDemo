package com.example.demo.fake;

import reactor.core.publisher.Flux;

import java.util.function.Function;

/**
 * @author Ron
 * @date 2021/3/23 上午 09:40
 */
public class FakeUtils1 {
    public static final Function<? super Flux<String>, Flux<String>> applyFilters =
            f -> f.filter(s -> s.startsWith("s"));
}
