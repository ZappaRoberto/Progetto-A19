/* @author Gruppo A19
    MultiThreaded server per gestire gameRoom multiple fino a 100 giocatori in contemporanea
 */
import server.MultiServer;

import java.io.IOException;

public class ServerMain {


    public static void main(String[] args) throws IOException{
        MultiServer server = new MultiServer();
        int port = 9876;
        server.start(port);
    }
}