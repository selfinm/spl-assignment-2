package bgu.spl.mics.application.objects;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
    private AtomicInteger publications = new AtomicInteger(0);
    private AtomicInteger papersRead = new AtomicInteger(0);

    private List<Model> models;

    public Developer() {
    }

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

    public Integer getPublications() {
        return publications.get();
    }

    public Integer getPapersRead() {
        return papersRead.get();
    }
}