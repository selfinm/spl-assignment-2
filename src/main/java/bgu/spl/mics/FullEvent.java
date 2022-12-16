package bgu.spl.mics;

public class FullEvent<T> {

    public Event<T> event;

    public Future<T> future;

    public FullEvent(Event<T> event, Future<T> future) {
        this.event = event;
        this.future = future;
    }

    public Event<T> getEvent() {
        return event;
    }

    public Future<T> getFuture() {
        return future;
    }

}
