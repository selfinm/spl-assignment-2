package bgu.spl.mics.application.objects;

import java.util.Collection;
import java.util.List;

/**
 * Passive object representing information on a conference.
 * Add fields and methods to this class as you see fit (including public methods
 * and constructors).
 */
public class ConferenceInformation {
    private String name;
    private int date;

    private Collection<String> publishedModels;

    public ConferenceInformation() {

    }

    public ConferenceInformation(String name, int date) {
        this.name = name;
        this.date = date;
    }

    public Collection<String> getPublishedModels() {
        return publishedModels;
    }

    public void setPublishedModels(Collection<String> publishedModels) {
        this.publishedModels = publishedModels;
    }

    public String getName() {
        return name;
    }

    public int getDate() {
        return date;
    }

}