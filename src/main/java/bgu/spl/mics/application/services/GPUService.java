package bgu.spl.mics.application.services;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Random;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CloseAllBroadcast;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.Developer;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Model.Results;

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

        backlog = new ArrayDeque<>();
        currentEvent = null;
    }

    private void handleTrainModelEvent(TrainModelEvent event) {
        event.getModel().setStatus(Model.Status.PreTrained);
        backlog.add(event);
    }

    private void handleTickBroadcast(TickBroadcast __) {
        // get current event or next one from backlog
        currentEvent = currentEvent == null ? backlog.poll() : currentEvent;

        // nothing to do
        if (currentEvent == null) {
            return;
        }

        // train next batch
        Model model = currentEvent.getModel();
        gpu.trainBatch(model);

        if (model.trained()) {
            model.setStatus(Model.Status.Trained);
            complete(currentEvent, model);
            currentEvent = null;
        }
    }

    private void handleTestModelEvent(TestModelEvent event) {
        int goodProbability = getGoodProbability(event.getDeveloper());

        if (new Random().nextInt(100) < goodProbability) {
            event.getModel().setResults(Results.Good);
        } else {
            event.getModel().setResults(Results.Bad);
        }

        complete(event, event.getModel());
    }

    private int getGoodProbability(Developer developer) {
        switch (developer.getStatus()) {
            case Intern:
                return 20;
            case Junior:
                return 60;
            case Senior:
                return 80;
            default:
                throw new UnsupportedOperationException(
                        "Unknown developer status: " + developer.getStatus().toString());
        }
    }

    @Override
    protected void initialize() {
        subscribeEvent(TrainModelEvent.class, this::handleTrainModelEvent);
        subscribeEvent(TestModelEvent.class, this::handleTestModelEvent);
        subscribeBroadcast(TickBroadcast.class, this::handleTickBroadcast);
        subscribeBroadcast(CloseAllBroadcast.class, __ -> terminate());
    }
}
