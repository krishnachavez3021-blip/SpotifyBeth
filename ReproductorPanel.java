package smartplayer.views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import smartplayer.controllers.PlayerController;
import smartplayer.controllers.CoverArtController;
import smartplayer.models.Song;

/**
 * Barra inferior del reproductor - Tema rosado premium.
 * Altura 110px. Tres zonas: [Caratula+Info] | [Controles+Progreso] | [Volumen+Ecualizador]
 * Botones dibujados con Graphics2D. Todos los botones tienen ActionListeners funcionales.
 * El slider de volumen controla el volumen real a traves de VolumeAudioDevice.
 * La barra de progreso soporta click para seek.
 */
public class ReproductorPanel extends JPanel {

    private static final Color BG_BAR    = new Color(0x16213E);
    private static final Color BG_BORDER = new Color(0x4A4A6A);
    private static final Color ACCENT    = new Color(0xFF69B4);
    private static final Color ACCENT_H  = new Color(0xFF85C8);
    private static final Color TEXT_MAIN = Color.WHITE;
    private static final Color TEXT_SEC  = new Color(0xC0C0C0);

    private PlayerController   playerCtrl;
    private CoverArtController coverArtCtrl;

    private JLabel lblTitle;
    private JLabel lblArtist;
    private JLabel lblCover;
    private JLabel lblTimeElapsed;
    private JLabel lblTimeDuration;
    private JButton btnVolIcon; // Icono de volumen clickeable (toggle mute)

    private JButton btnPlayPause;
    private JButton btnPrev;
    private JButton btnNext;
    private JButton btnStop;
    private JButton btnShuffle;
    private JButton btnRepeat;

    private SpotifyProgressBar progressBar;
    private SpotifySlider      sliderVolumen;
    private EqualizerPanel     equalizerPanel;

    private Runnable onSongChangeCallback;
    private int      currentSecs = 0;
    private boolean  muted       = false;
    private float    volAntesMute = 0.8f;

    // Estado de botones de modo
    private boolean shuffleActive = false;
    private boolean repeatActive  = false;

    public ReproductorPanel(PlayerController playerCtrl) {
        this.playerCtrl   = playerCtrl;
        this.coverArtCtrl = new CoverArtController();

        setPreferredSize(new Dimension(0, 110));
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_BAR);
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BG_BORDER));

        add(crearPanelIzquierdo(), BorderLayout.WEST);
        add(crearPanelCentral(),  BorderLayout.CENTER);
        add(crearPanelDerecho(),  BorderLayout.EAST);

        // Timer de progreso (1 seg)
        Timer progressTimer = new Timer(1000, e -> {
            if (playerCtrl.isPlaying() && playerCtrl.getCurrentSong() != null) {
                Song s = playerCtrl.getCurrentSong();
                long durMs = s.getDuration() * 1000L;
                currentSecs++;
                if (durMs > 0) {
                    int percent = (int)((currentSecs * 1000.0 / durMs) * 100);
                    progressBar.setValue(Math.min(percent, 100));
                }
                lblTimeElapsed.setText(formatTime(currentSecs));
            }
        });
        progressTimer.start();

        playerCtrl.setOnSongEndCallback(() -> SwingUtilities.invokeLater(this::nextSong));

        playerCtrl.setOnSongStartCallback(() -> {
            Song s = playerCtrl.getCurrentSong();
            if (s != null) actualizarUI(s);
        });
    }

    // ---- Panel izquierdo: caratula 80x80 con sombra rosa + info ----
    private JPanel crearPanelIzquierdo() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(290, 110));

        // Caratula con clip redondeado 20px y sombra rosa
        lblCover = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();

                // Sombra rosa suave simulada
                for (int i = 4; i >= 1; i--) {
                    g2.setColor(new Color(255, 105, 180, 12 * i));
                    g2.fillRoundRect(-i + 1, i, w + i * 2 - 2, h, 24, 24);
                }

                if (getIcon() != null) {
                    // Clip redondeado 20px
                    g2.setClip(new RoundRectangle2D.Float(0, 0, w, h, 20, 20));
                    getIcon().paintIcon(this, g2, 0, 0);
                    g2.setClip(null);
                    // Borde sutil rosa
                    g2.setColor(new Color(255, 105, 180, 60));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(0, 0, w - 1, h - 1, 20, 20);
                } else {
                    // Fondo degradado rosa/morado si no hay caratula
                    GradientPaint gp = new GradientPaint(0, 0, new Color(0x2A2A4A), w, h, new Color(0xFF69B4, true).darker());
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, w, h, 20, 20);
                    // Letra M (por Music)
                    g2.setColor(new Color(255, 255, 255, 120));
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 28));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString("M", (w - fm.stringWidth("M")) / 2, (h + fm.getAscent() - fm.getDescent()) / 2);
                }

                g2.dispose();
            }
        };
        lblCover.setIcon(coverArtCtrl.getIconoGenerico());
        lblCover.setPreferredSize(new Dimension(80, 80));
        lblCover.setOpaque(false);

        // Info vertical: titulo + artista
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.setPreferredSize(new Dimension(178, 52));

        lblTitle = new JLabel("Sin cancion");
        lblTitle.setForeground(TEXT_MAIN);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblArtist = new JLabel("");
        lblArtist.setForeground(TEXT_SEC);
        lblArtist.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblArtist.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(Box.createVerticalGlue());
        infoPanel.add(lblTitle);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(lblArtist);
        infoPanel.add(Box.createVerticalGlue());

        panel.add(lblCover);
        panel.add(infoPanel);
        return panel;
    }

    // ---- Panel central: botones graficos + barra de progreso ----
    private JPanel crearPanelCentral() {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        // Fila de botones graficos
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        controls.setOpaque(false);

        btnShuffle   = crearBotonGrafico("shuffle", 36);
        btnPrev      = crearBotonGrafico("prev",    36);
        btnPlayPause = crearBotonGrafico("play",    50);
        btnNext      = crearBotonGrafico("next",    36);
        btnStop      = crearBotonGrafico("stop",    36);
        btnRepeat    = crearBotonGrafico("repeat",  36);

        // Tooltips para todos los botones
        btnShuffle.setToolTipText("Modo aleatorio (activar/desactivar)");
        btnPrev.setToolTipText("Cancion anterior");
        btnPlayPause.setToolTipText("Reproducir / Pausar");
        btnNext.setToolTipText("Siguiente cancion");
        btnStop.setToolTipText("Detener reproduccion");
        btnRepeat.setToolTipText("Repetir lista (activar/desactivar)");

        // --- ActionListeners funcionales para todos los botones ---

        btnPrev.addActionListener(e -> {
            System.out.println("[DEBUG] Click: Anterior");
            prevSong();
        });

        btnPlayPause.addActionListener(e -> {
            System.out.println("[DEBUG] Click: Play/Pause");
            togglePlayPause();
        });

        btnNext.addActionListener(e -> {
            System.out.println("[DEBUG] Click: Siguiente");
            nextSong();
        });

        btnStop.addActionListener(e -> {
            System.out.println("[DEBUG] Click: Stop");
            stopSong();
        });

        // Boton Aleatorio: alterna entre modo NORMAL y RANDOM
        btnShuffle.addActionListener(e -> {
            System.out.println("[DEBUG] Click: Aleatorio");
            shuffleActive = !shuffleActive;
            btnShuffle.putClientProperty("active", shuffleActive);
            btnShuffle.repaint();
            if (shuffleActive) {
                // Desactivar repeticion si estaba activa
                repeatActive = false;
                btnRepeat.putClientProperty("active", false);
                btnRepeat.repaint();
                playerCtrl.setMode(PlayerController.PlayMode.RANDOM);
                mostrarMensajeModo("Modo aleatorio activado");
            } else {
                playerCtrl.setMode(PlayerController.PlayMode.NORMAL);
                mostrarMensajeModo("Modo normal");
            }
        });

        // Boton Repetir: alterna entre modo NORMAL y CIRCULAR
        btnRepeat.addActionListener(e -> {
            System.out.println("[DEBUG] Click: Repetir");
            repeatActive = !repeatActive;
            btnRepeat.putClientProperty("active", repeatActive);
            btnRepeat.repaint();
            if (repeatActive) {
                // Desactivar shuffle si estaba activo
                shuffleActive = false;
                btnShuffle.putClientProperty("active", false);
                btnShuffle.repaint();
                playerCtrl.setMode(PlayerController.PlayMode.CIRCULAR);
                mostrarMensajeModo("Modo repeticion activado");
            } else {
                playerCtrl.setMode(PlayerController.PlayMode.NORMAL);
                mostrarMensajeModo("Modo normal");
            }
        });

        controls.add(btnShuffle);
        controls.add(btnPrev);
        controls.add(btnPlayPause);
        controls.add(btnNext);
        controls.add(btnStop);
        controls.add(btnRepeat);

        // Fila de progreso: tiempo | barra | duracion
        JPanel progressRow = new JPanel(new BorderLayout(8, 0));
        progressRow.setOpaque(false);
        progressRow.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        lblTimeElapsed = new JLabel("0:00");
        lblTimeElapsed.setForeground(TEXT_SEC);
        lblTimeElapsed.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTimeElapsed.setPreferredSize(new Dimension(34, 16));

        lblTimeDuration = new JLabel("0:00");
        lblTimeDuration.setForeground(TEXT_SEC);
        lblTimeDuration.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTimeDuration.setPreferredSize(new Dimension(34, 16));
        lblTimeDuration.setHorizontalAlignment(SwingConstants.RIGHT);

        progressBar = new SpotifyProgressBar();

        // Seek: click en la barra de progreso salta a esa posicion
        progressBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (playerCtrl.getCurrentSong() == null) return;
                double pct = (double) e.getX() / progressBar.getWidth();
                pct = Math.max(0.0, Math.min(1.0, pct));
                System.out.println("[DEBUG] Seek al " + (int)(pct * 100) + "%");
                int newSecs = playerCtrl.seekTo(pct);
                if (newSecs >= 0) {
                    currentSecs = newSecs;
                    progressBar.setValue((int)(pct * 100));
                    lblTimeElapsed.setText(formatTime(newSecs));
                }
            }
        });
        progressBar.setToolTipText("Click para saltar a esa posicion");
        progressBar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        progressRow.add(lblTimeElapsed,  BorderLayout.WEST);
        progressRow.add(progressBar,     BorderLayout.CENTER);
        progressRow.add(lblTimeDuration, BorderLayout.EAST);

        panel.add(controls,    BorderLayout.CENTER);
        panel.add(progressRow, BorderLayout.SOUTH);
        return panel;
    }

    /** Muestra brevemente el modo activo como tooltip en la barra de estado. */
    private void mostrarMensajeModo(String mensaje) {
        // Mostrar en el label de titulo temporalmente
        final String tituloAnterior = lblTitle.getText();
        lblTitle.setForeground(ACCENT);
        lblTitle.setText(mensaje);
        Timer timer = new Timer(2000, e2 -> {
            lblTitle.setForeground(TEXT_MAIN);
            lblTitle.setText(tituloAnterior);
        });
        timer.setRepeats(false);
        timer.start();
    }

    // ---- Panel derecho: ecualizador + icono volumen + slider ----
    private JPanel crearPanelDerecho() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(280, 110));

        // Ecualizador visual
        equalizerPanel = new EqualizerPanel();
        equalizerPanel.setPreferredSize(new Dimension(80, 40));

        // Boton icono de volumen (toggle mute)
        btnVolIcon = new JButton("VOL") { // Texto volumen
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        btnVolIcon.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnVolIcon.setForeground(TEXT_SEC);
        btnVolIcon.setContentAreaFilled(false);
        btnVolIcon.setBorderPainted(false);
        btnVolIcon.setFocusPainted(false);
        btnVolIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnVolIcon.setPreferredSize(new Dimension(35, 30));
        btnVolIcon.setToolTipText("Click para silenciar/activar");
        btnVolIcon.addActionListener(e -> {
            System.out.println("[DEBUG] Click: Mute toggle");
            toggleMute();
        });

        // Slider de volumen - controla volumen real via VolumeAudioDevice
        sliderVolumen = new SpotifySlider(0, 100, 80);
        sliderVolumen.setPreferredSize(new Dimension(100, 20));
        sliderVolumen.setToolTipText("Volumen");
        sliderVolumen.addChangeListener(e -> {
            int val = sliderVolumen.getValue();
            playerCtrl.setVolume(val / 100.0f);
            actualizarIconoVolumen(val);
        });

        panel.add(equalizerPanel);
        panel.add(btnVolIcon);
        panel.add(sliderVolumen);
        return panel;
    }

    /** Actualiza el texto del icono segun el nivel de volumen. */
    private void actualizarIconoVolumen(int vol) {
        if (muted || vol == 0) {
            btnVolIcon.setText("MUTE");
        } else if (vol < 33) {
            btnVolIcon.setText("LOW");
        } else if (vol < 66) {
            btnVolIcon.setText("MED");
        } else {
            btnVolIcon.setText("VOL");
        }
    }

    /** Toggle mute: silencia o restaura el volumen. */
    private void toggleMute() {
        if (!muted) {
            volAntesMute = sliderVolumen.getValue() / 100.0f;
            muted = true;
            playerCtrl.setVolume(0f);
            sliderVolumen.setValue(0);
            btnVolIcon.setText("MUTE");
        } else {
            muted = false;
            int volRestore = Math.max(10, (int)(volAntesMute * 100));
            sliderVolumen.setValue(volRestore);
            playerCtrl.setVolume(volAntesMute);
            actualizarIconoVolumen(volRestore);
        }
    }

    /**
     * Crea un boton con icono dibujado con Graphics2D.
     * Soporta propiedad "active" (Boolean) para mostrar indicador de estado activo.
     * tipo: "play", "pause", "prev", "next", "stop", "shuffle", "repeat"
     * size: tamano del boton (cuadrado)
     */
    private JButton crearBotonGrafico(String tipo, int size) {
        final String tipoFinal = tipo;
        final int sizeFinal = size;

        JButton btn = new JButton() {
            private boolean hov = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hov = false; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();
                boolean active = Boolean.TRUE.equals(getClientProperty("active"));
                Color iconColor = hov ? ACCENT_H : (tipoFinal.equals("play") || tipoFinal.equals("pause") ? Color.BLACK : TEXT_SEC);

                // Fondo circular para play/pause
                if (tipoFinal.equals("play") || tipoFinal.equals("pause")) {
                    GradientPaint gp = new GradientPaint(0, 0, ACCENT, w, h, new Color(0xE91E8C));
                    g2.setPaint(gp);
                    g2.fillOval(0, 0, w, h);
                    iconColor = Color.WHITE;
                } else if (hov) {
                    g2.setColor(new Color(255, 255, 255, 18));
                    g2.fillOval(2, 2, w - 4, h - 4);
                }

                // Dibujar el icono
                g2.setColor(active ? ACCENT : iconColor);
                dibujarIcono(g2, tipoFinal, w, h);

                // Indicador de estado activo (punto rosa debajo del icono)
                if (active && !tipoFinal.equals("play") && !tipoFinal.equals("pause")) {
                    g2.setColor(ACCENT);
                    int cx = w / 2;
                    g2.fillOval(cx - 3, h - 7, 6, 6);
                }

                g2.dispose();
            }

            private void dibujarIcono(Graphics2D g2, String tipo, int w, int h) {
                int cx = w / 2, cy = h / 2;
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                switch (tipo) {
                    case "play": {
                        int[] px = {cx - 7, cx + 9, cx - 7};
                        int[] py = {cy - 9, cy,     cy + 9};
                        g2.fillPolygon(px, py, 3);
                        break;
                    }
                    case "pause": {
                        g2.setColor(Color.WHITE);
                        g2.fillRoundRect(cx - 8, cy - 9, 5, 18, 3, 3);
                        g2.fillRoundRect(cx + 3, cy - 9, 5, 18, 3, 3);
                        break;
                    }
                    case "prev": {
                        g2.fillRect(cx - 10, cy - 8, 3, 16);
                        int[] px = {cx - 6, cx - 6, cx + 4};
                        int[] py = {cy - 8, cy + 8, cy};
                        g2.fillPolygon(px, py, 3);
                        break;
                    }
                    case "next": {
                        int[] px = {cx - 4, cx - 4, cx + 6};
                        int[] py = {cy - 8, cy + 8, cy};
                        g2.fillPolygon(px, py, 3);
                        g2.fillRect(cx + 7, cy - 8, 3, 16);
                        break;
                    }
                    case "stop": {
                        g2.fillRoundRect(cx - 7, cy - 7, 14, 14, 3, 3);
                        break;
                    }
                    case "shuffle": {
                        g2.drawLine(cx - 8, cy - 6, cx + 8, cy + 6);
                        g2.drawLine(cx - 8, cy + 6, cx + 8, cy - 6);
                        g2.drawLine(cx + 5, cy + 6, cx + 8, cy + 6);
                        g2.drawLine(cx + 8, cy + 6, cx + 8, cy + 3);
                        g2.drawLine(cx + 5, cy - 6, cx + 8, cy - 6);
                        g2.drawLine(cx + 8, cy - 6, cx + 8, cy - 3);
                        break;
                    }
                    case "repeat": {
                        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2.drawArc(cx - 8, cy - 8, 16, 16, 30, 300);
                        g2.drawLine(cx + 4, cy - 9, cx + 8, cy - 8);
                        g2.drawLine(cx + 8, cy - 8, cx + 7, cy - 4);
                        break;
                    }
                }
            }
        };

        btn.setPreferredSize(new Dimension(sizeFinal, sizeFinal));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ---- Logica de reproduccion ----
    public void playSingle(Song song) {
        playerCtrl.playSong(song);
        actualizarUI(song);
    }

    public void togglePlayPause() {
        if (playerCtrl.isPlaying()) {
            playerCtrl.pause();
            btnPlayPause = reemplazarBotonGrafico(btnPlayPause, "play", 50);
            equalizerPanel.setAnimating(false);
        } else {
            playerCtrl.resume();
            btnPlayPause = reemplazarBotonGrafico(btnPlayPause, "pause", 50);
            equalizerPanel.setAnimating(true);
        }
    }

    /** Reemplaza el boton en el panel central manteniendo el ActionListener de toggle. */
    private JButton reemplazarBotonGrafico(JButton viejo, String tipo, int size) {
        Container parent = viejo.getParent();
        if (parent == null) {
            JButton nuevo = crearBotonGrafico(tipo, size);
            nuevo.setToolTipText("Reproducir / Pausar");
            nuevo.addActionListener(e -> {
                System.out.println("[DEBUG] Click: Play/Pause");
                togglePlayPause();
            });
            return nuevo;
        }
        int idx = 0;
        for (int i = 0; i < parent.getComponentCount(); i++) {
            if (parent.getComponent(i) == viejo) { idx = i; break; }
        }
        JButton nuevo = crearBotonGrafico(tipo, size);
        nuevo.setToolTipText("Reproducir / Pausar");
        nuevo.addActionListener(e -> {
            System.out.println("[DEBUG] Click: Play/Pause");
            togglePlayPause();
        });
        parent.remove(viejo);
        parent.add(nuevo, idx);
        parent.revalidate();
        parent.repaint();
        return nuevo;
    }

    public void nextSong() {
        System.out.println("[DEBUG] nextSong()");
        playerCtrl.next();
        Song siguiente = playerCtrl.getCurrentSong();
        if (siguiente != null) {
            actualizarUI(siguiente);
        }
    }

    public void prevSong() {
        System.out.println("[DEBUG] prevSong()");
        playerCtrl.prev();
        Song anterior = playerCtrl.getCurrentSong();
        if (anterior != null) {
            actualizarUI(anterior);
        }
    }

    private void stopSong() {
        playerCtrl.stop();
        lblTitle.setForeground(TEXT_MAIN);
        lblTitle.setText("Sin cancion");
        lblArtist.setText("");
        progressBar.setValue(0);
        lblTimeElapsed.setText("0:00");
        lblTimeDuration.setText("0:00");
        lblCover.setIcon(coverArtCtrl.getIconoGenerico());
        equalizerPanel.setAnimating(false);
        currentSecs = 0;
        btnPlayPause = reemplazarBotonGrafico(btnPlayPause, "play", 50);
    }

    private void actualizarUI(Song song) {
        currentSecs = 0;
        if (song != null) {
            lblTitle.setForeground(TEXT_MAIN);
            lblTitle.setText(truncar(song.getTitle(), 30));
            lblArtist.setText(truncar(song.getArtist() + " | " + song.getAlbum(), 36));
            progressBar.setValue(0);
            equalizerPanel.setAnimating(true);
            lblTimeDuration.setText(song.getDurationFormatted());
            lblTimeElapsed.setText("0:00");

            // Caratula 80x80
            ImageIcon cover = coverArtCtrl.getCaratulaGrande(song, 80, 80);
            lblCover.setIcon(cover);

            String tooltip = String.format("<html><b>%s</b><br>%s | %s<br>%s | %s<br>%.1f MB</html>",
                song.getTitle(), song.getArtist(), song.getAlbum(),
                song.getGenre(), song.getYear(), song.getSize() / (1024.0 * 1024.0));
            lblCover.setToolTipText(tooltip);

            btnPlayPause = reemplazarBotonGrafico(btnPlayPause, "pause", 50);
            if (onSongChangeCallback != null) onSongChangeCallback.run();
        } else {
            stopSong();
        }
    }

    private String truncar(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 2) + ".." : s;
    }

    private String formatTime(int secs) {
        return String.format("%d:%02d", secs / 60, secs % 60);
    }

    public void setOnSongChangeCallback(Runnable callback) { this.onSongChangeCallback = callback; }
    public PlayerController   getPlayerCtrl()   { return playerCtrl; }
    public CoverArtController getCoverArtCtrl() { return coverArtCtrl; }
}
