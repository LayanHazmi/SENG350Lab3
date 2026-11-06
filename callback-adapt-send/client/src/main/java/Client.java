import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;

import Demo.CallbackPrx;
import Demo.ImageManagerPrx;

public class Client {
    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.out.println("usage: Client <dir> <maxImages(0=all)> <periodMs> <pctGeneralInterest>");
            return;
        }
        File dir = new File(args[0]);
        int busId = Integer.parseInt(dir.getName());   // directory name = busId
        int maxImages = Integer.parseInt(args[1]);
        int periodMs = Integer.parseInt(args[2]);
        int pctGI = Integer.parseInt(args[3]);

        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".jpg"));
        if (files == null || files.length == 0) {
            System.out.println("no .jpg files in " + dir);
            return;
        }
        Arrays.sort(files);
        int total = (maxImages == 0) ? files.length : maxImages;

        try (Communicator communicator = Util.initialize(args, "client.cfg")) {
            ImageManagerPrx server = ImageManagerPrx.checkedCast(
                    communicator.propertyToProxy("ImageManager.Proxy"));

            CallbackImp callbackImp = new CallbackImp(busId);
            communicator.getProperties().setProperty("Callback.Endpoints", "tcp -p " + (18000 + busId));
            ObjectAdapter adapter = communicator.createObjectAdapter("Callback");
            ObjectPrx objectPrx = adapter.add(callbackImp, Util.stringToIdentity("Callback"));
            adapter.activate();

            server.subscribe(busId, CallbackPrx.uncheckedCast(objectPrx));

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            long next = System.nanoTime();

            for (int i = 0; i < total; i++) {
                // exactly pctGI% of images are GENERALINTEREST, evenly spread
                String tag = ((i + 1) * pctGI) / 100 > (i * pctGI) / 100 ? "GENERALINTEREST" : "NORMAL";
                byte[] image = Files.readAllBytes(files[i % files.length].toPath());

                server.sendImage(busId, tag, LocalDateTime.now().format(fmt), image);

                next += periodMs * 1_000_000L;
                long sleepMs = (next - System.nanoTime()) / 1_000_000L;
                if (sleepMs > 0) Thread.sleep(sleepMs);
            }

            Thread.sleep(3000);   // keep the callback adapter alive for late notifications
            System.out.println("bus " + busId + ": sent " + total
                    + ", notifications received " + callbackImp.getReceived());
        }
    }
}