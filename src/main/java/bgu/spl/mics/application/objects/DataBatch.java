package bgu.spl.mics.application.objects;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods
 * and constructors).
 */
public class DataBatch {

    public static int size = 1000;
    private Data data;
    private int startIndex;

    public DataBatch(Data data, int startIndex) {
        this.data = data;
        this.startIndex = startIndex;
    }

    public Data getData() {
        return data;
    }

    public int getStartIndex() {
        return startIndex;
    }
}