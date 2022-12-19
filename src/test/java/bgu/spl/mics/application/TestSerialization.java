package bgu.spl.mics.application;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;

import bgu.spl.mics.application.objects.CPU;
import bgu.spl.mics.application.objects.Data;
import bgu.spl.mics.application.objects.Developer;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Developer.Status;
import bgu.spl.mics.application.objects.GPU.Type;

public class TestSerialization {

    @Test
    public void testModelAndData() {
        String modelString = "{\"name\":\"model\",\"data\":{\"type\":\"Images\",\"size\":100}}";
        String expectedDeserializedString = "{\"name\":\"model\",\"data\":{\"type\":\"Images\",\"size\":100},\"status\":\"PreTrained\",\"results\":\"None\"}";
        Model expected = new Model("model", new Data(Data.Type.Images, 100));

        Gson serializer = new Gson();

        Model actual = serializer.fromJson(modelString, Model.class);
        String actualDeserializedString = serializer.toJson(actual);

        Assert.assertEquals(expected.getName(), actual.getName());
        Assert.assertEquals(expected.getData().getType(), actual.getData().getType());
        Assert.assertEquals(expected.getData().getSize(), actual.getData().getSize());
        Assert.assertEquals(expectedDeserializedString, actualDeserializedString);
    }

    @Test
    public void testDeveloper() {
        String developerString = "{\"name\":\"dev\",\"department\":\"department\",\"status\":\"Intern\",\"papersRead\":0,\"models\":[{\"name\":\"model\",\"data\":{\"type\":\"Images\",\"size\":100}}]}";
        String expectedDeserializedString = "{\"name\":\"dev\",\"department\":\"department\",\"status\":\"Intern\",\"publications\":0,\"papersRead\":0}";

        Model model = new Model("model", new Data(Data.Type.Images, 100));
        Developer developer = new Developer("dev", "department", Status.Intern, List.of(model));

        Developer actual = new Gson().fromJson(developerString, Developer.class);

        Assert.assertEquals(developer.getName(), actual.getName());
        Assert.assertEquals(developer.getDepartment(), actual.getDepartment());
        Assert.assertEquals(developer.getPublications(), actual.getPublications());
        Assert.assertEquals(developer.getStatus(), actual.getStatus());
        Assert.assertEquals(developer.getPapersRead(), actual.getPapersRead());

        Model actualModel = (Model) actual.getModels().toArray()[0];
        Assert.assertEquals(model.getName(), actualModel.getName());
        Assert.assertEquals(model.getData().getType(), actualModel.getData().getType());
        Assert.assertEquals(model.getData().getSize(), actualModel.getData().getSize());

        // set models to null
        actual = new Developer(actual.getName(), actual.getDepartment(), actual.getStatus(), null);
        String actualDeserializedString = new Gson().toJson(actual);
        Assert.assertEquals(expectedDeserializedString, actualDeserializedString);
    }

    @Test
    public void testGpu() {
        String gpuString = "{\"type\":\"RTX3090\"}";

        GPU expected = new GPU(Type.RTX3090);
        GPU actual = new Gson().fromJson(gpuString, GPU.class);

        Assert.assertEquals(expected.getType(), actual.getType());
        Assert.assertEquals(new Gson().toJson(actual), gpuString);
    }

    @Test
    public void testCpu() {
        String cpuString = "{\"cores\":1}";

        CPU expected = new CPU(1);

        CPU actual = new Gson().fromJson(cpuString, CPU.class);
        Assert.assertEquals(actual.getCores(), expected.getCores());
        Assert.assertEquals(new Gson().toJson(actual), cpuString);
    }

}
