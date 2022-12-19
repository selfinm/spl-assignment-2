package bgu.spl.mics.application.objects;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

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

    private Type type;
    private Integer size;

    private transient AtomicInteger processed = new AtomicInteger(0);
    private transient AtomicInteger offset = new AtomicInteger(0);

    public Data() {

    }

    public Data(Type type, int size) {
        this.type = type;
        this.size = size;
    }

    public Integer getOffset() {
        return offset.get();
    }

    public Integer getProcessed() {
        return processed.get();
    }

    public Integer getSize() {
        return size;
    }

    public Type getType() {
        return type;
    }

    public Optional<DataBatch> getNextBatch() {
        Integer currentOffset = offset.getAndAdd(DataBatch.size);

        if (currentOffset >= size) {
            // make sure offset doesn't explode
            if (currentOffset >= size + DataBatch.size * 3) {
                offset.addAndGet(-DataBatch.size);
            }

            return Optional.empty();
        } else {
            DataBatch nextBatch = new DataBatch(this);

            return Optional.of(nextBatch);
        }
    }

    public boolean done() {
        return processed.get() >= size;
    }

    public void process(Integer amount) {
        processed.addAndGet(amount);
    }
}