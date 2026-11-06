import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.zeroc.Ice.Current;

import Demo.CallbackPrx;

public class ImageManagerImp implements Demo.ImageManager {

    private static final String ROOT = "team1";   

    private final Map<Integer, CallbackPrx> subscribers = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();
    private final AtomicLong stored = new AtomicLong();
    private final AtomicLong notified = new AtomicLong();
    private volatile boolean started = false;

    ImageManagerImp() {
        startMetrics();
    }

    @Override
    public void subscribe(int busId, CallbackPrx callback, Current current) {
        subscribers.put(busId, callback);
        System.out.println("subscribed: bus " + busId);
    }

    @Override
    public void sendImage(int busId, String tag, String timestamp, byte[] image, Current current) {
        started = true;
        store(busId, timestamp, image);
        stored.incrementAndGet();

        if ("GENERALINTEREST".equals(tag)) {
            notifyAllBuses("GENERALINTEREST from " + busId);
        }
        // NORMAL: stored only. CRITICAL is out of scope for this lab.
    }

    private void store(int busId, String timestamp, byte[] image) {
        try {
            Path dir = Paths.get(ROOT, String.valueOf(busId));
            Files.createDirectories(dir);   // creates it if missing, no-op if it exists
            String safeTs = timestamp.replace(':', '-').replace(' ', '_');
            Files.write(dir.resolve(safeTs + "_" + sequence.incrementAndGet() + ".jpg"), image);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void notifyAllBuses(String msg) {
        for (Map.Entry<Integer, CallbackPrx> e : subscribers.entrySet()) {
            e.getValue().notifyCallbackAsync(msg).whenComplete((result, ex) -> {
                if (ex == null) {
                    notified.incrementAndGet();
                } else {
                    subscribers.remove(e.getKey(), e.getValue());   // bus is gone
                }
            });
        }
    }

    private void startMetrics() {
        PrintWriter csv;
        try {
            csv = new PrintWriter(new FileWriter("throughput-" + (System.currentTimeMillis() / 1000) + ".csv"), true);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        csv.println("second,stored_per_s,notified_per_s,stored_total,notified_total");

        long[] last = {0, 0};
        int[] second = {0};
        ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        timer.scheduleAtFixedRate(() -> {
            if (!started) return;                      // no rows before the first message
            long s = stored.get(), n = notified.get();
            csv.println(++second[0] + "," + (s - last[0]) + "," + (n - last[1]) + "," + s + "," + n);
            last[0] = s;
            last[1] = n;
        }, 1, 1, TimeUnit.SECONDS);
    }
}
