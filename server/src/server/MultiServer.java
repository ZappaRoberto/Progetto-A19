package server;

import card_management.Card;

import finals.Message;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class MultiServer {
    private boolean exit = false;
        private ServerSocket serverSocket;
    private  static ArrayList<User> loggedUsers = new ArrayList<>();
    private int counter = -1;
    private final Object lockRoomHandler = new Object();
    private ArrayList<GameRoom> rooms = new ArrayList<>();

    //create one thread for each client connected
    public void start(int port) throws IOException {
            serverSocket = new ServerSocket(port);
            RoomsHandler roomsHandler = new RoomsHandler(lockRoomHandler);
            roomsHandler.start();

            while (!exit) {
                        new ClientHandler(serverSocket.accept()).start();
            }
        }

    //close the ServerSocket object
        public void stop() throws IOException {
            exit =  true;
            serverSocket.close();
        }

    /* Thread che gestisce l'interazione con il singolo client */
        private  class ClientHandler extends Thread {
            private Socket clientSocket;
            private boolean exit = false;
            private User user;
            private ObjectOutputStream oos;
            private boolean flag = true;
            private int roomNumber;

             ClientHandler(Socket socket) {
                System.out.println("Connection created");
                this.clientSocket = socket;
            }

            public void run()  {
                System.out.println("Waiting for client request");
                try {
                    ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
                    oos = new ObjectOutputStream(clientSocket.getOutputStream());
                    while (!exit) {
                        String message = (String) ois.readObject();
                        System.out.println(message);
                        String[] details = message.split("&");
                        String control = details[0] + "&";
                        if (flag) {
                          if(Message.LOGIN.equals(control)){
                                login(details);
                          }
                        }
                        else {
                            gameUpdate(details, control);
                        }
                    }
                }catch (IOException | ClassNotFoundException e) {
                    System.out.println("Client" + user.getPlayer().getPlayerID() + " disconnected");
                    terminate();
                }
            }

            private void gameUpdate(String[] details, String control) {
                switch (control) {
                    case Message.SENDING_BET:
                    rooms.get(roomNumber).setCurrentBet(Integer.parseInt(details[1]));
                    rooms.get(roomNumber).setBetDone();
                    synchronized (rooms.get(roomNumber).getLock()) {
                        rooms.get(roomNumber).getLock().notifyAll();
                    }
                    System.out.println("message received");
                    System.out.println("bet setted");
                    break;
                    case Message.CHOOSE_YOUR_FELLOW:
                    Card card = new Card(details[1]);
                    rooms.get(roomNumber).setFellowCard(card);
                    rooms.get(roomNumber).setFellowChosen();
                    synchronized (rooms.get(roomNumber).getLock()) {
                        rooms.get(roomNumber).getLock().notifyAll();
                    }
                    break;
                    case Message.YOUR_TURN:
                    Card card1 = new Card(details[1]);
                    rooms.get(roomNumber).setCardSelected(card1);
                    rooms.get(roomNumber).setCardSelected();
                    synchronized (rooms.get(roomNumber).getLock()) {
                        rooms.get(roomNumber).getLock().notifyAll();
                    }
                    break;
                    case Message.END_OF_GAME:
                        loggedUsers.remove(user);
                        rooms.get(roomNumber).setGameEndedCounter(rooms.get(roomNumber).getGameEndedCounter()+ 1);
                        break;
                    default:
                        break;
                }
            }

            private void login(String[] details) {
                    counter++;
                    roomNumber = rooms.size() - 1;
                    User userTest = new User(details[1], counter);
                    userTest.setSocket(clientSocket);
                    userTest.setOos(oos);
                    user = userTest;
                    rooms.get(roomNumber).addUser(userTest);
                    loggedUsers.add(userTest);
                    System.out.println(rooms.get(roomNumber));
                    //oos.close();
                    flag = false;
                    if (rooms.get(roomNumber).isReady()) {
                        new Thread(() -> rooms.get(roomNumber).startGame()).start();
                        synchronized (lockRoomHandler) {
                            lockRoomHandler.notifyAll();
                        }
                    }
                }

            void terminate() {
                exit = true;
            }
        }
    /* Thread che gestisce la creazione di nuove stanze  */
        private class RoomsHandler extends Thread {
            private final Object lock1;
            private boolean exit = false;
            public RoomsHandler(Object lock1) {
                this.lock1 = lock1;
                final Object lock = new Object();
                 GameRoom room = new GameRoom(lock);
                rooms.add(room);
            }

            public void run() {
                while (!exit) {
                    synchronized (lock1) {
                        while (!rooms.get(rooms.size()-1).isReady()) {
                            try {
                                lock1.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    System.out.println("Hi");
                    System.out.println(rooms.get(rooms.size()-1));

                    counter = -1;
                    final Object lock = new Object();
                    GameRoom room = new GameRoom(lock);
                    rooms.add(room);
                }
            }
            void terminate() {
                exit = true;
            }
            }
}
