package bgu.spl.mics;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import bgu.spl.mics.example.messages.ExampleBroadcast;
import bgu.spl.mics.example.messages.ExampleEvent;
import bgu.spl.mics.example.services.ExampleEventHandlerService;

public class TestMessageBusImpl {

    @Before
    public void before() {
        MessageBusImpl.restart();
    }

    @Test
    public void testSubscribeMicroservice() {
        MessageBusImpl m = MessageBusImpl.getInstance();

        MicroService ms = new ExampleEventHandlerService("ms", new String[] { "10" });

        m.register(ms);

        m.subscribeEvent(ExampleEvent.class, ms);
        m.subscribeBroadcast(ExampleBroadcast.class, ms);

        Assert.assertTrue(m.messageSubscribers.containsKey(ExampleEvent.class));
        Assert.assertEquals(m.messageSubscribers.get(ExampleEvent.class).get(0), ms.getName());
        Assert.assertEquals(m.messageSubscribers.get(ExampleBroadcast.class).get(0), ms.getName());
    }

    @Test
    public void testSendBroadcast() {
        MessageBusImpl m = MessageBusImpl.getInstance();

        MicroService ms1 = new ExampleEventHandlerService("ms", new String[] { "10" });
        MicroService ms2 = new ExampleEventHandlerService("ms", new String[] { "10" });

        m.register(ms1);
        m.register(ms2);

        m.subscribeBroadcast(ExampleBroadcast.class, ms1);
        m.subscribeBroadcast(ExampleBroadcast.class, ms2);

        ExampleBroadcast msg = new ExampleBroadcast("test");
        m.sendBroadcast(msg);

        Assert.assertEquals(m.serviceMessages.get(ms1.getName()).poll(), msg);
        Assert.assertEquals(m.serviceMessages.get(ms2.getName()).poll(), msg);
    }

    @Test
    public void testSendEvent() {
        MessageBusImpl m = MessageBusImpl.getInstance();

        MicroService ms1 = new ExampleEventHandlerService("ms1", new String[] { "10" });
        MicroService ms2 = new ExampleEventHandlerService("ms2", new String[] { "10" });

        m.register(ms1);
        m.register(ms2);

        m.subscribeEvent(ExampleEvent.class, ms1);
        m.subscribeEvent(ExampleEvent.class, ms2);

        ExampleEvent msg = new ExampleEvent("test");
        m.sendEvent(msg);

        ExampleEvent event = (ExampleEvent) m.serviceMessages.get(ms1.getName()).poll();

        Assert.assertNotNull(event);
        Assert.assertEquals(msg, event);
        Assert.assertEquals(null, m.serviceMessages.get(ms2.getName()).poll());

        Assert.assertEquals(Integer.valueOf(1), m.roundRobinCounters.get(ExampleEvent.class));
    }

    @Test
    public void testUnregister() {
        MessageBusImpl m = MessageBusImpl.getInstance();

        MicroService ms1 = new ExampleEventHandlerService("ms1", new String[] { "10" });
        MicroService ms2 = new ExampleEventHandlerService("ms2", new String[] { "10" });

        m.register(ms1);
        m.register(ms2);

        m.subscribeEvent(ExampleEvent.class, ms1);
        m.subscribeEvent(ExampleEvent.class, ms2);

        m.sendEvent(new ExampleEvent("test1"));
        m.sendEvent(new ExampleEvent("test2"));

        m.unregister(ms1);

        Assert.assertEquals(null, m.serviceMessages.get(ms1.getName()));

        Assert.assertFalse(m.messageSubscribers.get(ExampleEvent.class).contains(ms1.getName()));
        Assert.assertNotNull(m.roundRobinCounters.get(ExampleEvent.class));

        Assert.assertTrue(m.messageSubscribers.get(ExampleEvent.class).contains(ms2.getName()));

        m.unregister(ms2);

        Assert.assertNull(m.messageSubscribers.get(ExampleEvent.class));
        Assert.assertNull(m.roundRobinCounters.get(ExampleEvent.class));
    }

    @Test
    public void testComplete() {
        MessageBusImpl m = MessageBusImpl.getInstance();

        MicroService ms1 = new ExampleEventHandlerService("ms1", new String[] { "10" });

        m.register(ms1);
        m.subscribeEvent(ExampleEvent.class, ms1);
        ExampleEvent msg = new ExampleEvent("foo");
        Future<String> future = m.sendEvent(msg);

        m.complete(msg, "bar");

        Assert.assertTrue(future.isDone());
        Assert.assertEquals("bar", future.get());
    }
}
