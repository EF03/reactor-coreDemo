package com.example.demo.disruptor;

import com.example.demo.disruptor.longevent.LongEventFactory;
import com.example.demo.disruptor.longevent.LongEvent;
import com.example.demo.disruptor.longevent.LongEventHandler;
import com.example.demo.disruptor.longevent.LongEventProducer;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;

import java.nio.ByteBuffer;

/**
 * @author IMI-Ron
 */
public class LongEventMain {
    public static void main(String[] args) throws Exception {
        // The factory for the event
        LongEventFactory factory = new LongEventFactory();
//        // Specify the size of the ring buffer, must be power of 2.
        int bufferSize = 1024;

        // Construct the Disruptor
        Disruptor<LongEvent> disruptor = new Disruptor<>(factory, bufferSize, DaemonThreadFactory.INSTANCE);

        // Connect the handler
        disruptor.handleEventsWith(new LongEventHandler());

        // Start the Disruptor, starts all threads running
        disruptor.start();

        // Get the ring buffer from the Disruptor to be used for publishing.
        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();

        LongEventProducer producer = new LongEventProducer(ringBuffer);

        ByteBuffer bb = ByteBuffer.allocate(8);
        for (long l = 0; true; l++) {
            bb.putLong(0, l);
            producer.onData(bb);
            Thread.sleep(1000);
        }

//        Disruptor<LongEvent> disruptor2 = new Disruptor<>(LongEvent::new, bufferSize, DaemonThreadFactory.INSTANCE, ProducerType.MULTI, new YieldingWaitStrategy());
//        LongEventHandler handler = new LongEventHandler();
//        // Connect the handler
//        disruptor2.handleEventsWith(handler);
//
//        disruptor2
//                .handleEventsWith(new LongEventHandler())
//                .then(new LongEventHandler());

    }
}
