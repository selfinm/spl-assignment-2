package bgu.spl.mics.application;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import bgu.spl.mics.Future;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.messages.CloseAllBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.CPU;
import bgu.spl.mics.application.objects.Cluster;
import bgu.spl.mics.application.objects.Data;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.services.CPUService;
import bgu.spl.mics.application.services.GPUService;

public class TestTrainModelEvent {

    MessageBusImpl m;

    @Before
    public void before() {
        MessageBusImpl.shutdown();
        Cluster.shutdown();

        m = MessageBusImpl.getInstance();
    }

    @Test
    public void testTrainModelEvent() {
        int cpu1Cores = 1;
        int cpu2Cores = 2;
        GPU.Type gpuType = GPU.Type.GTX1080;
        int modelSize = 1500;

        // build micro services
        GPU gpu = new GPU(gpuType);
        GPUService gpuMs = new GPUService("gs-1", gpu);
        CPU cpu1 = new CPU(cpu1Cores);
        CPUService cpu1Ms = new CPUService("cs-1", cpu1);
        CPU cpu2 = new CPU(cpu2Cores);
        CPUService cpu2Ms = new CPUService("cs-2", cpu2);

        // start microservices
        Thread gpuMsThread = new Thread(gpuMs);
        gpuMsThread.start();
        Thread cpu1MsThread = new Thread(cpu1Ms);
        cpu1MsThread.start();
        Thread cpu2MsThread = new Thread(cpu2Ms);
        cpu2MsThread.start();

        // wait for services to start
        while (!m.isRegistered(gpuMs, cpu1Ms, cpu2Ms)) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Model model = new Model("test-model", new Data(Data.Type.Tabular, modelSize));

        Future<Model> trainedModel = m.sendEvent(new TrainModelEvent(model));

        int totalTicks = 0;
        while (!trainedModel.isDone()) {
            m.sendBroadcast(new TickBroadcast());
            totalTicks++;

            try {
                Thread.sleep(100);
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
        //Assert.assertTrue(36 <= totalTicks && totalTicks <= 36 + maxCpuBeforeGpuTicks + maxGpuBeforeCpuTicks);

        Assert.assertEquals(model.getName(), trainedModel.get().getName());

    }

}
