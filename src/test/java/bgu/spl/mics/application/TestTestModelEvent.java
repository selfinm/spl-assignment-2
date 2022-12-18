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
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.CPU;
import bgu.spl.mics.application.objects.Data;
import bgu.spl.mics.application.objects.Developer;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Developer.Status;
import bgu.spl.mics.application.objects.Model.Results;
import bgu.spl.mics.application.services.CPUService;
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
        int cpuCores = 1;
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

        Model model = new Model("test-model", Data.Type.Tabular, modelSize);
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

        // cpu 1 has 1 core so tacks 32 ticks
        // cpu 2 finishes before cpu 1
        // gpu is GTX1080 so tacks 4 ticks
        // total 36 ticks

        // TODO: we have 2 data races, do we need to solve them?
        // possible solution: FORCE tick skipping, code will be slower (by 2 ticks each
        // batch), but predictable

        // if cpu thread notifies cluster that batch is done, after gpu checks, we skip
        // a tick
        int maxCpuBeforeGpuTicks = 2;
        // if gpu thread submits a batch to cluster, after cpu ticks, we skip a tick
        int maxGpuBeforeCpuTicks = 2;

        System.out.println("FINAL TICK COUNT: " + totalTicks);
        // Assert.assertTrue(36 <= totalTicks && totalTicks <= 36 + maxCpuBeforeGpuTicks
        // + maxGpuBeforeCpuTicks);

        Assert.assertTrue(testedModel.get().getResults() != null);
        Assert.assertNotEquals(Results.None, testedModel.get().getResults());

    }

}
