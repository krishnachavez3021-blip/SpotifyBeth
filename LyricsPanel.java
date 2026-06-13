package smartplayer.views;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.*;
import java.awt.geom.*;
import java.net.URI;
import java.net.URLEncoder;
import smartplayer.controllers.LyricsController;
import smartplayer.controllers.PlayerController;
import smartplayer.models.Song;

/**
 * Panel de letras con tema rosado premium.
 * Fondo con degradado oscuro, titulo cancion en rosa bold 22px,
 * artista en gris, letra centrada 18px.
 * Animacion "..." mientras busca. Icono microfono si no hay letra.
 * Incluye: boton "Buscar en Google", "Pegar Letra", auto-guardado.
 */
public class LyricsPanel extends JPanel {

    private static final Color BG_MAIN   = new Color(0x1A1A2E);
    private static final Color BG_HEADER = new Color(0x16213E);
    private static final Color ACCENT    = new Color(0xFF69B4);
    private static final Color TEXT_MAIN = Color.WHITE;
    private static final Color TEXT_SEC  = new Color(0xC0C0C0);
    private static final Color BORDER    = new Color(0x4A4A6A);

    private LyricsController lyricsCtrl;
    private PlayerController playerCtrl;

    // Componentes del header
    private JLabel  lblSongTitle;   // titulo cancion en rosa bold 22px
    private JLabel  lblArtist;      // artista en gris 14px
    private JLabel  lblInfo;        // info pequena

    // Area de letra
    private JPanel   lyricsBodyPanel;   // panel con degradado
    private JTextArea txtLetra;
    private JLabel   lblMic;            // icono microfono cuando no hay letra
    private JLabel   lblNoLyrics;       // mensaje cuando no hay letra

    private RoundedButton btnBuscarOnline;
    private RoundedButton btnBuscarEnGoogle; // abre navegador con busqueda
    private RoundedButton btnPegarLetra;     // pega desde portapapeles
    private RoundedButton btnEditar;
    private RoundedButton btnGuardar;
    private boolean       modoEdicion;

    // Timer para animacion "Buscando..."
    private Timer  loadingTimer;
    private int    dotCount = 0;

    public LyricsPanel(PlayerController playerCtrl) {
        this.playerCtrl = playerCtrl;
        this.lyricsCtrl = new LyricsController();
        this.modoEdicion = false;

        setLayout(new BorderLayout(0, 0));
        setBackground(BG_MAIN);

        add(crearHeader(), BorderLayout.NORTH);
        add(crearBodyArea(), BorderLayout.CENTER);

        // Timer de animacion de carga (puntos "...")
        loadingTimer = new Timer(400, e -> {
            dotCount = (dotCount + 1) % 4;
            String dots = ".".repeat(dotCount);
            txtLetra.setText("Buscando letra" + dots);
        });
    }

    /** Panel superior: titulo cancion (rosa bold 22px), artista (gris), botones. */
    private JPanel crearHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Degradado de navy oscuro a navy
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x0D0D1F), 0, getHeight(), BG_HEADER);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
            BorderFactory.createEmptyBorder(16, 24, 16, 24)
        ));

        // Columna izquierda: titulo y artista
        JPanel infoCol = new JPanel();
        infoCol.setLayout(new BoxLayout(infoCol, BoxLayout.Y_AXIS));
        infoCol.setOpaque(false);

        // Titulo seccion
        JLabel lblSeccion = new JLabel("LETRAS");
        lblSeccion.setForeground(new Color(0xFF85C8));
        lblSeccion.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblSeccion.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Titulo de la cancion en rosa bold 22px
        lblSongTitle = new JLabel("Sin cancion en reproduccion");
        lblSongTitle.setForeground(ACCENT);
        lblSongTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblSongTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Artista en gris 13px
        lblArtist = new JLabel("");
        lblArtist.setForeground(TEXT_SEC);
        lblArtist.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblArtist.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoCol.add(lblSeccion);
        infoCol.add(Box.createVerticalStrut(4));
        infoCol.add(lblSongTitle);
        infoCol.add(Box.createVerticalStrut(3));
        infoCol.add(lblArtist);

        // Botones a la derecha
        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botonesPanel.setOpaque(false);

        btnBuscarOnline    = new RoundedButton("Buscar en linea");
        btnBuscarEnGoogle  = new RoundedButton("Buscar en Google", RoundedButton.Variante.SECONDARY);
        btnPegarLetra      = new RoundedButton("Pegar Letra", RoundedButton.Variante.SECONDARY);
        btnEditar          = new RoundedButton("Editar Letra", RoundedButton.Variante.SECONDARY);
        btnGuardar         = new RoundedButton("Guardar Letra");
        btnGuardar.setEnabled(false);

        botonesPanel.add(btnBuscarOnline);
        botonesPanel.add(btnBuscarEnGoogle);
        botonesPanel.add(btnPegarLetra);
        botonesPanel.add(btnEditar);
        botonesPanel.add(btnGuardar);

        header.add(infoCol,      BorderLayout.WEST);
        header.add(botonesPanel, BorderLayout.EAST);

        // Acciones de botones
        btnEditar.addActionListener(e -> toggleEdicion());

        btnGuardar.addActionListener(e -> {
            Song current = playerCtrl.getCurrentSong();
            if (current != null && !txtLetra.getText().trim().isEmpty()) {
                lyricsCtrl.guardarLetraLocal(current, txtLetra.getText());
                // Desactivar modo edicion tras guardar
                if (modoEdicion) toggleEdicion();
                JOptionPane.showMessageDialog(this, "Letra guardada correctamente.", "Guardado", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnBuscarOnline.addActionListener(e -> buscarLetraOnline());

        // Boton "Buscar en Google": abre el navegador con busqueda de la letra
        btnBuscarEnGoogle.addActionListener(e -> abrirBusquedaGoogle());

        // Boton "Pegar Letra": pega el contenido del portapapeles en el area de texto
        btnPegarLetra.addActionListener(e -> pegarLetraDesdePortapapeles());

        return header;
    }

    /**
     * Abre el navegador predeterminado con una busqueda de Google para la letra.
     * Formato URL: https://www.google.com/search?q={artista}+{titulo}+letra
     */
    private void abrirBusquedaGoogle() {
        Song current = playerCtrl.getCurrentSong();
        if (current == null) {
            JOptionPane.showMessageDialog(this, "No hay cancion en reproduccion.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            String artista = lyricsCtrl.limpiarArtista(current.getArtist());
            String titulo  = lyricsCtrl.limpiarTitulo(current.getTitle());
            String query   = URLEncoder.encode(artista + " " + titulo + " letra", "UTF-8");
            String urlStr  = "https://www.google.com/search?q=" + query;
            Desktop desktop = Desktop.getDesktop();
            if (Desktop.isDesktopSupported() && desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(new URI(urlStr));
            } else {
                JOptionPane.showMessageDialog(this,
                    "Abre este enlace en tu navegador:\n" + urlStr,
                    "Buscar en Google", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            System.err.println("[Google] Error abriendo navegador: " + ex.getMessage());
        }
    }

    /**
     * Pega el contenido del portapapeles en el area de texto y activa el modo edicion.
     * Permite al usuario copiar la letra de Google y pegarla directamente.
     */
    private void pegarLetraDesdePortapapeles() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            String texto = (String) clipboard.getData(DataFlavor.stringFlavor);
            if (texto != null && !texto.trim().isEmpty()) {
                // Activar modo edicion si no esta activo
                if (!modoEdicion) {
                    modoEdicion = true;
                    txtLetra.setEditable(true);
                    btnGuardar.setEnabled(true);
                    btnEditar.setText("Cancelar");
                    txtLetra.setOpaque(true);
                    txtLetra.setBackground(new Color(0x1A2040));
                    mostrarPanelLetra();
                }
                txtLetra.setText(texto);
                txtLetra.setCaretPosition(0);
            } else {
                JOptionPane.showMessageDialog(this,
                    "El portapapeles esta vacio. Copia la letra primero.",
                    "Pegar Letra", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "No se pudo acceder al portapapeles.",
                "Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    /** Panel central con area de letra centrada sobre fondo degradado. */
    private JPanel crearBodyArea() {
        lyricsBodyPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Degradado de arriba (navy oscuro) a abajo (navy con toque morado)
                GradientPaint gp = new GradientPaint(0, 0, BG_MAIN, 0, getHeight(), new Color(0x10102A));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        lyricsBodyPanel.setOpaque(false);

        // Area de texto: SIEMPRE editable para facilitar pegar letras manualmente
        txtLetra = new JTextArea();
        txtLetra.setEditable(true); // siempre editable para copiar/pegar
        txtLetra.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        txtLetra.setBackground(new Color(0, 0, 0, 0));
        txtLetra.setOpaque(false);
        txtLetra.setForeground(TEXT_MAIN);
        txtLetra.setCaretColor(ACCENT);
        txtLetra.setLineWrap(true);
        txtLetra.setWrapStyleWord(true);
        txtLetra.setAlignmentX(JTextArea.CENTER_ALIGNMENT);
        txtLetra.setMargin(new Insets(40, 80, 40, 80));
        txtLetra.setText("Selecciona una cancion para ver la letra.");
        txtLetra.setSelectionColor(new Color(255, 105, 180, 80));

        JScrollPane scrollPane = new JScrollPane(txtLetra) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 0, 0, 0));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getViewport().setBackground(new Color(0, 0, 0, 0));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        CustomScrollBarUI.aplicarA(scrollPane);

        // Panel de icono microfono (visible cuando no hay letra)
        JPanel noLyricsPanel = new JPanel(new GridBagLayout());
        noLyricsPanel.setOpaque(false);
        noLyricsPanel.setVisible(false);

        JLabel lblMicIcono = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();
                int cx = w / 2;

                // Circulo de fondo degradado rosa/morado
                GradientPaint gp = new GradientPaint(cx - 40, 0, new Color(0xFF69B4, true).darker(),
                                                     cx + 40, h, new Color(0x2A2A4A));
                g2.setPaint(gp);
                g2.fillOval(cx - 44, h / 2 - 44, 88, 88);

                // Microfono simplificado con Graphics2D
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // Cuerpo del microfono (rectangulo redondeado)
                g2.fillRoundRect(cx - 12, h / 2 - 30, 24, 34, 12, 12);
                // Base
                g2.setColor(new Color(0xFF85C8));
                g2.drawArc(cx - 20, h / 2 - 4, 40, 30, 0, -180);
                g2.drawLine(cx, h / 2 + 26, cx, h / 2 + 38);
                g2.drawLine(cx - 12, h / 2 + 38, cx + 12, h / 2 + 38);

                g2.dispose();
            }
        };
        lblMicIcono.setPreferredSize(new Dimension(100, 120));

        JPanel micWrapper = new JPanel();
        micWrapper.setLayout(new BoxLayout(micWrapper, BoxLayout.Y_AXIS));
        micWrapper.setOpaque(false);

        JLabel lblInv = new JLabel("No hay letra disponible");
        lblInv.setForeground(TEXT_SEC);
        lblInv.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblInv.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblInv2 = new JLabel("Usa 'Buscar en Google' para encontrarla o 'Pegar Letra'");
        lblInv2.setForeground(new Color(0x4A4A6A));
        lblInv2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblInv2.setAlignmentX(Component.CENTER_ALIGNMENT);

        micWrapper.add(lblMicIcono);
        micWrapper.add(Box.createVerticalStrut(16));
        micWrapper.add(lblInv);
        micWrapper.add(Box.createVerticalStrut(6));
        micWrapper.add(lblInv2);

        noLyricsPanel.add(micWrapper);

        this.lblMic = lblMicIcono;
        this.lblNoLyrics = lblInv;

        // Usar CardLayout para alternar entre letra y "sin letra"
        JPanel cardPanel = new JPanel(new CardLayout());
        cardPanel.setOpaque(false);
        cardPanel.add(scrollPane,    "letra");
        cardPanel.add(noLyricsPanel, "noLetra");
        cardPanel.setOpaque(false);

        lyricsBodyPanel.add(cardPanel, BorderLayout.CENTER);
        lyricsBodyPanel.putClientProperty("cardPanel", cardPanel);

        return lyricsBodyPanel;
    }

    /**
     * Actualiza el panel con la letra de la cancion actual.
     */
    public void actualizarLetra() {
        Song current = playerCtrl.getCurrentSong();
        if (current == null) {
            lblSongTitle.setText("Sin cancion en reproduccion");
            lblArtist.setText("");
            txtLetra.setText("Selecciona una cancion para ver la letra.");
            txtLetra.setOpaque(false);
            txtLetra.setEditable(true);
            btnGuardar.setEnabled(false);
            if (modoEdicion) {
                modoEdicion = false;
                btnEditar.setText("Editar Letra");
            }
            mostrarPanelLetra();
            return;
        }

        lblSongTitle.setText(truncar(current.getTitle(), 50));
        lblArtist.setText(current.getArtist() + " | " + current.getAlbum());

        // Iniciar animacion de carga
        txtLetra.setOpaque(false);
        txtLetra.setEditable(false); // deshabilitar durante carga para evitar edicion accidental
        txtLetra.setText("Buscando letra.");
        dotCount = 0;
        loadingTimer.restart();
        mostrarPanelLetra();

        // Guardar referencia de la cancion para el worker
        final Song songParaBuscar = current;

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return lyricsCtrl.obtenerLetra(songParaBuscar);
            }

            @Override
            protected void done() {
                loadingTimer.stop();
                try {
                    String letra = get();
                    boolean noEncontrada = (letra == null || letra.trim().isEmpty()
                            || letra.equals("Letra no disponible"));

                    if (noEncontrada) {
                        // Mostrar area editable con mensaje informativo
                        String tituloLimpio = lyricsCtrl.limpiarTitulo(songParaBuscar.getTitle());
                        txtLetra.setEditable(true);
                        txtLetra.setOpaque(true);
                        txtLetra.setBackground(new Color(0x1A2040));
                        txtLetra.setText(
                            "No se encontro la letra de \"" + tituloLimpio + "\" automaticamente.\n\n" +
                            "Puedes:\n" +
                            "  1. Copiar la letra de otro sitio y hacer clic en 'Pegar Letra'\n" +
                            "  2. Escribir la letra manualmente aqui y guardarla\n\n" +
                            "----------------------------------------\n\n"
                        );
                        txtLetra.setCaretPosition(txtLetra.getText().length());
                        btnGuardar.setEnabled(true);
                        if (!modoEdicion) {
                            modoEdicion = true;
                            btnEditar.setText("Cancelar");
                        }
                        mostrarPanelLetra();
                    } else {
                        // Letra encontrada: mostrar en modo lectura
                        if (modoEdicion) {
                            modoEdicion = false;
                            btnEditar.setText("Editar Letra");
                        }
                        txtLetra.setEditable(true); // siempre editable para copiar/pegar
                        txtLetra.setOpaque(false);
                        btnGuardar.setEnabled(false);
                        txtLetra.setText(letra);
                        txtLetra.setCaretPosition(0);
                        mostrarPanelLetra();
                    }
                } catch (Exception ex) {
                    loadingTimer.stop();
                    txtLetra.setText("Error al cargar la letra. Intentalo de nuevo.");
                    txtLetra.setEditable(true);
                    mostrarPanelLetra();
                }
            }
        };
        worker.execute();
    }

    private void mostrarPanelLetra() {
        JPanel cardPanel = (JPanel) lyricsBodyPanel.getClientProperty("cardPanel");
        if (cardPanel != null) {
            CardLayout cl = (CardLayout) cardPanel.getLayout();
            cl.show(cardPanel, "letra");
        }
    }

    private void mostrarPanelNoLetra() {
        JPanel cardPanel = (JPanel) lyricsBodyPanel.getClientProperty("cardPanel");
        if (cardPanel != null) {
            CardLayout cl = (CardLayout) cardPanel.getLayout();
            cl.show(cardPanel, "noLetra");
        }
    }

    private void buscarLetraOnline() {
        Song current = playerCtrl.getCurrentSong();
        if (current == null) {
            JOptionPane.showMessageDialog(this, "No hay cancion en reproduccion.");
            return;
        }

        lblSongTitle.setText(truncar(current.getTitle(), 50));
        lblArtist.setText(current.getArtist());

        dotCount = 0;
        txtLetra.setEditable(false);
        txtLetra.setOpaque(false);
        txtLetra.setText("Buscando letra.");
        loadingTimer.restart();
        mostrarPanelLetra();

        final Song songParaBuscar = current;

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return lyricsCtrl.buscarLetraEnAPI(songParaBuscar.getArtist(), songParaBuscar.getTitle());
            }

            @Override
            protected void done() {
                loadingTimer.stop();
                try {
                    String letra = get();
                    if (letra != null && !letra.trim().isEmpty()) {
                        // Letra encontrada: guardar y mostrar
                        lyricsCtrl.guardarLetraLocal(songParaBuscar, letra);
                        if (modoEdicion) {
                            modoEdicion = false;
                            btnEditar.setText("Editar Letra");
                        }
                        txtLetra.setEditable(true); // siempre editable
                        txtLetra.setOpaque(false);
                        btnGuardar.setEnabled(false);
                        txtLetra.setText(letra);
                        txtLetra.setCaretPosition(0);
                        lblSongTitle.setText(truncar(songParaBuscar.getTitle(), 50) + "  (en linea)");
                        mostrarPanelLetra();
                    } else {
                        // No se encontro la letra: mostrar area editable con instrucciones
                        String tituloLimpio = lyricsCtrl.limpiarTitulo(songParaBuscar.getTitle());
                        txtLetra.setEditable(true);
                        txtLetra.setOpaque(true);
                        txtLetra.setBackground(new Color(0x1A2040));
                        txtLetra.setText(
                            "No se encontro la letra de \"" + tituloLimpio + "\" en linea.\n\n" +
                            "Puedes:\n" +
                            "  1. Copiar la letra de otro sitio y hacer clic en 'Pegar Letra'\n" +
                            "  2. Escribir la letra manualmente aqui y guardarla\n\n" +
                            "----------------------------------------\n\n"
                        );
                        txtLetra.setCaretPosition(txtLetra.getText().length());
                        btnGuardar.setEnabled(true);
                        if (!modoEdicion) {
                            modoEdicion = true;
                            btnEditar.setText("Cancelar");
                        }
                        mostrarPanelLetra();
                    }
                } catch (Exception ex) {
                    loadingTimer.stop();
                    txtLetra.setText("Error de conexion. Prueba 'Buscar en Google' o agrega la letra manualmente.");
                    txtLetra.setEditable(true);
                    mostrarPanelLetra();
                }
            }
        };
        worker.execute();
    }

    private void toggleEdicion() {
        modoEdicion = !modoEdicion;
        btnGuardar.setEnabled(modoEdicion);
        mostrarPanelLetra();

        if (modoEdicion) {
            btnEditar.setText("Cancelar");
            txtLetra.setEditable(true);
            txtLetra.setOpaque(true);
            txtLetra.setBackground(new Color(0x1A2040));
        } else {
            btnEditar.setText("Editar Letra");
            txtLetra.setOpaque(false);
            txtLetra.setEditable(true); // mantener siempre editable
            actualizarLetra();
        }
    }

    private String truncar(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 2) + ".." : s;
    }
}
