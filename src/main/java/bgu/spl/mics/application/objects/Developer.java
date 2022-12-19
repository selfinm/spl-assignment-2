package bgu.spl.mics.application.objects;

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

    private String name;
    private String department;
    private Status status;
    private int publications;
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

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public int getPublications() {
        return publications;
    }

    public int getPapersRead() {
        return papersRead;
    }
}