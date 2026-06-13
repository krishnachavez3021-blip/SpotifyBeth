package smartplayer.views;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import smartplayer.controllers.PlayerController;
import smartplayer.models.Song;
import smartplayer.structures.Cola;
import smartplayer.structures.Nodo;

/**
 * Panel de cola de reproducción con tema rosado premium.
 * Lista con filas alternadas navy, selección rosa, scrollbar rosa.
 */
public class QueuePanel extends JPanel {

    private static final Color BG_MAIN  = new Color(0x1A1A2E);
    private static final Color BG_PANEL = new Color(0x16213E);
    private static final Color ACCENT   = new Color(0xFF69B4);
    private static final Color TEXT_MAIN= Color.WHITE;
    private static final Color TEXT_SEC = new Color(0xC0C0C0);
    private static final Color BORDER   = new Color(0x4A4A6A);

    private PlayerController         playerCtrl;
    private DefaultListModel<String> modelCola;
    private JList<String>            listCola;
    private JLabel                   lblInfo;
    private JLabel                   lblSiguiente;
    private ArrayList<Song>          cancionesEnCola;

    public QueuePanel(PlayerController playerCtrl) {
        this.playerCtrl      = playerCtrl;
        this.cancionesEnCola = new ArrayList<>();

        setLayout(new BorderLayout(0, 0));
        setBackground(BG_MAIN);

        // ---- Panel superior con título e info ----
        JPanel topPanel = new JPanel(new BorderLayout(12, 0));
        topPanel.setBackground(BG_PANEL);
        topPanel.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JPanel infoPanel = new JPanel(new BorderLayout(0, 4));
        infoPanel.setOpaque(false);

        JLabel lblTitulo = new JLabel("Cola de Reproducción");
        lblTitulo.setForeground(ACCENT);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));

        lblInfo = new JLabel("0 canciones en cola");
        lblInfo.setForeground(TEXT_SEC);
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Label: siguiente cancion (vinculada a la biblioteca/playlist)
        lblSiguiente = new JLabel("▶ Siguiente: —");
        lblSiguiente.setForeground(new Color(0xFF69B4));
        lblSiguiente.setFont(new Font("Segoe UI", Font.BOLD, 13));

        infoPanel.add(lblTitulo, BorderLayout.NORTH);
        infoPanel.add(lblSiguiente, BorderLayout.CENTER);
        infoPanel.add(lblInfo,   BorderLayout.SOUTH);
        topPanel.add(infoPanel,  BorderLayout.WEST);

        // ---- Lista de canciones ----
        modelCola = new DefaultListModel<>();
        listCola  = new JList<>(modelCola);
        listCola.setBackground(BG_PANEL);
        listCola.setForeground(TEXT_MAIN);
        listCola.setSelectionBackground(new Color(255, 105, 180, 80));
        listCola.setSelectionForeground(Color.WHITE);
        listCola.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        listCola.setFixedCellHeight(44);
        listCola.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        listCola.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                lbl.setBackground(isSelected ? new Color(255, 105, 180, 80)
                        : (index % 2 == 0 ? BG_PANEL : new Color(0x1E1E3A)));
                lbl.setForeground(isSelected ? Color.WHITE : TEXT_MAIN);
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                return lbl;
            }
        });

        JScrollPane scrollPane = new JScrollPane(listCola);
        scrollPane.getViewport().setBackground(BG_PANEL);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        CustomScrollBarUI.aplicarA(scrollPane);

        // ---- Panel de botones ----
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        btnPanel.setBackground(BG_PANEL);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));

        RoundedButton btnSubir    = new RoundedButton("Subir",      RoundedButton.Variante.SECONDARY);
        RoundedButton btnBajar    = new RoundedButton("Bajar",      RoundedButton.Variante.SECONDARY);
        RoundedButton btnEliminar = new RoundedButton("Eliminar",   RoundedButton.Variante.DANGER);
        RoundedButton btnLimpiar  = new RoundedButton("Limpiar Todo",      RoundedButton.Variante.SECONDARY);
        RoundedButton btnRefresh  = new RoundedButton("Actualizar",        RoundedButton.Variante.SECONDARY);

        btnPanel.add(btnSubir);
        btnPanel.add(btnBajar);
        btnPanel.add(btnEliminar);
        btnPanel.add(btnLimpiar);
        btnPanel.add(btnRefresh);

        add(topPanel,   BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(btnPanel,   BorderLayout.SOUTH);

        // ---- Acciones ----
        btnSubir.addActionListener(e    -> moverArriba());
        btnBajar.addActionListener(e    -> moverAbajo());
        btnEliminar.addActionListener(e -> eliminarSeleccionado());
        btnLimpiar.addActionListener(e  -> limpiarCola());
        btnRefresh.addActionListener(e  -> actualizarCola());
    }

    public void actualizarCola() {
        modelCola.clear();
        cancionesEnCola.clear();
        Cola cola = playerCtrl.getColaReproduccion();
        Nodo actual = cola.frente;
        int index = 1;
        while (actual != null) {
            cancionesEnCola.add(actual.song);
            modelCola.addElement(index + ".   " + actual.song.getTitle() + " - " + actual.song.getArtist());
            actual = actual.siguiente;
            index++;
        }
        lblInfo.setText(cola.tamano + " canciones en cola");

        // Actualizar etiqueta de siguiente cancion (vinculada a biblioteca/playlist)
        if (lblSiguiente != null) {
            Song siguiente = playerCtrl.getNextSong();
            if (siguiente != null) {
                lblSiguiente.setText("▶ Siguiente: " + siguiente.getTitle() + " — " + siguiente.getArtist());
            } else {
                lblSiguiente.setText("▶ Siguiente: —");
            }
        }
    }

    private void moverArriba() {
        int idx = listCola.getSelectedIndex();
        if (idx <= 0) return;
        String itemVis = modelCola.get(idx); modelCola.remove(idx); modelCola.add(idx - 1, itemVis);
        Song s = cancionesEnCola.remove(idx); cancionesEnCola.add(idx - 1, s);
        listCola.setSelectedIndex(idx - 1);
        reconstruirNumeracion(); sincronizarColaReal();
        lblInfo.setText(cancionesEnCola.size() + " canciones en cola");
    }

    private void moverAbajo() {
        int idx = listCola.getSelectedIndex();
        if (idx < 0 || idx >= modelCola.size() - 1) return;
        String itemVis = modelCola.get(idx); modelCola.remove(idx); modelCola.add(idx + 1, itemVis);
        Song s = cancionesEnCola.remove(idx); cancionesEnCola.add(idx + 1, s);
        listCola.setSelectedIndex(idx + 1);
        reconstruirNumeracion(); sincronizarColaReal();
        lblInfo.setText(cancionesEnCola.size() + " canciones en cola");
    }

    private void eliminarSeleccionado() {
        int idx = listCola.getSelectedIndex();
        if (idx < 0) return;
        modelCola.remove(idx); cancionesEnCola.remove(idx);
        reconstruirNumeracion(); sincronizarColaReal();
        lblInfo.setText(cancionesEnCola.size() + " canciones en cola");
    }

    private void limpiarCola() {
        modelCola.clear(); cancionesEnCola.clear();
        Cola cola = playerCtrl.getColaReproduccion();
        while (!cola.estaVacia()) cola.desencolar();
        lblInfo.setText("0 canciones en cola");
    }

    private void reconstruirNumeracion() {
        modelCola.clear();
        for (int i = 0; i < cancionesEnCola.size(); i++) {
            Song s = cancionesEnCola.get(i);
            modelCola.addElement((i + 1) + ".   " + s.getTitle() + " - " + s.getArtist());
        }
    }

    private void sincronizarColaReal() {
        Cola cola = playerCtrl.getColaReproduccion();
        while (!cola.estaVacia()) cola.desencolar();
        for (Song song : cancionesEnCola) cola.encolar(song);
    }
}
