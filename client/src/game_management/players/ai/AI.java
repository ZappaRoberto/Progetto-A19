package game_management.players.ai;

import card_management.Card;
/**
 * Interfaccia per AI
 * @see AIPlayer
 * @author Team A19
 */
public interface AI {
    int chooseBet();

    Card chooseFellow();

    Card throwCard();
}
