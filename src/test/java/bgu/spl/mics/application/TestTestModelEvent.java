package bgu.spl.mics.application;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.messages.CloseAllBroadcast;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.objects.Cluster;
import bgu.spl.mics.application.objects.Data;
import bgu.spl.mics.application.objects.Developer;
import bgu.spl.mics.application.objects.Developer.Status;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Model.Results;
import bgu.spl.mics.application.services.GPUService;

public class TestTestModelEvent {
    MessageBusImpl m;

    @Before
    public void before() {
        MessageBusImpl.shutdown();
        Cluster.shutdown();

        m = MessageBusImpl.getInstance();
    }

    @Test
    public void testTestModelEvent() {
        GPU.Type gpuType = GPU.Type.GTX1080;
        int modelSize = 1500;

        // build micro services
        GPU gpu = new GPU(gpuType);
        GPUService gpuMs = new GPUService("gs-1", gpu);

        // start microservices
        Thread gpuMsThread = new Thread(gpuMs);
        gpuMsThread.start();

        // wait for services to start
        while (!m.isRegistered(gpuMs)) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Model model = new Model("test-model", new Data(Data.Type.Tabular, modelSize));
        Developer developer = new Developer("test-dev", "test", Status.Intern, List.of(model));

        Model testedModel = m.sendEvent(new TestModelEvent(model, developer)).get();

        m.sendBroadcast(new CloseAllBroadcast());

        Assert.assertTrue(testedModel.getResults() != null);
        Assert.assertNotEquals(Results.None, testedModel.getResults());
    }

}
