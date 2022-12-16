package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.application.objects.Developer;

import java.util.HashMap;

public class PublishConferenceBroadcast implements Broadcast {

    private final HashMap<Developer, Integer> publishing;

    public PublishConferenceBroadcast(HashMap<Developer, Integer> publishing) {
        this.publishing = publishing;
    }

    public HashMap<Developer, Integer> getPublishing() {
        return publishing;
    }
}