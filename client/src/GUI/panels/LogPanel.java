package GUI.panels;

import finals.MyColors;
import finals.MyFonts;

import javax.swing.*;
import java.awt.*;
/**
 * Pannello usato per loggare eventi
 * @author Team A19
 */
public class LogPanel extends JPanel {

    private JTextArea logsField;
    private JScrollBar vertical;
    private final Font font = MyFonts.COPPERPLATE_GOTHIC_BOLD;
    public LogPanel() {
        setBackground(Color.BLACK);

        logsField = new JTextArea("Game Started");

        logsField.setFont(font);
        logsField.setBackground(Color.BLACK);
        logsField.setForeground(Color.GREEN);

        JScrollPane scrollPane = new JScrollPane(logsField);

        vertical = scrollPane.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());

        this.setLayout(new BorderLayout());
        this.add(scrollPane, BorderLayout.CENTER);


    }

    /**
     * aggiorna il contenuto del log
     * @param s ultimo log
     */
    public void update(String s) {
        String text = logsField.getText() + "\n";
        logsField.setText(text + s);
        logsField.setLineWrap(true);
        logsField.setFont(font);
        logsField.setBackground(Color.BLACK);
        logsField.setForeground(MyColors.GREEN_LOG);
        logsField.setWrapStyleWord(true);
        vertical.setValue( vertical.getMaximum() );
    }

}
