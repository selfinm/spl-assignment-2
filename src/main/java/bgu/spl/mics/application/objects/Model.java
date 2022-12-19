package bgu.spl.mics.application.objects;

import static org.junit.Assert.fail;

import com.google.gson.annotations.Expose;

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

    private String name;
    private Data data;

    private Status status = Status.PreTrained;
    private Results results = Results.None;

    public Model() {
    }

    public Model(String name, Data data) {
        this.name = name;
        this.data = data;
    }

    public void setResults(Results results) {
        this.results = results;
    }

    public String getName() {
        return name;
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