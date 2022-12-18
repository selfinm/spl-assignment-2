package bgu.spl.mics.application.objects;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

import bgu.spl.mics.Future;

/**
 * Passive object representing a single GPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods
 * and constructors).
 */
public class GPU {
    /**
     * Enum representing the type of the GPU.
     */
    public enum Type {
        RTX3090, RTX2080, GTX1080
    }

    private Type type;

    private Cluster cluster;

    private Queue<DataBatch> disk;
    /**
     * Keeps track of how many batches are either in the cluster being
     * processed, or being trained by the GPU. We will keep this
     * below vramSize to ensure that even if all batches return from
     * the cluster at once, we will never have more than vramSize
     * batches in "vram".
     */
    private int batchesInProcessing;
    private int vramSize;

    private Queue<DataBatch> batchesInTraining;
    private Map<DataBatch, Integer> batchesTicksLeft;

    private int gpuTicks;

    public GPU(Type type) {
        this.type = type;

        cluster = Cluster.getInstance();
        cluster.addGpu(this);

        disk = new ArrayDeque<>();
        batchesInProcessing = 0;
        batchesInTraining = new ArrayDeque<>();
        batchesTicksLeft = new HashMap<>();

        gpuTicks = GPU.getGpuTicks(type);
        vramSize = GPU.maxBatches(type);
    }

    static int getGpuTicks(GPU.Type type) {
        switch (type) {
            case RTX3090:
                return 1;

            case RTX2080:
                return 2;

            case GTX1080:
                return 4;

            default:
                throw new UnsupportedOperationException("Unknown GPU type : " + type);
        }
    }

    public void trainBatch(Model model) {
        System.out.println("GPU ticks left: " + batchesTicksLeft.values().stream().mapToInt(Integer::intValue).sum());

        // save next batch to disk, if data is not done
        Optional<DataBatch> nextBatch = model.getData().getNextBatch();
        if (nextBatch.isPresent()) {
            disk.add(nextBatch.get());
        }

        // TODO: what is model status???
        // the models data is "pre-processed" in the CPUs and also the processed
        // batches are used to train the model, POSSIBLY AT THE SAME TIME, so what
        // is the status of the model?
        // maybe we need to wait for all DataBatches to be "pre-processed" before we
        // train the model, by "saving them to disk"?

        // apply processed batches results
        Optional<Collection<DataBatch>> processedBatches = cluster.popProcessedBatches(this);
        if (processedBatches.isPresent()) {
            for (DataBatch batch : processedBatches.get()) {
                batchesInTraining.add(batch);
                batchesTicksLeft.put(batch, gpuTicks);
            }
        }

        // tick current batches in training
        for (DataBatch batchInTraining : batchesInTraining) {
            int ticksLeft = batchesTicksLeft.get(batchInTraining) - 1;
            if (ticksLeft == 0) {
                batchesInTraining.remove();
                batchesTicksLeft.remove(batchInTraining);
                batchesInProcessing--;

                // apply training to model
                model.getData().process(DataBatch.size);
            } else {
                batchesTicksLeft.put(batchInTraining, ticksLeft);
            }
        }

        // send as many new batches as possible
        int amountToSend = vramSize - batchesInProcessing;
        for (int i = 0; i < amountToSend; i++) {
            DataBatch batch = disk.poll();
            if (batch == null)
                break;

            cluster.submitDataBatch(batch, this);
            batchesInProcessing++;
        }
    }

    static int maxBatches(GPU.Type type) {
        switch (type) {
            case RTX3090:
                return 32;
            case GTX1080:
                return 16;
            case RTX2080:
                return 8;

            default:
                throw new UnsupportedOperationException("unknown GPU type " + type.toString());
        }

    }

}
