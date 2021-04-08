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

    /**
     * The handle method is a bit different: it is an instance method, meaning that
     * it is chained on an existing source (as are the common operators). It is present in both Mono and Flux.
     * handle 方法有点不同：它是一个实例方法，
     * 意味着它是链在一个现有的源上的（就像常见的操作符一样）。它在Mono和Flux中都存在。
     * <p>
     * It is close to generate, in the sense that it uses a SynchronousSink and only allows one-by-one emissions.
     * However, handle can be used to generate an arbitrary value out of each source element, possibly skipping some elements.
     * In this way, it can serve as a combination of map and filter.
     * The signature of handle is as follows
     * 它和生成很接近，因为它使用了一个SynchronousSink，并且只允许一个一个的排放。
     * 然而，handle可以用来从每个源元素中产生一个任意的值，可能会跳过一些元素。
     * 这样，它就可以作为map和filter的组合。handle的签名如下
     * <p>
     * Let’s consider an example. The reactive streams specification disallows null values in a sequence.
     * What if you want to perform a map but you want to use a preexisting method as the map function,
     * and that method sometimes returns null?
     * 我们来看一个例子 反应式流规范不允许序列中出现空值。
     * 如果你想执行一个映射，但你想使用一个已有的方法作为映射函数，
     * 而这个方法有时会返回null，怎么办？
     * <p>
     * For instance, the following method can be applied safely to a source of integers
     * 例如，以下方法可以安全地应用于整数源
     */
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

    /**
     * When building complex chains of operators, you could come across cases where there are several possible execution paths,
     * materialized by distinct sub-sequences.
     * 当构建复杂的操作符链时，你可能会遇到这样的情况：有几个可能的执行路径，
     * 由不同的子序列具体化。
     * <p>
     * Most of the time, these sub-sequences produce a specific-enough onNext signal that you can assert that
     * it was executed by looking at the end result.
     * 大多数情况下，这些子序列会产生一个特定的onNext信号，
     * 你可以通过查看最终结果来断定它被执行了。
     * <p>
     * For instance, consider the following method, which builds a chain of operators from a source and
     * uses a switchIfEmpty to fall back to a particular alternative if the source is empty:
     * 例如，考虑以下方法，它从一个源建立一个操作符链，
     * 并在源为空的情况下使用switchIfEmpty来回落到一个特定的备选方案。
     */
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
                // <1> then() 忘记了命令的结果。它只关心它已经完成了。
                // then() forgets about the command result. It cares only that it was completed.
                .flatMap(command -> executeCommand(command).then())
                .log()
                // <2> 如何区分两个都是空序列的情况？
                // How to distinguish between two cases that are both empty sequences?
                .switchIfEmpty(doWhenEmpty);
    }

    /**
     * However, think about an example where the method produces a Mono<Void> instead.
     * It waits for the source to complete, performs an additional task, and completes.
     * If the source is empty, a fallback Runnable-like task must be performed instead.
     * The following example shows such a case:
     * <p>
     * 然而，想一想一个例子，在这个例子中，该方法产生的是一个Mono<Void>。
     * 它等待源完成，执行一个额外的任务，然后完成。
     * 如果源是空的，则必须执行一个类似Runnable的后备任务。
     * 下面的例子展示了这样一个案例。
     */
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

    public Mono<String> toDebug = scatterAndGather(urls()); //please overlook the public class attribute :p

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

    /**
     * Switching from an imperative and synchronous programming paradigm to a reactive
     * and asynchronous one can sometimes be daunting.
     * One of the steepest steps in the learning curve is how to analyze and debug when something goes wrong.
     * 从命令式和同步编程范式转换到反应式和
     * 异步编程范式有时会令人生畏。
     * 学习曲线中最陡峭的一步是当出现问题时如何分析和调试。
     * <p>
     * In the imperative world, debugging is usually pretty straightforward.
     * You can read the stacktrace and see where the problem originated.
     * Was it entirely a failure of your code?
     * Did the failure occur in some library code?
     * If so, what part of your code called the library,
     * potentially passing in improper parameters that ultimately caused the failure?
     * 在命令式世界中，调试通常是非常直接的。
     * 你可以阅读堆栈跟踪，看看问题的起源。
     * 是否完全是你的代码出现了故障？
     * 故障是否发生在某些库代码中？
     * 如果是的话，你的代码中的哪一部分调用了库，
     * 有可能传入了不恰当的参数，最终导致了失败？
     */
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

    /**
     * For instance, suppose we have Logback activated and configured and a chain like range(1,10).take(3).
     * By placing a log() before the take, we can get some insight into how it works and
     * what kind of events it propagates upstream to the range,
     * as the following example shows
     * 例如，假设我们激活并配置了Logback，并配置了一个像range(1,10).take(3)这样的链。
     * 通过在take之前放置一个log()，我们可以深入了解它是如何工作的，
     * 以及它向range上游传播了什么样的事件，
     * 如下例所示。
     */
    @Test
    public void debuggingLogging() {
        Flux<Integer> flux = Flux.range(1, 10)
                .log()
                .take(3);
//        flux.subscribe();

        //nothing much to test, but...
        StepVerifier.create(flux).expectNext(1, 2, 3).verifyComplete();
    }

    /**
     * One of the big technical challenges encountered when switching from an imperative programming perspective
     * to a reactive programming mindset lies in how you deal with threading.
     * 当从命令式编程的角度转换到反应式编程的思路时，遇到的一大技术挑战就在于如何处理线程。
     * <p>
     * Contrary to what you might be used to, in reactive programming,
     * you can use a Thread to process several asynchronous sequences that run at roughly the same time (actually, in non-blocking locksteps).
     * The execution can also easily and often jump from one thread to another.
     * 与你可能习惯的情况相反，在反应式编程中，
     * 你可以使用一个Thread来处理多个异步序列，这些序列大致在同一时间运行（实际上是以非阻塞锁步的方式）。
     * 执行时也可以很容易地经常从一个线程跳转到另一个线程。
     * <p>
     * This arrangement is especially hard for developers that use features dependent on the threading model being more “stable,”
     * such as ThreadLocal. As it lets you associate data with a thread, it becomes tricky to use in a reactive context.
     * As a result, libraries that rely on ThreadLocal at least introduce new challenges when used with Reactor.
     * At worst, they work badly or even fail.
     * Using the MDC of Logback to store and log correlation IDs is a prime example of such a situation.
     * 对于使用依赖于线程模型更 "稳定 "的特性的开发者来说，这种安排尤其困难，
     * 比如ThreadLocal。由于它让你将数据与线程关联起来，所以在反应式上下文中使用起来就变得很棘手。
     * 因此，依赖ThreadLocal的库在与Reactor一起使用时，至少会带来新的挑战。
     * 在最坏的情况下，它们会工作得很糟糕，甚至失败。
     * 使用Logback的MDC来存储和记录相关ID就是这种情况的一个典型例子。
     * <p>
     * The usual workaround for ThreadLocal usage is to move the contextual data,C,
     * along your business data, T, in the sequence, by using (for instance) Tuple2<T, C>.
     * This does not look good and leaks an orthogonal concern (the contextual data) into your method and Flux signatures.
     * 对于ThreadLocal的使用，通常的变通方法是通过使用（例如）Tuple2<T，C>，
     * 沿着你的业务数据T，顺序移动上下文数据，C。
     * 这看起来并不好，而且会将一个正交的关注点（上下文数据）泄露到你的方法和 Flux 签名中。
     * <p>
     * Since version 3.1.0, Reactor comes with an advanced feature that is somewhat comparable to ThreadLocal but
     * can be applied to a Flux or a Mono instead of a Thread. This feature is called Context.
     * 自3.1.0版本以来，Reactor自带了一个高级功能，它与ThreadLocal有些相似，
     * 但可以应用于Flux或Mono而不是Thread。这个功能叫做Context。
     * <p>
     * As an illustration of what it looks like, the following example both reads from and writes to Context:
     * 为了说明它是什么样子的，下面的例子既从Context中读取，又向Context写入。
     * <p>
     * Context is an interface reminiscent of Map.It stores key-value pairs and lets you fetch a value you stored by its key.
     * It has a simplified version that only exposes read methods, the ContextView. More specifically:
     * Context是一个让人联想到Map的接口，它存储键值对，并让你通过它的键来获取你存储的值。
     * 它有一个简化的版本，只暴露了读取方法，ContextView。更具体地说。
     * <p>
     * Both key and values are of type Object, so a Context (and ContextView) instance
     * can contain any number of highly divergent values from different libraries and sources.
     * 键和值都是Object类型的，所以一个Context（和ContextView）实例可以
     * 包含任何数量的来自不同库和来源的高度不同的值。
     * <p>
     * A Context is immutable. It expose write methods like put and putAll but they produce a new instance.
     * 一个Context是不可变的。它暴露了写方法，如put和putAll，但它们会产生一个新的实例。
     * <p>
     * For a read-only API that doesn’t even expose such write methods, there’s the ContextView superinterface since 3.4.0
     * 对于一个只读的API，甚至不暴露这样的写方法，有自3.4.0以来的ContextView超级接口。
     * <p>
     * You can check whether the key is present with hasKey(Object key).
     * 你可以用hasKey(Object key)检查key是否存在。
     * <p>
     * Use getOrDefault(Object key, T defaultValue) to retrieve a value (cast to a T) or
     * fall back to a default one if the Context instance does not have that key.
     * 使用getOrDefault(Object key, T defaultValue)来检索一个值(投向T)，
     * 或者如果Context实例没有该键，则回落到一个默认值。
     * <p>
     * Use getOrEmpty(Object key) to get an Optional<T> (the Context instance attempts to cast the stored value to T).
     * 使用getOrEmpty(Object key)来获取一个Optional<T>(Context实例试图将存储的值投向T)。
     * <p>
     * Use put(Object key, Object value) to store a key-value pair, returning a new Context instance.
     * You can also merge two contexts into a new one by using putAll(ContextView).
     * 使用put(Object key, Object value)来存储键值对，返回一个新的Context实例。
     * 你也可以通过使用putAll(ContextView)将两个上下文合并成一个新的上下文。
     * <p>
     * Use delete(Object key) to remove the value associated to a key, returning a new Context.
     * 使用delete(Object key)来删除与键相关联的值，返回一个新的Context
     * <p>
     * When you create a Context, you can create pre-valued Context instances with up to five key-value pairs by using the static Context.of methods.
     * They take 2, 4, 6, 8 or 10 Object instances, each couple of Object instances being a key-value pair to add to the Context.
     * 当你创建一个Context时，你可以通过使用静态Context.of方法创建最多五个键值对的预值Context实例。
     * 它们需要2、4、6、8或10个Object实例，每一对Object实例都是要添加到Context中的键值对
     * <p>
     * Alternatively you can also create an empty Context by using Context.empty().
     * 另外，你也可以使用Context.empty()来创建一个空的Context。
     */

    @Test
    public void contextSimple1() {
        //use of two-space indentation on purpose to maximise readability in refguide
        String key = "message";
        Mono<String> r = Mono.just("Hello")
                .flatMap(s -> Mono.deferContextual(ctx ->
                        //<2> 我们在源元素上进行flatMap，用Mono.deferContextual()将ContextView具体化，
                        // 并直接提取与 "message "相关联的数据，并将其与原词进行连接。
                        Mono.just(s + " " + ctx.get(key))))
                //<1> 操作符链的最后是调用contextWrite(Function)，将 "World "放入Context中，键为 "message"。
                .contextWrite(ctx -> ctx.put(key, "World"))
                .log();
        /*
         * The numbering above versus the actual line order is not a mistake.
         * It represents the execution order. Even though contextWrite is the last piece of the chain,
         * it is the one that gets executed first (due to its subscription-time nature
         * and the fact that the subscription signal flows from bottom to top).
         * 上面的编号与实际的行单并不是错误的。
         * 它代表的是执行顺序。尽管contextWrite是链中的最后一块，
         * 但它是最先被执行的一块（由于它的订阅时间性质和
         * 订阅信号从下往上流的事实）。
         *
         * In your chain of operators, the relative positions of where you write to the Context
         * and where you read from it matters.
         * The Context is immutable and its content can only be seen by operators above it,
         * as demonstrated in the following example:
         * 在你的操作符链中，你写到Context的位置和
         * 从Context读取的位置的相对位置很重要。
         * Context是不可改变的，它的内容只能被它
         * 上面的操作符看到，如下例所示。
         * */
        StepVerifier.create(r)
                //<3> 得到的Mono<String>发出 "Hello World"。
                .expectNext("Hello World")
                .verifyComplete();
    }

    @Test
    public void contextSimple2() {
        //use of two-space indentation on purpose to maximise readability in refguide
        String key = "message";
        Mono<String> r = Mono.just("Hello")
                //<1> Context被写到链中太高的位置。
                .contextWrite(ctx -> ctx.put(key, "World"))
                .flatMap(s -> Mono.deferContextual(ctx ->
                        //<2> 因此，在 flatMap 中，没有与我们的键相关联的值。取而代之的是一个默认值。
                        Mono.just(s + " " + ctx.getOrDefault(key, "Stranger"))));

        StepVerifier.create(r)
                //<3> 由此产生的Mono<String>发出 "Hello Stranger"。
                .expectNext("Hello Stranger")
                .verifyComplete();
    }

    //contextSimple3 deleted since writes are not exposed anymore with ContextView

    /**
     * Similarly, in the case of several attempts to write the same key to the Context,
     * the relative order of the writes matters, too.
     * Operators that read the Context see the value that was set closest to being under them,
     * as demonstrated in the following example:
     * 同样，在多次尝试向上下文写入同一个键的情况下，
     * 写入的相对顺序也很重要。
     * 读取上下文的操作者看到的是最接近其下的设置值，
     * 如下例所示。
     */
    @Test
    public void contextSimple4() {
        String key = "message";
        Mono<String> r = Mono
                .deferContextual(ctx -> Mono.just("Hello " + ctx.get(key)))
                //<1> 试图写入 "message "键。
                .contextWrite(ctx -> ctx.put(key, "Reactor"))
                //<2> 对 "message "键的另一次写入尝试。
                .contextWrite(ctx -> ctx.put(key, "World"));

        StepVerifier.create(r)
                //<3> deferContextual只看到了最接近它的值设置（以及它的下方）。"Reactor"。
                .expectNext("Hello Reactor")
                .verifyComplete();
    }

    /**
     * In the preceding example, the Context is populated with "World" during subscription.
     * Then the subscription signal moves upstream and another write happens.
     * This produces a second immutable Context with a value of "Reactor".
     * After that, data starts flowing. The deferContextual sees the Context closest to it,
     * which is our second Context with the "Reactor" value (exposed to the user as a ContextView).
     * 在前面的例子中，Context在订阅过程中被填充为 "World"。
     * 然后，订阅信号向上游移动，发生另一次写入。
     * 这将产生第二个不可变的Context，其值为 "Reactor"。之后，数据开始流动。
     * deferContextual看到的是最接近它的Context，
     * 也就是我们第二个具有 "Reactor "值的Context（作为ContextView暴露给用户）
     * <p>
     * You might wonder if the Context is propagated along with the data signal.
     * If that was the case, putting another flatMap between these two writes would use the value from the top Context.
     * But this is not the case, as demonstrated by the following example
     * 你可能会想，Context是否会随着数据信号一起传播。
     * 如果是这样的话，在这两次写入之间再放一个flatMap，就会使用顶部Context的值。
     * 但事实并非如此，正如下面的例子所证明的那样
     */
    @Test
    public void contextSimple5() {
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
