package smartplayer.views;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.concurrent.ExecutionException;
import smartplayer.controllers.PlaylistController;
import smartplayer.controllers.LibraryController;
import smartplayer.controllers.PlayerController;
import smartplayer.models.Playlist;
import smartplayer.models.Song;
import smartplayer.structures.ListaPlaylists;
import smartplayer.structures.Nodo;
import smartplayer.structures.ListaDoble;
import smartplayer.structures.ListaCircular;
import smartplayer.utils.FileChooserUtil;

/**
 * Panel de playlists con tema rosado premium.
 * Lista izquierda navy oscuro, canciones derecha, selecciÃÂÃÂ³n rosa.
 * Exportar como TXT legible + Encriptada + Importar.
 */
public class PlaylistPanel extends JPanel {

    private static final Color BG_MAIN  = new Color(0x1A1A2E);
    private static final Color BG_PANEL = new Color(0x16213E);
    private static final Color ACCENT   = new Color(0xFF69B4);
    private static final Color TEXT_MAIN= Color.WHITE;
    private static final Color TEXT_SEC = new Color(0xC0C0C0);
    private static final Color BORDER   = new Color(0x4A4A6A);

    private final PlaylistController playlistCtrl;
    private final LibraryController  libraryCtrl;
    private final ReproductorPanel   reproductorPanel;

    private final DefaultListModel<String> modelPlaylists;
    private final JList<String>            listPlaylists;
    private final DefaultListModel<String> modelCanciones;
    private final JList<String>            listCanciones;
    private JComboBox<String>        comboModos;

    public PlaylistPanel(PlaylistController playlistCtrl, LibraryController libraryCtrl,
                         ReproductorPanel reproductorPanel) {
        this.playlistCtrl     = playlistCtrl;
        this.libraryCtrl      = libraryCtrl;
        this.reproductorPanel = reproductorPanel;

        setLayout(new BorderLayout(0, 0));
        setBackground(BG_MAIN);

        add(crearPanelSuperior(), BorderLayout.NORTH);

        modelPlaylists = new DefaultListModel<>();
        listPlaylists  = crearListaEstilizada(modelPlaylists);
        modelCanciones = new DefaultListModel<>();
        listCanciones  = crearListaEstilizada(modelCanciones);

        listPlaylists.addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) actualizarCanciones(); });

        JScrollPane scrollPl = new JScrollPane(listPlaylists);
        CustomScrollBarUI.aplicarA(scrollPl);
        scrollPl.getViewport().setBackground(BG_PANEL);
        JScrollPane scrollCan = new JScrollPane(listCanciones);
        CustomScrollBarUI.aplicarA(scrollCan);
        scrollCan.getViewport().setBackground(BG_PANEL);

        // Columna izquierda: playlists
        JPanel leftCol = new JPanel(new BorderLayout());
        leftCol.setBackground(BG_PANEL);
        leftCol.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        JLabel lblPlTitle = new JLabel("Playlists");
        lblPlTitle.setForeground(ACCENT);
        lblPlTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPlTitle.setBorder(BorderFactory.createEmptyBorder(12, 8, 8, 8));
        leftCol.add(lblPlTitle, BorderLayout.NORTH);
        leftCol.add(scrollPl,  BorderLayout.CENTER);

        // Columna derecha: canciones
        JPanel rightCol = new JPanel(new BorderLayout());
        rightCol.setBackground(BG_MAIN);
        JLabel lblCanTitle = new JLabel("Canciones en la playlist");
        lblCanTitle.setForeground(ACCENT);
        lblCanTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCanTitle.setBorder(BorderFactory.createEmptyBorder(12, 16, 8, 8));
        rightCol.add(lblCanTitle, BorderLayout.NORTH);
        rightCol.add(scrollCan,   BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftCol, rightCol);
        splitPane.setDividerLocation(280);
        splitPane.setBackground(BG_MAIN);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setDividerSize(4);

        add(splitPane, BorderLayout.CENTER);
        add(crearPanelInferior(), BorderLayout.SOUTH);
    }

    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        panel.setBackground(BG_PANEL);
        panel.setBorder(BorderFactory.createEmptyBorder(2, 16, 2, 16));

        RoundedButton btnCrear    = new RoundedButton("+ Nueva Playlist");
        RoundedButton btnAgregar  = new RoundedButton("Anadir Cancion",    RoundedButton.Variante.SECONDARY);
        RoundedButton btnQuitarCan = new RoundedButton("Quitar Cancion",   RoundedButton.Variante.DANGER);
        RoundedButton btnPlay     = new RoundedButton("Reproducir");

        JLabel lblModo = new JLabel("Modo:");
        lblModo.setForeground(TEXT_SEC);
        lblModo.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        comboModos = new JComboBox<>(new String[]{"Normal", "Circular", "Aleatorio"});
        comboModos.setBackground(new Color(0x2A2A4A));
        comboModos.setForeground(Color.WHITE);
        comboModos.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        comboModos.setPreferredSize(new Dimension(110, 34));
        comboModos.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setBackground(isSelected ? ACCENT : new Color(0x2A2A4A));
                lbl.setForeground(Color.WHITE);
                lbl.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
                return lbl;
            }
        });

        panel.add(btnCrear);
        panel.add(btnAgregar);
        panel.add(btnQuitarCan);
        panel.add(lblModo);
        panel.add(comboModos);
        panel.add(btnPlay);

        btnCrear.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Nombre de la nueva playlist:");
            if (name != null && !name.trim().isEmpty()) {
                playlistCtrl.crearPlaylist(name.trim());
                actualizarPlaylists();
            }
        });

        btnAgregar.addActionListener((var e) -> {
            String sel = listPlaylists.getSelectedValue();
            if (sel == null) { mostrarError("Selecciona una playlist primero."); return; }
            Playlist p = playlistCtrl.getPlaylist(sel);
            if (p == null) return;

            // Mostrar diÃÂÃÂ¡logo con lista de canciones de la biblioteca para seleccionar
            java.util.List<Song> todasLasCanciones = new java.util.ArrayList<>();
            smartplayer.structures.Nodo nodo = libraryCtrl.getBiblioteca().getCabeza();
            while (nodo != null) { todasLasCanciones.add(nodo.song); nodo = nodo.siguiente; }

            if (todasLasCanciones.isEmpty()) {
                mostrarError("La biblioteca estÃÂÃÂ¡ vacÃÂÃÂ­a. Importa una carpeta de mÃÂÃÂºsica primero.");
                return;
            }

            // Crear modelo con "TÃÂÃÂ­tulo ÃÂ¢Ã¢ÂÂ¬Ã¢ÂÂ Artista"
            DefaultListModel<String> modelo = new DefaultListModel<>();
            for (Song s : todasLasCanciones)
                modelo.addElement(s.getTitle() + "  ÃÂ¢Ã¢ÂÂ¬Ã¢ÂÂ  " + s.getArtist());

            JList<String> lista = new JList<>(modelo);
            lista.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            lista.setBackground(new Color(0x1A1A2E));
            lista.setForeground(Color.WHITE);
            lista.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lista.setFixedCellHeight(36);
            lista.setSelectionBackground(ACCENT);
            lista.setSelectionForeground(Color.WHITE);

            // Campo de filtro rÃÂÃÂ¡pido
            JTextField txtFiltro = new JTextField();
            txtFiltro.setBackground(new Color(0x2A2A4A));
            txtFiltro.setForeground(Color.WHITE);
            txtFiltro.setCaretColor(Color.WHITE);
            txtFiltro.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            txtFiltro.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x4A4A6A)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
            txtFiltro.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                void filtrar() {
                    String q = txtFiltro.getText().trim().toLowerCase();
                    modelo.clear();
                    for (Song s : todasLasCanciones) {
                        String entrada = s.getTitle() + "  ÃÂ¢Ã¢ÂÂ¬Ã¢ÂÂ  " + s.getArtist();
                        if (q.isEmpty() || s.getTitle().toLowerCase().contains(q)
                                || s.getArtist().toLowerCase().contains(q))
                            modelo.addElement(entrada);
                    }
                }
                @Override
                public void insertUpdate(javax.swing.event.DocumentEvent e)  { filtrar(); }
                @Override
                public void removeUpdate(javax.swing.event.DocumentEvent e)  { filtrar(); }
                @Override
                public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            });

            JScrollPane scroll = new JScrollPane(lista);
            CustomScrollBarUI.aplicarA(scroll);
            scroll.setPreferredSize(new java.awt.Dimension(420, 280));

            JPanel dlgPanel = new JPanel(new BorderLayout(0, 6));
            dlgPanel.setBackground(new Color(0x16213E));
            JLabel lblFiltro = new JLabel("Filtrar:");
            lblFiltro.setForeground(new Color(0xC0C0C0));
            lblFiltro.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            dlgPanel.add(lblFiltro,  BorderLayout.NORTH);
            dlgPanel.add(txtFiltro,  BorderLayout.CENTER); // se agrega debajo del label
            JPanel topPanel = new JPanel(new BorderLayout(0, 4));
            topPanel.setBackground(new Color(0x16213E));
            topPanel.add(lblFiltro, BorderLayout.NORTH);
            topPanel.add(txtFiltro, BorderLayout.CENTER);
            dlgPanel.add(topPanel, BorderLayout.NORTH);
            dlgPanel.add(scroll,   BorderLayout.CENTER);
            dlgPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

            int res = JOptionPane.showConfirmDialog(this, dlgPanel,
                "AÃÂÃÂ±adir canciones a \"" + sel + "\"",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (res == JOptionPane.OK_OPTION && !lista.isSelectionEmpty()) {
                int[] indices = lista.getSelectedIndices();
                // Buscar las canciones originales por ÃÂÃÂ­ndice en modelo visible
                int agregadas = 0;
                for (int idx : indices) {
                    String item = modelo.getElementAt(idx);
                    String titulo = item.contains("  ÃÂ¢Ã¢ÂÂ¬Ã¢ÂÂ  ") ? item.substring(0, item.indexOf("  ÃÂ¢Ã¢ÂÂ¬Ã¢ÂÂ  ")).trim() : item;
                    Song s = libraryCtrl.buscarEnABB(titulo);
                    if (s == null) { // fallback bÃÂÃÂºsqueda parcial
                        smartplayer.structures.Nodo n2 = libraryCtrl.getBiblioteca().getCabeza();
                        while (n2 != null) {
                            if (n2.song.getTitle().equalsIgnoreCase(titulo)) { s = n2.song; break; }
                            n2 = n2.siguiente;
                        }
                    }
                    if (s != null) { p.getCanciones().insertar(s); agregadas++; }
                }
                actualizarCanciones();
                if (agregadas > 0)
                    JOptionPane.showMessageDialog(this, agregadas + " cancion(es) anadida(s) a \"" + sel + "\".",
                        "Exito", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnPlay.addActionListener(e -> reproducirPlaylist());

        // Quitar canciÃÂÃÂ³n seleccionada de la lista de canciones
        btnQuitarCan.addActionListener(e -> {
            String selPlaylist = listPlaylists.getSelectedValue();
            if (selPlaylist == null) { mostrarError("Selecciona una playlist primero."); return; }
            int selIdx = listCanciones.getSelectedIndex();
            if (selIdx < 0) { mostrarError("Selecciona una canciÃÂÃÂ³n de la lista para quitarla."); return; }

            Playlist p = playlistCtrl.getPlaylist(selPlaylist);
            if (p == null) return;

            // Eliminar el nodo en posiciÃÂÃÂ³n selIdx de la ListaSimple
            smartplayer.structures.Nodo actual = p.getCanciones().getCabeza();
            smartplayer.structures.Nodo anterior = null;
            int i = 0;
            while (actual != null) {
                if (i == selIdx) {
                    if (anterior == null) {
                        p.getCanciones().cabeza = actual.siguiente;
                    } else {
                        anterior.siguiente = actual.siguiente;
                    }
                    p.getCanciones().tamano--;
                    break;
                }
                anterior = actual;
                actual = actual.siguiente;
                i++;
            }
            actualizarCanciones();
        });

        return panel;
    }

    private JPanel crearPanelInferior() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        panel.setBackground(BG_PANEL);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));

        RoundedButton btnGestionar = new RoundedButton("Gestionar", RoundedButton.Variante.SECONDARY);

        panel.add(btnGestionar);

        // MenÃÂÃÂº popup con todas las opciones de gestiÃÂÃÂ³n
        JPopupMenu menuGestionar = new JPopupMenu();
        menuGestionar.setBackground(new Color(0x2A2A4A));
        menuGestionar.setBorder(BorderFactory.createLineBorder(new Color(0x4A4A6A), 1));

        JMenuItem itemEliminarPl = crearMenuItemOscuro("Eliminar Playlist");
        itemEliminarPl.addActionListener(e -> {
            String sel = listPlaylists.getSelectedValue();
            if (sel == null) { mostrarError("Selecciona una playlist primero."); return; }
            int confirm = JOptionPane.showConfirmDialog(this,
                "ÃÂ¿EstÃÂ¡s seguro de que deseas eliminar la playlist \"" + sel + "\"?\nEsta acciÃÂ³n no se puede deshacer.",
                "Eliminar Playlist", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                playlistCtrl.eliminarPlaylist(sel);
                actualizarPlaylists();
                modelCanciones.clear();
                JOptionPane.showMessageDialog(this,
                    "Playlist \"" + sel + "\" eliminada.", "Lumina", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JMenuItem itemExportEnc = crearMenuItemOscuro("Exportar Encriptada");
        itemExportEnc.addActionListener(e -> {
            String sel = listPlaylists.getSelectedValue();
            if (sel == null) { mostrarError("Selecciona una playlist."); return; }
            File archivo = FileChooserUtil.guardarArchivo(this, "Exportar playlist encriptada");
            if (archivo != null) {
                String rec = "InOrden";
                playlistCtrl.encriptarYGuardar(sel, archivo.getAbsolutePath(), rec);
                JOptionPane.showMessageDialog(this, "Playlist exportada y encriptada.", "Lumina", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JMenuItem itemExportMP3 = crearMenuItemOscuro("Exportar Canciones (MP3)");
        itemExportMP3.addActionListener(e -> exportarCancionesAMp3());

        JMenuItem itemExportZIP = crearMenuItemOscuro("Exportar Playlist (ZIP)");
        itemExportZIP.addActionListener(e -> exportarPlaylistZip());

        JMenuItem itemImportTxt = crearMenuItemOscuro("Importar TXT / Encriptada");
        itemImportTxt.addActionListener(e -> {
            File archivo = FileChooserUtil.abrirArchivo(this, "Importar playlist (TXT o Encriptada)");
            if (archivo != null) {
                PlaylistController.ResultadoImportacion res =
                    playlistCtrl.recuperarYDesencriptar(archivo.getAbsolutePath(), libraryCtrl.getBiblioteca());
                actualizarPlaylists();
                JOptionPane.showMessageDialog(this, res.mensaje, "Lumina", JOptionPane.INFORMATION_MESSAGE);
            }
        });


        JMenuItem itemDesencriptar = crearMenuItemOscuro("Desencriptar Archivo");
        itemDesencriptar.addActionListener(e -> desencriptarArchivo());

        menuGestionar.add(itemEliminarPl);
        menuGestionar.addSeparator();
        menuGestionar.add(itemExportEnc);
        menuGestionar.add(itemExportMP3);
        menuGestionar.add(itemExportZIP);
        menuGestionar.addSeparator();
        menuGestionar.add(itemImportTxt);
        menuGestionar.addSeparator();
        menuGestionar.add(itemDesencriptar);

        btnGestionar.addActionListener(e -> {
            menuGestionar.show(btnGestionar, 0, btnGestionar.getHeight());
        });

        return panel;
    }

    /**
     * Exporta los archivos MP3 de la playlist seleccionada copiÃÂÃÂ¡ndolos a una carpeta.
     * Crea una subcarpeta con el nombre de la playlist y copia cada canciÃÂÃÂ³n ahÃÂÃÂ­.
     */
    private void exportarCancionesAMp3() {
        String sel = listPlaylists.getSelectedValue();
        if (sel == null) { mostrarError("Selecciona una playlist primero."); return; }
        Playlist p = playlistCtrl.getPlaylist(sel);
        if (p == null || p.getCanciones().tamano == 0) { mostrarError("La playlist estÃÂÃÂ¡ vacÃÂÃÂ­a."); return; }

        // Seleccionar carpeta destino
        File carpetaDestino = FileChooserUtil.seleccionarCarpeta(this);
        if (carpetaDestino == null) return;

        // Crear subcarpeta con nombre de la playlist
        String nombreCarpeta = sel.replaceAll("[^a-zA-Z0-9ÃÂÃÂ¡ÃÂÃÂ©ÃÂÃÂ­ÃÂÃÂ³ÃÂÃÂºÃÂÃÂÃÂÃ¢ÂÂ°ÃÂÃÂÃÂÃ¢ÂÂÃÂÃÂ¡ÃÂÃÂ±ÃÂÃ¢ÂÂ _-]", "_");
        File carpetaPlaylist = new File(carpetaDestino, nombreCarpeta);
        if (!carpetaPlaylist.exists()) carpetaPlaylist.mkdirs();

        // Copiar canciones en hilo secundario con barra de progreso
        int total = p.getCanciones().tamano;
        JProgressBar progressBar = new JProgressBar(0, total);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new java.awt.Dimension(400, 28));

        JDialog dialogo = new JDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this),
            "Exportando canciones...", false);
        dialogo.setLayout(new java.awt.BorderLayout(12, 12));
        dialogo.add(new JLabel("  Copiando archivos MP3 a:\n  " + carpetaPlaylist.getAbsolutePath()),
            java.awt.BorderLayout.NORTH);
        dialogo.add(progressBar, java.awt.BorderLayout.CENTER);
        dialogo.setSize(460, 110);
        dialogo.setLocationRelativeTo(this);
        dialogo.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        SwingWorker<int[], Void> worker = new SwingWorker<int[], Void>() {
            @Override
            protected int[] doInBackground() throws Exception {
                int copiadas = 0, errores = 0, idx = 0;
                Nodo actual = p.getCanciones().getCabeza();
                while (actual != null) {
                    Song s = actual.song;
                    try {
                        java.io.File origen = new java.io.File(s.getPath());
                        if (origen.exists()) {
                            // Nombre destino: "01 - Titulo - Artista.mp3"
                            String nombreBase = String.format("%02d - %s - %s",
                                ++idx,
                                sanitizar(s.getTitle()),
                                sanitizar(s.getArtist()));
                            java.io.File destino = new java.io.File(carpetaPlaylist, nombreBase + ".mp3");
                            // Evitar sobreescribir si ya existe
                            if (destino.exists()) {
                                destino = new java.io.File(carpetaPlaylist, nombreBase + "_" + idx + ".mp3");
                            }
                            copiarArchivo(origen, destino);
                            copiadas++;
                        } else {
                            errores++;
                        }
                    } catch (IOException ex) {
                        errores++;
                    }
                    final int progActual = copiadas + errores;
                    SwingUtilities.invokeLater(() -> progressBar.setValue(progActual));
                    actual = actual.siguiente;
                }
                return new int[]{copiadas, errores};
            }

            @Override
            protected void done() {
                dialogo.dispose();
                try {
                    int[] res = get();
                    String msg = String.format(
                        "ExportaciÃÂÃÂ³n completada.\n\n" +
                        "  Canciones copiadas: %d\n" +
                        "  Errores: %d\n\n" +
                        "Carpeta: %s",
                        res[0], res[1], carpetaPlaylist.getAbsolutePath());
                    JOptionPane.showMessageDialog(PlaylistPanel.this, msg, "ExportaciÃÂÃÂ³n MP3", JOptionPane.INFORMATION_MESSAGE);
                } catch (HeadlessException | InterruptedException | ExecutionException ex) {
                    JOptionPane.showMessageDialog(PlaylistPanel.this,
                        "Error durante la exportaciÃÂÃÂ³n: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        dialogo.setVisible(true);
        worker.execute();
    }

    /** Copia un archivo de origen a destino usando streams. */
    private void copiarArchivo(java.io.File origen, java.io.File destino) throws java.io.IOException {
        try (java.io.InputStream in  = new java.io.FileInputStream(origen);
             java.io.OutputStream out = new java.io.FileOutputStream(destino)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
        }
    }

    /** Elimina caracteres no vÃÂÃÂ¡lidos para nombres de archivo. */
    private String sanitizar(String s) {
        if (s == null) return "Desconocido";
        return s.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
    }


    private <E> JList<E> crearListaEstilizada(DefaultListModel<E> model) {
        JList<E> list = new JList<>(model);
        list.setBackground(BG_PANEL);
        list.setForeground(TEXT_MAIN);
        list.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        list.setFixedCellHeight(40);
        list.setSelectionBackground(new Color(255, 105, 180, 80));
        list.setSelectionForeground(Color.WHITE);
        list.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setBackground(isSelected ? new Color(255, 105, 180, 80) : (index % 2 == 0 ? BG_PANEL : new Color(0x1E1E3A)));
                lbl.setForeground(isSelected ? Color.WHITE : TEXT_MAIN);
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                return lbl;
            }
        });
        return list;
    }

    private void reproducirPlaylist() {
        String sel = listPlaylists.getSelectedValue();
        if (sel == null) { mostrarError("Selecciona una playlist."); return; }
        Playlist p = playlistCtrl.getPlaylist(sel);
        if (p == null || p.getCanciones().tamano == 0) { mostrarError("Playlist vacÃÂÃÂ­a."); return; }
        String modo = (String) comboModos.getSelectedItem();
        PlayerController pc = reproductorPanel.getPlayerCtrl();
        if (null == modo) {
            pc.setMode(PlayerController.PlayMode.RANDOM);
            Nodo act = p.getCanciones().getCabeza();
            while (act != null) { pc.getColaReproduccion().encolar(act.song); act = act.siguiente; }
            pc.next();
        } else switch (modo) {
            case "Normal":
                {
                    ListaDoble ld = new ListaDoble();
                    Nodo act = p.getCanciones().getCabeza();
                    while (act != null) { ld.insertar(act.song); act = act.siguiente; }
                    pc.setPlaylistNormal(ld);
                    pc.next();
                    break;
                }
            case "Circular":
                {
                    ListaCircular lc = new ListaCircular();
                    Nodo act = p.getCanciones().getCabeza();
                    while (act != null) { lc.insertar(act.song); act = act.siguiente; }
                    pc.setPlaylistCircular(lc);
                    pc.next();
                    break;
                }
            default:
                {
                    pc.setMode(PlayerController.PlayMode.RANDOM);
                    Nodo act = p.getCanciones().getCabeza();
                    while (act != null) { pc.getColaReproduccion().encolar(act.song); act = act.siguiente; }
                    pc.next();
                    break;
                }
        }
    }

    private void actualizarPlaylists() {
        modelPlaylists.clear();
        ListaPlaylists.NodoPlaylist actual = playlistCtrl.getPlaylists().cabeza;
        while (actual != null) {
            modelPlaylists.addElement(actual.playlist.getNombre());
            actual = actual.siguiente;
        }
    }

    private void actualizarCanciones() {
        modelCanciones.clear();
        String sel = listPlaylists.getSelectedValue();
        if (sel == null) return;
        Playlist p = playlistCtrl.getPlaylist(sel);
        if (p != null) {
            Nodo actual = p.getCanciones().getCabeza();
            while (actual != null) {
                modelCanciones.addElement(actual.song.getTitle() + " - " + actual.song.getArtist());
                actual = actual.siguiente;
            }
        }
    }


    private void mostrarError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lumina", JOptionPane.WARNING_MESSAGE);
    }

    /** Crea un JMenuItem con estilo oscuro. */
    private JMenuItem crearMenuItemOscuro(String texto) {
        JMenuItem item = new JMenuItem(texto);
        item.setBackground(new Color(0x2A2A4A));
        item.setForeground(Color.WHITE);
        item.setFont(new Font("Segoe UI", Font.BOLD, 12));
        item.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        item.setOpaque(true);
        // Hover effect
        item.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                item.setBackground(ACCENT);
                item.setForeground(Color.WHITE);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                item.setBackground(new Color(0x2A2A4A));
                item.setForeground(Color.WHITE);
            }
        });
        return item;
    }

    /**
     * Exporta la playlist seleccionada como archivo ZIP con todos los MP3.
     */
    private void exportarPlaylistZip() {
        String sel = listPlaylists.getSelectedValue();
        if (sel == null) { mostrarError("Selecciona una playlist primero."); return; }
        final Playlist p = playlistCtrl.getPlaylist(sel);
        if (p == null || p.getCanciones().tamano == 0) { mostrarError("La playlist estÃÂÃÂ¡ vacÃÂÃÂ­a."); return; }

        // Seleccionar archivo ZIP destino
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Guardar como ZIP");
        String nombreBase = sel.replaceAll("[^a-zA-Z0-9ÃÂÃÂ¡ÃÂÃÂ©ÃÂÃÂ­ÃÂÃÂ³ÃÂÃÂºÃÂÃÂÃÂÃ¢ÂÂ°ÃÂÃÂÃÂÃ¢ÂÂÃÂÃÂ¡ÃÂÃÂ±ÃÂÃ¢ÂÂ _-]", "_");
        fc.setSelectedFile(new File(nombreBase + ".zip"));
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ex) {}
        int res = fc.showSaveDialog(this);
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ex) {}

        if (res != JFileChooser.APPROVE_OPTION) return;

        final File archivoZip;
        File tempZip = fc.getSelectedFile();
        if (!tempZip.getName().endsWith(".zip")) {
            archivoZip = new File(tempZip.getAbsolutePath() + ".zip");
        } else {
            archivoZip = tempZip;
        }

        // Comprimir en hilo secundario con barra de progreso
        int total = p.getCanciones().tamano;
        JProgressBar progressBar = new JProgressBar(0, total);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new java.awt.Dimension(400, 28));

        JDialog dialogo = new JDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this),
            "Exportando ZIP...", false);
        dialogo.setLayout(new java.awt.BorderLayout(12, 12));
        dialogo.add(new JLabel("  Comprimiendo canciones a:\n  " + archivoZip.getAbsolutePath()),
            java.awt.BorderLayout.NORTH);
        dialogo.add(progressBar, java.awt.BorderLayout.CENTER);
        dialogo.setSize(460, 110);
        dialogo.setLocationRelativeTo(this);
        dialogo.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        SwingWorker<int[], Void> worker = new SwingWorker<int[], Void>() {
            @Override
            protected int[] doInBackground() throws Exception {
                int agregadas = 0, errores = 0, idx = 0;
                try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(
                        new java.io.FileOutputStream(archivoZip))) {
                    Nodo actual = p.getCanciones().getCabeza();
                    while (actual != null) {
                        Song s = actual.song;
                        try {
                            java.io.File origen = new java.io.File(s.getPath());
                            if (origen.exists()) {
                                String nombreArchivo = String.format("%02d - %s - %s.mp3",
                                    ++idx, sanitizar(s.getTitle()), sanitizar(s.getArtist()));
                                java.util.zip.ZipEntry entry = new java.util.zip.ZipEntry(nombreArchivo);
                                zos.putNextEntry(entry);

                                try (java.io.FileInputStream fis = new java.io.FileInputStream(origen)) {
                                    byte[] buf = new byte[8192];
                                    int n;
                                    while ((n = fis.read(buf)) > 0) zos.write(buf, 0, n);
                                }
                                zos.closeEntry();
                                agregadas++;
                            } else {
                                errores++;
                            }
                        } catch (IOException ex) {
                            errores++;
                        }
                        final int progActual = agregadas + errores;
                        SwingUtilities.invokeLater(() -> progressBar.setValue(progActual));
                        actual = actual.siguiente;
                    }
                }
                return new int[]{agregadas, errores};
            }

            @Override
            protected void done() {
                dialogo.dispose();
                try {
                    int[] res = get();
                    String msg = String.format(
                        "ExportaciÃÂÃÂ³n completada.\n\n" +
                        "  Canciones agregadas: %d\n" +
                        "  Errores: %d\n\n" +
                        "Archivo: %s",
                        res[0], res[1], archivoZip.getAbsolutePath());
                    JOptionPane.showMessageDialog(PlaylistPanel.this, msg, "ExportaciÃÂÃÂ³n ZIP", JOptionPane.INFORMATION_MESSAGE);
                } catch (HeadlessException | InterruptedException | ExecutionException ex) {
                    JOptionPane.showMessageDialog(PlaylistPanel.this,
                        "Error durante la exportaciÃÂÃÂ³n: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        dialogo.setVisible(true);
        worker.execute();
    }

    /**
     * Desencripta un archivo y muestra su contenido en un diÃÂÃÂ¡logo.
     */
    /**
     * Desencripta un archivo encriptado (.enc) y carga sus canciones como una nueva playlist.
     * Las canciones se buscan primero por path exacto en la biblioteca; si no estÃÂ¡n,
     * se intenta escanear el directorio del archivo MP3 directamente.
     */
    private void desencriptarArchivo() {
        File archivo = FileChooserUtil.abrirArchivo(this, "Seleccionar archivo encriptado");
        if (archivo == null) return;

        java.util.List<String> rutas = new java.util.ArrayList<>();
        String nombrePlaylist = archivo.getName().replaceAll("(?i)\\.enc$|\\.txt$", "").trim();
        if (nombrePlaylist.isEmpty()) nombrePlaylist = "Playlist Importada";

        // 1) Leer y desencriptar todas las lÃÂ­neas
        try (java.io.BufferedReader br = new java.io.BufferedReader(
                new java.io.InputStreamReader(new java.io.FileInputStream(archivo), "UTF-8"))) {
            String linea;
            boolean primeraLinea = true;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;
                String desc;
                try { desc = smartplayer.utils.Encriptacion.desencriptar(linea); }
                catch (Exception ex) { continue; }

                // Primera lÃÂ­nea: nombre de playlist o marcador
                if (primeraLinea) {
                    primeraLinea = false;
                    if (desc.startsWith("__ALBUM__:")) {
                        nombrePlaylist = desc.substring("__ALBUM__:".length()).trim();
                        continue;
                    }
                    // Si la primera lÃÂ­nea no tiene separador de ruta ni termina en .mp3 Ã¢ÂÂ es el nombre
                    if (!desc.contains(java.io.File.separator) && !desc.contains("/")
                            && !desc.toLowerCase().endsWith(".mp3")) {
                        nombrePlaylist = desc.trim();
                        continue;
                    }
                }
                if (desc.startsWith("__ALBUM__:")) continue;
                rutas.add(desc);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error al leer el archivo:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (rutas.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "El archivo no contiene rutas de canciones vÃÂ¡lidas.", "Lumina", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2) Construir la playlist: buscar cada canciÃÂ³n en la biblioteca; si no estÃÂ¡, escanear carpeta
        smartplayer.models.Playlist nuevaPlaylist = new smartplayer.models.Playlist(nombrePlaylist);
        int encontradas = 0, noEncontradas = 0, nuevasEnBiblioteca = 0;
        smartplayer.structures.ListaSimple biblioteca = libraryCtrl.getBiblioteca();

        for (String ruta : rutas) {
            Song cancion = null;

            // Buscar por path exacto en la biblioteca
            smartplayer.structures.Nodo n = biblioteca.getCabeza();
            while (n != null) {
                if (n.song.getPath().equals(ruta)) { cancion = n.song; break; }
                n = n.siguiente;
            }

            // Si no estÃÂ¡ en biblioteca, intentar cargarla del disco
            if (cancion == null) {
                java.io.File f = new java.io.File(ruta);
                if (f.exists() && f.getName().toLowerCase().endsWith(".mp3")) {
                    smartplayer.structures.ListaSimple mini =
                        smartplayer.utils.FileManager.scanDirectory(f.getParentFile());
                    smartplayer.structures.Nodo m = mini.getCabeza();
                    while (m != null) {
                        if (m.song.getPath().equals(ruta)) { cancion = m.song; break; }
                        m = m.siguiente;
                    }
                    // TambiÃÂ©n agregarla a la biblioteca global
                    if (cancion != null) {
                        libraryCtrl.agregarCancion(cancion);
                        nuevasEnBiblioteca++;
                    }
                }
            }

            if (cancion != null) {
                nuevaPlaylist.getCanciones().insertar(cancion);
                encontradas++;
            } else {
                noEncontradas++;
            }
        }

        // 3) Registrar la playlist (si tiene al menos una canciÃÂ³n)
        if (encontradas > 0) {
            playlistCtrl.crearPlaylist(nombrePlaylist);
            smartplayer.models.Playlist p = playlistCtrl.getPlaylist(nombrePlaylist);
            if (p == null) p = nuevaPlaylist; // fallback
            else {
                // Copiar las canciones a la playlist reciÃÂ©n creada
                smartplayer.structures.Nodo nx = nuevaPlaylist.getCanciones().getCabeza();
                while (nx != null) { p.getCanciones().insertar(nx.song); nx = nx.siguiente; }
            }
        }

        actualizarPlaylists();

        String msg = String.format(
            "Desencriptado completado.\n\n" +
            "  Playlist: \"%s\"\n" +
            "  Canciones en playlist: %d\n" +
            (nuevasEnBiblioteca > 0 ? "  Agregadas a biblioteca: %d\n" : "") +
            (noEncontradas > 0 ? "  No encontradas en disco: %d" : ""),
            nombrePlaylist, encontradas,
            nuevasEnBiblioteca > 0 ? nuevasEnBiblioteca : noEncontradas,
            noEncontradas > 0 ? noEncontradas : 0
        ).trim();
        JOptionPane.showMessageDialog(this, msg, "Lumina", JOptionPane.INFORMATION_MESSAGE);
    }
}
