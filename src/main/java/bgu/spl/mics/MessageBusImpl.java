package bgu.spl.mics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus
 * interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

    public Map<String, Queue<Message>> serviceMessages;
    Map<Class<? extends Message>, List<String>> messageSubscribers;
    Map<Class<? extends Message>, Integer> roundRobinCounters;
    Map<Event<?>, Future<?>> eventFutures;

    private static MessageBusImpl instance = null;

    private MessageBusImpl() {
        serviceMessages = new ConcurrentHashMap<>();
        messageSubscribers = new ConcurrentHashMap<>();
        roundRobinCounters = new ConcurrentHashMap<>();
        eventFutures = new ConcurrentHashMap<>();
    }

    public static MessageBusImpl getInstance() {
        if (instance == null)
            instance = new MessageBusImpl();

        return instance;
    }

    public static void shutdown() {
        instance = null;
    }

    @Override
    public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
        subscribeMicroservice(type, m);
    }

    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
        subscribeMicroservice(type, m);
    }

    private void subscribeMicroservice(Class<? extends Message> type, MicroService m) {
        if (!serviceMessages.containsKey(m.getName())) {
            // TODO: do we want to handle this differently
            System.out.println("Service wasn't registered, doing nothing");
            return;
        }

        List<String> def = Collections.synchronizedList(new ArrayList<>());

        messageSubscribers.putIfAbsent(type, def);
        messageSubscribers.get(type).add(m.getName());
    }

    @Override
    public <T> void complete(Event<T> e, T result) {
        ((Future<T>) eventFutures.get(e)).resolve(result);
    }

    @Override
    public void sendBroadcast(Broadcast b) {
        for (String microservice : messageSubscribers.get(b.getClass())) {
            serviceMessages.get(microservice).add(b);
        }
    }

    @Override
    public <T> Future<T> sendEvent(Event<T> e) {
        List<String> subscribers = messageSubscribers.get(e.getClass());

        if (subscribers == null) {
            return null;
        }

        roundRobinCounters.putIfAbsent(e.getClass(), Integer.valueOf(0));
        Integer eventCounter = roundRobinCounters.get(e.getClass());

        String subscriber = subscribers.get(eventCounter % subscribers.size());
        eventCounter++;
        roundRobinCounters.put(e.getClass(), eventCounter % subscribers.size());

        Future<T> future = new Future<>();
        eventFutures.put(e, future);

        serviceMessages.get(subscriber).add(e);

        return future;
    }

    @Override
    public void register(MicroService m) {
        serviceMessages.put(m.getName(), new ConcurrentLinkedQueue<>());
    }

    @Override
    public void unregister(MicroService m) {
        String name = m.getName();
        serviceMessages.remove(name);

        for (Map.Entry<?, List<String>> entry : messageSubscribers.entrySet()) {
            entry.getValue().remove(name);

            if (entry.getValue().isEmpty()) {
                messageSubscribers.remove(entry.getKey());

                // if the event is a Broadcast, this has no effect
                roundRobinCounters.remove(entry.getKey());
            }
        }

    }

    @Override
    public Message awaitMessage(MicroService m) throws InterruptedException {
        if (!serviceMessages.containsKey(m.getName())) {
            throw new IllegalStateException("m was never registered");
        }

        while (true) {
            Queue<Message> queue = serviceMessages.get(m.getName());
            Message msg = queue.poll();
            if (msg != null) {
                return msg;
            }
        }
    }

    public boolean isRegistered(MicroService... ms) {
        for (MicroService m : ms) {
            if (!serviceMessages.containsKey(m.getName())) {
                return false;
            }
        }

        return true;
    }
}
