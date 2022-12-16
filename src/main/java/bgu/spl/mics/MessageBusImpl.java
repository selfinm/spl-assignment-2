package bgu.spl.mics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus
 * interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

	Map<String, Queue<Message>> serviceQueues;
	Map<Class<? extends Message>, List<String>> messageSubscribers;

	private static MessageBusImpl instance = null;

	private MessageBusImpl() {
		serviceQueues = Collections.synchronizedMap(new HashMap<>());
		messageSubscribers = Collections.synchronizedMap(new HashMap<>());
	}

	public static MessageBusImpl getInstance() {
		if (instance == null)
			instance = new MessageBusImpl();

		return instance;
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
		if (!serviceQueues.containsKey(m.getName())) {
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
		// TODO Auto-generated method stub

	}

	@Override
	public void sendBroadcast(Broadcast b) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void register(MicroService m) {
		serviceQueues.put(m.getName(), new ConcurrentLinkedQueue<>());
	}

	@Override
	public void unregister(MicroService m) {
		// TODO Auto-generated method stub

	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

}
