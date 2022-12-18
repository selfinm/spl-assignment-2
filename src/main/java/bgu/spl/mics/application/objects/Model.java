package bgu.spl.mics.application.objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Passive object representing a Deep Learning model.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods
 * and constructors).
 */
public class Model {

    public enum Results {
        None, Good, Bad
    }

    public enum Status {
        PreTrained, Training, Trained, Tested
    }

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("type")
    @Expose
    private Data.Type type;
    @SerializedName("size")
    @Expose
    private int size;

    private Developer developer;
    private Status status = Status.PreTrained;
    private Results results = Results.None;
    private Data data;

    public Model(String name, Data.Type type, int size) {
        this.name = name;
        this.type = type;
        this.size = size;

        data = new Data(type, size);
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Results getResults() {
        return results;
    }

    public Data getData() {
        return data;
    }

    public boolean trained() {
        return data.done();
    }
}