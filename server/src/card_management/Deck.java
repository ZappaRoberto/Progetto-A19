package card_management;

import java.util.ArrayList;
import java.util.Collections;

public class Deck {
    private ArrayList<Card> deck;

    public Deck() {
        deck = new ArrayList<>();
        createDeck();
    }

    public void shuffle() {
        Collections.shuffle(deck);
    }

    private  void createDeck() {
        for(Seme s: Seme.values()) {
            for(int i = 1; i < 11; i++) {
                Card card = new Card(s,i);
                deck.add(card);
            }
        }
        addValore();
    }

    public Card distributeCard() {
        if(deck.size()>0) {
            Card card = this.deck.get(deck.size() - 1);
            this.deck.remove(deck.size() - 1);
            return card;
        }
        else {
            return null;
        }
    }

    private void addValore() {
        for (Card c:deck) {
            Valore valore = null;
            switch (c.getValor()) {
                case 1:
                    valore = Valore.ACE;
                    break;
                case 2:
                    valore = Valore.TWO;
                    break;
                case 3:
                    valore = Valore.THREE;
                    break;
                case 4:
                    valore = Valore.FOUR;
                    break;
                case 5:
                    valore = Valore.FIVE;
                    break;
                case 6:
                    valore = Valore.SIX;
                    break;
                case 7:
                    valore = Valore.SEVEN;
                    break;
                case 8:
                    valore = Valore.EIGHT;
                    break;
                case 9:
                    valore = Valore.NINE;
                    break;
                case 10:
                    valore = Valore.TEN;
                    break;
            }
            c.setValue(valore);
        }
    }
}
