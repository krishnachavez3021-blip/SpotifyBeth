package smartplayer.views;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Scrollbar delgada tema rosado: 8px de ancho, thumb navy redondeado,
 * hover thumb rosa, track transparente.
 */
public class CustomScrollBarUI extends BasicScrollBarUI {

    private static final Color THUMB_NORMAL = new Color(0x4A4A6A);
    private static final Color THUMB_HOVER  = new Color(0xFF69B4);
    private static final int   THUMB_WIDTH  = 8;

    private boolean thumbHovered = false;

    @Override
    protected void installListeners() {
        super.installListeners();
        scrollbar.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { thumbHovered = true;  scrollbar.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { thumbHovered = false; scrollbar.repaint(); }
        });
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        // Track completamente transparente
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int x = thumbBounds.x + (thumbBounds.width - THUMB_WIDTH) / 2;
        int y = thumbBounds.y + 2;
        int w = THUMB_WIDTH;
        int h = thumbBounds.height - 4;

        g2.setColor(thumbHovered ? THUMB_HOVER : THUMB_NORMAL);
        g2.fill(new RoundRectangle2D.Float(x, y, w, h, THUMB_WIDTH, THUMB_WIDTH));
        g2.dispose();
    }

    @Override protected JButton createDecreaseButton(int orientation) { return crearBotonInvisible(); }
    @Override protected JButton createIncreaseButton(int orientation) { return crearBotonInvisible(); }

    private JButton crearBotonInvisible() {
        JButton btn = new JButton();
        btn.setPreferredSize(new Dimension(0, 0));
        btn.setMinimumSize(new Dimension(0, 0));
        btn.setMaximumSize(new Dimension(0, 0));
        return btn;
    }

    /** Aplica este UI a un JScrollPane y ajusta su ancho. */
    public static void aplicarA(JScrollPane scrollPane) {
        JScrollBar vsb = scrollPane.getVerticalScrollBar();
        vsb.setUI(new CustomScrollBarUI());
        vsb.setPreferredSize(new Dimension(10, 0));
        vsb.setBackground(new Color(0x1A1A2E));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(new Color(0x1A1A2E));
        scrollPane.getViewport().setBackground(new Color(0x1A1A2E));
    }
}
