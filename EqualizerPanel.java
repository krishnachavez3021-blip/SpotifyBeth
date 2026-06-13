package smartplayer.views;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

/**
 * Panel de ecualizador visual animado tema rosado.
 * 8 barras con gradiente rosa, animación suave con Timer.
 */
public class EqualizerPanel extends JPanel {

    private static final int    NUM_BARRAS = 8;
    private static final Color  COLOR_TOP  = new Color(0xFF85C8); // Rosa claro
    private static final Color  COLOR_BOT  = new Color(0xE91E8C); // Rosa magenta

    private int[]   alturas;
    private int[]   targetAlturas;
    private boolean animating;
    private Timer   timer;
    private Random  random;

    public EqualizerPanel() {
        alturas       = new int[NUM_BARRAS];
        targetAlturas = new int[NUM_BARRAS];
        animating     = false;
        random        = new Random();

        setOpaque(false);
        setPreferredSize(new Dimension(80, 40));

        timer = new Timer(60, e -> {
            if (animating) {
                for (int i = 0; i < NUM_BARRAS; i++) {
                    if (random.nextInt(4) == 0) {
                        targetAlturas[i] = random.nextInt(Math.max(6, getHeight() - 4)) + 3;
                    }
                    // Interpolación suave
                    int diff = targetAlturas[i] - alturas[i];
                    alturas[i] += diff > 0 ? Math.max(1, diff / 3) : Math.min(-1, diff / 3);
                }
            } else {
                // Bajar barras al detenerse
                for (int i = 0; i < NUM_BARRAS; i++) {
                    if (alturas[i] > 1) alturas[i] = Math.max(1, alturas[i] - 3);
                }
            }
            repaint();
        });
        timer.start();
    }

    public void setAnimating(boolean animating) {
        this.animating = animating;
        if (animating) {
            int h = Math.max(10, getHeight());
            for (int i = 0; i < NUM_BARRAS; i++)
                targetAlturas[i] = random.nextInt(h - 4) + 3;
        }
    }

    public boolean isAnimating() { return animating; }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        int barW = Math.max(3, (w - (NUM_BARRAS + 1) * 2) / NUM_BARRAS);
        int gap  = 3;
        int totalW  = NUM_BARRAS * barW + (NUM_BARRAS - 1) * gap;
        int startX  = (w - totalW) / 2;

        for (int i = 0; i < NUM_BARRAS; i++) {
            int x    = startX + i * (barW + gap);
            int barH = Math.min(Math.max(alturas[i], 2), h - 2);
            int y    = h - barH;

            GradientPaint gp = new GradientPaint(x, y, COLOR_TOP, x, h, COLOR_BOT);
            g2.setPaint(gp);
            g2.fillRoundRect(x, y, barW, barH, 3, 3);
        }
        g2.dispose();
    }

    public void detener() { if (timer != null) timer.stop(); }
}
