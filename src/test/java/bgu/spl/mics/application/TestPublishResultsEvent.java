package bgu.spl.mics.application;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import bgu.spl.mics.Future;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.messages.CloseAllBroadcast;
import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.Data;
import bgu.spl.mics.application.objects.Developer;
import bgu.spl.mics.application.objects.Developer.Status;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Model.Results;
import bgu.spl.mics.application.services.ConferenceService;
import bgu.spl.mics.application.services.GPUService;

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
        ConferenceService confMS = new ConferenceService("conf-1");

        // start microservices
        Thread confMsThread = new Thread(confMS);
        confMsThread.start();

        // wait for services to start
        while (!m.isRegistered(confMS)) {
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

        m.sendBroadcast(new CloseAllBroadcast());

        Assert.assertTrue(publishedModel.get() != null);
    }

}
