package bgu.spl.mics.application;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import bgu.spl.mics.Future;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.messages.CloseAllBroadcast;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
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

        m = MessageBusImpl.getInstance();
    }

    @Test
    public void testTrainModelEvent() {
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

        Future<Model> testedModel = m.sendEvent(new TestModelEvent(model, developer));

        int totalTicks = 0;
        while (!testedModel.isDone()) {
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
        Assert.assertEquals(1, totalTicks);

        Assert.assertTrue(testedModel.get().getResults() != null);
        Assert.assertNotEquals(Results.None, testedModel.get().getResults());
    }

}
