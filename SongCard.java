package smartplayer.views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import smartplayer.models.Song;
import smartplayer.controllers.CoverArtController;

/**
 * Tarjeta de canción tema rosado: carátula redondeada, título y artista.
 * Hover: fondo azul navy más claro, leve escala de la carátula.
 */
public class SongCard extends JPanel {

    private static final Color BG_NORMAL = new Color(0x16213E);
    private static final Color BG_HOVER  = new Color(0x2A2A4A);
    private static final int   CARD_W    = 160;
    private static final int   CARD_H    = 200;
    private static final int   IMG_SIZE  = 120;

    private Song song;
    private ImageIcon coverIcon;
    private boolean hovered = false;
    private Timer hoverTimer;

    private JLabel lblTitulo;
    private JLabel lblArtista;
    private JLabel lblCover;

    private Runnable onClickCallback;

    public SongCard(Song song, CoverArtController coverArtCtrl) {
        this.song = song;
        this.coverIcon = coverArtCtrl.getCaratulaGrande(song, IMG_SIZE, IMG_SIZE);

        setLayout(new BorderLayout(0, 4));
        setOpaque(false);
        setPreferredSize(new Dimension(CARD_W, CARD_H));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Imagen de carátula
        lblCover = new JLabel(coverIcon);
        lblCover.setHorizontalAlignment(SwingConstants.CENTER);

        // Título en rosa
        lblTitulo = new JLabel(truncar(song.getTitle(), 16));
        lblTitulo.setForeground(new Color(0xFF69B4));
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);

        // Artista en gris
        lblArtista = new JLabel(truncar(song.getArtist(), 18));
        lblArtista.setForeground(new Color(0xC0C0C0));
        lblArtista.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblArtista.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        textPanel.setOpaque(false);
        textPanel.add(lblTitulo);
        textPanel.add(lblArtista);

        add(lblCover, BorderLayout.CENTER);
        add(textPanel, BorderLayout.SOUTH);

        // Timer de animación hover
        hoverTimer = new Timer(16, e -> repaint());

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                hoverTimer.start();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                hoverTimer.stop();
                repaint();
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                if (onClickCallback != null) onClickCallback.run();
            }
        });
    }

    private String truncar(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 2) + ".." : s;
    }

    public void setOnClickCallback(Runnable r) { this.onClickCallback = r; }
    public Song getSong() { return song; }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        g2.setColor(hovered ? BG_HOVER : BG_NORMAL);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 12, 12));

        g2.dispose();
        super.paintComponent(g);
    }
}
