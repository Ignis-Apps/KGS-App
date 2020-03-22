package de.kgs.vertretungsplan.broadcaster;

import android.util.Log;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Broadcast {

    private static final String TAG = "Broadcaster";
    private final Map<Receiver, List<BroadcastEvent>> observers = new HashMap<>();

    /**
     * Subscribes the given observer to the given broadcast events.
     * The observer will receive events until the subscription is canceled.
     *
     * @param receiver the observer that should subscribe
     * @param event    the event/s to which the observer will subscribe
     */
    public void subscribe(Receiver receiver, BroadcastEvent... event) {

        List<BroadcastEvent> list = observers.get(receiver);
        if (list == null)
            list = new LinkedList<>();
        for (BroadcastEvent e : event) {
            if (list.contains(e))
                continue;
            list.add(e);
            observers.put(receiver, list);
        }
    }

    /**
     * Sends the given command to all subscribed observers
     *
     * @param events the events that should be send
     */
    public void send(BroadcastEvent... events) {

        for (BroadcastEvent event : events) {

            Log.d(TAG, "send: " + event);

            for (Receiver receiver : observers.keySet()) {
                List<BroadcastEvent> subscribedEvents = observers.get(receiver);
                if (subscribedEvents == null) {
                    throw new AssertionError("Bug");
                }
                if (subscribedEvents.contains(event)) {
                    receiver.onEventTriggered(event);
                }
            }

        }

    }

    // Not yet implemented. Beware of Concurrent Modification !.
    public void unsubscribe(Receiver receiver) {
        //observers.remove(observer);
    }

    public interface Receiver {
        void onEventTriggered(BroadcastEvent broadcastEvent);
    }
}
