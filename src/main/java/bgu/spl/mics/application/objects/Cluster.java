package bgu.spl.mics.application.objects;

import java.util.Collection;
import java.util.List;

import bgu.spl.mics.Future;

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

    /**
     * Retrieves the single instance of this class.
     */
    public static Cluster getInstance() {
        if (instance == null)
            instance = new Cluster();

        return instance;
    }

    private Cluster() {
        gpus = List.of();
        cpus = List.of();

        modelsTrained = List.of();
        cpuDataBatchesProcessed = 0;
        cpuTimeUnitsUsed = 0;
        gpuTimeUnitsUsed = 0;

        roundRobinCounter = 0;
    }

    public void addGpu(GPU gpu) {
        gpus.add(gpu);
    }

    public void addCpu(CPU cpu) {
        cpus.add(cpu);
    }

    public Future<Integer> processDataBatch(DataBatch batch) {
        CPU cpu = cpus.get(roundRobinCounter % cpus.size());

        // tell cpu to process batch
        return cpu.processDataBatch(batch);
    }

}
