package bgu.spl.mics.application;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;

import bgu.spl.mics.application.objects.Data;
import bgu.spl.mics.application.objects.Model;

public class TestSerialization {

    @Test
    public void testModel() {
        String modelString = "{\"name\":\"model\",\"data\":{\"type\":\"Images\",\"size\":100}}";
        String expectedDeserializedString = "{\"name\":\"model\",\"data\":{\"type\":\"Images\",\"size\":100},\"status\":\"PreTrained\",\"results\":\"None\"}";
        Model expected = new Model("model", new Data(Data.Type.Images, 100));

        Gson serializer = new Gson();

        Model actual = serializer.fromJson(modelString, Model.class);
        String actualDeserializedString = serializer.toJson(actual, Model.class);

        Assert.assertEquals(expected.getName(), actual.getName());
        Assert.assertEquals(expected.getData().getType(), actual.getData().getType());
        Assert.assertEquals(expected.getData().getSize(), actual.getData().getSize());
        Assert.assertEquals(expectedDeserializedString, actualDeserializedString);
    }
}
