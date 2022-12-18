package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

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
    private Collection<String> modelsTrained;
    private Integer cpuDataBatchesProcessed;
    private Integer cpuTimeUnitsUsed;
    private Integer gpuTimeUnitsUsed;

    private Integer roundRobinCounter;

    private Map<GPU, Collection<DataBatch>> processedBatches;
    private Map<DataBatch, GPU> batchSubmitters;

    /**
     * Retrieves the single instance of this class.
     */
    public static Cluster getInstance() {
        if (instance == null)
            instance = new Cluster();

        return instance;
    }

    private Cluster() {
        gpus = new ArrayList<>();
        cpus = new ArrayList<>();

        modelsTrained = new ArrayList<>();
        cpuDataBatchesProcessed = 0;
        cpuTimeUnitsUsed = 0;
        gpuTimeUnitsUsed = 0;

        roundRobinCounter = 0;

        processedBatches = new ConcurrentHashMap<>();
        batchSubmitters = new ConcurrentHashMap<>();
    }

    public void addGpu(GPU gpu) {
        gpus.add(gpu);
    }

    public void addCpu(CPU cpu) {
        cpus.add(cpu);
    }

    public void submitDataBatch(DataBatch batch, GPU submitter) {
        batchSubmitters.put(batch, submitter);
        CPU cpu = cpus.get(roundRobinCounter % cpus.size());
        roundRobinCounter = (roundRobinCounter + 1) % cpus.size();

        cpu.submitDataBatch(batch);
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

    public void notifyBatchProcessed(DataBatch batch) {
        GPU submitter = batchSubmitters.remove(batch);

        processedBatches.putIfAbsent(submitter, new ConcurrentLinkedDeque<>());
        processedBatches.get(submitter).add(batch);
    }

}
