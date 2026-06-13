package smartplayer.views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Barra de progreso con tema rosado premium.
 * Track oscuro, progreso con gradiente rosa, thumb blanco con borde rosa en hover.
 */
public class SpotifyProgressBar extends JProgressBar {

    private static final Color TRACK_COLOR = new Color(0x4A4A6A);
    private static final Color PROG_START  = new Color(0xFF69B4); // Rosa hot pink
    private static final Color PROG_END    = new Color(0xE91E8C); // Rosa magenta
    private static final Color THUMB_COLOR = Color.WHITE;

    private boolean hovered = false;

    public SpotifyProgressBar() {
        super(0, 100);
        setOpaque(false);
        setBorderPainted(false);
        setPreferredSize(new Dimension(0, 18));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
            @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        int trackH = hovered ? 6 : 4;
        int y = (h - trackH) / 2;

        // Track oscuro de fondo
        g2.setColor(TRACK_COLOR);
        g2.fill(new RoundRectangle2D.Float(0, y, w, trackH, trackH, trackH));

        // Progreso con gradiente rosa
        double progress = (getMaximum() > 0) ? (getValue() / (double) getMaximum()) : 0;
        int progressW = (int)(w * progress);
        if (progressW > 0) {
            GradientPaint gp = new GradientPaint(0, y, PROG_START, progressW, y, PROG_END);
            g2.setPaint(gp);
            g2.fill(new RoundRectangle2D.Float(0, y, progressW, trackH, trackH, trackH));
        }

        // Thumb blanco circular visible en hover
        if (hovered && progressW >= 4) {
            int thumbDiam = 14;
            int tx = progressW - thumbDiam / 2;
            int ty = h / 2 - thumbDiam / 2;
            // Sombra rosa sutil
            g2.setColor(new Color(255, 105, 180, 80));
            g2.fillOval(tx - 2, ty - 2, thumbDiam + 4, thumbDiam + 4);
            // Thumb blanco
            g2.setColor(THUMB_COLOR);
            g2.fillOval(tx, ty, thumbDiam, thumbDiam);
            // Borde rosa
            g2.setColor(PROG_START);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(tx, ty, thumbDiam, thumbDiam);
        }

        g2.dispose();
    }
}
