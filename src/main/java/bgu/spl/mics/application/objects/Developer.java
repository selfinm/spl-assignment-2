package bgu.spl.mics.application.objects;

import java.util.ArrayList;
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
    private Integer publications = 0;
    private AtomicInteger papersRead = new AtomicInteger(0);

    private List<Model> models;
    private List<Model> publishedModels = new ArrayList<>();

    public Developer() {
    }

    public Developer(String name, String department, Status status, List<Model> models) {
        this.name = name;
        this.department = department;
        this.status = status;

        this.models = models;
    }

    public List<Model> getPublishedModels() {
        return publishedModels;
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
        return publications;
    }

    public Integer getPapersRead() {
        return papersRead.get();
    }

    public void addPublishedModel(Model publishedModel) {
        publications++;
        publishedModels.add(publishedModel);
    }

    public void readPaper(String modelName) {
        // check if model is this developer's
        for (Model model : models) {
            if (model.getName().equals(modelName)) {
                return;
            }
        }

        papersRead.incrementAndGet();
    }
}