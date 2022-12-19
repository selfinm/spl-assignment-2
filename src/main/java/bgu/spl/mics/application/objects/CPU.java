package bgu.spl.mics.application.objects;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Passive object representing a single CPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods
 * and constructors).
 */
public class CPU {

    private int cores;

    /**
     * From the project spec:
     * When a CPU finishes processing a batch, it sends the Cluster the processed
     * batch, and takes a new batch for processing.
     * </p>
     * So why is data a collection and not just a DataBatch?
     * 
     * We don't use this attribute, so it is always set as null.
     */
    private transient Collection<DataBatch> data = null;

    private transient Map<DataBatch, Integer> batchesTicksLeft = new ConcurrentHashMap<>();

    /**
     * Using this instead of data, since we don't need a collection
     */
    private transient Optional<DataBatch> dataBatch = Optional.empty();

    public CPU() {
        Cluster.getInstance().addCpu(this);
    }

    public CPU(int cores) {
        this.cores = cores;

        Cluster.getInstance().addCpu(this);
    }

    public int getCores() {
        return cores;
    }

    public void tick() {
        // get next batch from cluster if needed
        dataBatch = dataBatch.isPresent() ? dataBatch : Cluster.getInstance().getNextDataBatch();

        // if no new batches are available, we do nothing
        if (!dataBatch.isPresent()) {
            return;
        }

        DataBatch batch = dataBatch.get();

        int ticksLeft = batchesTicksLeft.getOrDefault(batch, calcBatchTicks(batch.getData().getType()));
        batchesTicksLeft.put(batch, ticksLeft - 1);
        Cluster.getInstance().cpuTickUsed();

        if (batchesTicksLeft.get(batch) == 0) {
            batchesTicksLeft.remove(batch);

            Cluster.getInstance().notifyBatchProcessed(batch);
            dataBatch = Optional.empty();
        }
    }

    int calcBatchTicks(Data.Type type) {
        switch (type) {
            case Images:
                return (32 / cores) * 4;

            case Voice:
                return (32 / cores) * 3;

            case Text:
                return (32 / cores) * 2;

            case Tabular:
                return (32 / cores) * 1;

            default:
                throw new UnsupportedOperationException("Unknown data type: " + type.toString());
        }
    }
}
