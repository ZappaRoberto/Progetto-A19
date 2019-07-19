package game_management.game;

import GUI.frames.ChoseFellowScreen;
import GUI.frames.OnlineGameScreen;
import GUI.frames.UserLoginScreen;
import card_management.Card;
import card_management.Deck;
import finals.Message;
import game_management.players.OnlinePlayer;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
/**
 * Controlla tutta la logica del gioco
 * @author Team A19
 */
public class Client {
    private ArrayList<String> messagesQue;
    private final  Object screenLock = new Object();
    private final  Object remoteLock = new Object();
    private LocalGame game;
    private  ObjectInputStream ois;
    private ObjectOutputStream oos;
    private boolean gameRoomReady;
    private boolean isIconVisible;
    private OnlinePlayer onlinePlayer;
    private int counter;
    private Deck deck;
    private GameStatus gameStatus;
    private OnlineGameScreen screen;
    private UserLoginScreen loginScreen;
    private boolean isBettingEnded;
    private boolean isGameEnded;
    private ListenForMessages listenForMessages;
    private ArrayList<String> opponentsNames;
    private HandleMessages handleMessages;

    public Client() {
        resetEnvironment();
    }
    /**
     * Apre il menu e in base a input crea partite locale o avvia comunicazione client-server
     */
    public void startGame() {
        game.goToMenuScreen();
        if (game.isMultiplayer()) {
            runMultiplayerGame();
        }
    }
    /**
     * avvia partita online
     */
    private void runMultiplayerGame() {
            try {
                setup();
                bet();
                chooseFellow();
                play();
            } catch (IOException | InterruptedException e) {
                if (e.getMessage().equals("Connection reset")) {
                    resetGame();
                } else {
                    e.printStackTrace();
                }
            }
    }
    /**
     * fase di gioco
     *
     * @throws InterruptedException lanciata se interrotta comunicazione con server
     * @throws IOException
     */
    private void play() throws InterruptedException, IOException {
        while (!isGameEnded) {
            synchronized (remoteLock) {
                while (!gameStatus.equals(GameStatus.MY_TURN)) {
                    remoteLock.wait();
                }
            }
            screen.setCardsEnabled(true);
            screen.setActionListener();
            synchronized (screenLock) {
                while (screen.isTurnDone()) {
                    screenLock.wait();
                }
            }
            sendMessage(Message.YOUR_TURN + onlinePlayer.pickACard(screen.getImageString()).getCardId());
            screen.updatePlayerCards(onlinePlayer);
            screen.setTurnDone(false);
            screen.setCardsEnabled(false);
            gameStatus = GameStatus.WAIT;
        }
    }
    /**
     * fase di scelta compagno
     *
     * @throws InterruptedException lanciata se interrotta comunicazione con server
     * @throws IOException
     */
    private void chooseFellow() throws InterruptedException, IOException {
        synchronized (remoteLock) {
            while (gameStatus.equals(GameStatus.WAIT)) {
                remoteLock.wait();
            }
        }
        if (gameStatus.equals(GameStatus.CHOOSE_FELLOW)) {
            screen.setVisible(false);
            ChoseFellowScreen choseFellowScreen = new ChoseFellowScreen(onlinePlayer, screenLock);
            choseFellowScreen.setVisible(true);
            synchronized (screenLock) {
                while (choseFellowScreen.isFellowChosen()) {
                    screenLock.wait();
                }
            }
            String cardChosen = Message.CHOOSE_YOUR_FELLOW + choseFellowScreen.getCardChosen().getCardId();
            sendMessage(cardChosen);
            screen.setVisible(true);
        }
        screen.addNameOnIcons(opponentsNames);
        screen.setTableVisibility(true);
        screen.update(screen.getGraphics());
        screen.revalidate();

        screen.repaint();
    }
    /**
     * fase scommessa
     *
     * @throws InterruptedException lanciata se interrotta comunicazione con server
     * @throws IOException
     */
    private void bet() throws InterruptedException, IOException {
        while (!isBettingEnded) {
            synchronized (remoteLock) {
                while (gameStatus.equals(GameStatus.WAIT)) {
                    remoteLock.wait();
                }
            }
            if (gameStatus.equals(GameStatus.BETTING)) {
                screen.setBetAreaVisibility(true);
                synchronized (screenLock) {
                    while (screen.isBetDone()) {
                        try {
                            screenLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                screen.setBetDone(false);
                String bet = Message.SENDING_BET + screen.getBet();
                screen.setBetAreaVisibility(false);
                sendMessage(bet);
                gameStatus = GameStatus.WAIT;
            } else if (gameStatus.equals(GameStatus.RUNNING)) {
                isBettingEnded = true;
                gameStatus = GameStatus.WAIT;
            }
        }
    }
    /**
     * Inizializzazione partita online
     *
     * @see ListenForMessages
     * @see HandleMessages
     * @throws IOException
     * @throws InterruptedException lanciata se interrotta comunicazione con server
     */
    private void setup() throws IOException, InterruptedException {
        loginScreen = new UserLoginScreen(screenLock);
        loginScreen.getFrame().setVisible(true);
        synchronized (screenLock) {
            while (!loginScreen.isLogged()) {
                try {
                    screenLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        loginScreen.setLoginPanelVisibility(false);

            login(loginScreen.getUsername(), loginScreen.getIp());
        listenForMessages = new ListenForMessages();
        Thread thread = new Thread(listenForMessages);
        thread.start();
        handleMessages = new HandleMessages();
        Thread thread1 = new Thread(handleMessages);
        thread1.start();

        onlinePlayer = new OnlinePlayer(loginScreen.getUsername());
        synchronized (remoteLock) {
            while (gameStatus != GameStatus.SETUP) {
                remoteLock.wait();
            }
        }
        onlinePlayer.sortHand();
        System.out.println(onlinePlayer);
        loginScreen.dispose();
        screen = new OnlineGameScreen(onlinePlayer, screenLock);
        screen.pack();
        screen.setLocationRelativeTo(null);
        screen.setVisible(true);
        screen.setGameType(GameType.ONLINE);
        screen.setTurnDone(false);
        isBettingEnded = false;
    }
    /**
     * resetta partita se connessione interrotta
     */
    private void resetGame() {
        System.out.println("Connection with Server lost, Game Restarting");
        loginScreen.dispose();
        if(gameRoomReady) {
            this.screen.dispose();
        }
        if(handleMessages.isRunning && listenForMessages.isRunning) {
            this.listenForMessages.stop();
            this.handleMessages.stop();
        }
        resetEnvironment();
        startGame();
    }
    /**
     * resetta variabili d'istanza
     */
    private void resetEnvironment() {
        messagesQue = new ArrayList<>();
        opponentsNames = new ArrayList<>();
        game = new LocalGame(screenLock);
        gameRoomReady = false;
        isGameEnded = false;
        isIconVisible = false;
        counter = 0;
        deck = new Deck();
        gameStatus = GameStatus.WAIT;
    }
    /**
     * Event listener che riceve messaggi dal server e li aggiunge a una lista
     */
    private class ListenForMessages implements Runnable {
        private volatile boolean exit;
        private boolean isRunning = false;
        @Override
        public void run() {
            isRunning = true;
            try {
                while (!exit) {
                    String message;
                    message = (String) ois.readObject();
                    System.out.println("messageReceived " + message);
                    messagesQue.add(message);
                }
            }catch (IOException | ClassNotFoundException e) {
                    if(e.getMessage().equals("Connection reset")) {
                        resetGame();
                    }
                    else {
                        e.printStackTrace();
                    }
            }
        }
        void stop() {
            exit = true;
        }
    }
    /**
     * Observer che elabora i messaggi nella lista e sblocca i threads interessati
     *
     * @see ListenForMessages
     */
    private class HandleMessages implements Runnable {
        private volatile boolean exit;
        private boolean isRunning = false;

        HandleMessages() {
            this.exit = false;
        }

        @Override
        public void run() {
            isRunning = true;
            while (!exit) {
                if(!messagesQue.isEmpty()) {
                    String message = messagesQue.get(0);
                    String[] details = message.split("&");
                    System.out.println("messageHandled " + message);
                    String control = details[0] + "&";
                    switch (control) {
                        case Message.SIGN_UP:
                            if(details[1].equals("ok")) {
                                String s = "You have correctly signed up! Now you can play Online!";
                                JOptionPane.showMessageDialog(loginScreen.getFrame(),s,"Successfull",JOptionPane.INFORMATION_MESSAGE);
                                resetGame();
                                try {
                                    ois.close();
                                    oos.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                        case Message.ERROR:
                            String s = details[1];
                            JOptionPane.showMessageDialog(loginScreen.getFrame(),s,"Attention",JOptionPane.ERROR_MESSAGE);
                            resetGame();
                            break;
                        case Message.NO_BET:
                            isBettingEnded = true;
                            gameStatus = GameStatus.RUNNING;
                            synchronized (remoteLock) {
                                remoteLock.notifyAll();
                            }
                            break;
                        case Message.CHOOSE_YOUR_FELLOW:
                            gameStatus = GameStatus.CHOOSE_FELLOW;
                            synchronized (remoteLock) {
                                remoteLock.notifyAll();
                            }
                            break;
                        case Message.SENDING_CARD:
                            gameRoomReady = true;
                            onlinePlayer.draw(convertCard(details[1]));
                            counter++;
                            if (counter == 8) {
                                gameStatus = GameStatus.SETUP;
                                synchronized (remoteLock) {
                                    remoteLock.notifyAll();
                                }
                            }
                            break;
                        case Message.YOUR_BETTING_TURN:
                            gameStatus = GameStatus.BETTING;
                            synchronized (remoteLock) {
                                remoteLock.notifyAll();
                            }
                            break;
                        case Message.SENDING_NAME:
                            opponentsNames.add(details[1]);
                            break;
                        case Message.LOG:
                            if(details[1].equals("hand")) {
                                screen.logHandWinner(details[2]);
                            }
                            else {
                                screen.log(details[1]);
                            }
                            break;
                        case Message.UPDATE:
                            deck = new Deck();
                            Card card = new Card(details[1]);
                            for (Card c:deck.getDeck()) {
                                if(c.equals(card)) {
                                    card.setCardImage(c.getCardImage());
                                }
                            }
                            screen.updateTableCards(card);
                            break;
                        case Message.HIGHER_BET:
                            screen.setHigherBet(Integer.parseInt(details[1]));
                            screen.updateSpinner();
                            break;
                        case  Message.YOUR_TURN:
                            gameStatus = GameStatus.MY_TURN;
                            synchronized (remoteLock) {
                                remoteLock.notifyAll();
                            }
                            break;
                        case Message.MY_ICON_TURN:
                            if(!details[1].equals(onlinePlayer.getPlayerName())) {
                                if (!isIconVisible) {
                                    screen.showYourTurn(details[1], true);
                                    isIconVisible = true;
                                } else {
                                    screen.showYourTurn(details[1], false);
                                    isIconVisible = false;
                                }
                            }
                            break;
                        case Message.CALLER:
                            screen.showBriscola(convertCard(details[1]),details[2]);
                            break;
                        case Message.END_OF_GAME:
                            isGameEnded = true;
                            try {
                                sendMessage(Message.END_OF_GAME + onlinePlayer.getPlayerName());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            screen.setExitButtonVisibility(true);
                            synchronized (screenLock) {
                                while (screen.isGameEnded()) {
                                    try {
                                        screenLock.wait();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                                screen.setVisible(false);
                                resetGame();
                                break;
                        default:
                            break;
                    }
                    messagesQue.remove(0);
                }
            }
        }
        void stop() {
            exit = true;
        }
    }


    /**
     * converte i messaggi in oggetti carte
     *
     * @param message stringa con id carta
     * @return oggetto carta con immagine associata
     */
    private Card convertCard(String message) {
        Card card = new Card(message);
        for (Card c:deck.getDeck()) {
            if(c.equals(card)) {
                card.setCardImage(c.getCardImage());
            }
        }
        return card;
    }

    private void login(String name, String ip) throws IOException, InterruptedException {
        createSocket(ip);
        String s = Message.LOGIN + name ;
            sendMessage(s);
            Thread.sleep(100);
        }

    /**
     * Apre connessione con server
     * @param ip ip del server
     * @throws IOException
     */
    private void createSocket(String ip) throws IOException {
        ois = null;
        int port = 9876;
        Socket socket = new Socket(ip, port);
        oos = new ObjectOutputStream(socket.getOutputStream());
        ois = new ObjectInputStream(socket.getInputStream());
        System.out.println("Sending request to Socket Server");
    }
    /**
     * Invia messaggi al server
     * @param message messaggio da inviare
     * @throws IOException
     */
    private void sendMessage(String message) throws IOException {
        System.out.println("Sending request to Socket Server");
        System.out.println(message);
        oos.writeObject(message);
    }
}
