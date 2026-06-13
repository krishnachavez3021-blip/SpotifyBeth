package smartplayer.views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Campo de texto con esquinas redondeadas tema rosado.
 * Borde rosa en foco, placeholder gris plateado.
 */
public class RoundedTextField extends JTextField {

    private static final Color BG             = new Color(0x2A2A4A);
    private static final Color BORDER_NORMAL  = new Color(0x4A4A6A);
    private static final Color BORDER_FOCUS   = new Color(0xFF69B4);
    private static final Color TEXT_COLOR     = Color.WHITE;
    private static final Color PLACEHOLDER_COLOR = new Color(0xC0C0C0);

    private String placeholder;
    private Color borderColor = BORDER_NORMAL;
    private boolean focused = false;

    public RoundedTextField(String placeholder) {
        super();
        this.placeholder = placeholder;
        init();
    }

    public RoundedTextField() {
        super();
        this.placeholder = "";
        init();
    }

    private void init() {
        setOpaque(false);
        setBackground(BG);
        setForeground(TEXT_COLOR);
        setCaretColor(new Color(0xFF69B4));
        setFont(new Font("Segoe UI", Font.PLAIN, 13));
        setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        setEditable(true);
        setEnabled(true);
        setFocusable(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));

        addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { focused = true;  borderColor = BORDER_FOCUS;  repaint(); }
            @Override public void focusLost(FocusEvent e)   { focused = false; borderColor = BORDER_NORMAL; repaint(); }
        });
    }

    /** Fuerza los colores correctos ignorando UIManager, necesario con tema oscuro. */
    @Override
    public void updateUI() {
        super.updateUI();
        setBackground(BG);
        setForeground(TEXT_COLOR);
        setCaretColor(new Color(0xFF69B4));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();

        // Fondo
        g2.setColor(BG);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 12, 12));

        // Borde
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new RoundRectangle2D.Float(0.75f, 0.75f, w - 1.5f, h - 1.5f, 11, 11));

        g2.dispose();
        super.paintComponent(g);

        // Placeholder
        if (placeholder != null && !placeholder.isEmpty() && getText().isEmpty() && !focused) {
            Graphics2D gp = (Graphics2D) g.create();
            gp.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            gp.setColor(PLACEHOLDER_COLOR);
            gp.setFont(getFont());
            Insets ins = getInsets();
            FontMetrics fm = gp.getFontMetrics();
            int y = ins.top + (h - ins.top - ins.bottom - fm.getHeight()) / 2 + fm.getAscent();
            gp.drawString(placeholder, ins.left, y);
            gp.dispose();
        }
    }

    @Override protected void paintBorder(Graphics g) { /* manejado en paintComponent */ }

    public void setPlaceholder(String placeholder) { this.placeholder = placeholder; repaint(); }

    @Override
    public void setEditable(boolean b) {
        super.setEditable(b);
        repaint();
    }
}
