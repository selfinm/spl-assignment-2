package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.services.DeveloperService;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Passive object representing single developer.
 * Add fields and methods to this class as you see fit (including public methods
 * and constructors).
 */
public class Developer {

    enum Position {
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
    private Position status;
    @SerializedName("models")
    @Expose
    private ArrayList<Model> models;
    private ArrayList<Model> publishedModels;
    private  int papersRead;
    private Thread studentServiceThread;


    public Developer(String name, String department, Degree status, ArrayList<Model> models) {
        this.name = name;
        this.department = department;
        this.status = status;
        this.models = models;
    }

    public void init(int i){
        this.papersRead = 0;
        DeveloperService developerService = new DeveloperService("student " + i,this);
        Thread t = new Thread(studentService);
        developerServiceThread = t;
        t.start();
    }

    public Thread getStudentServiceThread() {
        return studentServiceThread;
    }

    public ArrayList<Model> getModels() {
        return this.models;
    }

    public Position getDegree() {
        return status;
    }

    public void read(int numberOfPublications) {
        papersRead += numberOfPublications;
    }

    public String getName() {
        return name;
    }

    public void addPublishedModel(Model model){
        publishedModels.add(model);
    }
}


