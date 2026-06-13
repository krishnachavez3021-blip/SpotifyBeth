package smartplayer.views;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.awt.event.*;
import smartplayer.controllers.LibraryController;
import smartplayer.structures.NodoArbol;
import smartplayer.models.Song;

/**
 * Panel de visualizacion de arboles ABB y AVL.
 * Cada arbol tiene su propia pestana independiente con:
 *   - Visualizador grafico (TreeVisualizerPanel)
 *   - Panel de control propio (buscar, insertar, eliminar, recorridos)
 *   - Panel de info del nodo seleccionado
 *   - Comparativa de tiempos (solo en pestana de comparacion)
 *
 * SIN boton de reproduccion A¢â‚¬Â es un panel de estructuras de datos puro.
 */
public class ArbolesPanel extends JPanel {

    // ---- Paleta ----
    private static final Color BG       = new Color(0x12122A);
    private static final Color BG_PANEL = new Color(0x16213E);
    private static final Color BG_DARK  = new Color(0x0D0D1F);
    private static final Color ACCENT   = new Color(0xFF69B4);
    private static final Color ACCENT2  = new Color(0xE91E8C);
    private static final Color TEXT     = Color.WHITE;
    private static final Color TEXT_SEC = new Color(0xB0B0D0);
    private static final Color BORDER   = new Color(0x3A3A5A);
    private static final Color TAB_ABB  = new Color(0xFF69B4);
    private static final Color TAB_AVL  = new Color(0x6A9FFF);

    private final LibraryController libraryCtrl;

    // Visualizadores
    private TreeVisualizerPanel vizABB;
    private TreeVisualizerPanel vizAVL;
    
    // Combo de recorrido global
    private JComboBox<String> comboRecorridoGlobal;

    // Labels de info nodo por arbol
    private JLabel lblInfoABB;
    private JLabel lblInfoAVL;


    public ArbolesPanel(LibraryController libraryCtrl) {
        this.libraryCtrl = libraryCtrl;
        setLayout(new BorderLayout());
        setBackground(BG);

        // Cabecera general
        add(crearCabecera(), BorderLayout.NORTH);

        // JTabbedPane con tres pestanas: ABB | AVL | Comparativa
        JTabbedPane tabs = crearTabs();
        add(tabs, BorderLayout.CENTER);

        // Auto-cargar arboles cuando el panel se hace visible
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                javax.swing.SwingUtilities.invokeLater(this::visualizarArboles);
            }
        });
    }

    // ===================== CABECERA =====================

    private JPanel crearCabecera() {
        JPanel cab = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, BG_DARK, 0, getHeight(), BG_PANEL);
                g2.setPaint(gp); g2.fillRect(0, 0, getWidth(), getHeight()); g2.dispose();
            }
        };
        cab.setOpaque(false);
        cab.setPreferredSize(new Dimension(0, 58));
        cab.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)));

        // Titulo con icono
        JLabel lblTitulo = new JLabel("Arbols de Busqueda - ABB y AVL") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Punto de color rosado
                g2.setColor(ACCENT);
                g2.fillOval(0, (getHeight() - 8) / 2, 8, 8);
                // Texto
                g2.setFont(getFont());
                g2.setColor(getForeground());
                g2.drawString(getText(), 18, (getHeight() + g2.getFontMetrics().getAscent()
                    - g2.getFontMetrics().getDescent()) / 2);
                g2.dispose();
            }
        };
        lblTitulo.setForeground(TEXT);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setPreferredSize(new Dimension(440, 38));

        // Combo de recorrido con actualizacion automatica
        comboRecorridoGlobal = new JComboBox<>(new String[]{"InOrden", "PreOrden", "PostOrden"});
        comboRecorridoGlobal.setBackground(new Color(0x2A2A4A));
        comboRecorridoGlobal.setForeground(Color.WHITE);
        comboRecorridoGlobal.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        comboRecorridoGlobal.setPreferredSize(new Dimension(120, 32));
        comboRecorridoGlobal.setRenderer(new DefaultListCellRenderer() {
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
        // Actualizar automaticamente cuando cambia el combo
        comboRecorridoGlobal.addActionListener(e -> visualizarArboles());

        // Boton Visualizar
        RoundedButton btnViz = new RoundedButton("Visualizar Arbols");
        btnViz.addActionListener(e -> visualizarArboles());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(comboRecorridoGlobal);
        right.add(btnViz);

        cab.add(lblTitulo, BorderLayout.WEST);
        cab.add(right,     BorderLayout.EAST);
        return cab;
    }
    
    private void visualizarArboles() {
        String recorrido = (String) comboRecorridoGlobal.getSelectedItem();
        
        // Actualizar visualizador ABB
        if (vizABB != null) {
            vizABB.setRecorridoActual(recorrido);
            vizABB.setRaiz(libraryCtrl.getArbolABB().raiz);
            lblInfoABB.setText("Arbol ABB - Recorrido: " + recorrido);
        }
        
        // Actualizar visualizador AVL
        if (vizAVL != null) {
            vizAVL.setRecorridoActual(recorrido);
            vizAVL.setRaiz(libraryCtrl.getArbolAVL().raiz);
            lblInfoAVL.setText("Arbol AVL - Recorrido: " + recorrido);
        }
    }
    
    private javax.swing.tree.DefaultMutableTreeNode construirTreeNode(NodoArbol nodo, String recorrido) {
        // Metodo no usado - se usa TreeVisualizerPanel
        return null;
    }
    
    private void expandirTodo(JTree tree) {
        // Metodo no usado - se usa TreeVisualizerPanel
    }

    // ===================== TABS =====================

    private JTabbedPane crearTabs() {
        JTabbedPane tp = new JTabbedPane() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(BG); g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        tp.setBackground(BG);
        tp.setForeground(TEXT_SEC);
        tp.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        // AAƒÂ±adir las tres pestaAƒÂ±as
        tp.addTab("ABB", crearPanelArbol("ABB"));
        tp.addTab("AVL", crearPanelArbol("AVL"));

        
        return tp;
    }
    
    private JPanel crearPanelArbol(String tipo) {
        boolean esAVL = "AVL".equals(tipo);
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG);
        TreeVisualizerPanel viz = new TreeVisualizerPanel();
        viz.setTituloArbol(tipo);
        viz.setMostrarBalanceo(esAVL);
        if (esAVL) { vizAVL = viz; vizAVL.setOnNodeClick(s -> mostrarInfoNodo(s, "AVL")); }
        else { vizABB = viz; vizABB.setOnNodeClick(s -> mostrarInfoNodo(s, "ABB")); }
        JScrollPane scroll = new JScrollPane(viz);
        CustomScrollBarUI.aplicarA(scroll);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(BG);
        JPanel ctrl = crearPanelControl(tipo, esAVL, viz);
        JLabel lblInfo = new JLabel("Selecciona recorrido y presiona Visualizar");
        lblInfo.setForeground(TEXT_SEC);
        lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblInfo.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
        if (esAVL) { lblInfoAVL = lblInfo; } else { lblInfoABB = lblInfo; }
        JLabel lblZoom = new JLabel();
        lblZoom.setForeground(new Color(0x9090B0));
        lblZoom.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblZoom.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        JButton btnZoomIn = crearBtnZoom("+");
        JButton btnZoomOut = crearBtnZoom("-");
        JButton btnFit = crearBtnZoom("Ajustar");
        Runnable actualizarLblZoom = () -> lblZoom.setText(String.format("%.0f%%", viz.getZoom() * 100));
        actualizarLblZoom.run();
        btnZoomIn.addActionListener(e -> { viz.zoomIn(); actualizarLblZoom.run(); });
        btnZoomOut.addActionListener(e -> { viz.zoomOut(); actualizarLblZoom.run(); });
        btnFit.addActionListener(e -> { viz.zoomFit(); actualizarLblZoom.run(); });
        JPanel zoomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 4));
        zoomBar.setBackground(BG_PANEL);
        zoomBar.add(btnZoomOut); zoomBar.add(lblZoom); zoomBar.add(btnZoomIn);
        zoomBar.add(Box.createHorizontalStrut(6)); zoomBar.add(btnFit);
        JPanel infoBar = new JPanel(new BorderLayout());
        infoBar.setBackground(BG_PANEL);
        infoBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));
        infoBar.add(lblInfo, BorderLayout.CENTER);
        infoBar.add(zoomBar, BorderLayout.EAST);
        JPanel centerArea = new JPanel(new BorderLayout());
        centerArea.setBackground(BG);
        centerArea.add(scroll, BorderLayout.CENTER);
        centerArea.add(infoBar, BorderLayout.SOUTH);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, ctrl, centerArea);
        split.setDividerLocation(220);
        split.setDividerSize(4);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setBackground(BG);
        root.add(split, BorderLayout.CENTER);
        return root;
    }
    /** Crea un boton pequeno de zoom con estilo consistente. */
    private JButton crearBtnZoom(String texto) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isArmed() ? new Color(0xFF69B4) :
                           getModel().isRollover() ? new Color(0x3A3A5A) : new Color(0x252545);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(0x5A5A7A));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(texto.length() > 2 ? 78 : 28, 24));
        return btn;
    }

    /** Panel de control lateral con operaciones del arbol. */
    private JPanel crearPanelControl(String tipo, boolean esAVL, TreeVisualizerPanel viz) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER),
            BorderFactory.createEmptyBorder(16, 12, 16, 12)));
        panel.setPreferredSize(new Dimension(220, 0));

        // Titulo del panel de control
        JLabel lblCabCtrl = new JLabel(tipo + " - Operaciones");
        lblCabCtrl.setForeground(esAVL ? TAB_AVL : TAB_ABB);
        lblCabCtrl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCabCtrl.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblCabCtrl);
        panel.add(Box.createVerticalStrut(14));

        // Barra divisora
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(sep);
        panel.add(Box.createVerticalStrut(12));

        // Campo de busqueda
        JLabel lblBuscar = new JLabel("Buscar cancion:");
        lblBuscar.setForeground(TEXT_SEC);
        lblBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblBuscar.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblBuscar);
        panel.add(Box.createVerticalStrut(4));

        JTextField txtBuscar = new JTextField();
        txtBuscar.setBackground(new Color(0x1A1A3A));
        txtBuscar.setForeground(Color.WHITE);
        txtBuscar.setCaretColor(ACCENT);
        txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtBuscar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        txtBuscar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        txtBuscar.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtBuscar.setEditable(true);
        txtBuscar.setEnabled(true);
        panel.add(txtBuscar);
        panel.add(Box.createVerticalStrut(6));

        RoundedButton btnBuscar = new RoundedButton("Buscar");
        btnBuscar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        btnBuscar.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(btnBuscar);
        panel.add(Box.createVerticalStrut(14));

        // ---- Insertar ----
        JSeparator sep2 = new JSeparator();
        sep2.setForeground(BORDER);
        sep2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep2.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(sep2);
        panel.add(Box.createVerticalStrut(10));

        RoundedButton btnInsertar = new RoundedButton("Insertar cancion", RoundedButton.Variante.SECONDARY);
        btnInsertar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        btnInsertar.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(btnInsertar);
        panel.add(Box.createVerticalStrut(14));

        // ---- Eliminar ----
        JSeparator sep3 = new JSeparator();
        sep3.setForeground(BORDER);
        sep3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep3.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(sep3);
        panel.add(Box.createVerticalStrut(10));

        RoundedButton btnEliminar = new RoundedButton("Eliminar cancion", RoundedButton.Variante.DANGER);
        btnEliminar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        btnEliminar.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(btnEliminar);
        panel.add(Box.createVerticalStrut(14));

        // ---- Modificar ----
        JSeparator sep3b = new JSeparator();
        sep3b.setForeground(BORDER);
        sep3b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep3b.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(sep3b);
        panel.add(Box.createVerticalStrut(10));

        RoundedButton btnModificar = new RoundedButton("Modificar cancion", RoundedButton.Variante.SECONDARY);
        btnModificar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        btnModificar.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(btnModificar);
        panel.add(Box.createVerticalStrut(14));

        // ---- Recorridos ----
        JSeparator sep4 = new JSeparator();
        sep4.setForeground(BORDER);
        sep4.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep4.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(sep4);
        panel.add(Box.createVerticalStrut(10));

        // Recorrido: usar combo global en cabecera
        panel.add(Box.createVerticalStrut(6));

        RoundedButton btnRec = new RoundedButton("Ver Recorrido", RoundedButton.Variante.SECONDARY);
        btnRec.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        btnRec.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(btnRec);
        panel.add(Box.createVerticalGlue());

        // ---- Acciones ----
        btnBuscar.addActionListener(e -> {
            String t = txtBuscar.getText().trim();
            if (t.isEmpty()) return;
            long t0 = System.nanoTime();
            Song result = esAVL ? libraryCtrl.buscarEnAVL(t) : libraryCtrl.buscarEnABB(t);
            long ns = System.nanoTime() - t0;
            // If exact match fails, try partial search in library
            if (result == null) {
                String tLower = t.toLowerCase();
                smartplayer.structures.Nodo n = libraryCtrl.getBiblioteca().getCabeza();
                while (n != null) {
                    if (n.song.getTitle().toLowerCase().contains(tLower) ||
                        n.song.getArtist().toLowerCase().contains(tLower)) {
                        result = n.song;
                        break;
                    }
                    n = n.siguiente;
                }
            }
            JLabel lbl = esAVL ? lblInfoAVL : lblInfoABB;
            if (result != null) {
                
                lbl.setText(String.format(
                    "<html><b style='color:#FF69B4'>[%s]</b> Encontrada: <b>%s</b> A¢â‚¬Â %s  |  <i>%d ns</i></html>",
                    tipo, result.getTitle(), result.getArtist(), ns));
            } else {
                lbl.setText("<html><b style='color:#FF6060'>No encontrada:</b> " + t + "</html>");
            }
        });

        txtBuscar.addActionListener(e -> btnBuscar.doClick());

        btnInsertar.addActionListener(e -> {
            // Mostrar lista de canciones de la biblioteca para elegir
            java.util.List<Song> todas = new java.util.ArrayList<>();
            smartplayer.structures.Nodo n = libraryCtrl.getBiblioteca().getCabeza();
            while (n != null) { todas.add(n.song); n = n.siguiente; }
            if (todas.isEmpty()) {
                JOptionPane.showMessageDialog(this, "La biblioteca esta vacia.",
                    tipo + " A¢â‚¬Â Insertar", JOptionPane.WARNING_MESSAGE); return;
            }
            Song elegida = mostrarSelectorCancion(todas, tipo + " A¢â‚¬Â Insertar cancion");
            if (elegida == null) return;
            if (esAVL) libraryCtrl.getArbolAVL().insertar(elegida);
            else       libraryCtrl.getArbolABB().insertar(elegida);
            visualizarArboles();
            
        });

        btnEliminar.addActionListener(e -> {
            // Mostrar lista de nodos del arbol para elegir cual eliminar
            java.util.List<Song> enArbol = new java.util.ArrayList<>();
            NodoArbol raizActual = esAVL ? libraryCtrl.getArbolAVL().raiz : libraryCtrl.getArbolABB().raiz;
            recolectarInOrden(raizActual, enArbol);
            if (enArbol.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El arbol esta vacio.",
                    tipo + " A¢â‚¬Â Eliminar", JOptionPane.WARNING_MESSAGE); return;
            }
            Song elegida = mostrarSelectorCancion(enArbol, tipo + " A¢â‚¬Â Eliminar cancion del arbol");
            if (elegida == null) return;
            if (esAVL) libraryCtrl.getArbolAVL().eliminar(elegida.getTitle());
            else       libraryCtrl.getArbolABB().eliminar(elegida.getTitle());
            visualizarArboles();
            JLabel lbl = esAVL ? lblInfoAVL : lblInfoABB;
            lbl.setText("\"" + elegida.getTitle() + "\" eliminada del arbol " + tipo);
        });

        btnModificar.addActionListener(e -> {
            java.util.List<Song> enArbol = new java.util.ArrayList<>();
            NodoArbol raizMod = esAVL ? libraryCtrl.getArbolAVL().raiz : libraryCtrl.getArbolABB().raiz;
            recolectarInOrden(raizMod, enArbol);
            if (enArbol.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El arbol esta vacio.",
                    tipo + " - Modificar", JOptionPane.WARNING_MESSAGE); return;
            }
            Song elegida = mostrarSelectorCancion(enArbol, tipo + " - Seleccionar cancion a modificar");
            if (elegida == null) return;

            JTextField fTitulo  = new JTextField(elegida.getTitle(), 20);
            JTextField fArtista = new JTextField(elegida.getArtist(), 20);
            JTextField fAlbum   = new JTextField(elegida.getAlbum(), 20);
            JTextField fGenero  = new JTextField(elegida.getGenre(), 20);
            JTextField fAnio    = new JTextField(elegida.getYear(), 6);

            JPanel form = new JPanel(new java.awt.GridLayout(5, 2, 6, 6));
            form.add(new JLabel("Titulo:"));   form.add(fTitulo);
            form.add(new JLabel("Artista:"));  form.add(fArtista);
            form.add(new JLabel("Album:"));    form.add(fAlbum);
            form.add(new JLabel("Genero:"));   form.add(fGenero);
            form.add(new JLabel("Anio:"));     form.add(fAnio);

            int ok = JOptionPane.showConfirmDialog(this, form,
                tipo + " - Modificar: " + elegida.getTitle(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (ok != JOptionPane.OK_OPTION) return;

            Song actualizada = new Song(
                fTitulo.getText().trim().isEmpty()  ? elegida.getTitle()  : fTitulo.getText().trim(),
                fArtista.getText().trim().isEmpty() ? elegida.getArtist() : fArtista.getText().trim(),
                fAlbum.getText().trim().isEmpty()   ? elegida.getAlbum()  : fAlbum.getText().trim(),
                fGenero.getText().trim().isEmpty()  ? elegida.getGenre()  : fGenero.getText().trim(),
                elegida.getDuration(), elegida.getSize(), elegida.getPath(),
                fAnio.getText().trim().isEmpty()    ? elegida.getYear()   : fAnio.getText().trim()
            );
            actualizada.setPlayCount(elegida.getPlayCount());
            actualizada.setFavorite(elegida.isFavorite());

            boolean modificado;
            if (esAVL) modificado = libraryCtrl.getArbolAVL().modificar(elegida.getTitle(), actualizada);
            else       modificado = libraryCtrl.getArbolABB().modificar(elegida.getTitle(), actualizada);

            visualizarArboles();
            JLabel lbl2 = esAVL ? lblInfoAVL : lblInfoABB;
            lbl2.setText(modificado
                ? "\"" + elegida.getTitle() + "\" modificada en arbol " + tipo
                : "No se encontro la cancion en arbol " + tipo);
        });

        btnRec.addActionListener(e -> {
            String rec = (String) comboRecorridoGlobal.getSelectedItem();
            NodoArbol raiz = esAVL ? libraryCtrl.getArbolAVL().raiz : libraryCtrl.getArbolABB().raiz;
            StringBuilder sb = new StringBuilder();
            switch (rec) {
                case "InOrden":   inOrden(raiz, sb);   break;
                case "PreOrden":  preOrden(raiz, sb);  break;
                default:          postOrden(raiz, sb); break;
            }
            mostrarRecorrido(tipo, rec, sb.toString());
        });

        return panel;
    }

    // ===================== HELPERS =====================

    private void mostrarInfoNodo(Song song, String tipo) {
        if (song == null) return;
        String html = String.format(
            "<html><b style='color:#FF69B4'>[%s]</b>  <b>%s</b>  A¢â‚¬Â  %s  |  AA‚Âlbum: %s  |  %s  |  Plays: %d</html>",
            tipo, song.getTitle(), song.getArtist(), song.getAlbum(),
            song.getDurationFormatted(), song.getPlayCount());
        if ("ABB".equals(tipo) && lblInfoABB != null) lblInfoABB.setText(html);
        if ("AVL".equals(tipo) && lblInfoAVL != null) lblInfoAVL.setText(html);
    }

    private void mostrarRecorrido(String tipo, String rec, String texto) {
        JTextArea ta = new JTextArea(texto.isEmpty() ? "(arbol vacio)" : texto);
        ta.setEditable(false);
        ta.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ta.setBackground(new Color(0x16213E));
        ta.setForeground(Color.WHITE);
        ta.setMargin(new Insets(10, 14, 10, 14));
        JScrollPane sp = new JScrollPane(ta);
        CustomScrollBarUI.aplicarA(sp);
        sp.setPreferredSize(new Dimension(420, 320));
        JOptionPane.showMessageDialog(this, sp,
            tipo + "  A¢â‚¬Â  Recorrido " + rec, JOptionPane.PLAIN_MESSAGE);
    }

    // ---- Helpers de seleccion ----

    /** Muestra un dialogo con lista filtrable para elegir una cancion. */
    private Song mostrarSelectorCancion(java.util.List<Song> canciones, String titulo) {
        DefaultListModel<String> modelo = new DefaultListModel<>();
        for (Song s : canciones) modelo.addElement(s.getTitle() + "  A¢â‚¬Â  " + s.getArtist());

        JList<String> lista = new JList<>(modelo);
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lista.setBackground(new Color(0x1A1A2E));
        lista.setForeground(Color.WHITE);
        lista.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lista.setFixedCellHeight(34);
        lista.setSelectionBackground(ACCENT);
        lista.setSelectionForeground(Color.WHITE);
        if (!canciones.isEmpty()) lista.setSelectedIndex(0);

        JTextField txtFiltro = new JTextField();
        txtFiltro.setBackground(new Color(0x2A2A4A));
        txtFiltro.setForeground(Color.WHITE);
        txtFiltro.setCaretColor(Color.WHITE);
        txtFiltro.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtFiltro.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        txtFiltro.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            void filtrar() {
                String q = txtFiltro.getText().trim().toLowerCase();
                modelo.clear();
                for (Song s : canciones) {
                    if (q.isEmpty() || s.getTitle().toLowerCase().contains(q)
                            || s.getArtist().toLowerCase().contains(q))
                        modelo.addElement(s.getTitle() + "  A¢â‚¬Â  " + s.getArtist());
                }
                if (!modelo.isEmpty()) lista.setSelectedIndex(0);
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filtrar(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filtrar(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
        });

        JScrollPane scroll = new JScrollPane(lista);
        CustomScrollBarUI.aplicarA(scroll);
        scroll.setPreferredSize(new java.awt.Dimension(400, 260));

        JPanel top = new JPanel(new BorderLayout(0, 4));
        top.setBackground(new Color(0x16213E));
        JLabel lblF = new JLabel("Filtrar:");
        lblF.setForeground(TEXT_SEC); lblF.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        top.add(lblF, BorderLayout.NORTH);
        top.add(txtFiltro, BorderLayout.CENTER);

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(new Color(0x16213E));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(top,    BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        int res = JOptionPane.showConfirmDialog(this, panel, titulo,
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION || lista.isSelectionEmpty()) return null;

        String sel = lista.getSelectedValue();
        String tituloSel = sel.contains("  A¢â‚¬Â  ") ? sel.substring(0, sel.indexOf("  A¢â‚¬Â  ")).trim() : sel;
        // Buscar en la lista original
        for (Song s : canciones) if (s.getTitle().equals(tituloSel)) return s;
        return null;
    }

    /** Recolecta canciones del arbol en orden in-order. */
    private void recolectarInOrden(NodoArbol n, java.util.List<Song> lista) {
        if (n == null) return;
        recolectarInOrden(n.izquierdo, lista);
        lista.add(n.song);
        recolectarInOrden(n.derecho, lista);
    }

    // ---- Recorridos ----
    private void inOrden(NodoArbol n, StringBuilder sb) {
        if (n == null) return;
        inOrden(n.izquierdo, sb);
        sb.append(n.song.getTitle()).append("\n");
        inOrden(n.derecho, sb);
    }
    private void preOrden(NodoArbol n, StringBuilder sb) {
        if (n == null) return;
        sb.append(n.song.getTitle()).append("\n");
        preOrden(n.izquierdo, sb);
        preOrden(n.derecho, sb);
    }
    private void postOrden(NodoArbol n, StringBuilder sb) {
        if (n == null) return;
        postOrden(n.izquierdo, sb);
        postOrden(n.derecho, sb);
        sb.append(n.song.getTitle()).append("\n");
    }

    // ===================== UI PERSONALIZADA DE PESTAAA¢â‚¬ËœAS =====================

    private static class TreeTabUI extends BasicTabbedPaneUI {
        private static final Color BG      = new Color(0x12122A);
        private static final Color ACTIVE  = new Color(0xFF69B4);
        private static final Color NORMAL  = new Color(0x9090B0);

        @Override protected void installDefaults() {
            super.installDefaults();
            tabInsets            = new Insets(10, 18, 10, 18);
            selectedTabPadInsets = new Insets(0, 0, 0, 0);
            tabAreaInsets        = new Insets(0, 0, 0, 0);
            contentBorderInsets  = new Insets(0, 0, 0, 0);
        }

        @Override protected void paintTabBackground(Graphics g, int tp, int idx,
                int x, int y, int w, int h, boolean sel) {
            g.setColor(BG); g.fillRect(x, y, w, h);
        }

        @Override protected void paintTabBorder(Graphics g, int tp, int idx,
                int x, int y, int w, int h, boolean sel) {
            if (sel) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(x, 0, new Color(0xFF85C8),
                    x + w, 0, new Color(0xE91E8C));
                g2.setPaint(gp);
                g2.fillRoundRect(x + 6, y + h - 3, w - 12, 3, 3, 3);
                g2.dispose();
            }
        }

        @Override protected void paintText(Graphics g, int tp, Font font,
                FontMetrics fm, int idx, String title, Rectangle tr, boolean sel) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(new Font("Segoe UI", sel ? Font.BOLD : Font.PLAIN, 13));
            g2.setColor(sel ? Color.WHITE : NORMAL);
            g2.drawString(title, tr.x, tr.y + fm.getAscent());
            g2.dispose();
        }

        @Override protected void paintContentBorder(Graphics g, int tp, int idx) {}
        @Override protected void paintFocusIndicator(Graphics g, int tp,
                Rectangle[] rects, int idx, Rectangle ir, Rectangle tr, boolean sel) {}
        @Override protected int calculateTabHeight(int tp, int idx, int fh) { return 44; }
    }
}