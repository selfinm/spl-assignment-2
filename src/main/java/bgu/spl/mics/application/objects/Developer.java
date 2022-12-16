package bgu.spl.mics.application.objects;

/**
 * Passive object representing single developer.
 * Add fields and methods to this class as you see fit (including public methods
 * and constructors).
 */
public class Developer {

    enum Position {
        Intern, Junior, Senior
    }

    private String name;
    private String department;
    private Position status;
    private int publications;
    private int papersRead;

}
