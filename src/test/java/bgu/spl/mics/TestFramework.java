package bgu.spl.mics;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import bgu.spl.mics.example.messages.ExampleBroadcast;
import bgu.spl.mics.example.messages.ExampleEvent;

public class TestFramework {

    @Before
    public void before() {
        MessageBusImpl.restart();
    }

    @Test
    public void testBroadcast() {
        MessageBusImpl m = MessageBusImpl.getInstance();

        ConcurrentLinkedQueue<String> q = new ConcurrentLinkedQueue<>();

        MicroService ms = new MicroService("b-1") {

            @Override
            protected void initialize() {
                subscribeBroadcast(ExampleBroadcast.class, b -> {
                    System.out.println("called");
                    q.add(b.getSenderId());
                    terminate();
                });
            }

        };

        Thread thread = new Thread(ms);
        thread.start();

        while (!m.isRegistered(ms)) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                return;
            }
        }

        m.sendBroadcast(new ExampleBroadcast("hello"));

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }

        Assert.assertEquals("hello", q.poll());
    }

    @Test
    public void testEvent() {
        MessageBusImpl m = MessageBusImpl.getInstance();

        Thread ms1 = new Thread(new MicroService("slp-1") {

            @Override
            protected void initialize() {
                subscribeEvent(ExampleEvent.class, ev -> {
                    System.out.println(
                            "slp-1 got a new event from " + ev.getSenderName() + "!");
                    int sleep = 1000;

                    try {
                        Thread.sleep(sleep);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    complete(ev, ev.getSenderName());
                    terminate();
                });
            }

        });

        Thread ms2 = new Thread(new MicroService("slp-2") {

            @Override
            protected void initialize() {
                subscribeEvent(ExampleEvent.class, ev -> {
                    System.out.println(
                            "slp-2 got a new event from " + ev.getSenderName() + "!");
                    int sleep = 1000;

                    try {
                        Thread.sleep(sleep);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    complete(ev, ev.getSenderName());
                    terminate();
                });
            }

        });

        ms1.start();
        ms2.start();

        // TODO: do we need to force the message bus to wait for all services to be
        // registered before we allow it to send messages?
        while (!m.serviceMessages.containsKey("slp-1") || !m.serviceMessages.containsKey("slp-2")) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Long start = System.currentTimeMillis();

        Future<String> f1 = m.sendEvent(new ExampleEvent("first event"));
        Future<String> f2 = m.sendEvent(new ExampleEvent("second event"));

        Long duration = System.currentTimeMillis() - start;

        Assert.assertTrue(duration < 100);

        Assert.assertEquals("first event", f1.get(1500, TimeUnit.MILLISECONDS));
        Assert.assertEquals("second event", f2.get(1500, TimeUnit.MILLISECONDS));

        int maxLag = 100;
        Assert.assertTrue(System.currentTimeMillis() - start <= 1000 + maxLag);

        try {
            ms1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            ms2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
