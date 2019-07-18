package card_management;

import java.util.ArrayList;

public class Hand {
    private int[] cardsForSuit = {0,0,0,0};
    private ArrayList<Card> cards;

    public Hand() {
        this.cards = new ArrayList<>();
    }

    public void draw(Card card)
    {
            this.cards.add(card);
    }

    Hand(ArrayList<Card> cards) {
        this.cards = new ArrayList<>(cards);
    }

    public void divideCardsForSuit() {
        for (Card c:cards) {
            cardsForSuit[c.getSeme().ordinal()] += 1;
        }
    }

    public Card chooseCard(String string) {
        Card card = null;
        for (Card c:cards) {
            if(c.getCardImage().toString().equals(string)) {
                card = c;
            }
        }
        this.cards.remove(card);
        return card;
    }

    public void chooseCard(Card card) {
        this.cards.remove(card);
    }

    @Override
    public String toString() {
        return "Hand{" +
                "cards=" + cards +
                '}';
    }

    public ArrayList<Card> getCards() {
        return cards;
    }

    public void addCard(Card cards) {
        if(!this.cards.contains(cards)) {
            this.cards.add(cards);
        }
    }

    public int[] getCardsForSuit() {
        return cardsForSuit;
    }
}
