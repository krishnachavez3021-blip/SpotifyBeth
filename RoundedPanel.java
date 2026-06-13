package smartplayer.views;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Panel con esquinas redondeadas y fondo navy oscuro tema rosado.
 * Sombra rosa suave simulada con dibujo.
 */
public class RoundedPanel extends JPanel {

    private static final Color DEFAULT_BG     = new Color(0x16213E);
    private static final Color DEFAULT_BORDER = new Color(0x4A4A6A);

    private int radius;
    private Color bgColor;
    private Color borderColor;
    private boolean hasBorder;
    private boolean hasShadow;

    public RoundedPanel() {
        this(12, false);
    }

    public RoundedPanel(int radius) {
        this(radius, false);
    }

    public RoundedPanel(int radius, boolean hasBorder) {
        this.radius = radius;
        this.bgColor = DEFAULT_BG;
        this.borderColor = DEFAULT_BORDER;
        this.hasBorder = hasBorder;
        this.hasShadow = true;
        setOpaque(false);
    }

    public void setBgColor(Color color)     { this.bgColor = color;     repaint(); }
    public void setBorderColor(Color color) { this.borderColor = color; repaint(); }
    public void setHasShadow(boolean v)     { this.hasShadow = v;       repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();

        // Sombra rosa suave simulada
        if (hasShadow) {
            for (int i = 4; i > 0; i--) {
                int alpha = 8 * (5 - i);
                g2.setColor(new Color(255, 105, 180, alpha));
                g2.fill(new RoundRectangle2D.Float(i, i + 1, w - i * 2, h - i * 2, radius, radius));
            }
        }

        // Fondo
        g2.setColor(bgColor);
        g2.fill(new RoundRectangle2D.Float(0, 0, w - 2, h - 3, radius, radius));

        // Borde opcional
        if (hasBorder) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 3, h - 4, radius - 1, radius - 1));
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
