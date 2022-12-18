package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing the cluster.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods
 * and constructors).
 */
public class Cluster {

    private static Cluster instance = null;

    private Collection<GPU> gpus;
    private List<CPU> cpus;

    // statistics
    private ConcurrentLinkedQueue<String> modelsTrained;
    private AtomicInteger cpuDataBatchesProcessed;
    private AtomicInteger cpuTimeUnitsUsed;
    private AtomicInteger gpuTimeUnitsUsed;

    private Map<GPU, Collection<DataBatch>> processedBatches;
    private Map<DataBatch, GPU> batchSubmitters;
    private ConcurrentLinkedQueue<DataBatch> backlog;

    /**
     * Retrieves the single instance of this class.
     */
    public static Cluster getInstance() {
        if (instance == null)
            instance = new Cluster();

        return instance;
    }

    public static void shutdown() {
        instance = null;
    }

    private Cluster() {
        gpus = new ArrayList<>();
        cpus = new ArrayList<>();

        modelsTrained = new ConcurrentLinkedQueue<>();
        cpuDataBatchesProcessed = new AtomicInteger(0);
        cpuTimeUnitsUsed = new AtomicInteger(0);
        gpuTimeUnitsUsed = new AtomicInteger(0);

        processedBatches = new ConcurrentHashMap<>();
        batchSubmitters = new ConcurrentHashMap<>();

        backlog = new ConcurrentLinkedQueue<>();
    }

    public synchronized void addGpu(GPU gpu) {
        gpus.add(gpu);
    }

    public synchronized void addCpu(CPU cpu) {
        cpus.add(cpu);
    }

    public void submitDataBatch(DataBatch batch, GPU submitter) {
        batchSubmitters.put(batch, submitter);
        backlog.add(batch);
    }

    public Optional<DataBatch> getNextDataBatch() {
        return backlog.peek() != null ? Optional.of(backlog.remove()) : Optional.empty();
    }

    public Optional<Collection<DataBatch>> popProcessedBatches(GPU gpu) {
        Collection<DataBatch> gpuProcessedBatches = processedBatches.remove(gpu);

        if (gpuProcessedBatches == null)
            return Optional.empty();

        for (DataBatch batch : gpuProcessedBatches) {
            batchSubmitters.remove(batch);
        }

        return Optional.of(gpuProcessedBatches);
    }

    public void cpuTickUsed() {
        cpuTimeUnitsUsed.incrementAndGet();
    }

    public void gpuTickUsed() {
        gpuTimeUnitsUsed.incrementAndGet();
    }

    public void modelTrained(String modelName) {
        modelsTrained.add(modelName);
    }

    public void notifyBatchProcessed(DataBatch batch) {
        // wait for submitter to be registered
        while (!batchSubmitters.containsKey(batch)) {
            // TODO: maybe add timeout?
        }

        GPU submitter = batchSubmitters.remove(batch);

        processedBatches.putIfAbsent(submitter, new ConcurrentLinkedDeque<>());
        processedBatches.get(submitter).add(batch);

        cpuDataBatchesProcessed.incrementAndGet();
    }

}
