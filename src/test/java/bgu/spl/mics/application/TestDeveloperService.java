package bgu.spl.mics.application;

import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import bgu.spl.mics.Future;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CloseAllBroadcast;
import bgu.spl.mics.application.messages.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.CPU;
import bgu.spl.mics.application.objects.ConferenceInformation;
import bgu.spl.mics.application.objects.Developer;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Data.Type;
import bgu.spl.mics.application.objects.Developer.Status;
import bgu.spl.mics.application.services.CPUService;
import bgu.spl.mics.application.services.ConferenceService;
import bgu.spl.mics.application.services.GPUService;

public class TestDeveloperService {

    MessageBusImpl m;

    @Before
    public void before() {
        MessageBusImpl.shutdown();

        m = MessageBusImpl.getInstance();
    }

    class AlwaysGoodModel extends Model {

        public AlwaysGoodModel(String name, Type type, int size) {
            super(name, type, size);
        }

        @Override
        public void setResults(Results results) {
            this.results = Results.Good;
        }

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
        ConferenceInformation confInf = new ConferenceInformation("conf-1-inf", 10);
        ConferenceService confMS = new ConferenceService("conf-1", confInf);
        Model model = new AlwaysGoodModel("test-model", Type.Tabular, modelSize);
        Developer developer = new Developer("test-dev", "test", Status.Intern, List.of(model));

        // build tester microservice
        Future<Collection<String>> publishedModels = new Future<>();
        MicroService testerMs = new MicroService("tester") {
            @Override
            protected void initialize() {
                subscribeBroadcast(PublishConferenceBroadcast.class,
                        msg -> publishedModels.resolve(msg.getModelNames()));
            }
        };

        // start microservices
        Thread gpuMsThread = new Thread(gpuMs);
        gpuMsThread.start();
        Thread cpu1MsThread = new Thread(cpu1Ms);
        cpu1MsThread.start();
        Thread cpu2MsThread = new Thread(cpu2Ms);
        cpu2MsThread.start();
        Thread confMsThread = new Thread(confMS);
        confMsThread.start();

        // wait for services to start
        while (!m.isRegistered(gpuMs, cpu1Ms, cpu2Ms, confMS)) {
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
        Assert.assertTrue(36 <= totalTicks && totalTicks <= 36 + maxCpuBeforeGpuTicks + maxGpuBeforeCpuTicks);

    }
}
