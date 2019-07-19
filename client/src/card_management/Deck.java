package card_management;

import GUI.frames.NewGameScreen;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
/**
 * mazzo di carte
 * @author Team A19
 */
public class Deck {
    private ArrayList<Card> deck;

    public Deck() {
        deck = new ArrayList<>();
        createDeck();
    }

    /**
     * mescola il mazzo
     */
    public void shuffle() {
        Collections.shuffle(deck);
    }

    /**
     * crea carte, assegna le immagini e aggiunge il valore(1=asso>10=re)
     */
    private  void createDeck() {
        int factor = 0;
        for(Semi s: Semi.values()) {
            for(int i = 1; i < 11; i++) {


                Card card = new Card(s,i);
                URL resource;
                if(s == Semi.COPPE) {
                    resource = getClass().getClassLoader().getResource( "resources/card_images/" + "a" + i + ".png" );
                }
                else {
                    resource = getClass().getClassLoader().getResource( "resources/card_images/" + "a" + (i+factor) + ".png" );
                }
                card.setCardImage(NewGameScreen.findImage(resource));
                deck.add(card);
            }
            factor +=10;

        }
        addValore();
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

    @Override
    public String toString() {
        return "Deck{" +
                "deck=" + deck.toString() +
                '}';
    }

    public ArrayList<Card> getDeck() {
        return deck;
    }
}
