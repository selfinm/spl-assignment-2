package bgu.spl.mics.application.services;

import java.util.Timer;
import java.util.TimerTask;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CloseAllBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;

/**
 * TimeService is the global system timer There is only one instance of this
 * micro-service.
 * It keeps track of the amount of ticks passed since initialization and
 * notifies
 * all other micro-services about the current time tick using
 * {@link TickBroadcast}.
 * This class may not hold references for objects which it is not responsible
 * for.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends MicroService {

    private int speed, duration;
    private long time;

    public TimeService(int speed, int duration) {
        super("TimeService");

        this.speed = speed;
        this.duration = duration;
        this.time = 0;
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(CloseAllBroadcast.class, _c -> {
            this.terminate();
        });

        Timer timer = new Timer();

        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                time += speed;

                if (time >= duration) {
                    sendBroadcast(new CloseAllBroadcast());
                    timer.cancel();
                    timer.purge();
                } else {
                    sendBroadcast(new TickBroadcast());
                }
            }

        }, 0, speed);
    }

}
