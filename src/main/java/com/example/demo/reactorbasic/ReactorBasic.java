package com.example.demo.reactorbasic;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.System.out;
import static java.time.Duration.ofSeconds;
import static java.util.stream.Collectors.toList;

/**
 * @author Ron
 * @date 2021/3/22 上午 10:09
 */
public class ReactorBasic {
    public static void main(String[] args) throws Exception {

        out.println("=== flux ===");
        flux();
        out.println("=== flux ===");

        out.println("=== mono ===");
        mono();
        out.println("=== mono ===");

        whyNotOnlyFlux();

        out.println("===  Collecting Elements ===");
        collectingElements();
        out.println("===  Collecting Elements ===");

        out.println("=== The Flow of Elements ===");
        flowOfElements();
        out.println("=== The Flow of Elements ===");

        out.println("=== Comparison to Java 8 Streams ===");
        comparisonToJava8Streams();
        out.println("=== Comparison to Java 8 Streams ===");

        out.println("=== Backpressure ===");
        backpressure();
        out.println("=== Backpressure ===");

        out.println("=== Mapping Data in a Stream ===");
        mappingDataInAStream();
        out.println("=== Mapping Data in a Stream ===");

        out.println("=== Combining Two Streams ===");
        combiningTwoStreams();
        out.println("=== Combining Two Streams ===");

        out.println("=== Creating a ConnectableFlux ===");
//        creatingAConnectableFlux();
        out.println("=== Creating a ConnectableFlux ===");

        out.println("=== Throttling ===");
//        throttling();
        out.println("=== Throttling ===");

        out.println("=== Concurrency ===");
        concurrency();

        out.println("=== Concurrency ===");

    }

    /**
     * All of our above examples have currently run on the main thread.
     * However, we can control which thread our code runs on if we want.
     * The Scheduler interface provides an abstraction around asynchronous code,
     * for which many implementations are provided for us.
     * Let's try subscribing to a different thread to main
     * 我们上面所有的例子目前都已经在主线程上运行。
     * 然而，如果我们愿意，我们可以控制我们的代码运行在哪个线程上
     * Scheduler接口提供了一个围绕异步代码的抽象，为此我们提供了许多实现。让我们尝试订阅一个不同的线程到main上。
     * <p>
     * The Parallel scheduler will cause our subscription to be run on a different thread,
     * which we can prove by looking at the logs.
     * We see the first entry comes from the main thread and the Flux is running in another thread called parallel-1.
     * 并行调度器会使我们的订阅运行在不同的线程上，
     * 我们可以通过查看日志来证明这一点。
     * 我们看到第一个条目来自主线程，而Flux是在另一个叫做parallel-1的线程中运行的。
     */
    private static void concurrency() throws Exception {
        List<Integer> elements = new ArrayList<>();
        Flux.just(1, 2, 3, 4)
                .log()
                .map(i -> i * 2)
                .subscribeOn(Schedulers.parallel())
                .subscribe(elements::add);
        out.println("Concurrency1 = " + elements);
        Thread.sleep(1000);
        out.println("Concurrency2 = " + elements);
    }

    /**
     * If we run our code, our console will be overwhelmed with logging.
     * This is simulating a situation where too much data is being passed to our consumers.
     * Let's try getting around this with throttling
     * 如果我们运行我们的代码，我们的控制台将被日志淹没。
     * 这是在模拟一种情况，即有太多的数据被传递给我们的消费者。
     * 让我们尝试用节流来解决这个问题。
     * <p>
     * Here, we've introduced a sample() method with an interval of two seconds.
     * Now values will only be pushed to our subscriber every two seconds, meaning the console will be a lot less hectic.
     * 这里，我们引入了一个间隔为两秒的sample()方法。
     * 现在，每隔两秒才会向我们的订阅者推送一次值，这意味着控制台将减少很多忙碌。
     * <p>
     * Of course, there are multiple strategies to reduce the amount of data sent downstream,
     * such as windowing and buffering, but they will be left out of scope for this article.
     * 当然，还有多种策略可以减少下游发送的数据量，比如窗口(windowing)和缓冲(buffering)，但它们将不在本文的讨论范围内。
     */
    private static void throttling() {
        ConnectableFlux<Object> publish = Flux.create(fluxSink -> {
            while (true) {
                fluxSink.next(System.currentTimeMillis());
            }
        })
                .sample(ofSeconds(2))
                .publish();
        publish.subscribe(System.out::println);
        publish.subscribe(System.out::println);
        publish.connect();
    }


    /**
     * One way to create a hot stream is by converting a cold stream into one.
     * Let's create a Flux that lasts forever, outputting the results to the console,
     * which would simulate an infinite stream of data coming from an external resource
     * 让我们创建一个永远持续的Flux，将结果输出到控制台，这将模拟一个来自外部资源的无限数据流。
     * <p>
     * By calling publish() we are given a ConnectableFlux.
     * This means that calling subscribe() won't cause it to start emitting ,allowing us to add multiple subscriptions
     * 通过调用publish()，我们得到了一个ConnectableFlux。
     * 这意味着调用subscribe()不会导致它开始发射，允许我们添加多个订阅。
     */
    private static void creatingAConnectableFlux() {
        ConnectableFlux<Object> publish = Flux.create(fluxSink -> {
            while (true) {
                fluxSink.next(System.currentTimeMillis());
            }
        })
                .publish();
        publish.subscribe(System.out::println);
        publish.subscribe(System.out::println);
        // the Flux will start emitting
        publish.connect();
    }


    private static void combiningTwoStreams() {
        List<String> elements = new ArrayList<>();
        Flux.just(1, 2, 3, 4)
                .log()
                .map(i -> i * 2)
                .zipWith(Flux.range(0, Integer.MAX_VALUE),
                        (one, two) -> String.format("First Flux: %d, Second Flux: %d", one, two))
                .subscribe(elements::add);
        out.println(elements);
    }

    private static void mappingDataInAStream() {
        List<Integer> elements = new ArrayList<>();
        Flux.just(1, 2, 3, 4)
                .log()
                .map(i -> i * 2)
                .subscribe(elements::add);
        out.println(elements);
    }

    /**
     * The next thing we should consider is backpressure.
     * In our example, the subscriber is telling the producer to push every single element at once.
     * This could end up becoming overwhelming for the subscriber, consuming all of its resources.
     * 在我们的例子中，订阅者告诉生产者一次推送每一个元素，这可能最终会让订阅者不堪重负，消耗其所有资源。
     * 这最终可能会让订阅者不堪重负，消耗其所有资源。
     * <p>
     * Backpressure is when a downstream can tell an upstream to send it fewer data in order to prevent it from being overwhelmed.
     * 背压(回压)是指下游可以告诉上游发送更少的数据，以防止它被淹没。
     * <p>
     * We can modify our Subscriber implementation to apply backpressure.
     * Let's tell the upstream to only send two elements at a time by using request():
     * 我们可以修改我们的Subscriber实现来应用背压(回压)。
     * 让我们通过 request() 告诉上游一次只发送两个元素。
     * <p>
     * SUMMARY
     * 小结
     * Essentially, this is reactive pull backpressure.
     * We are requesting the upstream to only push a certain amount of elements, and only when we are ready.
     * 本质上，这就是被动拉回压力(回压)。我们要求上游只推送一定量的元素，并且只在我们准备好的时候推送。
     * <p>
     * If we imagine we were being streamed tweets from twitter,
     * it would then be up to the upstream to decide what to do.
     * If tweets were coming in but there are no requests from the downstream,
     * then the upstream could drop items, store them in a buffer, or some other strategy.
     * 如果我们想象一下，我们被从twitter上流传过来的tweets，那么就由上游来决定该怎么做。
     * 如果有推文进来，但下游没有请求，那么上游可以丢弃物品，将它们存储在缓冲区，或者其他策略。
     */
    private static void backpressure() {
        List<Integer> elements = new ArrayList<>();
        Flux.just(1, 2, 3, 4)
                .log()
                .subscribe(new Subscriber<Integer>() {
                    private Subscription s;
                    int onNextAmount;

                    @Override
                    public void onSubscribe(Subscription s) {
                        this.s = s;
                        s.request(2);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        elements.add(integer);
                        onNextAmount++;
                        if (onNextAmount % 2 == 0) {
                            s.request(2);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
        out.println(elements);
    }

    /**
     * The core difference is that Reactive is a push model, whereas the Java 8 Streams are a pull model.
     * In a reactive approach, events are pushed to the subscribers as they come in.
     * 核心的区别在于，Reactive是一种推模式，而Java 8 Streams是一种拉模式。
     * 在reactive方式中，事件进来后会推送给订阅者。
     * <p>
     * The next thing to notice is a Streams terminal operator is just that, terminal,
     * pulling all the data and returning a result.
     * With Reactive we could have an infinite stream coming in from an external resource,
     * with multiple subscribers attached and removed on an ad hoc basis. We can also do things like combine streams,
     * throttle streams and apply backpressure, which we will cover next.
     * Streams的终端操作符就是终端，拉动所有数据并返回一个结果。
     * 有了Reactive，我们可以有一个无限的流从外部资源进来，有多个订阅者被临时连接和删除。
     * 我们还可以做一些事情，比如合并流、节流和施加背压(回压)
     */
    private static void comparisonToJava8Streams() {
        List<Integer> collected = Stream.of(1, 2, 3, 4)
                .collect(toList());
        out.println(collected);
    }

    /**
     * 1. onSubscribe() – This is called when we subscribe to our stream
     * 当我们订阅我们的流时，会调用这个函数。
     * 2. request(unbounded) – When we call subscribe, behind the scenes we are creating a Subscription.
     * This subscription requests elements from the stream.In this case, it defaults to unbounded,
     * meaning it requests every single element available
     * 当我们调用subscribe时，我们正在创建一个Subscription。
     * 这个订阅从流中请求元素。在这种情况下，它的默认值是unbounded，这意味着它请求每一个可用的元素
     * 3. onNext() – This is called on every single element
     * 这是在每一个元素上调用的
     * 4. onComplete() – This is called last, after receiving the last element.
     * There's actually a onError() as well, which would be called if there is an exception, but in this case, there isn't
     * 这是在接收到最后一个元素后最后调用的。
     * 实际上还有一个onError()，如果出现异常，就会被调用
     */
    private static void flowOfElements() {
        List<Integer> elements = new ArrayList<>();
        Flux.just(1, 2, 3, 4)
                .log()
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        elements.add(integer);
                    }

                    @Override
                    public void onError(Throwable t) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
        out.println(elements);
    }

    private static void collectingElements() {
        List<Integer> elements = new ArrayList<>();

        Flux.just(1, 2, 3, 4)
                .log()
                .subscribe(elements::add);
        out.println("collectingElements = " + elements);

    }

    /**
     * First, it should be noted that both a Flux and Mono are implementations of
     * the Reactive Streams Publisher interface.
     * Both classes are compliant with the specification,
     * and we could use this interface in their place
     * Flux 和 Mono 都是 Reactive Streams Publisher 接口的实现。
     * 这两个类都是符合规范的，我们可以用这个接口来代替它们
     * <p>
     * This is because a few operations only make sense for one of the two types,
     * and because it can be more expressive (imagine findOne() in a repository).
     * 这是因为一些操作只对这两种类型中的一种有意义
     * 也因为它可以更有表现力（想象一下仓库中的findOne()）
     */
    private static void whyNotOnlyFlux() {
        Publisher<String> just = Mono.just("foo");
        List<String> elements = new ArrayList<>();
        just.subscribe(
                new Subscriber<String>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(String integer) {
                        elements.add(integer);
                    }

                    @Override
                    public void onError(Throwable t) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private static void mono() {
        Mono<Integer> just = Mono.just(1);
        just.subscribe(out::println);
    }

    private static void flux() {
        Flux<Integer> just = Flux.just(1, 2, 3, 4);
        just.subscribe(out::println);
    }
}
