package bgu.spl.mics.application.messages;

import java.util.Collection;

import bgu.spl.mics.Broadcast;

public class PublishConferenceBroadcast implements Broadcast {

    private Collection<String> modelNames;

    public PublishConferenceBroadcast(Collection<String> modelNames) {
        this.modelNames = modelNames;
    }

    public Collection<String> getModelNames() {
        return modelNames;
    }
}