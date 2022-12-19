package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CloseAllBroadcast;
import bgu.spl.mics.application.messages.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.Developer;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Model.Results;

/**
 * developer is responsible for sending the {@link TrainModelEvent},
 * {@link TestModelEvent} and {@link PublishResultsEvent}.
 * In addition, it must sign up for the conference publication broadcasts.
 * This class may not hold references for objects which it is not responsible
 * for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class DeveloperService extends MicroService {
    private Developer developer;

    public DeveloperService(String name, Developer developer) {
        super(name);

        this.developer = developer;
    }

    void handlePublishConferenceBroadcast(PublishConferenceBroadcast broadcast) {
        for (String modelName : broadcast.getModelNames()) {
            developer.readPaper(modelName);
        }
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(PublishConferenceBroadcast.class, this::handlePublishConferenceBroadcast);
        subscribeBroadcast(CloseAllBroadcast.class, __ -> terminate());

        for (Model model : developer.getModels()) {
            Model trainedModel = sendEvent(new TrainModelEvent(model)).get();
            Model testedModel = sendEvent(new TestModelEvent(trainedModel, developer)).get();

            if (testedModel.getResults() == Results.Good) {
                Model publishedModel = sendEvent(new PublishResultsEvent(testedModel)).get();
                if (model != null)
                    developer.addPublishedModel(publishedModel);
            }
        }

    }
}
