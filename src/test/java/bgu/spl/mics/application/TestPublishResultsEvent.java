package bgu.spl.mics.application;

import java.util.Arrays;
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
import bgu.spl.mics.application.objects.Cluster;
import bgu.spl.mics.application.objects.ConferenceInformation;
import bgu.spl.mics.application.objects.Data;
import bgu.spl.mics.application.objects.Developer;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.services.ConferenceService;
import bgu.spl.mics.application.services.DeveloperService;

public class TestPublishResultsEvent {
    MessageBusImpl m;

    @Before
    public void before() {
        MessageBusImpl.shutdown();
        Cluster.shutdown();

        m = MessageBusImpl.getInstance();
    }

    @Test
    public void testPublishResultsEvent() {
        // build micro services
        ConferenceInformation confInf = new ConferenceInformation("conf-1-inf", 10);
        ConferenceService confMS = new ConferenceService("conf-1", confInf);

        Model model = new Model("test-model", new Data(Data.Type.Tabular, 1000));
        Developer developer = new Developer("developer", "test", Developer.Status.Intern, Arrays.asList());
        Developer publisher = new Developer("publisher", "test", Developer.Status.Intern, Arrays.asList(model));
        DeveloperService developerMs = new DeveloperService("dev-ms", developer);

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

        Thread developerMsThread = new Thread(developerMs);
        developerMsThread.start();

        // wait for services to start
        while (!m.isRegistered(confMS, testerMs, developerMs)) {
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

        model.setResults(Model.Results.Good);
        Model publishedModel = m.sendEvent(new PublishResultsEvent(model)).get();
        Assert.assertTrue(publishedModel != null);

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

        Assert.assertEquals(1, developer.getPapersRead().intValue());
        Assert.assertEquals(0, publisher.getPapersRead().intValue());
    }

}
