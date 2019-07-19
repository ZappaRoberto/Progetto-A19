package GUI.panels;

import GUI.buttons.ButtonCardImage;
import card_management.Card;
import finals.MyColors;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
/**
 * Pannello usato per contenere le carte nel tavolo
 * @author Team A19
 */
public class TableCardPanel {

    private JPanel panel;
    private ArrayList<ButtonCardImage> cardsSlot;

    public TableCardPanel() {
        this.panel = new JPanel();
        cardsSlot = new ArrayList<>();
        panel.setLayout(new GridLayout(2,3));
        panel.setBackground(MyColors.GREEN_TABLE);
    }

    public void update() {
        if(cardsSlot.size()== 5) {
            for (ButtonCardImage p : cardsSlot) {
                panel.remove(p);
            }
            cardsSlot.clear();
        }
    }

    /**
     * aggiunge la carta, quando giocano tutti i giocatori si resetta
     * @param card ultima carta giocata
     */
    public void update(Card card) {
        if(cardsSlot.size()== 5) {
            clear();
            addSlot(card);
        }
        else {
            addSlot(card);
        }
    }

    private void clear() {
        for (ButtonCardImage p : cardsSlot) {
            panel.remove(p);
        }
        cardsSlot.clear();
    }

    private void addSlot(Card card) {
        ButtonCardImage cardSlot = new ButtonCardImage(card.getCardImage());
        cardSlot.setBackground(MyColors.GREEN_TABLE);
        cardSlot.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panel.add(cardSlot);
        cardsSlot.add(cardSlot);
    }

    public JPanel getPanel() {
        return panel;
    }
}
