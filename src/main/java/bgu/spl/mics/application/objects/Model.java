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
    }

    public void init() {
        this.status = Status.PreTrained;
        this.results = Results.None;
        data = new Data(type, size);
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status newStatus) {
        this.status = newStatus;
    }

    public Results getResults() {
        return results;
    }

    public void setResults(Results newResults) {
        this.results = newResults;
    }

    public Developer getDeveloper() {
        return developer;
    }

    public void setDeveloper(Developer student) {
        this.developer = student;
    }

    public String getName() {
        return name;
    }

    public Data getData() {
        return data;
    }

    public int getSize() {
        return size;
    }

    public Data.Type getType() {
        return type;
    }

    public void setType(Data.Type type) {
        this.type = type;
    }

    public boolean trained() {
        return this.data.done();
    }
}