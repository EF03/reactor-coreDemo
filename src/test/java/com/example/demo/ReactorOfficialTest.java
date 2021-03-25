package com.example.demo;

import com.example.demo.listener.MyEventListener;
import com.example.demo.processor.MyEventProcessor;
import com.example.demo.processor.MyEventProcessorImpl;
import com.example.demo.subscriber.SampleSubscriber;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInfo;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.PublisherProbe;
import reactor.util.context.Context;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.System.out;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Ron
 * @date 2021/3/22 上午 10:09
 */
public class ReactorOfficialTest {


    @Test
    public void mono() {
        Mono<Integer> just = Mono.just(1);
        just.subscribe(out::println);
    }

    @Test
    public void create1() {
        Flux<String> seq1 = Flux.just("foo", "bar", "foobar");
        seq1.log().subscribe();
    }

    @Test
    public void create2() {
        List<String> iterable = Arrays.asList("foo", "bar", "foobar");
        Flux<String> seq2 = Flux.fromIterable(iterable);
        seq2.log().subscribe();
    }

    @Test
    public void create3() {
        Mono<String> noData = Mono.empty();
        noData.log().subscribe();
    }

    @Test
    public void create4() {
        Mono<String> data = Mono.just("foo");
        data.log().subscribe();
    }

    @Test
    public void create5() {
        Flux<Integer> numbersFromFiveToSeven = Flux.range(5, 3);
        numbersFromFiveToSeven.log().subscribe();
        numbersFromFiveToSeven.subscribe(out::println);
    }

    @Test
    public void throwError() {
        Flux<Integer> ints = Flux.range(1, 4)
                .map(i -> {
                    if (i <= 3) return i;
                    throw new RuntimeException("Got to 4");
                });
        ints.subscribe(out::println,
                error -> System.err.println("Error: " + error));
    }

    @Test
    public void completeConsumer() {
        Flux<Integer> ints = Flux.range(1, 4);
        ints.subscribe(out::println,
                error -> System.err.println("Error " + error),
                () -> System.out.println("Done"));
    }

    @Test
    public void completeConsumer2() {
        Flux<Integer> ints = Flux.range(1, 4);
        ints.log().subscribe(out::println,
                error -> System.err.println("Error " + error),
                () -> System.out.println("Done"),
                sub -> sub.request(20));
    }

    @Test
    public void baseSubscriber() {
        SampleSubscriber<Integer> ss = new SampleSubscriber<>();
        Flux<Integer> ints = Flux.range(1, 4);
        ints.log().subscribe(ss);
    }

    /**
     * Cancelling after having received
     * cancel() 在 request前 被呼叫
     */
    @Test
    public void baseSubscriberOverride() {
        Flux.range(1, 10)
                .doOnRequest(r -> System.out.println("request of " + r))
                .log()
                .subscribe(new BaseSubscriber<Integer>() {

                    @Override
                    public void hookOnSubscribe(Subscription subscription) {
                        request(1);
                    }

                    @Override
                    public void hookOnNext(Integer integer) {
                        System.out.println("Cancelling after having received " + integer);
                        cancel();
                    }
                });
    }


    /**
     * The simplest form of programmatic creation of a Flux is through the generate method, which takes a generator function.
     * 最简单的程序化创建Flux的形式是通过generate方法，它需要一个生成函数
     * <p>
     * This is for synchronous and one-by-one emissions, meaning that the sink is a SynchronousSink
     * and that its next() method can only be called at most once per callback invocation.
     * You can then additionally call error(Throwable) or complete(), but this is optional.
     * 这是针对同步和逐一排放的，也就是说，水槽是一个SynchronousSink，
     * 它的next()方法每次回调调用最多只能调用一次。
     * 然后你可以另外调用error(Throwable)或complete()，但这是可选的。
     * <p>
     * The most useful variant is probably the one that also lets you keep a state that
     * you can refer to in your sink usage to decide what to emit next.
     * The generator function then becomes a BiFunction<S, SynchronousSink<T>, S>, with <S> the type of the state object.
     * You have to provide a Supplier<S> for the initial state, and your generator function now returns a new state on each round.
     * 最有用的变体可能是还可以让你保留一个状态，
     * 你可以在你的sink使用中参考这个状态来决定下一步要发出什么。
     * 然后，生成函数就变成了一个BiFunction<S，SynchronousSink<T>，S>，其中<S>是状态对象的类型。
     * 你必须为初始状态提供一个Supplier<S>，你的生成函数现在在每一轮都返回一个新的状态。
     */
    @Test
    public void stateBasedGenerate() {
        Flux<String> flux = Flux.generate(
                () -> 0,
                (state, sink) -> {
                    sink.next("3 x " + state + " = " + 3 * state);
                    if (state == 10) sink.complete();
                    return state + 1;
                });
        flux.log().subscribe();

    }

    /**
     * You can also use a mutable <S>.
     * The example above could for instance be rewritten using a single AtomicLong as the state, mutating it on each round
     * 你也可以使用一个可突变的<S>。例如，上面的例子可以重写，使用单个AtomicLong作为状态，并在每一轮中对其进行突变。
     */
    @Test
    public void mutableStateVariant() {
        Flux<String> flux = Flux.generate(
                AtomicLong::new,
                (state, sink) -> {
                    long i = state.getAndIncrement();
                    sink.next("3 x " + i + " = " + 3 * i);
                    if (i == 10) sink.complete();
                    return state;
                });
        flux.log().subscribe();

    }

    private MyEventProcessor myEventProcessor = new MyEventProcessorImpl();

    /**
     * create can be very useful to bridge an existing API with the reactive world - such as an asynchronous API based on listeners.
     * create doesn’t parallelize your code nor does it make it asynchronous, even though it can be used with asynchronous APIs.
     * create可以非常有用地将现有的API与反应式世界连接起来--比如基于监听器的异步API。
     * create不会并行你的代码，也不会让它成为异步的，尽管它可以与异步API一起使用
     * <p>
     * If you block within the create lambda, you expose yourself to deadlocks and similar side effects. Even with the use of subscribeOn,
     * there’s the caveat that a long-blocking create lambda (such as an infinite loop calling sink.next(t)) can lock the pipeline:
     * the requests would never be performed due to the loop starving the same thread they are supposed to run from.
     * Use the subscribeOn(Scheduler, false) variant: requestOnSeparateThread = false will use the Scheduler thread for the create
     * and still let data flow by performing request in the original thread.
     * 如果你在create lambda中阻塞，你就会面临死锁和类似的副作用。即使使用了 subscribeOn，
     * 也有一个警告，那就是一个长阻塞的 create lambda（比如一个调用 sink.next(t)的无限循环）可能会锁定管道：
     * 由于循环饿死了它们应该运行的同一个线程，请求永远不会被执行。
     * 使用subscribeOn(Scheduler，false)变体：requestOnSeparateThread = false将使用Scheduler线程进行创建，
     * 并且仍然通过在原始线程中执行请求来让数据流动。
     * <p>
     * Imagine that you use a listener-based API.
     * It processes data by chunks and has two events: (1) a chunk of data is ready and (2) the processing is complete (terminal event),
     * as represented in the MyEventListener interface
     * 想象一下，你使用了一个基于监听器的API。
     * 它按分块处理数据，有两个事件。(1)一个数据块准备好了，(2)处理完成(终端事件)，
     * 在MyEventListener接口中表示。
     */
    @Test
    public void myEventListener() {
        Flux<String> bridge = Flux.create(sink -> {
            myEventProcessor.register(
                    new MyEventListener<String>() {
                        public void onDataChunk(List<String> chunk) {
                            for (String s : chunk) {
                                sink.next(s);
                            }
                        }

                        public void processComplete() {
                            sink.complete();
                        }
                    });
        });

        StepVerifier
                .withVirtualTime(() -> bridge)
                .expectSubscription()
                .expectNoEvent(Duration.ofSeconds(10))
                .then(() -> myEventProcessor.dataChunk("foo", "bar", "baz"))
                .expectNext("foo", "bar", "baz")
                .expectNoEvent(Duration.ofSeconds(10))
                .then(() -> myEventProcessor.processComplete())
                .verifyComplete();
    }

    public String alphabet(int letterNumber) {
        if (letterNumber < 1 || letterNumber > 26) {
            return null;
        }
        int letterIndexAscii = 'A' + letterNumber - 1;
        return "" + (char) letterIndexAscii;
    }

    @Test
    public void producingHandle() {
        Flux<String> alphabet = Flux.just(-1, 30, 13, 9, 20)
                .handle((i, sink) -> {
                    String letter = alphabet(i); // <1>
                    if (letter != null) // <2>
                        sink.next(letter); // <3>
                });

        alphabet.subscribe(System.out::println);

        StepVerifier.create(alphabet)
                .expectNext("M", "I", "T")
                .verifyComplete();
    }


    /**
     * s+ >> 多个空白字元
     */
    public Flux<String> processOrFallback(Mono<String> source, Publisher<String> fallback) {
        return source
                .log()
                .flatMapMany(phrase -> Flux.fromArray(phrase.split("\\s+")))
                .log()
                .switchIfEmpty(fallback);
    }

    @Test
    public void testSplitPathIsUsed() {
        StepVerifier.create(processOrFallback(Mono.just("just a  phrase with    tabs!"),
                Mono.just("EMPTY_PHRASE")))
                .expectNext("just", "a", "phrase", "with", "tabs!")
                .verifyComplete();
    }

    @Test
    public void testEmptyPathIsUsed() {
        StepVerifier.create(processOrFallback(Mono.empty(), Mono.just("EMPTY_PHRASE")))
                .expectNext("EMPTY_PHRASE")
                .verifyComplete();
    }

    private Mono<String> executeCommand(String command) {
        return Mono.just(command + " DONE");
    }

    public Mono<Void> processOrFallback(Mono<String> commandSource, Mono<Void> doWhenEmpty) {
        return commandSource
                .log()
                .flatMap(command -> executeCommand(command).then()) // <1>
                .log()
                .switchIfEmpty(doWhenEmpty); // <2>
    }

    @Test
    public void testCommandEmptyPathIsUsedBoilerplate() {
        AtomicBoolean wasInvoked = new AtomicBoolean();
        AtomicBoolean wasRequested = new AtomicBoolean();
        Mono<Void> testFallback = Mono.<Void>empty()
                .doOnSubscribe(s -> wasInvoked.set(true))
                .doOnRequest(l -> wasRequested.set(true));

        processOrFallback(Mono.empty(), testFallback).subscribe();

        assertThat(wasInvoked.get()).isTrue();
        assertThat(wasRequested.get()).isTrue();
    }

    @Test
    public void testCommandEmptyPathIsUsed() {
        PublisherProbe<Void> probe = PublisherProbe.empty(); // <1>

        StepVerifier.create(processOrFallback(Mono.empty(), probe.mono())) // <2>
                .verifyComplete();

        probe.assertWasSubscribed(); //<3>
        probe.assertWasRequested(); //<4>
        probe.assertWasNotCancelled(); //<5>
    }

    //Note: the following static methods and fields are grouped here on purpose
    //as they all relate to the same section of the reference guide (activating debug mode).
    //some of these lines are copied verbatim in the reference guide, like the declaration of toDebug.

    private Flux<String> urls() {
        return Flux.range(1, 5)
                .map(i -> "https://www.mysite.io/quote" + i);
    }

    private Flux<String> doRequest(String url) {
        return Flux.just("{\"quote\": \"inspiring quote from " + url + "\"}");
    }

    private Mono<String> scatterAndGather(Flux<String> urls) {
        return urls.flatMap(this::doRequest)
                .single();
    }

    @BeforeEach
    public void populateDebug(TestInfo testInfo) {
        if (testInfo.getTags().contains("debugModeOn")) {
            Hooks.onOperatorDebug();
        }
        if (testInfo.getTags().contains("debugInit")) {
            toDebug = scatterAndGather(urls());
        }
    }

    @AfterEach
    public void removeHooks(TestInfo testInfo) {
        if (testInfo.getTags().contains("debugModeOn")) {
            Hooks.resetOnOperatorDebug();
        }
    }

    public Mono<String> toDebug; //please overlook the public class attribute :p

    private void printAndAssert(Throwable t, boolean checkForAssemblySuppressed) {
        t.printStackTrace();
        assertThat(t)
                .isInstanceOf(IndexOutOfBoundsException.class)
                .hasMessage("Source emitted more than one item");
        if (!checkForAssemblySuppressed) {
            assertThat(t).hasNoSuppressedExceptions();
        } else {
            assertThat(t).satisfies(withSuppressed -> {
                assertThat(withSuppressed.getSuppressed()).hasSize(1);
                assertThat(withSuppressed.getSuppressed()[0])
                        .hasMessageStartingWith("\nAssembly trace from producer [reactor.core.publisher.MonoSingle] :")
                        .hasMessageContaining("Flux.single ⇢ at reactor.guide.GuideTests.scatterAndGather(GuideTests.java:1017)\n");
            });
        }
    }

    @Test
    @Tag("debugInit")
    public void debuggingCommonStacktrace() {
        toDebug.subscribe(System.out::println, t -> printAndAssert(t, false));
    }

    @Test
    @Tag("debugModeOn")
    @Tag("debugInit")
    public void debuggingActivated() {
        toDebug.subscribe(System.out::println, t -> printAndAssert(t, true));
    }

    @Test
    public void debuggingLogging() {
        Flux<Integer> flux = Flux.range(1, 10)
                .log()
                .take(3);
        //flux.subscribe();

        //nothing much to test, but...
        StepVerifier.create(flux).expectNext(1, 2, 3).verifyComplete();
    }

    @Test
    public void contextSimple1() {
        //use of two-space indentation on purpose to maximise readability in refguide
        String key = "message";
        Mono<String> r = Mono.just("Hello")
                .flatMap(s -> Mono.deferContextual(ctx ->
                        Mono.just(s + " " + ctx.get(key)))) //<2>
                .contextWrite(ctx -> ctx.put(key, "World")); //<1>

        StepVerifier.create(r)
                .expectNext("Hello World") //<3>
                .verifyComplete();
    }

    @Test
    public void contextSimple2() {
        //use of two-space indentation on purpose to maximise readability in refguide
        String key = "message";
        Mono<String> r = Mono.just("Hello")
                .contextWrite(ctx -> ctx.put(key, "World")) //<1>
                .flatMap(s -> Mono.deferContextual(ctx ->
                        Mono.just(s + " " + ctx.getOrDefault(key, "Stranger")))); //<2>

        StepVerifier.create(r)
                .expectNext("Hello Stranger") //<3>
                .verifyComplete();
    }

    //contextSimple3 deleted since writes are not exposed anymore with ContextView

    @Test
    public void contextSimple4() {
        //use of two-space indentation on purpose to maximise readability in refguide
        String key = "message";
        Mono<String> r = Mono
                .deferContextual(ctx -> Mono.just("Hello " + ctx.get(key)))
                .contextWrite(ctx -> ctx.put(key, "Reactor")) //<1>
                .contextWrite(ctx -> ctx.put(key, "World")); //<2>

        StepVerifier.create(r)
                .expectNext("Hello Reactor") //<3>
                .verifyComplete();
    }

    @Test
    public void contextSimple5() {
        //use of two-space indentation on purpose to maximise readability in refguide
        String key = "message";
        Mono<String> r = Mono
                .deferContextual(ctx -> Mono.just("Hello " + ctx.get(key))) //<3>
                .contextWrite(ctx -> ctx.put(key, "Reactor")) //<2>
                .flatMap(s -> Mono.deferContextual(ctx ->
                        Mono.just(s + " " + ctx.get(key)))) //<4>
                .contextWrite(ctx -> ctx.put(key, "World")); //<1>

        StepVerifier.create(r)
                .expectNext("Hello Reactor World") //<5>
                .verifyComplete();
    }

    @Test
    public void contextSimple6() {
        //use of two-space indentation on purpose to maximise readability in refguide
        String key = "message";
        Mono<String> r = Mono.just("Hello")
                .flatMap(s -> Mono
                        .deferContextual(ctxView -> Mono.just(s + " " + ctxView.get(key)))
                )
                .flatMap(s -> Mono
                        .deferContextual(ctxView -> Mono.just(s + " " + ctxView.get(key)))
                        .contextWrite(ctx -> ctx.put(key, "Reactor")) //<1>
                )
                .contextWrite(ctx -> ctx.put(key, "World")); // <2>

        StepVerifier.create(r)
                .expectNext("Hello World Reactor")
                .verifyComplete();
    }

    //use of two-space indentation on purpose to maximise readability in refguide
    static final String HTTP_CORRELATION_ID = "reactive.http.library.correlationId";

    Mono<Tuple2<Integer, String>> doPut(String url, Mono<String> data) {
        Mono<Tuple2<String, Optional<Object>>> dataAndContext =
                data.zipWith(Mono.deferContextual(c -> // <1>
                        Mono.just(c.getOrEmpty(HTTP_CORRELATION_ID))) // <2>
                );

        return dataAndContext.<String>handle((dac, sink) -> {
            if (dac.getT2().isPresent()) { // <3>
                sink.next("PUT <" + dac.getT1() + "> sent to " + url +
                        " with header X-Correlation-ID = " + dac.getT2().get());
            } else {
                sink.next("PUT <" + dac.getT1() + "> sent to " + url);
            }
            sink.complete();
        })
                .map(msg -> Tuples.of(200, msg));
    }

    //use of two-space indentation on purpose to maximise readability in refguide
    @Test
    public void contextForLibraryReactivePut() {
        Mono<String> put = doPut("www.example.com", Mono.just("Walter"))
                .contextWrite(Context.of(HTTP_CORRELATION_ID, "2-j3r9afaf92j-afkaf"))
                .filter(t -> t.getT1() < 300)
                .map(Tuple2::getT2);

        StepVerifier.create(put)
                .expectNext("PUT <Walter> sent to www.example.com" +
                        " with header X-Correlation-ID = 2-j3r9afaf92j-afkaf")
                .verifyComplete();
    }

    @Test
    public void contextForLibraryReactivePutNoContext() {
        //use of two-space indentation on purpose to maximise readability in refguide
        Mono<String> put = doPut("www.example.com", Mono.just("Walter"))
                .filter(t -> t.getT1() < 300)
                .map(Tuple2::getT2);

        StepVerifier.create(put)
                .expectNext("PUT <Walter> sent to www.example.com")
                .verifyComplete();
    }

}
