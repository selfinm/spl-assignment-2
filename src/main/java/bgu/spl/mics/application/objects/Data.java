package bgu.spl.mics.application.objects;

import java.util.Optional;

import com.google.gson.annotations.Expose;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods
 * and constructors).
 */
public class Data {
    /**
     * Enum representing the Data type.
     */
    public enum Type {
        Images, Text, Tabular, Voice;

    }

    @Expose
    private Type type;
    @Expose
    private int size;

    private transient int processed;
    private transient int offset;

    public Data(Type type, int size) {
        this.type = type;
        this.size = size;

        processed = 0;
        offset = 0;
    }

    public int getProcessed() {
        return processed;
    }

    public int getSize() {
        return size;
    }

    public Type getType() {
        return type;
    }

    public Optional<DataBatch> getNextBatch() {
        if (offset >= size) {
            return Optional.empty();
        } else {
            DataBatch nextBatch = new DataBatch(this, processed);
            offset += DataBatch.size;

            return Optional.of(nextBatch);
        }
    }

    public boolean done() {
        return processed >= size;
    }

    public void process(int amount) {
        processed += amount;
    }
}