package server;

import card_management.Card;
import card_management.Hand;
import card_management.Seme;
import card_management.Table;
import finals.Message;
import game_management.GameDataHolder;
import game_management.Player;

import java.io.IOException;
import java.util.ArrayList;
/**
 * Una volta raggiunti i 5 giocatori viene avviato un thread separato per gestire la partita
 * @author Ludovico Viola
 */
public class GameRoom {
    private boolean isReady;
    private ArrayList<User> users;
    private GameDataHolder game;
    private boolean betDone;
    private boolean fellowChosen;
    private boolean isCardSelected;
    private Card cardSelected;
    private String playingUser;
    private Card fellowCard;
    private final Object lock;
    private int currentBet = 0;
    private int gameEndedCounter;

     GameRoom(Object lock) {
        users = new ArrayList<>();
        isReady = false;
        betDone = false;
        fellowChosen = false;
        isCardSelected = false;
        this.lock = lock;
        gameEndedCounter=0;
    }
    /**
     * Thread partita avviato
     */
     void startGame() {
        System.out.println(users);
        new MultiplayerGameHandler().run();
    }

     void addUser(User user) {
            users.add(user);
            if (users.size() == 5) {
                isReady = true;
            }
    }

     boolean isReady() {
        return isReady;
    }


    @Override
    public String toString() {
        return "server.GameRoom{" +
                "isReady=" + isReady +
                ", users=" + users +
                '}';
    }



     void setCurrentBet(int currentBet) {
        this.currentBet = currentBet;
    }

     void setBetDone() {
        this.betDone = true;
    }

     Object getLock() {
        return lock;
    }

     void setFellowCard(Card fellowCard) {
        this.fellowCard = fellowCard;
    }

     void setFellowChosen() {
        this.fellowChosen = true;
    }

     void setCardSelected() {
        isCardSelected = true;
    }

     void setCardSelected(Card cardSelected) {
        this.cardSelected = cardSelected;
    }

     int getGameEndedCounter() {
        return gameEndedCounter;
    }

     void setGameEndedCounter(int gameEndedCounter) {
        this.gameEndedCounter = gameEndedCounter;
    }
    /**
     *Thread che gestisce la singola partita nella room
     */    private class MultiplayerGameHandler extends Thread {
        private Seme briscola;
        public void run() {
            try {
                setupNewGame();
                bettingTurn();
                chooseFellow();
                playPhase();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        /**
         * fase scelta compagno
         * @throws IOException
         */
        private void chooseFellow() throws IOException {
            sendMessage(Message.NO_BET);
            for (User u: users) {
                if(u.getPlayer().getOrder()== game.startingPlayer) {
                    System.out.println("user found "+u.toString());
                    sendMessage(u, Message.CHOOSE_YOUR_FELLOW);
                }
            }
            synchronized (lock) {
                while (!fellowChosen) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            game.setFellowCard(fellowCard);
            briscola = fellowCard.getSeme();
            game.createTeams();
            System.out.println(game.teamCaller);
            System.out.println(game.teamPopolo);
            String s = Message.CALLER+fellowCard.getCardId() +"&" + game.players.get(0).getPlayerID();
            sendMessage(s);
            sendMessage(Message.NO_BET);
        }
        /**
         * fase scommesse
         * @throws IOException
         */
        private void bettingTurn() throws IOException {
            while (game.bettingPlayers.size()!=1) {
                for (Player p : game.bettingPlayers) {
                    System.out.println(p);
                    int bet;
                    if (!(game.bettingPlayers.contains(p) && game.bettingPlayers.size() == 1)) {
                        for (User u: users) {
                            if(u.getPlayer().equals(p)) {
                                System.out.println("user found");
                                sendMessage(u, Message.YOUR_BETTING_TURN);
                                sendMessage(Message.LOG+u.getPlayer().getPlayerID()+ " is your betting turn!");
                            }
                        }

                        synchronized (lock) {
                            while (!betDone) {
                                try {
                                    lock.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        bet = currentBet; //bet selected by real player
                        String s = Message.LOG;
                        if(bet ==0) {
                            s += p.getPlayerID() +" Pass";
                        }
                        else {
                            s += p.getPlayerID() + " bet " + bet;
                        }
                        sendMessage(s);
                        betDone = false;
                        if (bet > 60 && bet < 121) {
                            if (bet > game.higherBet) {
                                game.higherBet = bet;
                                game.startingPlayer = p.getOrder();
                                sendMessage(Message.HIGHER_BET+bet);
                            }
                        } else if (bet == 0) {

                            game.bettingPlayers.remove(p);
                        }
                    }
                }
            }
            if(game.higherBet == 60) {
                bettingTurn();
            }
            String s = ("Starting player " + game.startingPlayer + "with bet: " + game.higherBet);
            sendMessage(Message.LOG+s);
            GameDataHolder.gameOrder(game.players,game.startingPlayer);

        }
        /**
         * fase di gioco
         * @throws IOException
         */
        private void playPhase() throws IOException {

            int hands = 0;
            Card c;

            while(hands!=8) {
                Table table = new Table(briscola);
                Hand hand = new Hand();
                int i = 0;
                for (Player p:game.players) {
                    p.setOrder(i);
                    i++;
                    for (User u: users) {
                        if(u.getPlayer().equals(p)) {
                            System.out.println("user found");
                            sendMessage(u, Message.YOUR_TURN);
                            playingUser = u.getPlayer().getPlayerID();
                        }
                    }
                        sendMessage(Message.MY_ICON_TURN+playingUser);
                        synchronized (lock) {
                            while (!isCardSelected) {
                                try {
                                    lock.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        c = cardSelected;
                    isCardSelected = false;
                    sendMessage(Message.LOG+ p.getPlayerID()+" throws "+ c.getCardId());

                    table.addCard(c, p);
                    hand.addCard(c);
                    sendMessage(Message.UPDATE+c.getCardId());
                    sendMessage(Message.MY_ICON_TURN+playingUser);
                    waitTime();
                }
                hands++;
                for (Player p:game.players) {
                    if(table.getWinner().equals(p.getPlayerID())) {
                        p.winHand(table.getCards());
                        sendMessage(Message.LOG+"hand&"+p.getPlayerID());
                    }
                }

                GameDataHolder.gameOrder(game.players,table.getStartingPlayer());

            }
            sendMessage(Message.LOG+game.makeScoreBoard());
            sendMessage(Message.END_OF_GAME);

        }


        /**
         * genera tempi morti
         */
        private void waitTime() {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /**
         * inserisce i dati dei giocatori nel data Holder e inizializza la partita
         * @throws IOException
         */
        private void setupNewGame() throws IOException {
            game = new GameDataHolder();
            fellowCard = null;
            for (User user : users) {
                game.addPlayers(user.getPlayer());
            }
            game.distributeCard();
            System.out.println(game);
            for(User u: users) {
                for(Card c:u.getPlayer().getHand().getCards()) {
                    String s = Message.SENDING_CARD + c.getCardId();
                    sendMessage(u,s);
                }
            }
            for(User u: users) {
                for(User p: users) {
                    if(!u.equals(p)) {
                        String s = Message.SENDING_NAME + p.getPlayer().getPlayerID();
                        sendMessage(u,s);
                    }
                }
            }
        }
        /**
         * Invia messaggi al giocatore singolo
         * @param user giocatore interessato
         * @param message messaggio
         * @throws IOException
         */
        private void sendMessage(User user, String message) throws IOException {
            if(user.getSocket().isConnected()) {
                waitTime();
                user.getOos().writeObject(message);
                System.out.println(message);
                //oos.close();
            }
        }
        /**
         * Invia messaggi a tutti igiocatori
         * @param message messaggio
         * @throws IOException
         */
        private void sendMessage(String message) throws IOException {
            for (User u:users) {
                waitTime();
                if (u.getSocket().isConnected()) {
                    u.getOos().writeObject(message);
                    System.out.println(message);
                    //oos.close();
                }
            }
        }
    }
}


