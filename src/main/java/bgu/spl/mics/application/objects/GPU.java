package bgu.spl.mics.application.objects;

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
    enum Type {
        RTX3090, RTX2080, GTX1080
    }

    private Type type;

    private Cluster cluster;

    private Queue<DataBatch> backlog;
    private List<Future<Integer>> inFlight;

    public GPU(Type type) {
        this.type = type;
    }

    public void trainBatch(Model model) {
        updateBacklog(model);

        // update in flight
        List<Integer> processedAmount = updateInFlight();

        for (Integer amount : processedAmount) {
            model.getData().process(amount);
        }
    }

    private List<Integer> updateInFlight() {
        // check if futures have resolved
        // remove resolved futures from in flight
        List<Integer> processedAmounts = List.of();
        for (Future<Integer> future : inFlight) {
            if (future.isDone()) {
                inFlight.remove(future);
                processedAmounts.add(future.get());
            }
        }

        // send as many new batches as possible
        int amountToSend = maxBatches() - inFlight.size();
        for (int i = 0; i < amountToSend; i++) {
            DataBatch nextBatch = backlog.poll();
            if (nextBatch == null)
                break;

            inFlight.add(this.cluster.processDataBatch(nextBatch));
        }

        // return amounts processed in resolved futures
        return processedAmounts;
    }

    private void updateBacklog(Model model) {
        Data data = model.getData();
        DataBatch nextBatch = data.getNextBatch().get();

        if (nextBatch == null) {
            throw new IllegalStateException("Tried to get next batch of done Data");
        }

        backlog.add(nextBatch);
    }

    private int maxBatches() {
        switch (type) {
            case RTX3090:
                return 32;
            case GTX1080:
                return 16;
            case RTX2080:
                return 8;
        }

        throw new UnsupportedOperationException("unknown GPU type " + this.type.toString());
    }

}
