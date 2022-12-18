package bgu.spl.mics.application.services;

import java.util.concurrent.ConcurrentLinkedQueue;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CloseAllBroadcast;
import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Model.Results;

/**
 * Conference service is in charge of
 * aggregating good results and publishing them via the
 * {@link PublishConfrenceBroadcast},
 * after publishing results the conference will unregister from the system.
 * This class may not hold references for objects which it is not responsible
 * for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ConferenceService extends MicroService {
    private ConcurrentLinkedQueue<String> successfulModels;

    public ConferenceService(String name) {
        super(name);

        successfulModels = new ConcurrentLinkedQueue<>();
    }

    @Override
    protected void initialize() {
        subscribeEvent(PublishResultsEvent.class, this::handlePublishResultsEvent);
        subscribeBroadcast(CloseAllBroadcast.class, __ -> terminate());
    }

    private void handlePublishResultsEvent(PublishResultsEvent event) {
        System.out.println("got event: " + event.toString());
        Model model = event.getModel();
        if (model.getResults() != Results.Good) {
            // we don't need to crash, just refuse to publish
            System.out.println("WARNING - tried to publish model without good results, model results: "
                    + model.getResults().toString());

            complete(event, null);
            return;
        }

        successfulModels.add(model.getName());
        complete(event, model);
    }
}
