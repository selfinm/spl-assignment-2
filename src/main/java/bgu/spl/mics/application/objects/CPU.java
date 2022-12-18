package bgu.spl.mics.application.objects;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

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
     */
    private Collection<DataBatch> data;
    private Cluster cluster;

    private Map<DataBatch, Integer> batchesTicksLeft;
    /**
     * Using this instead of data, since we don't need a collection
     */
    private Optional<DataBatch> dataBatch;

    public CPU(int cores) {
        this.cores = cores;

        data = new ConcurrentLinkedQueue<>();
        cluster = Cluster.getInstance();
        cluster.addCpu(this);

        batchesTicksLeft = new ConcurrentHashMap<>();
        dataBatch = Optional.empty();
    }

    public void submitDataBatch(DataBatch batch) {
        data.add(batch);
        // TODO maybe we need to submit calcBatchTicks-1 here?
        // maybe this counts as a "tick"?
        batchesTicksLeft.putIfAbsent(batch, calcBatchTicks(batch.getData().getType()));
    }

    public void tick() {
        // get next batch from cluster if needed
        dataBatch = dataBatch.isPresent() ? dataBatch : cluster.getNextDataBatch();

        // if no new batches are available, we do nothing
        if (!dataBatch.isPresent()) {
            return;
        }

        DataBatch batch = dataBatch.get();

        int ticksLeft = batchesTicksLeft.getOrDefault(batch, calcBatchTicks(batch.getData().getType()));
        batchesTicksLeft.put(batch, ticksLeft - 1);
        cluster.cpuTickUsed();

        if (batchesTicksLeft.get(batch) == 0) {
            data.remove(batch);
            batchesTicksLeft.remove(batch);

            cluster.notifyBatchProcessed(batch);
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
