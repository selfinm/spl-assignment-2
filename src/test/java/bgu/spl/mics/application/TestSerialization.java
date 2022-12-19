package bgu.spl.mics.application;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;

import bgu.spl.mics.application.CRMSRunner.InputJson;
import bgu.spl.mics.application.objects.CPU;
import bgu.spl.mics.application.objects.ConferenceInformation;
import bgu.spl.mics.application.objects.Data;
import bgu.spl.mics.application.objects.Developer;
import bgu.spl.mics.application.objects.Developer.Status;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Model;

public class TestSerialization {

    @Test
    public void testModelAndData() {
        String modelString = "{\"name\":\"model\",\"data\":{\"type\":\"Images\",\"size\":100}}";
        String expectedDeserializedString = "{\"name\":\"model\",\"data\":{\"type\":\"Images\",\"size\":100},\"status\":\"PreTrained\",\"results\":\"None\"}";
        Model expected = new Model("model", new Data(Data.Type.Images, 100));

        Gson serializer = new Gson();

        Model actual = serializer.fromJson(modelString, Model.class);
        String actualDeserializedString = serializer.toJson(actual);

        SharedTestUtils.assertModelsEqual(expected, actual);

        Assert.assertEquals(expectedDeserializedString, actualDeserializedString);
    }

    @Test
    public void testDeveloper() {
        String developerString = "{\"name\":\"dev\",\"department\":\"department\",\"status\":\"Intern\",\"papersRead\":0,\"models\":[{\"name\":\"model\",\"data\":{\"type\":\"Images\",\"size\":100}}]}";
        String expectedDeserializedString = "{\"name\":\"dev\",\"department\":\"department\",\"status\":\"Intern\",\"publications\":0,\"papersRead\":0,\"models\":[{\"name\":\"model\",\"data\":{\"type\":\"Images\",\"size\":100},\"status\":\"PreTrained\",\"results\":\"None\"}],\"publishedModels\":[]}";

        Model model = new Model("model", new Data(Data.Type.Images, 100));
        Developer developer = new Developer("dev", "department", Status.Intern, Arrays.asList(model));

        Developer actual = new Gson().fromJson(developerString, Developer.class);

        SharedTestUtils.assertDevelopersEqual(developer, actual);

        // set models to null
        String actualDeserializedString = new Gson().toJson(actual);
        Assert.assertEquals(expectedDeserializedString, actualDeserializedString);
    }

    @Test
    public void testGpu() {
        String gpuString = "{\"type\":\"RTX3090\"}";

        GPU expected = new GPU(GPU.Type.RTX3090);
        GPU actual = new Gson().fromJson(gpuString, GPU.class);

        SharedTestUtils.assertGpusEqual(expected, actual);
        Assert.assertEquals(new Gson().toJson(actual), gpuString);
    }

    @Test
    public void testCpu() {
        String cpuString = "{\"cores\":1}";

        CPU expected = new CPU(1);

        CPU actual = new Gson().fromJson(cpuString, CPU.class);
        SharedTestUtils.assertCpusEqual(expected, actual);
        Assert.assertEquals(new Gson().toJson(actual), cpuString);
    }

    @Test
    public void testConferenceInformation() {
        String confString = "{\"name\":\"conference\",\"date\":10}";

        ConferenceInformation expected = new ConferenceInformation("conference", 10);

        ConferenceInformation actual = new Gson().fromJson(confString, ConferenceInformation.class);
        SharedTestUtils.assertConferenceInformationsEqual(expected, actual);
        Assert.assertEquals(new Gson().toJson(actual), confString);
    }

    @Test
    public void testInputJson() {
        String inputJsonString = "{\"developers\":[{\"name\":\"Dev-1\",\"department\":\"Dep-1\",\"status\":\"Intern\",\"publications\":0,\"papersRead\":0,\"models\":[{\"name\":\"YOLO10\",\"data\":{\"type\":\"Images\",\"size\":200000},\"status\":\"PreTrained\",\"results\":\"None\"},{\"name\":\"ResNet9000\",\"data\":{\"type\":\"Images\",\"size\":200000},\"status\":\"PreTrained\",\"results\":\"None\"},{\"name\":\"LessEfficientNet\",\"data\":{\"type\":\"Images\",\"size\":20000},\"status\":\"PreTrained\",\"results\":\"None\"},{\"name\":\"DensestNet\",\"data\":{\"type\":\"Images\",\"size\":200000},\"status\":\"PreTrained\",\"results\":\"None\"}]},{\"name\":\"Dev-2\",\"department\":\"Dep-2\",\"status\":\"Junior\",\"publications\":0,\"papersRead\":0,\"models\":[{\"name\":\"VIT\",\"data\":{\"type\":\"Images\",\"size\":100000000},\"status\":\"PreTrained\",\"results\":\"None\"}]},{\"name\":\"Dev-3\",\"department\":\"Dep-1\",\"status\":\"Senior\",\"publications\":0,\"papersRead\":0,\"models\":[{\"name\":\"Bert\",\"data\":{\"type\":\"Text\",\"size\":1000000},\"status\":\"PreTrained\",\"results\":\"None\"},{\"name\":\"GPT4\",\"data\":{\"type\":\"Text\",\"size\":1000000},\"status\":\"PreTrained\",\"results\":\"None\"},{\"name\":\"GPT5\",\"data\":{\"type\":\"Text\",\"size\":200000},\"status\":\"PreTrained\",\"results\":\"None\"},{\"name\":\"GPT10\",\"data\":{\"type\":\"Text\",\"size\":50000},\"status\":\"PreTrained\",\"results\":\"None\"}]},{\"name\":\"Dev-4\",\"department\":\"Dep-2\",\"status\":\"Intern\",\"publications\":0,\"papersRead\":0,\"models\":[{\"name\":\"Percepetron\",\"data\":{\"type\":\"Tabular\",\"size\":1000000},\"status\":\"PreTrained\",\"results\":\"None\"},{\"name\":\"GNN\",\"data\":{\"type\":\"Tabular\",\"size\":1000000},\"status\":\"PreTrained\",\"results\":\"None\"},{\"name\":\"MoreStyleGAN\",\"data\":{\"type\":\"Images\",\"size\":100000},\"status\":\"PreTrained\",\"results\":\"None\"},{\"name\":\"ConditionalGAN\",\"data\":{\"type\":\"Images\",\"size\":500000},\"status\":\"PreTrained\",\"results\":\"None\"}]},{\"name\":\"Dev-5\",\"department\":\"Dep-3\",\"status\":\"Junior\",\"publications\":0,\"papersRead\":0,\"models\":[{\"name\":\"YOLO9000\",\"data\":{\"type\":\"Images\",\"size\":100000},\"status\":\"PreTrained\",\"results\":\"None\"},{\"name\":\"VIT2\",\"data\":{\"type\":\"Images\",\"size\":1000000},\"status\":\"PreTrained\",\"results\":\"None\"},{\"name\":\"MuchMoreEfficientNet\",\"data\":{\"type\":\"Images\",\"size\":20000},\"status\":\"PreTrained\",\"results\":\"None\"},{\"name\":\"DenserNet\",\"data\":{\"type\":\"Images\",\"size\":100000},\"status\":\"PreTrained\",\"results\":\"None\"}]}],\"gpus\":[{\"type\":\"GTX1080\"},{\"type\":\"RTX3090\"},{\"type\":\"RTX2080\"},{\"type\":\"GTX1080\"}],\"cpus\":[{\"cores\":32},{\"cores\":32},{\"cores\":32},{\"cores\":16},{\"cores\":16},{\"cores\":16},{\"cores\":16}],\"conferenceInformations\":[{\"name\":\"ICML\",\"date\":20000},{\"name\":\"NeurIPS\",\"date\":25000},{\"name\":\"CVPR\",\"date\":30000},{\"name\":\"ECCV\",\"date\":40000},{\"name\":\"AISTATS\",\"date\":50000}],\"TickTime\":1,\"Duration\":55000}";

        List<Developer> expectedDevelopers = SharedTestUtils.getDevelopers();
        List<GPU> expectedGpus = SharedTestUtils.getGpus();
        List<CPU> expectedCpus = SharedTestUtils.getCpus();
        List<ConferenceInformation> expectedConferenceInformations = SharedTestUtils.getConferenceInformations();

        InputJson actual = new Gson().fromJson(inputJsonString, InputJson.class);

        Assert.assertEquals(expectedDevelopers.size(), actual.developers.size());
        for (int i = 0; i < expectedDevelopers.size(); i++) {
            SharedTestUtils.assertDevelopersEqual(expectedDevelopers.get(i), actual.developers.get(i));
        }

        Assert.assertEquals(expectedGpus.size(), actual.gpus.size());
        for (int i = 0; i < expectedGpus.size(); i++) {
            SharedTestUtils.assertGpusEqual(expectedGpus.get(i), actual.gpus.get(i));
        }

        Assert.assertEquals(expectedCpus.size(), actual.cpus.size());
        for (int i = 0; i < expectedCpus.size(); i++) {
            SharedTestUtils.assertCpusEqual(expectedCpus.get(i), actual.cpus.get(i));
        }

        Assert.assertEquals(expectedConferenceInformations.size(),
                actual.conferenceInformations.size());
        for (int i = 0; i < expectedConferenceInformations.size(); i++) {
            SharedTestUtils.assertConferenceInformationsEqual(expectedConferenceInformations.get(i),
                    actual.conferenceInformations.get(i));
        }
    }

}
