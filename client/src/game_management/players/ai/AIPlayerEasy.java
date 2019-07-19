package game_management.players.ai;

import card_management.Card;
import game_management.players.PlayerRole;

import java.util.Collections;
import java.util.Random;
/**
 * Genera giocatori livello facile
 * @author Team A19
 */
public class AIPlayerEasy extends AIPlayer {

    public AIPlayerEasy(int order) {
        super(order);
    }

    /**
     * sceglie scommessa
     * @return scommessa scelta
     */
    @Override
    public int chooseBet() {
        int max;
       if(Collections.max(cardsBySuit.values()) == 4) {
           max = 81;
       }
       else if(Collections.max(cardsBySuit.values()) >= 5) {
           max = 91;
       }
       else if(Collections.max(cardsBySuit.values()) == 3) {
           max = 71;
       }
       else if(Collections.max(cardsBySuit.values()) == 2) {
           max = 61;
       }
       else {
           max = 0;
       }
        if(max==0) {
            return max;
        }else {
            int d = randomMinMax(max);
            return d;
        }
    }

    /**
     *
     * @param max massima scommessa prestabilita
     * @return valore casuale tra min e max scommessa
     */
    private int randomMinMax(int max) {
        Random random = new Random();
        int min = 61;
        int c = ((max-min) + 1);
        int d;
        if(max<=81) {
            while (((d = (random.nextInt(c) + min)) % 2 == 0)) {
                c = ((max - min) + 1);
            }
        }else {
            d = (random.nextInt(c) + min);
        }
        return d;
    }

    /**
     * lancia la carta
     * @return carta scelta in base a logica AI
     */
    @Override
    public Card throwCard() {
        if(!role.equals(PlayerRole.FELLOW)) {
            return getCardToWin();
            }
        else {
            if(tempWinningPlayer!=null) {
                if(isFellowWinning()) {
                    return giveCarico();
                }
                else {
                   return getCardToWin();
                }
                }
            else {
                return giveLiscio();
            }
        }
    }

    /**
     *
     * @return true se il compagno sta vincendo la mano
     */
    private boolean isFellowWinning() {
        return tempWinningPlayer.getRole().equals(PlayerRole.CALLER) && tempWinningCard.getSeme().equals(briscola);
    }
}
