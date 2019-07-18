package server;

import game_management.Player;

import java.io.ObjectOutputStream;
import java.net.Socket;

public class User {
    private String name;
    private Socket socket;
    private Player player;
    private ObjectOutputStream oos;

     User(String name,int order) {
        this.name = name;
        this.player = new Player(this.name,order);
    }

    @Override
    public String toString() {
        return "server.User{" +
                "name='" + name + '\'' +
                '}';
    }

    void setSocket(Socket socket) {
        this.socket = socket;
    }

    Socket getSocket() {
        return socket;
    }

    void setOos(ObjectOutputStream oos) {
        this.oos = oos;
    }

    ObjectOutputStream getOos() {
        return oos;
    }

    Player getPlayer() {
        return player;
    }
}
