package smartplayer.views;

import javax.swing.*;
import java.awt.*;

/**
 * Panel de gráfico de barras horizontales tema rosado premium.
 * Gradiente rosa, fondo navy oscuro, texto blanco/gris, barras redondeadas.
 */
public class BarChartPanel extends JPanel {

    private static final Color BG          = new Color(0x16213E);
    private static final Color TEXT_TITLE  = Color.WHITE;
    private static final Color TEXT_LABEL  = new Color(0xC0C0C0);
    private static final Color TEXT_VALUE  = Color.WHITE;
    private static final Color COLOR_EMPTY = new Color(0x4A4A6A);

    private String[] labels;
    private int[]    valores;
    private String   titulo;
    private Color    colorBarra;

    public BarChartPanel() {
        this.labels     = new String[0];
        this.valores    = new int[0];
        this.titulo     = "";
        this.colorBarra = new Color(0xFF69B4); // Rosa hot pink
        setBackground(BG);
        setPreferredSize(new Dimension(400, 280));
    }

    public void setDatos(String[] labels, int[] valores, String titulo) {
        this.labels  = labels  != null ? labels  : new String[0];
        this.valores = valores != null ? valores : new int[0];
        this.titulo  = titulo  != null ? titulo  : "";
        repaint();
    }

    public void setColorBarra(Color color) { this.colorBarra = color; repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        int mL = 20, mR = 60, mT = 50, mB = 20;
        int labelW   = 140;
        int barAreaX = mL + labelW;
        int barAreaW = w - barAreaX - mR;

        if (labels.length == 0) {
            g2.setColor(new Color(0x4A4A6A));
            g2.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            String msg = "Sin datos disponibles";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, h / 2);
            g2.dispose();
            return;
        }

        // Título
        g2.setColor(TEXT_TITLE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2.drawString(titulo, mL, mT - 12);

        int numBars = Math.min(labels.length, valores.length);
        int maxVal  = 1;
        for (int v : valores) if (v > maxVal) maxVal = v;

        int availH  = h - mT - mB;
        int barH    = Math.min(28, Math.max(16, (availH / numBars) - 6));
        int gap     = 8;
        int startY  = mT;

        // Color secundario para el degradado
        Color colorEnd = new Color(
            Math.max(0, colorBarra.getRed()   - 60),
            Math.max(0, colorBarra.getGreen() - 30),
            Math.min(255, colorBarra.getBlue() + 30)
        );

        for (int i = 0; i < numBars; i++) {
            int y = startY + i * (barH + gap);
            if (y + barH > h - mB) break;

            // Etiqueta truncada
            String label = labels[i] != null ? labels[i] : "";
            if (label.length() > 18) label = label.substring(0, 16) + "..";
            g2.setColor(TEXT_LABEL);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(label, mL, y + barH / 2 + fm.getAscent() / 2);

            // Track de fondo
            g2.setColor(new Color(0x2A2A4A));
            g2.fillRoundRect(barAreaX, y, barAreaW, barH, 6, 6);

            // Barra de progreso con gradiente rosa
            int barW = (int)((valores[i] / (double)maxVal) * barAreaW);
            barW = Math.max(barW, 4);
            GradientPaint gp = new GradientPaint(
                    barAreaX, y, colorBarra,
                    barAreaX + barW, y, colorEnd);
            g2.setPaint(gp);
            g2.fillRoundRect(barAreaX, y, barW, barH, 6, 6);

            // Valor
            g2.setColor(TEXT_VALUE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g2.drawString(String.valueOf(valores[i]), barAreaX + barW + 8,
                          y + barH / 2 + g2.getFontMetrics().getAscent() / 2);
        }

        g2.dispose();
    }
}
