package bgu.spl.mics;

import org.junit.Assert;
import org.junit.Test;

import bgu.spl.mics.example.messages.ExampleBroadcast;
import bgu.spl.mics.example.messages.ExampleEvent;
import bgu.spl.mics.example.services.ExampleEventHandlerService;

public class TestMessageBusImpl {

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

        Assert.assertEquals(m.serviceBroadcasts.get(ms1.getName()).poll(), msg);
        Assert.assertEquals(m.serviceBroadcasts.get(ms2.getName()).poll(), msg);
    }
}
