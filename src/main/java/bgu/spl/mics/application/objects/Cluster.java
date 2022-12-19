package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
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

    private Collection<GPU> gpus;
    private List<CPU> cpus;

    // statistics
    private ConcurrentLinkedQueue<String> modelsTrained;
    private AtomicInteger cpuDataBatchesProcessed;
    private AtomicInteger cpuTimeUnitsUsed;
    private AtomicInteger gpuTimeUnitsUsed;

    private Map<GPU, Queue<DataBatch>> processedBatches;
    private Map<DataBatch, GPU> batchSubmitters;

    private ConcurrentLinkedQueue<DataBatch> backlog;

    private Map<DataBatch, Boolean> batchRemoved;

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

        batchRemoved = new ConcurrentHashMap<>();
    }

    public Integer getCpuTimeUnitsUsed() {
        return cpuTimeUnitsUsed.get();
    }

    public Integer getGpuTimeUnitsUsed() {
        return gpuTimeUnitsUsed.get();
    }

    public synchronized void addGpu(GPU gpu) {
        gpus.add(gpu);
        processedBatches.put(gpu, new ConcurrentLinkedQueue<>());
    }

    public synchronized void addCpu(CPU cpu) {
        cpus.add(cpu);
    }

    public void submitDataBatch(DataBatch batch, GPU submitter) {
        batchSubmitters.put(batch, submitter);
        backlog.add(batch);
    }

    public Optional<DataBatch> getNextDataBatch() {
        DataBatch nextDataBatch = backlog.poll();

        return nextDataBatch != null ? Optional.of(nextDataBatch) : Optional.empty();
    }

    public Optional<Collection<DataBatch>> popProcessedBatches(GPU gpu) {
        // get all processed batches
        List<DataBatch> gpuProcessedBatches = new ArrayList<>();
        while (!processedBatches.get(gpu).isEmpty()) {
            gpuProcessedBatches.add(processedBatches.get(gpu).remove());
        }

        if (gpuProcessedBatches.isEmpty())
            return Optional.empty();

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
        int time = 0;
        // wait for submitter to be registered
        while (!batchSubmitters.containsKey(batch)) {
            // TODO: maybe add timeout?
            time++;
            System.out.println("cluster - " + time + ", " + batchRemoved.containsKey(batch));
        }

        GPU submitter = batchSubmitters.remove(batch);
        batchRemoved.put(batch, true);

        processedBatches.get(submitter).add(batch);

        cpuDataBatchesProcessed.incrementAndGet();
    }

}
