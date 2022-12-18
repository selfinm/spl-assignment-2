package bgu.spl.mics.application.objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Passive object representing single developer.
 * Add fields and methods to this class as you see fit (including public methods
 * and constructors).
 */
public class Developer {

    public enum Status {
        Intern, Junior, Senior
    }

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("department")
    @Expose
    private String department;
    @SerializedName("status")
    @Expose
    private Status status;

    @SerializedName("publications")
    @Expose
    private Integer publications;

    @SerializedName("papersRead")
    @Expose
    private int papersRead;
    private List<Model> models;

    public Developer(String name, String department, Status status, List<Model> models) {
        this.name = name;
        this.department = department;
        this.status = status;

        this.models = models;
    }

    public Status getStatus() {
        return status;
    }

    public List<Model> getModels() {
        return models;
    }
}