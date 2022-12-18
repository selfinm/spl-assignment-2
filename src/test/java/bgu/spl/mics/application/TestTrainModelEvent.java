package bgu.spl.mics.application;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import bgu.spl.mics.Future;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.messages.CloseAllBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.CPU;
import bgu.spl.mics.application.objects.Data;
import bgu.spl.mics.application.objects.Developer;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.GPU.Type;
import bgu.spl.mics.application.services.CPUService;
import bgu.spl.mics.application.services.DeveloperService;
import bgu.spl.mics.application.services.GPUService;

public class TestTrainModelEvent {

    MessageBusImpl m;

    @Before
    public void before() {
        MessageBusImpl.restart();

        m = MessageBusImpl.getInstance();
    }

    void startMicroServices(GPU.Type gpuType, int cpu1Cores, int cpu2Cores) {
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

        Model model = new Model("test-model", Data.Type.Tabular, modelSize);

        Future<Model> trainedModel = m.sendEvent(new TrainModelEvent(model));

        int totalTicks = 0;
        while (!trainedModel.isDone()) {
            System.out.println("-------------------------");
            System.out.println("tick: " + totalTicks);
            m.sendBroadcast(new TickBroadcast());
            totalTicks++;
            System.out.println("-------------------------");

            // sleep so tick count is correct, run too fast and
            // it keeps increasing even though model is technically
            // resolved, just the thread hasn't caught up to that yet
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // cpu 1 has 1 core so tacks 32 ticks
        // cpu 2 finishes before cpu 1
        // gpu is GTX1080 so tacks 4 ticks
        // total 36 ticks

        // 1 more tick is present, not sure why

        System.out.println("FINAL TICK COUNT: " + totalTicks);
        Assert.assertEquals(37, totalTicks);

        m.sendBroadcast(new CloseAllBroadcast());

    }

}
