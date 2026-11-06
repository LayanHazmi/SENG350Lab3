import java.util.concurrent.atomic.AtomicInteger;

import com.zeroc.Ice.Current;

public class CallbackImp implements Demo.Callback {

    private static final boolean VERBOSE = false;   // set false during experiments
    private final int busId;
    private final AtomicInteger received = new AtomicInteger();

    CallbackImp(int busId) {
        this.busId = busId;
    }

    @Override
    public void notifyCallback(String msg, Current current) {
        received.incrementAndGet();
        if (VERBOSE) System.out.println("[bus " + busId + "] notified: " + msg);
    }

    int getReceived() {
        return received.get();
    }
}