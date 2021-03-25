package com.example.demo.fake;

import reactor.core.publisher.Flux;

/**
 * @author Ron
 * @date 2021/3/23 上午 09:39
 */
public class FakeRepository {
    public static Flux<String> findAllUserByName(Flux<String> source) {
        return source.map(s -> { throw new IllegalStateException("boom"); })
                .map(s -> s + "-user");
    }
}
