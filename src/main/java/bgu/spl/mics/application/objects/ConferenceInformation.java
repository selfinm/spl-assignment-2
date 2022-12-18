package bgu.spl.mics.application.objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Passive object representing information on a conference.
 * Add fields and methods to this class as you see fit (including public methods
 * and constructors).
 */
public class ConferenceInformation {
    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("date")
    @Expose
    private int date;

    public ConferenceInformation(String name, int date) {
        this.name = name;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public int getDate() {
        return date;
    }
}