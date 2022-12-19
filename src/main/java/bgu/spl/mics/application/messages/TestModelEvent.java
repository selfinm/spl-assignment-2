package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Developer;
import bgu.spl.mics.application.objects.Model;

public class TestModelEvent implements Event<Model> {
    private Model model;
    private Developer developer;

    public TestModelEvent(Model model, Developer developer) {
        this.model = model;
        this.developer = developer;
    }

    public Developer getDeveloper() {
        return developer;
    }

    public Model getModel() {
        return model;
    }

    @Override
    public String toString() {
        return "TestModelEvent [model=" + model + ", developer=" + developer + "]";
    }
}