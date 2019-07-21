package game_management;

import card_management.Card;
import card_management.Hand;
import java.util.ArrayList;
/**
 * giocatori usati nella logica back-end della partita
 * @author Ludovico Viola
 */
public class Player implements Comparable<Player> {
    private int order;
    private Hand hand;
    private ArrayList<Card> wonCards;
    private int score;
    private String playerID;
    private boolean flag = true;

    public Player(String name, int order) {
        this.order = order;
        this.wonCards = new ArrayList<>();
        this.hand = new Hand();
        this.score = 0;
        playerID = name;
    }

    /**
     * Aggiunge le carte vinte al mazzetto e calcola i punti ottenuti
     * @param cards
     */
    public void winHand(ArrayList<Card> cards) {
        this.wonCards.addAll(cards);
        for (Card c:cards) {
            this.score += c.getPoints();
        }
    }


    void draw(Card card) {
        this.hand.draw(card);
    }

    public int getOrder() {
        return order;
    }

    public Hand getHand() {
        return hand;
    }

    public String getPlayerID() {
        return playerID;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int compareTo(Player o) {
        return Integer.compare(o.score, this.score);
    }

     int getScore() {
        return score;
    }

    /**
     * Ordina la mano per seme
     */
     void divideCardForSuit() {
        if(flag) {
            this.hand.divideCardsForSuit();
            flag = false;
        }
    }

    @Override
    public String toString() {
        return playerID;
    }
}
