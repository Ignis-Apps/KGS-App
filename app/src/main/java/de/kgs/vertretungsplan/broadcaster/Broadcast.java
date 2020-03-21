package de.kgs.vertretungsplan.broadcaster;

import android.util.Log;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class Broadcast {

    private static final String TAG = "Broadcaster";
    private final Map<Observer, List<BroadcastEvent>> observers = new HashMap<>();

    public interface Observer {
        void onEventTriggered(BroadcastEvent broadcastEvent);
    }

    public void subscribe(Observer observer, BroadcastEvent... event) {

        List<BroadcastEvent> list = observers.get(observer);
        if (list == null)
            list = new LinkedList<>();

        for (BroadcastEvent e : event) {
            if (list.contains(e))
                continue;
            list.add(e);
            observers.put(observer, list);
        }
    }

    public void send(BroadcastEvent event) {

        Log.d(TAG, "send : " + event);
        for (Observer observer : observers.keySet()) {
            List<BroadcastEvent> subscribedEvents = observers.get(observer);
            if (subscribedEvents == null)
                throw new AssertionError("Bug");
            observer.onEventTriggered(event);
        }
    }

    public void unsubscribe(Observer observer) {
        this.observers.remove(observer);
    }
}
