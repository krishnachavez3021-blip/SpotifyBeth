package smartplayer;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(SmartPlayerUI::new);
    }
}
