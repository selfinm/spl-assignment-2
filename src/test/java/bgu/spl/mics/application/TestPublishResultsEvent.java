package bgu.spl.mics.application;

import java.util.Collection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import bgu.spl.mics.Future;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CloseAllBroadcast;
import bgu.spl.mics.application.messages.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.ConferenceInformation;
import bgu.spl.mics.application.objects.Data;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Model.Results;
import bgu.spl.mics.application.services.ConferenceService;

public class TestPublishResultsEvent {
    MessageBusImpl m;

    @Before
    public void before() {
        MessageBusImpl.shutdown();

        m = MessageBusImpl.getInstance();
    }

    @Test
    public void testPublishResultsEvent() {
        // build micro services
        ConferenceInformation confInf = new ConferenceInformation("conf-1-inf", 10);
        ConferenceService confMS = new ConferenceService("conf-1", confInf);

        Future<Collection<String>> publishedModels = new Future<>();

        MicroService testerMs = new MicroService("tester") {
            @Override
            protected void initialize() {
                subscribeBroadcast(PublishConferenceBroadcast.class,
                        msg -> publishedModels.resolve(msg.getModelNames()));
            }
        };

        Thread testerMsThread = new Thread(testerMs);
        testerMsThread.start();

        // start microservices
        Thread confMsThread = new Thread(confMS);
        confMsThread.start();

        // wait for services to start
        while (!m.isRegistered(confMS, testerMs)) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Model model = new Model("test-model", Data.Type.Tabular, 1000);
        model.setResults(Results.Good);

        Future<Model> publishedModel = m.sendEvent(new PublishResultsEvent(model));
        Assert.assertTrue(publishedModel.get() != null);

        int totalTicks = 0;
        while (!publishedModels.isDone()) {
            m.sendBroadcast(new TickBroadcast());
            totalTicks++;

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        m.sendBroadcast(new CloseAllBroadcast());

        System.out.println("FINAL TICK COUNT: " + totalTicks);
        Assert.assertEquals(confInf.getDate(), totalTicks);
        Assert.assertTrue(publishedModels.get().contains(model.getName()));
    }

}
