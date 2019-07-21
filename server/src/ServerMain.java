
import server.MultiServer;

import java.io.IOException;
/**
 MultiThreaded server per gestire gameRoom multiple fino a 100 giocatori in contemporanea
 <p>
 Punto di Avvio del server
 </p>
 @author Ludovico Viola
 */
public class ServerMain {


    public static void main(String[] args) throws IOException{
        MultiServer server = new MultiServer();
        int port = 9876;
        server.start(port);
    }
}