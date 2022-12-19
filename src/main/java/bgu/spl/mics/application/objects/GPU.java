package bgu.spl.mics.application.objects;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

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

    public static int getGpuTicks(GPU.Type type) {
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

    public static int maxBatches(GPU.Type type) {
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

    private Type type;

    private transient Cluster cluster;
    private transient Queue<DataBatch> disk;
    /**
     * Keeps track of how many batches are either in the cluster being
     * processed, or being trained by the GPU. We will keep this
     * below vramSize to ensure that even if all batches return from
     * the cluster at once, we will never have more than vramSize
     * batches in "vram".
     */
    private transient int batchesInProcessing;

    private transient int vramSize;
    private transient Queue<DataBatch> batchesInTraining;

    private transient Map<DataBatch, Integer> batchesTicksLeft;

    private transient int gpuTicks;

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

    public Type getType() {
        return type;
    }

    public void trainBatch(Model model) {
        // save next batch to disk, if data is not done
        Optional<DataBatch> nextBatch = model.getData().getNextBatch();
        if (nextBatch.isPresent()) {
            disk.add(nextBatch.get());
        }

        // apply processed batches results
        Optional<Collection<DataBatch>> processedBatches = cluster.popProcessedBatches(this);
        if (processedBatches.isPresent()) {
            // TODO: what is model status???
            // the models data is "pre-processed" in the CPUs and also the processed
            // batches are used to train the model, POSSIBLY AT THE SAME TIME, so what
            // is the status of the model?
            // maybe we need to wait for all DataBatches to be "pre-processed" before we
            // train the model, by "saving them to disk"?
            // right now, "Training", means that at least one batch was used to
            // "train" the model, even though more batches might still be being
            // "pre-processed"
            model.setStatus(Model.Status.Training);

            for (DataBatch batch : processedBatches.get()) {
                batchesInTraining.add(batch);
                batchesTicksLeft.put(batch, gpuTicks);
            }
        }

        // tick current batches in training
        // TODO: do GPUs train batches in parallel or not?
        for (DataBatch batchInTraining : batchesInTraining) {
            int ticksLeft = batchesTicksLeft.get(batchInTraining) - 1;
            // TODO: do we count ticks by batch or by tick?
            // lets say in one tick CPU processed 5 batches
            // is that 1 tick or 5 ticks for the cluster cpuTickUsed?
            cluster.gpuTickUsed();

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

}
