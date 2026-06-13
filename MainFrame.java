package smartplayer.views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import smartplayer.controllers.LibraryController;
import smartplayer.controllers.PlayerController;
import smartplayer.controllers.PlaylistController;
import smartplayer.utils.ConfigManager;
import smartplayer.utils.StatsManager;
import smartplayer.utils.ThemeManager;

/**
 * Ventana principal de Lumina con diseño rosado premium.
 * TabbedPane custom con indicador rosa, barra superior con degradado.
 */
public class MainFrame extends JFrame {

    // Paleta rosada premium
    private static final Color BG_MAIN  = new Color(0x1A1A2E);
    private static final Color BG_PANEL = new Color(0x16213E);
    private static final Color BG_TOP   = new Color(0x0D0D1F);
    private static final Color ACCENT   = new Color(0xFF69B4);
    private static final Color TEXT_SEC = new Color(0xC0C0C0);

    private final LibraryController  libraryCtrl;
    private PlayerController   playerCtrl;
    private final PlaylistController playlistCtrl;
    private final ConfigManager      configManager;

    private final BibliotecaPanel   bibliotecaPanel;
    private ReproductorPanel  reproductorPanel;
    private final PlaylistPanel     playlistPanel;
    private final FavoritosPanel    favoritosPanel;
    private final EstadisticasPanel estadisticasPanel;
    private final ArbolesPanel      arbolesPanel;
    private LyricsPanel       lyricsPanel;
    private QueuePanel        queuePanel;

    public MainFrame(ConfigManager configManager) {
        this.configManager = configManager;

        libraryCtrl  = new LibraryController();
        playerCtrl   = new PlayerController();
        playlistCtrl = new PlaylistController();

        StatsManager statsManager = playerCtrl.getStatsManager();

        setTitle("Lumina — Tu música, tu esencia");
        setSize(1200, 820);
        setMinimumSize(new Dimension(1000, 680));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_MAIN);

        aplicarEstiloOscuro();

        JPanel topBar = crearBarraSuperior();
        add(topBar, BorderLayout.NORTH);

        reproductorPanel  = new ReproductorPanel(playerCtrl);
        bibliotecaPanel   = new BibliotecaPanel(libraryCtrl, reproductorPanel);
        playlistPanel     = new PlaylistPanel(playlistCtrl, libraryCtrl, reproductorPanel);
        favoritosPanel    = new FavoritosPanel(libraryCtrl, reproductorPanel);
        estadisticasPanel = new EstadisticasPanel(libraryCtrl, playerCtrl, playlistCtrl, statsManager);
        arbolesPanel      = new ArbolesPanel(libraryCtrl);
        lyricsPanel       = new LyricsPanel(playerCtrl);
        queuePanel        = new QueuePanel(playerCtrl);

        JTabbedPane tabbedPane = crearTabbedPane();
        tabbedPane.addTab("Biblioteca",   bibliotecaPanel);
        tabbedPane.addTab("Playlists",    playlistPanel);
        tabbedPane.addTab("Favoritos",    favoritosPanel);
        tabbedPane.addTab("Estadísticas", estadisticasPanel);
        tabbedPane.addTab("Árboles",      arbolesPanel);
        tabbedPane.addTab("Letras",       lyricsPanel);
        tabbedPane.addTab("Cola",         queuePanel);

        add(tabbedPane,       BorderLayout.CENTER);
        add(reproductorPanel, BorderLayout.SOUTH);

        // Ocultar/mostrar el reproductor según la pestaña activa
        // En "Árboles" (índice 4) no se muestra el reproductor
        tabbedPane.addChangeListener(e -> {
            int idx = tabbedPane.getSelectedIndex();
            boolean mostrarReproductor = (idx != 4); // 4 = Árboles
            reproductorPanel.setVisible(mostrarReproductor);
            // Actualizar favoritos cuando se selecciona esa pestaña
            if (idx == 2) favoritosPanel.enfocar();
            revalidate();
        });

        reproductorPanel.setOnSongChangeCallback(() -> {
            lyricsPanel.actualizarLetra();
            queuePanel.actualizarCola();
        });

        int volGuardado = configManager.getInt("volume", 80);
        playerCtrl.setVolume(volGuardado / 100.0f);

        String temaGuardado = configManager.get("theme", "DARK");
        if ("LIGHT".equalsIgnoreCase(temaGuardado)) {
            ThemeManager.aplicarTema(this, ThemeManager.Theme.LIGHT);
        }

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                configManager.set("volume", String.valueOf((int)(playerCtrl.getVolume() * 100)));
                configManager.set("theme", ThemeManager.getCurrentTheme().toString());
                configManager.guardar();
                playerCtrl.stop();
                statsManager.guardarEstadisticas();
                System.exit(0);
            }
        });
    }

    /** Crea la barra superior con logo "Lumina" en rosa y menú de opciones. */
    private JPanel crearBarraSuperior() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Degradado oscuro de arriba a abajo
                GradientPaint gp = new GradientPaint(0, 0, BG_TOP, 0, getHeight(), BG_MAIN);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0, 60));
        bar.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        // Logo "Lumina" con icono de vinilo/nota musical
        JLabel lblLogo = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int cx = 20, cy = (getHeight() - 36) / 2;
                int r = 18; // radio del disco

                // Disco vinilo exterior (degradado oscuro-morado)
                GradientPaint gpDisco = new GradientPaint(cx - r, cy, new Color(0x3A1A5E), cx + r, cy + 2*r, new Color(0x1A0A2E));
                g2.setPaint(gpDisco);
                g2.fillOval(cx - r, cy, 2*r, 2*r);

                // Surcos del disco (anillos concéntricos)
                g2.setColor(new Color(0xFF69B4, true));
                for (int ring = 4; ring < r - 5; ring += 4) {
                    g2.drawOval(cx - ring, cy + r - ring, 2*ring, 2*ring);
                }

                // Círculo central rosa (etiqueta)
                GradientPaint gpLabel = new GradientPaint(cx - 7, cy + r - 7, new Color(0xFF85C8), cx + 7, cy + r + 7, new Color(0xE91E8C));
                g2.setPaint(gpLabel);
                g2.fillOval(cx - 7, cy + r - 7, 14, 14);

                // Nota musical blanca en la etiqueta
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                FontMetrics fmn = g2.getFontMetrics();
                g2.drawString("♪", cx - fmn.stringWidth("♪")/2, cy + r + fmn.getAscent()/2 - 1);

                // "Lumina" en blanco grande con acento brillante
                g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
                FontMetrics fm2 = g2.getFontMetrics();
                int tx = cx + r + 10;
                int ty = (getHeight() + fm2.getAscent() - fm2.getDescent()) / 2;
                // Sombra sutil
                g2.setColor(new Color(0, 0, 0, 80));
                g2.drawString("Lumina", tx + 1, ty + 1);
                // Degradado texto
                GradientPaint gpText = new GradientPaint(tx, ty - fm2.getAscent(), new Color(0xFFB0D8), tx, ty, new Color(0xFF69B4));
                g2.setPaint(gpText);
                g2.drawString("Lumina", tx, ty);

                // Subtítulo pequeño
                g2.setColor(new Color(0x9090B0));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2.drawString("Tu música, tu esencia", tx + 1, ty + 13);

                g2.dispose();
            }
        };
        lblLogo.setPreferredSize(new Dimension(230, 60));

        // Popup oscuro para menú
        JPopupMenu popupOpciones = new JPopupMenu();
        popupOpciones.setBackground(new Color(0x2A2A4A));
        popupOpciones.setBorder(BorderFactory.createLineBorder(new Color(0x4A4A6A), 1));
        popupOpciones.setOpaque(true);

        JMenuItem itemTema = crearMenuItem("Cambiar Tema");
        itemTema.addActionListener(e -> ThemeManager.toggleTema(this));

        JMenuItem itemGuardar = crearMenuItem("Guardar Configuración");
        itemGuardar.addActionListener(e -> {
            configManager.set("volume", String.valueOf((int)(playerCtrl.getVolume() * 100)));
            configManager.set("theme", ThemeManager.getCurrentTheme().toString());
            configManager.guardar();
            JOptionPane.showMessageDialog(this, "Configuración guardada", "Lumina", JOptionPane.INFORMATION_MESSAGE);
        });

        popupOpciones.add(itemTema);
        popupOpciones.addSeparator();
        popupOpciones.add(itemGuardar);

        // Botón Opciones rosa
        JButton btnOpciones = new JButton("Opciones") {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isArmed() || getModel().isRollover()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(0x2A2A4A));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        btnOpciones.setForeground(TEXT_SEC);
        btnOpciones.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnOpciones.setContentAreaFilled(false);
        btnOpciones.setBorderPainted(false);
        btnOpciones.setFocusPainted(false);
        btnOpciones.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnOpciones.addActionListener(e ->
            popupOpciones.show(btnOpciones, 0, btnOpciones.getHeight()));

        bar.add(lblLogo,     BorderLayout.WEST);
        bar.add(btnOpciones, BorderLayout.EAST);
        return bar;
    }

    /** Crea TabbedPane con indicador rosa en la pestaña activa. */
    private JTabbedPane crearTabbedPane() {
        JTabbedPane tp = new JTabbedPane() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BG_MAIN);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tp.setBackground(BG_MAIN);
        tp.setForeground(TEXT_SEC);
        tp.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tp.setBorder(BorderFactory.createEmptyBorder());
        tp.setUI(new PinkTabbedPaneUI());
        return tp;
    }

    private JMenuItem crearMenuItem(String texto) {
        JMenuItem item = new JMenuItem(texto);
        item.setBackground(new Color(0x2A2A4A));
        item.setForeground(Color.WHITE);
        item.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        item.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        return item;
    }

    private void aplicarEstiloOscuro() {
        UIManager.put("Panel.background",             BG_MAIN);
        UIManager.put("Label.foreground",             Color.WHITE);
        UIManager.put("Button.background",            ACCENT);
        UIManager.put("Button.foreground",            Color.WHITE);
        UIManager.put("Table.background",             BG_PANEL);
        UIManager.put("Table.foreground",             Color.WHITE);
        UIManager.put("Table.gridColor",              new Color(0x2A2A4A));
        UIManager.put("Table.selectionBackground",    new Color(255, 105, 180, 80));
        UIManager.put("Table.selectionForeground",    Color.WHITE);
        UIManager.put("TableHeader.background",       BG_TOP);
        UIManager.put("TableHeader.foreground",       TEXT_SEC);
        UIManager.put("ScrollPane.background",        BG_MAIN);
        UIManager.put("ScrollPane.border",            BorderFactory.createEmptyBorder());
        UIManager.put("TextArea.background",          BG_PANEL);
        UIManager.put("TextArea.foreground",          Color.WHITE);
        UIManager.put("TextField.background",         new Color(0x2A2A4A));
        UIManager.put("TextField.foreground",         Color.WHITE);
        UIManager.put("TextField.caretForeground",    Color.WHITE);
        UIManager.put("TabbedPane.background",        BG_MAIN);
        UIManager.put("TabbedPane.foreground",        TEXT_SEC);
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
        UIManager.put("List.background",              BG_PANEL);
        UIManager.put("List.foreground",              Color.WHITE);
        UIManager.put("List.selectionBackground",     new Color(0x2A2A4A));
        UIManager.put("List.selectionForeground",     Color.WHITE);
        UIManager.put("ComboBox.background",          new Color(0x2A2A4A));
        UIManager.put("ComboBox.foreground",          Color.WHITE);
        UIManager.put("ComboBox.selectionBackground", new Color(0xFF69B4));
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);
        UIManager.put("ComboBox.buttonBackground",    new Color(0x2A2A4A));
        UIManager.put("ComboBox.buttonShadow",        new Color(0x2A2A4A));
        UIManager.put("ComboBox.buttonDarkShadow",    new Color(0x2A2A4A));
        UIManager.put("ComboBox.buttonHighlight",     new Color(0x3A3A6A));
        UIManager.put("ComboBox.disabledBackground",  BG_PANEL);
        UIManager.put("ComboBox.disabledForeground",  new Color(0x4A4A6A));
        UIManager.put("ComboBoxUI",                   "javax.swing.plaf.basic.BasicComboBoxUI");
        UIManager.put("MenuBar.background",           BG_MAIN);
        UIManager.put("Menu.background",              BG_MAIN);
        UIManager.put("Menu.foreground",              TEXT_SEC);
        UIManager.put("MenuItem.background",          new Color(0x2A2A4A));
        UIManager.put("MenuItem.foreground",          Color.WHITE);
        UIManager.put("MenuItem.selectionBackground", new Color(0xFF69B4));
        UIManager.put("MenuItem.selectionForeground", Color.WHITE);
        UIManager.put("PopupMenu.background",         new Color(0x2A2A4A));
        UIManager.put("PopupMenu.border",             BorderFactory.createLineBorder(new Color(0x4A4A6A), 1));
        UIManager.put("SplitPane.background",         BG_MAIN);
        UIManager.put("SplitPane.dividerSize",        6);
        UIManager.put("OptionPane.background",        new Color(0x16213E));
        UIManager.put("OptionPane.messageForeground", Color.WHITE);
        UIManager.put("CheckBox.background",          BG_MAIN);
        UIManager.put("CheckBox.foreground",          Color.WHITE);
        UIManager.put("Separator.foreground",         new Color(0x4A4A6A));
        UIManager.put("Separator.background",         new Color(0x2A2A4A));
    }

    // ---- UI de pestañas tema rosado ----
    private static class PinkTabbedPaneUI extends javax.swing.plaf.basic.BasicTabbedPaneUI {

        private static final Color BG         = new Color(0x1A1A2E);
        private static final Color TAB_BG     = new Color(0x1A1A2E);
        private static final Color TAB_SEL_BG = new Color(0x1A1A2E);
        private static final Color ACCENT     = new Color(0xFF69B4);
        private static final Color TEXT_NORMAL= new Color(0xC0C0C0);
        private static final Color TEXT_SEL   = Color.WHITE;

        @Override
        protected void installDefaults() {
            super.installDefaults();
            tabInsets            = new Insets(10, 18, 10, 18);
            selectedTabPadInsets = new Insets(0, 0, 0, 0);
            tabAreaInsets        = new Insets(0, 0, 0, 0);
            contentBorderInsets  = new Insets(0, 0, 0, 0);
        }

        @Override
        protected void paintTabBackground(Graphics g, int tabPlacement,
                int tabIndex, int x, int y, int w, int h, boolean isSelected) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(isSelected ? TAB_SEL_BG : TAB_BG);
            g2.fillRect(x, y, w, h);
            g2.dispose();
        }

        @Override
        protected void paintTabBorder(Graphics g, int tabPlacement,
                int tabIndex, int x, int y, int w, int h, boolean isSelected) {
            if (isSelected) {
                // Línea rosa abajo de la pestaña activa
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Degradado horizontal en la línea indicadora
                GradientPaint gp = new GradientPaint(x, y + h - 3, new Color(0xFF85C8), x + w, y + h - 3, new Color(0xE91E8C));
                g2.setPaint(gp);
                g2.fillRoundRect(x + 4, y + h - 3, w - 8, 3, 3, 3);
                g2.dispose();
            }
        }

        @Override
        protected void paintText(Graphics g, int tabPlacement, Font font,
                FontMetrics metrics, int tabIndex, String title,
                Rectangle textRect, boolean isSelected) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(new Font("Segoe UI", isSelected ? Font.BOLD : Font.PLAIN, 13));
            g2.setColor(isSelected ? TEXT_SEL : TEXT_NORMAL);
            g2.drawString(title, textRect.x, textRect.y + metrics.getAscent());
            g2.dispose();
        }

        @Override
        protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
            // Sin borde de contenido
        }

        @Override
        protected void paintFocusIndicator(Graphics g, int tabPlacement,
                Rectangle[] rects, int tabIndex, Rectangle iconRect,
                Rectangle textRect, boolean isSelected) {
            // Sin indicador de foco
        }

        @Override
        protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
            return 44;
        }
    }

}
