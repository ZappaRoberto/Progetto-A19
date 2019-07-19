/* @author Gruppo A19
    MultiThreaded server per gestire gameRoom multiple fino a 100 giocatori in contemporanea
 */
import game_management.game.Client;

public class ClientMain {

    public static void main(String[] args) {
        new Thread(() -> {
            Client user = new Client();
            user.startGame();
        }).start();

    }
}
