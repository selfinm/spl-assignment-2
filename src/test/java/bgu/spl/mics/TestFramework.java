package bgu.spl.mics;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import bgu.spl.mics.example.messages.ExampleEvent;

public class TestFramework {

    @After
    public void after() {
        MessageBusImpl m = MessageBusImpl.getInstance();
        m.messageSubscribers.clear();
        m.roundRobinCounters.clear();
        m.serviceMessages.clear();
    }

    @Test
    public void testSynchronization() {
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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        Long start = System.currentTimeMillis();

        Future<String> f1 = m.sendEvent(new ExampleEvent("first event"));
        Future<String> f2 = m.sendEvent(new ExampleEvent("second event"));

        Long duration = System.currentTimeMillis() - start;

        Assert.assertTrue(duration < 100);

        Assert.assertEquals(f1.get(1001, TimeUnit.MILLISECONDS), "first event");
        Assert.assertEquals(f2.get(1001, TimeUnit.MILLISECONDS), "second event");

        try {
            ms1.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            ms2.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
