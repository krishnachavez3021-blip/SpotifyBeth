package smartplayer.utils;

import javax.swing.*;
import java.awt.*;

/**
 * Gestor de temas para alternar entre modo oscuro (rosado) y modo claro.
 * Paleta principal: azul oscuro + rosa (hot pink).
 */
public class ThemeManager {
    
    public enum Theme { DARK, LIGHT }
    
    private static Theme currentTheme = Theme.DARK;
    
    // Colores del tema oscuro rosado
    public static final Color DARK_BG = new Color(0x1A1A2E);
    public static final Color DARK_BG_SECONDARY = new Color(0x16213E);
    public static final Color DARK_BG_TERTIARY = new Color(0x2A2A4A);
    public static final Color DARK_FG = Color.WHITE;
    public static final Color DARK_FG_SECONDARY = new Color(0xC0C0C0);
    public static final Color DARK_ACCENT = new Color(0xFF69B4);
    
    // Colores del tema claro
    public static final Color LIGHT_BG = new Color(245, 245, 245);
    public static final Color LIGHT_BG_SECONDARY = Color.WHITE;
    public static final Color LIGHT_BG_TERTIARY = new Color(230, 230, 230);
    public static final Color LIGHT_FG = new Color(30, 30, 30);
    public static final Color LIGHT_FG_SECONDARY = new Color(100, 100, 100);
    public static final Color LIGHT_ACCENT = new Color(0xFF69B4);

    public static void aplicarTema(JFrame frame, Theme tema) {
        currentTheme = tema;
        
        Color bg = (tema == Theme.DARK) ? DARK_BG : LIGHT_BG;
        Color bgSec = (tema == Theme.DARK) ? DARK_BG_SECONDARY : LIGHT_BG_SECONDARY;
        Color bgTer = (tema == Theme.DARK) ? DARK_BG_TERTIARY : LIGHT_BG_TERTIARY;
        Color fg = (tema == Theme.DARK) ? DARK_FG : LIGHT_FG;
        Color fgSec = (tema == Theme.DARK) ? DARK_FG_SECONDARY : LIGHT_FG_SECONDARY;
        Color accent = (tema == Theme.DARK) ? DARK_ACCENT : LIGHT_ACCENT;
        
        UIManager.put("Panel.background", bg);
        UIManager.put("Label.foreground", fg);
        UIManager.put("Button.background", accent);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Table.background", bgSec);
        UIManager.put("Table.foreground", fg);
        UIManager.put("Table.gridColor", bgTer);
        UIManager.put("ScrollPane.background", bg);
        UIManager.put("TextArea.background", bgSec);
        UIManager.put("TextArea.foreground", fg);
        UIManager.put("TextField.background", bgTer);
        UIManager.put("TextField.foreground", fg);
        UIManager.put("TabbedPane.background", bgTer);
        UIManager.put("TabbedPane.foreground", fg);
        UIManager.put("List.background", bgSec);
        UIManager.put("List.foreground", fg);
        
        aplicarRecursivo(frame.getContentPane(), bg, bgSec, bgTer, fg, fgSec);
        
        frame.getContentPane().setBackground(bg);
        SwingUtilities.updateComponentTreeUI(frame);
        frame.repaint();
    }

    public static void toggleTema(JFrame frame) {
        Theme nuevoTema = (currentTheme == Theme.DARK) ? Theme.LIGHT : Theme.DARK;
        aplicarTema(frame, nuevoTema);
    }

    public static Theme getCurrentTheme() { return currentTheme; }

    public static Color getBg() {
        return currentTheme == Theme.DARK ? DARK_BG : LIGHT_BG;
    }

    public static Color getBgSecondary() {
        return currentTheme == Theme.DARK ? DARK_BG_SECONDARY : LIGHT_BG_SECONDARY;
    }

    public static Color getBgTertiary() {
        return currentTheme == Theme.DARK ? DARK_BG_TERTIARY : LIGHT_BG_TERTIARY;
    }

    public static Color getFg() {
        return currentTheme == Theme.DARK ? DARK_FG : LIGHT_FG;
    }

    public static Color getFgSecondary() {
        return currentTheme == Theme.DARK ? DARK_FG_SECONDARY : LIGHT_FG_SECONDARY;
    }

    public static Color getAccent() {
        return currentTheme == Theme.DARK ? DARK_ACCENT : LIGHT_ACCENT;
    }

    private static void aplicarRecursivo(Component comp, Color bg, Color bgSec, Color bgTer, Color fg, Color fgSec) {
        if (comp instanceof JPanel) {
            comp.setBackground(bg);
            comp.setForeground(fg);
        } else if (comp instanceof JTable) {
            comp.setBackground(bgSec);
            comp.setForeground(fg);
            ((JTable) comp).setGridColor(bgTer);
            ((JTable) comp).setSelectionBackground(new Color(255, 105, 180, 80));
        } else if (comp instanceof JTextArea) {
            comp.setBackground(bgSec);
            comp.setForeground(fg);
        } else if (comp instanceof JTextField) {
            comp.setBackground(bgTer);
            comp.setForeground(fg);
        } else if (comp instanceof JList) {
            comp.setBackground(bgSec);
            comp.setForeground(fg);
        } else if (comp instanceof JLabel) {
            comp.setForeground(fg);
        } else if (comp instanceof JTabbedPane) {
            comp.setBackground(bgTer);
            comp.setForeground(fg);
        } else if (comp instanceof JButton) {
            // Asegurar que los botones siempre tengan texto visible (blanco)
            // Los RoundedButton y botones graficos dibujan su propio fondo
            comp.setForeground(Color.WHITE);
        }
        
        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                aplicarRecursivo(child, bg, bgSec, bgTer, fg, fgSec);
            }
        }
    }
}
