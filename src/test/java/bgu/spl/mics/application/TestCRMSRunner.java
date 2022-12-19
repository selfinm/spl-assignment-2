package bgu.spl.mics.application;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.CRMSRunner.InputJson;
import bgu.spl.mics.application.CRMSRunner.OutputJson;
import bgu.spl.mics.application.SharedTestUtils.AlwaysGoodModel;
import bgu.spl.mics.application.objects.CPU;
import bgu.spl.mics.application.objects.Cluster;
import bgu.spl.mics.application.objects.ConferenceInformation;
import bgu.spl.mics.application.objects.Data;
import bgu.spl.mics.application.objects.Developer;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Model;

public class TestCRMSRunner {
    MessageBusImpl m;

    @Before
    public void before() {
        MessageBusImpl.shutdown();
        Cluster.shutdown();

        m = MessageBusImpl.getInstance();
    }

    @Test
    public void testCRMSRunnerSmall() throws InterruptedException {
        List<Model> models = Arrays.asList(
                new Model("m1", new Data(Data.Type.Tabular, 10)),
                new AlwaysGoodModel("m2", new Data(Data.Type.Tabular, 10)));
        List<Developer> developers = Arrays.asList(
                new Developer("dev1", "test", Developer.Status.Intern, Arrays.asList(models.get(0))),
                new Developer("dev2", "test", Developer.Status.Intern, Arrays.asList(models.get(1))));
        List<GPU> gpus = Arrays.asList(
                new GPU(GPU.Type.RTX3090),
                new GPU(GPU.Type.GTX1080));
        List<CPU> cpus = Arrays.asList(
                new CPU(1),
                new CPU(2));
        List<ConferenceInformation> conferenceInformations = Arrays.asList(
                new ConferenceInformation("conf1", 100),
                new ConferenceInformation("conf2", 200));

        InputJson input = new InputJson(developers, gpus, cpus, conferenceInformations, 1, 500);
        OutputJson expected = new OutputJson(input.developers, input.conferenceInformations, 0, 0);

        OutputJson actual = CRMSRunner.run(input);

        // check we got the same instances of developers
        Assert.assertArrayEquals(
                expected.developers.toArray(new Developer[0]),
                actual.developers.toArray(new Developer[0]));

        // check that always good model was published and read
        Assert.assertTrue(expected.developers.get(1).getPublishedModels().contains(models.get(1)));
        Assert.assertTrue(expected.developers.get(0).getPapersRead() >= 1);

        // check that a conference published always good model
        boolean conf0Published = expected.conferenceInformations
                .get(0).getPublishedModels().contains(models.get(1).getName());
        boolean conf1Published = expected.conferenceInformations
                .get(1).getPublishedModels().contains(models.get(1).getName());

        Assert.assertTrue(conf0Published || conf1Published);
    }

}
