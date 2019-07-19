package card_management;

import game_management.players.Player;

import java.util.ArrayList;
/**
 * Tavolo di gioco.
 * contiene logica per determinare il vincente di una mano.
 * <p>
 *    mantiene dati come vincitore e carta vincente temporanei, utili per logica AI
 * </p>
 * @see Card
 * @see Player
 * @author Team A19
 */
public class Table {
    private ArrayList<Card> cards;
    private boolean isFirst;
    private String winner;
    private int startingPlayer;
    private Card winningCard;
    private Semi briscola;
    private Hand hand;
    private Player tempWinningPlayer;

    public Table(Semi briscola) {
        this.startingPlayer=0;
        this.cards = new ArrayList<>();
        this.briscola = briscola;
        isFirst = true;
        this.hand = new Hand(cards);
    }

    /**
     * logica per determinare vincente temporaneo
     * @param card carta selezionata
     * @param player giocatore che ha selezionato la carta
     */
    public void addCard(Card card, Player player) {
        if(isFirst) {
            tempWinner(card, player);
            isFirst = false;
        }
        else {
            if(winningCard.getSeme() == briscola && card.getSeme() == briscola && card.isGreaterStessoSeme(winningCard)) {
                tempWinner(card,player);
            }
            else if (card.getSeme() == winningCard.getSeme() && card.isGreaterStessoSeme(winningCard)) {
                tempWinner(card, player);
            }
            else {
                if(card.getSeme() == briscola && winningCard.getSeme()!=briscola) {
                tempWinner(card,player);
                }
            }
        }
        card.setOwner(player.getRole());
        this.cards.add(card);
    }

    private void tempWinner(Card card, Player player) {
        winner = player.getPlayerID();
        winningCard = card;
        startingPlayer = player.getOrder();
        tempWinningPlayer = player;
    }

    public ArrayList<Card> getCards() {
        return cards;
    }

    public Hand getHand() {
        return this.hand;
    }

    public String getWinner() {
        return winner;
    }

    public int getStartingPlayer() {
        return startingPlayer;
    }

    public Card getWinningCard() {
        return winningCard;
    }

    public Player getTempWinningPlayer() {
        return tempWinningPlayer;
    }
}
