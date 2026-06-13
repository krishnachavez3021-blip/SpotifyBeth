package smartplayer.views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Botón con esquinas redondeadas tema rosado premium.
 * Animación suave de hover con javax.swing.Timer.
 */
public class RoundedButton extends JButton {

    // Colores primarios (rosa hot pink)
    private static final Color BG_PRIMARY        = new Color(0xFF69B4);
    private static final Color BG_PRIMARY_HOVER  = new Color(0xFF85C8);
    private static final Color BG_PRIMARY_PRESS  = new Color(0xE91E8C);

    // Colores secundarios (superficies oscuras navy)
    private static final Color BG_SECONDARY       = new Color(0x2A2A4A);
    private static final Color BG_SECONDARY_HOVER = new Color(0x3A3A6A);

    // Colores peligro (rojo eliminar)
    private static final Color BG_DANGER       = new Color(0xE22134);
    private static final Color BG_DANGER_HOVER = new Color(0xFF3347);

    public enum Variante { PRIMARY, SECONDARY, DANGER }

    private final Variante variante;
    private Color bgNormal;
    private Color bgHover;
    private Color bgPress;
    private Color currentBg;
    private Color targetBg;
    private Timer hoverTimer;

    public RoundedButton(String text) {
        this(text, Variante.PRIMARY);
    }

    public RoundedButton(String text, Variante variante) {
        super(text);
        this.variante = variante;

        switch (variante) {
            case SECONDARY:
                bgNormal = BG_SECONDARY;
                bgHover  = BG_SECONDARY_HOVER;
                bgPress  = BG_SECONDARY;
                break;
            case DANGER:
                bgNormal = BG_DANGER;
                bgHover  = BG_DANGER_HOVER;
                bgPress  = BG_DANGER;
                break;
            default: // PRIMARY
                bgNormal = BG_PRIMARY;
                bgHover  = BG_PRIMARY_HOVER;
                bgPress  = BG_PRIMARY_PRESS;
                break;
        }

        currentBg = bgNormal;
        targetBg  = bgNormal;

        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setForeground(Color.WHITE);
        setFont(new Font("Segoe UI", Font.BOLD, 13));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(8, 22, 8, 22));

        // Timer de animación hover (16ms ~ 60fps)
        hoverTimer = new Timer(16, e -> {
            currentBg = interpolarColor(currentBg, targetBg, 0.18f);
            repaint();
            if (coloresCercanos(currentBg, targetBg)) {
                currentBg = targetBg;
                hoverTimer.stop();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { targetBg = bgHover;  hoverTimer.restart(); }
            @Override public void mouseExited(MouseEvent e)  { targetBg = bgNormal; hoverTimer.restart(); }
            @Override public void mousePressed(MouseEvent e) { currentBg = bgPress; repaint(); }
            @Override public void mouseReleased(MouseEvent e){ targetBg = bgHover;  hoverTimer.restart(); }
        });
    }

    private Color interpolarColor(Color a, Color b, float t) {
        return new Color(
            clamp(a.getRed()   + (int)((b.getRed()   - a.getRed())   * t)),
            clamp(a.getGreen() + (int)((b.getGreen() - a.getGreen()) * t)),
            clamp(a.getBlue()  + (int)((b.getBlue()  - a.getBlue())  * t))
        );
    }

    private int clamp(int v) { return Math.min(255, Math.max(0, v)); }

    private boolean coloresCercanos(Color a, Color b) {
        return Math.abs(a.getRed()   - b.getRed())   < 3 &&
               Math.abs(a.getGreen() - b.getGreen()) < 3 &&
               Math.abs(a.getBlue()  - b.getBlue())  < 3;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        g2.setColor(currentBg);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 22, 22));
        g2.dispose();
        super.paintComponent(g);
    }

    @Override protected void paintBorder(Graphics g) { /* sin borde */ }
}
