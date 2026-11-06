import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;

public class Server {
    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args, "server.cfg")) {
            ObjectAdapter adapter = communicator.createObjectAdapter("Service");
            adapter.add(new ImageManagerImp(), Util.stringToIdentity("ImageManager"));
            adapter.activate();
            System.out.println("Server ready");
            communicator.waitForShutdown();
        }
    }
}