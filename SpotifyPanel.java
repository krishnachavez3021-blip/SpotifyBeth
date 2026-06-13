package smartplayer.views;

import javax.swing.*;
import java.awt.*;
import smartplayer.controllers.SpotifyController;
import smartplayer.controllers.LibraryController;
import smartplayer.controllers.PlaylistController;
import smartplayer.models.Playlist;
import smartplayer.structures.ListaSimple;
import smartplayer.structures.Nodo;

/**
 * Panel de integración con Spotify rediseñado estilo Spotify.
 * RoundedTextField, RoundedButton, CustomScrollBar, tema oscuro premium.
 */
public class SpotifyPanel extends JPanel {

    // Colores Spotify
    private static final Color BG_MAIN  = new Color(0x121212);
    private static final Color BG_PANEL = new Color(0x181818);
    private static final Color BG_CARD  = new Color(0x1A1A1A);
    private static final Color ACCENT   = new Color(0x1DB954);
    private static final Color TEXT_MAIN= Color.WHITE;
    private static final Color TEXT_SEC = new Color(0xB3B3B3);
    private static final Color BORDER   = new Color(0x282828);

    private SpotifyController  spotifyCtrl;
    private LibraryController  libraryCtrl;
    private PlaylistController playlistCtrl;

    private RoundedTextField         txtClientId;
    private JPasswordField           txtClientSecret;
    private RoundedButton            btnAutenticar;
    private JLabel                   lblEstado;

    private DefaultListModel<String> modelPlaylists;
    private JList<String>            listPlaylists;
    private String[][]               playlistsData;

    private JTextArea                txtResultado;

    public SpotifyPanel(LibraryController libraryCtrl, PlaylistController playlistCtrl) {
        this.libraryCtrl  = libraryCtrl;
        this.playlistCtrl = playlistCtrl;
        this.spotifyCtrl  = null;

        setLayout(new BorderLayout(0, 0));
        setBackground(BG_MAIN);

        // ---- Contenedor con padding ----
        JPanel contenido = new JPanel(new BorderLayout(0, 14));
        contenido.setBackground(BG_MAIN);
        contenido.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        contenido.add(crearPanelAuth(),       BorderLayout.NORTH);
        contenido.add(crearPanelPlaylists(),  BorderLayout.CENTER);
        contenido.add(crearPanelResultado(),  BorderLayout.SOUTH);

        add(contenido, BorderLayout.CENTER);
    }

    // ---- Panel de autenticación ----
    private JPanel crearPanelAuth() {
        RoundedPanel panel = new RoundedPanel(12, true);
        panel.setLayout(new BorderLayout(0, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel lblTitulo = new JLabel("Conexión con Spotify");
        lblTitulo.setForeground(TEXT_MAIN);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Campos de credenciales
        JPanel camposPanel = new JPanel(new GridLayout(2, 2, 12, 8));
        camposPanel.setOpaque(false);

        JLabel lblCID = new JLabel("Client ID:");
        lblCID.setForeground(TEXT_SEC);
        lblCID.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        txtClientId = new RoundedTextField("Ingresa tu Spotify Client ID");

        JLabel lblCS = new JLabel("Client Secret:");
        lblCS.setForeground(TEXT_SEC);
        lblCS.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // JPasswordField estilizado similar a RoundedTextField
        txtClientSecret = new JPasswordField(30);
        txtClientSecret.setBackground(new Color(0x282828));
        txtClientSecret.setForeground(TEXT_MAIN);
        txtClientSecret.setCaretColor(TEXT_MAIN);
        txtClientSecret.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtClientSecret.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x535353), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        txtClientSecret.setEchoChar('●');

        camposPanel.add(lblCID);
        camposPanel.add(txtClientId);
        camposPanel.add(lblCS);
        camposPanel.add(txtClientSecret);

        // Botón + estado
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        btnRow.setOpaque(false);

        btnAutenticar = new RoundedButton("Autenticar con Spotify");
        btnAutenticar.addActionListener(e -> autenticar());

        lblEstado = new JLabel("No conectado");
        lblEstado.setForeground(new Color(0xE22134));
        lblEstado.setFont(new Font("Segoe UI", Font.BOLD, 12));

        btnRow.add(btnAutenticar);
        btnRow.add(lblEstado);

        panel.add(lblTitulo,   BorderLayout.NORTH);
        panel.add(camposPanel, BorderLayout.CENTER);
        panel.add(btnRow,      BorderLayout.SOUTH);
        return panel;
    }

    // ---- Panel de playlists ----
    private JPanel crearPanelPlaylists() {
        RoundedPanel panel = new RoundedPanel(12, true);
        panel.setLayout(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        // Encabezado con título y botones
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitulo = new JLabel("Playlists de Spotify");
        lblTitulo.setForeground(TEXT_MAIN);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        btnRow.setOpaque(false);

        RoundedButton btnCargar    = new RoundedButton("Cargar Mis Playlists", RoundedButton.Variante.SECONDARY);
        RoundedButton btnImportar  = new RoundedButton("Importar Seleccionada");

        btnRow.add(btnCargar);
        btnRow.add(btnImportar);

        headerPanel.add(lblTitulo, BorderLayout.WEST);
        headerPanel.add(btnRow,    BorderLayout.EAST);

        // Lista de playlists
        modelPlaylists = new DefaultListModel<>();
        listPlaylists  = new JList<>(modelPlaylists);
        listPlaylists.setBackground(BG_CARD);
        listPlaylists.setForeground(TEXT_MAIN);
        listPlaylists.setSelectionBackground(new Color(0x282828));
        listPlaylists.setSelectionForeground(Color.WHITE);
        listPlaylists.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        listPlaylists.setFixedCellHeight(40);
        listPlaylists.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listPlaylists.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        listPlaylists.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                lbl.setBackground(isSelected ? new Color(0x282828)
                        : (index % 2 == 0 ? BG_CARD : BG_PANEL));
                lbl.setForeground(isSelected ? Color.WHITE : TEXT_MAIN);
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                return lbl;
            }
        });

        JScrollPane scrollPl = new JScrollPane(listPlaylists);
        scrollPl.getViewport().setBackground(BG_CARD);
        scrollPl.setBorder(BorderFactory.createEmptyBorder());
        CustomScrollBarUI.aplicarA(scrollPl);
        scrollPl.setPreferredSize(new Dimension(0, 160));

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPl,    BorderLayout.CENTER);

        btnCargar.addActionListener(e   -> cargarPlaylists());
        btnImportar.addActionListener(e -> importarPlaylist());

        return panel;
    }

    // ---- Panel de resultado ----
    private JPanel crearPanelResultado() {
        RoundedPanel panel = new RoundedPanel(12, true);
        panel.setLayout(new BorderLayout(0, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel lblTitulo = new JLabel("Resultado");
        lblTitulo.setForeground(TEXT_SEC);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 11));

        txtResultado = new JTextArea(5, 40);
        txtResultado.setEditable(false);
        txtResultado.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtResultado.setBackground(BG_CARD);
        txtResultado.setForeground(TEXT_MAIN);
        txtResultado.setMargin(new Insets(8, 12, 8, 12));
        txtResultado.setLineWrap(true);
        txtResultado.setWrapStyleWord(true);

        JScrollPane scrollRes = new JScrollPane(txtResultado);
        scrollRes.getViewport().setBackground(BG_CARD);
        scrollRes.setBorder(BorderFactory.createEmptyBorder());
        CustomScrollBarUI.aplicarA(scrollRes);

        panel.add(lblTitulo, BorderLayout.NORTH);
        panel.add(scrollRes, BorderLayout.CENTER);
        return panel;
    }

    // ---- Lógica de negocio ----
    private void autenticar() {
        String clientId     = txtClientId.getText().trim();
        String clientSecret = new String(txtClientSecret.getPassword()).trim();

        if (clientId.isEmpty() || clientSecret.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Ingresa tu Client ID y Client Secret de Spotify.\n"
                    + "Obtenerlos en: https://developer.spotify.com/dashboard",
                    "Credenciales requeridas", JOptionPane.WARNING_MESSAGE);
            return;
        }

        spotifyCtrl = new SpotifyController(clientId, clientSecret);
        btnAutenticar.setEnabled(false);
        lblEstado.setText("Esperando autorización...");
        lblEstado.setForeground(Color.ORANGE);

        new Thread(() -> {
            boolean exito = spotifyCtrl.autenticar();
            SwingUtilities.invokeLater(() -> {
                if (exito) {
                    lblEstado.setText("Conectado");
                    lblEstado.setForeground(ACCENT);
                    txtResultado.setText("Autenticación con Spotify exitosa.\nPuedes cargar tus playlists.");
                } else {
                    lblEstado.setText("Error de autenticación");
                    lblEstado.setForeground(new Color(0xE22134));
                    txtResultado.setText("No se pudo autenticar. Verifica tus credenciales e intenta de nuevo.");
                }
                btnAutenticar.setEnabled(true);
            });
        }).start();
    }

    private void cargarPlaylists() {
        if (spotifyCtrl == null || !spotifyCtrl.isAutenticado()) {
            JOptionPane.showMessageDialog(this, "Primero debes autenticarte con Spotify.");
            return;
        }

        new Thread(() -> {
            playlistsData = spotifyCtrl.obtenerPlaylists();
            SwingUtilities.invokeLater(() -> {
                modelPlaylists.clear();
                if (playlistsData.length == 0) {
                    txtResultado.setText("No se encontraron playlists.");
                } else {
                    for (String[] pl : playlistsData) {
                        if (pl[1] != null) modelPlaylists.addElement(pl[1]);
                    }
                    txtResultado.setText("Se encontraron " + playlistsData.length + " playlists.");
                }
            });
        }).start();
    }

    private void importarPlaylist() {
        if (spotifyCtrl == null || !spotifyCtrl.isAutenticado()) {
            JOptionPane.showMessageDialog(this, "Primero debes autenticarte con Spotify.");
            return;
        }

        int selectedIdx = listPlaylists.getSelectedIndex();
        if (selectedIdx < 0 || playlistsData == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una playlist de la lista.");
            return;
        }

        String playlistId   = playlistsData[selectedIdx][0];
        String playlistName = playlistsData[selectedIdx][1];

        new Thread(() -> {
            ListaSimple cancionesSpotify = spotifyCtrl.obtenerCancionesDePlaylist(playlistId);
            ListaSimple emparejadas      = spotifyCtrl.emparejarConBiblioteca(cancionesSpotify,
                                              libraryCtrl.getBiblioteca());

            SwingUtilities.invokeLater(() -> {
                playlistCtrl.crearPlaylist("Spotify - " + playlistName);
                Playlist nuevaPlaylist = playlistCtrl.getPlaylist("Spotify - " + playlistName);

                if (nuevaPlaylist != null) {
                    Nodo actual = emparejadas.getCabeza();
                    while (actual != null) {
                        nuevaPlaylist.getCanciones().insertar(actual.song);
                        actual = actual.siguiente;
                    }
                }

                StringBuilder sb = new StringBuilder();
                sb.append("Playlist importada: ").append(playlistName).append("\n");
                sb.append("Canciones en Spotify: ").append(cancionesSpotify.tamano).append("\n");
                sb.append("Emparejadas con biblioteca: ").append(emparejadas.tamano).append("\n\n");

                if (emparejadas.tamano < cancionesSpotify.tamano) {
                    sb.append("No encontradas localmente:\n");
                    Nodo spotNode = cancionesSpotify.getCabeza();
                    while (spotNode != null) {
                        boolean found = false;
                        Nodo empNode = emparejadas.getCabeza();
                        while (empNode != null) {
                            if (empNode.song.getTitle().equalsIgnoreCase(spotNode.song.getTitle())) {
                                found = true;
                                break;
                            }
                            empNode = empNode.siguiente;
                        }
                        if (!found) {
                            sb.append("  - ").append(spotNode.song.getTitle())
                              .append(" (").append(spotNode.song.getArtist()).append(")\n");
                        }
                        spotNode = spotNode.siguiente;
                    }
                }
                txtResultado.setText(sb.toString());
            });
        }).start();
    }
}
