package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
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
            // wait for a gpu to register
            Future<Model> trainedModelFuture = sendEvent(new TrainModelEvent(model));
            while (trainedModelFuture == null) {
                trainedModelFuture = sendEvent(new TrainModelEvent(model));

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }
            System.out.println("Developer " + getName() + " sent " + model.getName() + " for training");

            while (!trainedModelFuture.isDone()) {
                System.out.println("Developer " + getName() + " waiting for " + model.getName() + " training results");
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }

            Model trainedModel = trainedModelFuture.get();
            System.out.println("Developer " + getName() + " got " + model.getName() + " after training");

            // wait for a gpu to register
            Future<Model> testedModelFuture = sendEvent(new TestModelEvent(trainedModel, developer));

            while (testedModelFuture == null) {
                testedModelFuture = sendEvent(new TestModelEvent(model, developer));

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }
            System.out.println("Developer " + getName() + " sent " + model.getName() + " for testing");

            Model testedModel = testedModelFuture.get();
            System.out.println("Developer " + getName() + " got " + model.getName() + " after testing");

            if (testedModel.getResults() == Results.Good) {
                System.out.println("Developer " + getName() + " wants to publish " + model.getName());

                // wait for a conference to register
                Future<Model> publishedModelFuture = sendEvent(new PublishResultsEvent(testedModel));
                while (publishedModelFuture == null) {
                    publishedModelFuture = sendEvent(new PublishResultsEvent(model));

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
                System.out.println("Developer " + getName() + " sent " + model.getName() + " for publishing");

                Model publishedModel = publishedModelFuture.get();

                // update published models if model was published
                if (publishedModel != null) {
                    developer.addPublishedModel(publishedModel);
                    System.out.println("Developer " + getName() + " got " + model.getName() + " publish accepted");
                } else {
                    System.out.println("Developer " + getName() + " got " + model.getName() + " publish rejected");
                }
            } else {
                System.out.println("Developer " + getName() + " doesn't want to publish " + model.getName());
            }
        }

    }
}
