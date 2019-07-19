package game_management.players.ai;

import card_management.Card;
import card_management.Deck;
import card_management.Semi;
import game_management.players.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
/**
 * Genera giocatori artificiali
 * @author Team A19
 */
public abstract class AIPlayer extends Player implements AI{

     Card tempWinningCard;
     Player tempWinningPlayer;
    Semi briscola;
    private int[] cardsForSuit;
    HashMap<Semi,Integer> cardsBySuit;


    AIPlayer(int order) {
        super(order);
        tempWinningCard = null;
    }

    /**
     * Divide le carte per seme
     */
    public void setCardsForSuit() {
        this.cardsForSuit = this.hand.getCardsForSuit();
        this.cardsBySuit = new HashMap<>();
        for (Semi s:Semi.values()) {
            cardsBySuit.put(s,cardsForSuit[s.ordinal()]);
        }
    }

    /**
     * Sceglie briscola e compagno
     * @return carta scelta
     */
    public Card chooseFellow() {
        Deck deck = new Deck();
        deck.getDeck().removeAll(this.hand.getCards());
        int j = 0;
        for (Integer i:cardsForSuit) {
            if(i.equals(Collections.max(cardsBySuit.values()))) {
                briscola = Semi.values()[j];
            }
            j++;
        }
        ArrayList<Card> cards = new ArrayList<>();
        for (Card c:deck.getDeck() ) {
            if(c.getSeme().equals(briscola)) {
                cards.add(c);
            }
        }

        cards.sort(Card::compareTo);
        return  cards.get(cards.size()-1);
    }

    /**
     * lancia carte senza punti
     * @return Carta senza punteggio
     */
     Card giveLiscio() {
        Card card = null;
        for (Card c:this.hand.getCards()) {
            if(c.getPoints()==0 && c.getSeme()!=briscola) {
                card = c;
            }
        }
        if(card == null) {
            card = giveRandomCard();
        }
        return card;
    }

    /**
     * lancia carte di 10 o 11 punti(non briscola)
     * @return Carta "Carico"
     */
     Card giveCarico() {
        Card card = null;
        for (Card c:this.hand.getCards()) {
            if((c.getPoints()==10 || c.getPoints()==11) && c.getSeme()!=briscola) {
                card = c;
            }
        }
        if(card == null) {
            card = giveRandomCard();
        }
        return card;
    }

    /**
     * gioca per vincere
     * @return carta per vincere la mano
     */
     Card getCardToWin() {
        Card d = null;
        if(tempWinningCard!=null) {
            for (Card c : this.hand.getCards()) {
                if (c.getSeme() == briscola && tempWinningCard.getSeme() == briscola && c.isGreaterStessoSeme(tempWinningCard)) {
                    d = c;
                    break;
                } else if (c.getSeme() == tempWinningCard.getSeme() && c.isGreaterStessoSeme(tempWinningCard)) {
                    d = c;
                    break;
                } else {
                    if (c.getSeme() == briscola && tempWinningCard.getSeme() != briscola) {
                        d = c;
                        break;
                    }
                }
            }

            if(d == null) {
                d = giveLiscio();
            }

            return d;
        }
        else {
            return giveLiscio();
        }
    }

    /**
     * gioca casuale
     * @return carta casuale
     */
    private Card giveRandomCard() {
        Random random = new Random();
        return this.hand.getCards().get(random.nextInt(this.hand.getCards().size()));
    }

    public void setTempWinningCard(Card tempWinningCard) {
        this.tempWinningCard = tempWinningCard;
    }

    public void setTempWinningPlayer(Player tempWinningPlayer) {
        this.tempWinningPlayer = tempWinningPlayer;
    }

}

