package game_management;

import card_management.Card;
import card_management.Deck;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Contiene i dati della singola partita online
 * @author Ludovico Viola
 */
public class GameDataHolder {
    public ArrayList<Player> players;
    public ArrayList<Player> teamCaller;
    public ArrayList<Player> teamPopolo;
    private Deck deck;
    private Card fellowCard;
    public int startingPlayer;
    public int higherBet;
    public CopyOnWriteArrayList<Player> bettingPlayers;

    public GameDataHolder() {
        higherBet = 60;
        startingPlayer = 0;
        this.players = new ArrayList<>();
        this.deck = new Deck();
        this.deck.shuffle();
    }

    public void addPlayers(Player player) {
        this.players.add(player);
    }

    public void distributeCard() {
        for (int j = 0; j<8; j++) {
            for(int i = 0; i < 5; i++) {
                players.get(i).draw(deck.distributeCard());
            }
        }
        for (Player p:players ) {
            p.divideCardForSuit();
        }
        bettingPlayers = new CopyOnWriteArrayList<>(players);
    }



    @Override
    public String toString() {
        return "GameDataHolder{" +
                "players=" + players +
                '}';
    }

    public void setFellowCard(Card fellowCard) {
        this.fellowCard = fellowCard;
    }

    public static <T> void gameOrder(ArrayList<T> players, int shift)
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
    public String makeScoreBoard() {
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
        StringBuilder builder = new StringBuilder();
        if(teamCallerScore>=higherBet) {
            for (Player p:teamCaller) {
                builder.append(p.getPlayerID()).append("&");
            }
        }else {
            for (Player p:teamPopolo) {
                builder.append(p.getPlayerID()).append("&");
            }
        }
        return s.toString();
    }

    public void createTeams() {
        gameOrder(players,startingPlayer);
        this.teamCaller = new ArrayList<>();
        this.teamPopolo = new ArrayList<>();
        teamCaller.add(players.get(0));
        for (Player p:players) {
            for (Card c:p.getHand().getCards()) {
                if(c.getCardId().equals(fellowCard.getCardId())) {
                    teamCaller.add(p);
                }
                else {
                    if(!p.equals(players.get(0))) {
                        teamPopolo.add(p);
                    }
                }
            }

        }
    }
}
