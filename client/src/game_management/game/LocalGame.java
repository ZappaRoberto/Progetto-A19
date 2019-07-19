package game_management.game;

import GUI.frames.ChoseFellowScreen;
import GUI.frames.GameScreen;
import GUI.frames.NewGameScreen;
import card_management.*;
import finals.Visibility;
import game_management.players.*;
import game_management.players.ai.AIPlayer;
import game_management.players.ai.AIPlayerEasy;
import game_management.players.ai.AIPlayerRandom;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Controlla la partita locale
 * @author Team A19
 */
public class LocalGame {
    private ArrayList<Player> players;
    private ArrayList<Player> teamCaller;
    private ArrayList<Player> teamPopolo;
    private Deck deck;
    private Semi briscola;
    private Card fellowCard;
    private int startingPlayer;
    private int higherBet;
    private CopyOnWriteArrayList<Player> bettingPlayers;
    private GameScreen screen;
    private final Object lock;
    private boolean isFirst;
    private boolean isMultiplayer;
    private GameType gameType;
    private  final int animationSpeed = 1500;

    LocalGame(Object lock) {
        this.lock = lock;
        isMultiplayer = false;
    }

    /**
     * resetta le variabili d'istanza
     */
    private void resetGameEnvironment() {
        higherBet = 60;
        startingPlayer = 0;
        isFirst = true;
        this.players = new ArrayList<>();
        generatePlayers();
        this.deck = new Deck();
        this.deck.shuffle();
        distributeCard();
        this.screen = new GameScreen(players.get(0), lock);
        bettingPlayers = new CopyOnWriteArrayList<>(players);
        this.screen.pack();
        this.screen.setLocationRelativeTo(null);
        this.screen.setVisible(false);
        this.screen.setGameType(gameType);

        this.screen.setTurnDone(false);
    }

    /**
     * controlla il flusso di gioco
     */
    private void startGame() {
        bettingTurn();
        chooseFellow();
        playPhase();
        makeScoreBoard();
    }

    /**
     * gestisce fase di scommessa
     */
    private void bettingTurn() {
        screen.enableCards(false);
        while (bettingPlayers.size()!=1) {
            for (Player p : bettingPlayers) {
                int bet = 0;
                p.sortHand();
                if(p instanceof AIPlayer) {
                    ((AIPlayer) p).setCardsForSuit();
                }
                switchScreen(p);
                if (!(bettingPlayers.contains(p) && bettingPlayers.size() == 1)) {
                    if(p instanceof ControlledPlayer) {
                        synchronized (lock) {
                            while (screen.isBetDone()) {
                                try {
                                    lock.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        bet = screen.getBet();
                    }
                    else if(p instanceof AIPlayer) {
                        try {
                            Thread.sleep(animationSpeed/2);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        AIPlayer aiPlayer = (AIPlayer) p;
                        int temp = aiPlayer.chooseBet();
                        while (!(temp<121 && temp> higherBet || temp == 0)) {
                            temp = aiPlayer.chooseBet();
                            if(p instanceof AIPlayerEasy) {
                                temp = 0;
                            }
                        }
                        bet = temp;
                    }

                    if (bet > 60 && bet < 121) {
                        if (bet > higherBet) {
                            higherBet = bet;
                            screen.setHigherBet(higherBet);
                            startingPlayer = p.getOrder();
                            screen.updateSpinner();
                            screen.displayBettingMove(p,bet);
                        }
                    } else if (bet == 0) {

                        screen.displayBettingMove(p,bet);
                        bettingPlayers.remove(p);
                    }
                    try {
                        Thread.sleep(animationSpeed/2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    screen.setBetDone(false);
                    isFirst = false;
                }
            }
        }
        if(higherBet == 60) {
            String b = "Start a new game_management";
            screen.diplayBettingWinner(b);
            resetGameEnvironment();
            startGame();
        }
        String s = ("Starting player " + startingPlayer + "with bet: " + higherBet);

        screen.diplayBettingWinner(s);
        try {
            Thread.sleep(animationSpeed/2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * gestisce scelta compagno
     */
    private void chooseFellow() {
        gameOrder(players,startingPlayer);
        ChoseFellowScreen screen1 = new ChoseFellowScreen(players.get(0),lock);
        if(players.get(0) instanceof ControlledPlayer) {
            screen1.setVisible(true);
            synchronized (lock) {
                while (screen1.isFellowChosen()) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            fellowCard = screen1.getCardChosen();
            screen.log("Carta Chiamata: " + fellowCard);

        }
        else if(players.get(0) instanceof AIPlayer) {
            try {
                Thread.sleep(animationSpeed/2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            AIPlayer player = (AIPlayer) players.get(0);
            fellowCard = player.chooseFellow();
            screen.log("Carta Chiamata: " + fellowCard);
            screen1.endPhase();
        }
        briscola = fellowCard.getSeme();
        screen.setListenerEnabled(true);
        screen.updatePlayerCards(players.get(0));
        screen.setLabelText(players.get(0));
        screen.showBriscola(fellowCard,players.get(0).getPlayerID());
        screen.update(screen.getGraphics());
        screen.revalidate();
        screen.repaint();

        createTeams();
        screen.setBetAreaVisibility(false);

    }

    private String makeScoreBoard() {
        int teamCallerScore= 0;
        int teamPopoloScore = 0;
        ArrayList<Player> scoreOrder = new ArrayList<>(players);
        scoreOrder.sort(Player::compareTo);
        int i = 1;
        StringBuilder s = new StringBuilder();
        for (Player p: scoreOrder) {
            if(teamCaller.contains(p)) {
                teamCallerScore+=p.getScore();
            }
            else if(teamPopolo.contains(p)) {
                teamPopoloScore+=p.getScore();
            }
            s.append("\n").append(i).append(". ").append(p.getPlayerID()).append(" Score: ").append(p.getScore());
            i++;
        }
        s.append("\nTeamCaller score: ").append(teamCallerScore).append("\nTeamPopolo score: ").append(teamPopoloScore);

        return s.toString();
    }

    /**
     * gestisce fase di gioco
     */
    private void playPhase() {
        screen.setActionListener();
        screen.addNameOnIcon(players);
        screen.setBettingTurn(false);
        int hands = 0;
        Card c = null;
        Card winningCard = null;

        while(hands!=8) {
            Table table = new Table(briscola);
            Hand hand = new Hand();
            int i = 0;
            for (Player p:players) {
                p.setOrder(i);
                i++;
                screen.setTableVisibility(true);
                switchScreen(p);
                screen.showYourTurn(p.getPlayerID(), Visibility.VISIBLE);
                if(p instanceof ControlledPlayer) {
                    synchronized (lock) {
                        while (screen.isTurnDone()) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    c = p.pickACard(screen.getImageString());
                    screen.updatePlayerCards(p);
                    screen.setCheck(false);
                }
                else if(p instanceof AIPlayer) {
                    try {
                        Thread.sleep(animationSpeed);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    AIPlayer player = (AIPlayer) p;
                    if(winningCard!=null) {
                        player.setTempWinningCard(winningCard);
                    }
                    if(table.getTempWinningPlayer()!=null) {
                        ((AIPlayer) p).setTempWinningPlayer(table.getTempWinningPlayer());
                    }
                    c = player.throwCard();
                    player.getHand().chooseCard(c);
                }
                screen.log(p.getOrder() + p.getPlayerID()+" throws "+ c);
                table.addCard(c, p);
                winningCard = table.getWinningCard();
                hand.addCard(c);
                screen.updateTableCards(c);
                screen.setTurnDone(false);
                screen.showYourTurn(p.getPlayerID(), Visibility.INVISIBLE);
            }
            hands++;
            for (Player p:players) {
                if(table.getWinner().equals(p.getPlayerID())) {
                    p.winHand(table.getCards());
                    screen.displayHandWinner(p);
                }
            }
            try {
                Thread.sleep(animationSpeed);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            gameOrder(players,table.getStartingPlayer());

        }
        screen.diplayScoreBoard(makeScoreBoard());
        screen.setExitButtonVisibility(true);
        synchronized (lock) {
            while (screen.isGameEnded()) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.exit(0);
        }

    }

    void goToMenuScreen() {
        NewGameScreen screen = new NewGameScreen(lock);
        screen.getFrame().pack();
        screen.getFrame().setLocationRelativeTo(null);
        screen.getFrame().setVisible(true);
        synchronized (lock) {
            while (!screen.isGameChosen()) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            this.gameType = screen.getGameType();
            if(gameType!=GameType.MULTIPLAYER) {
                resetGameEnvironment();
                this.screen.setVisible(true);
                startGame();
            }
            else {
                isMultiplayer = true;
            }
        }
    }

    /**
     * aggiorna la schermata in base al giocatore di turno
     * @param p giocatore di turno
     */
    private void switchScreen(Player p) {
        if (p.getOrder() != 0 || !isFirst) {
            screen.updatePlayerCards(p);
        }
        screen.setLabelText(p);
    }

    /**
     * genera i giocatori secondo il tipo di gioco scelto dall'utente
     */
    private void generatePlayers() {
        switch (gameType) {
            case CONTROLLED:
                for(int i = 0; i< 5; i++) {
                    ControlledPlayer player = new ControlledPlayer(i);
                    player.setCurrentBet(0);
                    players.add(player);
                }
                break;
            case SIMULATED:
                for(int i = 0; i < 2; i++) {
                    AIPlayer player1 = new AIPlayerEasy(i);
                    player1.setCurrentBet(0);
                    players.add(player1);
                }
                for(int i = 2; i < 5; i++) {
                    AIPlayer player1 = new AIPlayerRandom(i);
                    player1.setCurrentBet(0);
                    players.add(player1);
                }

                break;
            case EASY:
                ControlledPlayer player2 = new ControlledPlayer(0);
                player2.setCurrentBet(0);
                players.add(player2);
                for(int i = 1; i < 5; i++) {
                    AIPlayer player1 = new AIPlayerEasy(i);
                    player1.setCurrentBet(0);
                    players.add(player1);
                }
                break;
        }
    }

    private static <T> void gameOrder(ArrayList<T> players, int shift)
    {
        if (players.size() == 0)
            return;

        T element;
        for(int i = 0; i < shift; i++)
        {
            // remove first element, add it to the end of the ArrayList
            element = players.remove( 0);
            players.add(element);
        }

    }

    /**
     * crea le squadre dopo la scelta del compagno
     *
     */
    private void createTeams() {
        this.teamCaller = new ArrayList<>();
        this.teamPopolo = new ArrayList<>();
        teamCaller.add(players.get(0));
        players.get(0).setRole(PlayerRole.CALLER);
        for (Player p:players) {
            if(p.getHand().getCards().contains(fellowCard)) {
                teamCaller.add(p);
                p.setRole(PlayerRole.FELLOW);
            }
            else {
                if(!p.equals(players.get(0))) {
                    teamPopolo.add(p);
                    p.setRole(PlayerRole.POPOLO);
                }
            }
        }
    }


    private void distributeCard() {
        for (int j = 0; j<8; j++) {
            for(int i = 0; i < 5; i++) {
                players.get(i).draw(deck.distributeCard());
            }
        }
      for (Player p:players ) {
          p.divideCardForSuit();
        }
    }

    boolean isMultiplayer() {
        return isMultiplayer;
    }

    @Override
    public String toString() {
        return "LocalGame{" +
                "players=" + players +
                '}';
    }


}
