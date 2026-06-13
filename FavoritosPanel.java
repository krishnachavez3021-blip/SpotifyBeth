package smartplayer.views;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import smartplayer.controllers.LibraryController;
import smartplayer.controllers.CoverArtController;
import smartplayer.models.Song;
import smartplayer.structures.Nodo;

/**
 * Panel de canciones favoritas con tema rosado premium.
 * Muestra solo las canciones marcadas con 🌸.
 */
public class FavoritosPanel extends JPanel {

    private static final int COL_COVER  = 0;
    private static final int COL_NUM    = 1;
    private static final int COL_TITLE  = 2;
    private static final int COL_ARTIST = 3;
    private static final int COL_ALBUM  = 4;
    private static final int COL_DUR    = 5;

    private static final Color BG_MAIN   = new Color(0x1A1A2E);
    private static final Color BG_ROW1   = new Color(0x16213E);
    private static final Color BG_ROW2   = new Color(0x1A1A2E);
    private static final Color BG_TOP    = new Color(0x16213E);
    private static final Color ACCENT    = new Color(0xFF69B4);
    private static final Color TEXT_MAIN = Color.WHITE;
    private static final Color TEXT_SEC  = new Color(0xC0C0C0);

    private LibraryController  libraryCtrl;
    private CoverArtController coverArtCtrl;
    private ReproductorPanel   reproductorPanel;

    private JTable              table;
    private DefaultTableModel   tableModel;
    private SwingWorker<Void, Object[]> coverWorker;

    public FavoritosPanel(LibraryController libraryCtrl, ReproductorPanel reproductorPanel) {
        this.libraryCtrl      = libraryCtrl;
        this.reproductorPanel = reproductorPanel;
        this.coverArtCtrl     = new CoverArtController();

        setLayout(new BorderLayout(0, 0));
        setBackground(BG_MAIN);

        add(crearPanelSuperior(), BorderLayout.NORTH);
        crearTabla();
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(BG_MAIN);
        CustomScrollBarUI.aplicarA(scrollPane);
        add(scrollPane, BorderLayout.CENTER);
        configurarListeners();
    }

    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setBackground(BG_TOP);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel lblTitulo = new JLabel("🌸  Canciones Favoritas") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(getFont());
                g2.setColor(getForeground());
                g2.drawString(getText(), 0, (getHeight() + g2.getFontMetrics().getAscent()
                    - g2.getFontMetrics().getDescent()) / 2);
                g2.dispose();
            }
        };
        lblTitulo.setForeground(ACCENT);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));

        RoundedButton btnActualizar = new RoundedButton("🔄  Actualizar", RoundedButton.Variante.SECONDARY);
        btnActualizar.addActionListener(e -> actualizarTabla());

        panel.add(lblTitulo,      BorderLayout.WEST);
        panel.add(btnActualizar,  BorderLayout.EAST);
        return panel;
    }

    private void crearTabla() {
        String[] columnas = {"", "#", "Título", "Artista", "Álbum", "Duración"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                if (c == COL_COVER) return ImageIcon.class;
                if (c == COL_NUM) return Integer.class;
                return String.class;
            }
        };

        table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component comp = super.prepareRenderer(renderer, row, col);
                if (!isRowSelected(row)) {
                    comp.setBackground(row % 2 == 0 ? BG_ROW1 : BG_ROW2);
                    comp.setForeground(col == COL_ARTIST || col == COL_ALBUM ? TEXT_SEC : TEXT_MAIN);
                } else {
                    comp.setBackground(new Color(255, 105, 180, 60));
                    comp.setForeground(Color.WHITE);
                }
                return comp;
            }
        };

        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setBackground(BG_MAIN);
        table.setForeground(TEXT_MAIN);
        table.setGridColor(new Color(0x2A2A4A));
        table.setSelectionBackground(new Color(255, 105, 180, 60));
        table.setSelectionForeground(Color.WHITE);
        table.setRowHeight(44);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setShowGrid(false);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(0x0D0D1F));
        header.setForeground(TEXT_SEC);
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setPreferredSize(new Dimension(0, 36));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0x4A4A6A)));
        header.setReorderingAllowed(false);

        table.getColumnModel().getColumn(COL_COVER).setMaxWidth(48); table.getColumnModel().getColumn(COL_COVER).setMinWidth(48);
        table.getColumnModel().getColumn(COL_NUM).setMaxWidth(42);   table.getColumnModel().getColumn(COL_NUM).setMinWidth(42);
        table.getColumnModel().getColumn(COL_DUR).setMaxWidth(80);

        table.getColumnModel().getColumn(COL_COVER).setCellRenderer(
            new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    JLabel lbl = new JLabel() {
                        @Override
                        protected void paintComponent(Graphics g) {
                            Graphics2D g2 = (Graphics2D) g.create();
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            g2.setColor(getBackground());
                            g2.fillRect(0, 0, getWidth(), getHeight());
                            if (getIcon() != null) {
                                int iw = getIcon().getIconWidth(), ih = getIcon().getIconHeight();
                                int x = (getWidth() - iw) / 2, y = (getHeight() - ih) / 2;
                                java.awt.geom.RoundRectangle2D clip = new java.awt.geom.RoundRectangle2D.Float(x, y, iw, ih, 8, 8);
                                g2.setClip(clip);
                                getIcon().paintIcon(this, g2, x, y);
                            }
                            g2.dispose();
                        }
                    };
                    lbl.setHorizontalAlignment(SwingConstants.CENTER);
                    lbl.setOpaque(true);
                    lbl.setBackground(isSelected ? new Color(255, 105, 180, 60) : (row % 2 == 0 ? BG_ROW1 : BG_ROW2));
                    if (value instanceof ImageIcon) lbl.setIcon((ImageIcon) value);
                    return lbl;
                }
            }
        );

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(COL_NUM).setCellRenderer(centerRenderer);
    }

    private void configurarListeners() {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = table.rowAtPoint(evt.getPoint());
                    if (row >= 0) {
                        int modelRow = table.convertRowIndexToModel(row);
                        String titulo = (String) tableModel.getValueAt(modelRow, COL_TITLE);
                        String artista = (String) tableModel.getValueAt(modelRow, COL_ARTIST);

                        smartplayer.structures.ListaDoble listaNav = libraryCtrl.getListaDoble();
                        Song song = null;
                        smartplayer.structures.Nodo n = listaNav.cabeza;
                        while (n != null) {
                            if (n.song != null && n.song.getTitle() != null
                                    && n.song.getTitle().equalsIgnoreCase(titulo)
                                    && (artista == null || n.song.getArtist().equalsIgnoreCase(artista))) {
                                song = n.song;
                                break;
                            }
                            n = n.siguiente;
                        }
                        if (song != null) {
                            reproductorPanel.getPlayerCtrl().setCurrentSongInList(song, listaNav);
                            reproductorPanel.playSingle(song);
                        }
                    }
                }
            }
        });

        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) table.setRowSelectionInterval(row, row);
            }
        });
    }

    public void actualizarTabla() {
        tableModel.setRowCount(0);
        int num = 1;
        ImageIcon placeholder = coverArtCtrl.getMiniatura(null, 40);
        Nodo actual = libraryCtrl.getBiblioteca().getCabeza();
        while (actual != null) {
            Song s = actual.song;
            if (s.isFavorite()) {
                tableModel.addRow(new Object[]{
                    placeholder, num++, s.getTitle(), s.getArtist(), s.getAlbum(), s.getDurationFormatted()
                });
            }
            actual = actual.siguiente;
        }
        iniciarCargaMiniaturas();
    }

    private void iniciarCargaMiniaturas() {
        if (coverWorker != null && !coverWorker.isDone()) coverWorker.cancel(true);
        final int numFilas = tableModel.getRowCount();
        final String[] titulos = new String[numFilas];
        for (int i = 0; i < numFilas; i++)
            titulos[i] = (String) tableModel.getValueAt(i, COL_TITLE);

        coverWorker = new SwingWorker<Void, Object[]>() {
            @Override
            protected Void doInBackground() throws InterruptedException {
                for (int i = 0; i < titulos.length && !isCancelled(); i++) {
                    Song song = libraryCtrl.buscarEnABB(titulos[i]);
                    if (song != null && !isCancelled()) {
                        ImageIcon miniatura = coverArtCtrl.getMiniatura(song, 40);
                        publish(new Object[]{i, miniatura});
                    }
                }
                return null;
            }
            @Override
            protected void process(java.util.List<Object[]> chunks) {
                for (Object[] chunk : chunks) {
                    int fila = (Integer) chunk[0];
                    ImageIcon icon = (ImageIcon) chunk[1];
                    if (fila < tableModel.getRowCount())
                        tableModel.setValueAt(icon, fila, COL_COVER);
                }
            }
        };
        coverWorker.execute();
    }

    public void enfocar() { actualizarTabla(); }
}
