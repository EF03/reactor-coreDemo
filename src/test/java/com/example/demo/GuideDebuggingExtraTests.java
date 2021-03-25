package com.example.demo;

import com.example.demo.fake.FakeRepository;
import com.example.demo.fake.FakeUtils1;
import com.example.demo.fake.FakeUtils2;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Ron
 * @date 2021/3/23 上午 09:37
 */
public class GuideDebuggingExtraTests {
    @Test
    public void debuggingActivatedWithDeepTraceback() {
        Hooks.onOperatorDebug();

        StringWriter sw = new StringWriter();
        FakeRepository.findAllUserByName(Flux.just("pedro", "simon", "stephane"))
                .transform(FakeUtils1.applyFilters)
                .transform(FakeUtils2.enrichUser)
                .subscribe(System.out::println,
                        t -> t.printStackTrace(new PrintWriter(sw))
                );

        String debugStack = sw.toString();

        assertThat(debugStack.substring(0, debugStack.indexOf("Stack trace:")))
                .endsWith("Error has been observed at the following site(s):\n"
                        + "\t|_       Flux.map ⇢ at reactor.guide.FakeRepository.findAllUserByName(FakeRepository.java:11)\n"
                        + "\t|_       Flux.map ⇢ at reactor.guide.FakeRepository.findAllUserByName(FakeRepository.java:12)\n"
                        + "\t|_    Flux.filter ⇢ at reactor.guide.FakeUtils1.lambda$static$1(FakeUtils1.java:13)\n"
                        + "\t|_ Flux.transform ⇢ at reactor.guide.GuideDebuggingExtraTests.debuggingActivatedWithDeepTraceback(GuideDebuggingExtraTests.java:26)\n"
                        + "\t|_   Flux.elapsed ⇢ at reactor.guide.FakeUtils2.lambda$static$0(FakeUtils2.java:27)\n"
                        + "\t|_ Flux.transform ⇢ at reactor.guide.GuideDebuggingExtraTests.debuggingActivatedWithDeepTraceback(GuideDebuggingExtraTests.java:27)\n");
    }
}
