package bgu.spl.mics.application.services;

import java.util.List;
import java.util.Optional;
import java.util.Queue;

import javax.xml.crypto.Data;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.DataBatch;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Model;

/**
 * GPU service is responsible for handling the
 * {@link TrainModelEvent} and {@link TestModelEvent},
 * in addition to sending the {@link DataPreProcessEvent}.
 * This class may not hold references for objects which it is not responsible
 * for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class GPUService extends MicroService {

    private GPU gpu;

    private Queue<TrainModelEvent> backlog;
    private TrainModelEvent currentEvent;

    public GPUService(String name, GPU gpu) {
        super(name);

        this.gpu = gpu;
    }

    private void handleTrainModelEvent(TrainModelEvent event) {
        backlog.add(event);
    }

    private void handleTickBroadcast(TickBroadcast _t) {
        currentEvent = currentEvent == null ? backlog.poll() : currentEvent;
        if (currentEvent == null) {
            return;
        }

        // train next batch
        Model model = currentEvent.getModel();
        this.gpu.trainBatch(model);

        if (model.trained()) {
            complete(currentEvent, model);
            currentEvent = null;
        }

    }

    @Override
    protected void initialize() {
        subscribeEvent(TrainModelEvent.class, this::handleTrainModelEvent);
        subscribeBroadcast(TickBroadcast.class, this::handleTickBroadcast);
    }
}
