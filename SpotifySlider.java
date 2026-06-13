package smartplayer.views;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Slider de volumen con tema rosado premium.
 * Track navy oscuro, parte rellena rosa gradiente, thumb rosa circular.
 */
public class SpotifySlider extends JSlider {

    public SpotifySlider(int min, int max, int value) {
        super(min, max, value);
        setOpaque(false);
        setPreferredSize(new Dimension(100, 20));
        setUI(new SpotifySliderUI(this));
    }

    // UI interna
    private static class SpotifySliderUI extends BasicSliderUI {

        private static final Color TRACK_COLOR  = new Color(0x4A4A6A);
        private static final Color FILLED_START = new Color(0xFF69B4);
        private static final Color FILLED_END   = new Color(0xE91E8C);
        private static final Color THUMB_NORMAL = new Color(0xFF69B4);
        private static final Color THUMB_HOVER  = Color.WHITE;

        private boolean thumbHovered = false;

        SpotifySliderUI(JSlider slider) {
            super(slider);
            slider.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { thumbHovered = true;  slider.repaint(); }
                @Override public void mouseExited(MouseEvent e)  { thumbHovered = false; slider.repaint(); }
            });
        }

        @Override
        public void paintTrack(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Rectangle trackRect = this.trackRect;
            int h = 4;
            int y = trackRect.y + (trackRect.height - h) / 2;

            // Track completo
            g2.setColor(TRACK_COLOR);
            g2.fill(new RoundRectangle2D.Float(trackRect.x, y, trackRect.width, h, h, h));

            // Parte rellena con gradiente rosa
            int thumbX = thumbRect.x + thumbRect.width / 2;
            int filled = thumbX - trackRect.x;
            if (filled > 0) {
                GradientPaint gp = new GradientPaint(
                    trackRect.x, y, FILLED_START,
                    trackRect.x + filled, y, FILLED_END);
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(trackRect.x, y, filled, h, h, h));
            }

            g2.dispose();
        }

        @Override
        public void paintThumb(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int diam = thumbHovered ? 16 : 12;
            int x = thumbRect.x + (thumbRect.width  - diam) / 2;
            int y = thumbRect.y + (thumbRect.height - diam) / 2;
            if (thumbHovered) {
                // Fondo blanco con borde rosa
                g2.setColor(THUMB_HOVER);
                g2.fill(new Ellipse2D.Float(x, y, diam, diam));
                g2.setColor(FILLED_START);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new Ellipse2D.Float(x, y, diam, diam));
            } else {
                g2.setColor(THUMB_NORMAL);
                g2.fill(new Ellipse2D.Float(x, y, diam, diam));
            }
            g2.dispose();
        }

        @Override public void paintFocus(Graphics g) { /* sin foco visible */ }
    }
}
