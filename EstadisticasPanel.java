package smartplayer.views;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.*;
import java.util.List;
import smartplayer.controllers.LibraryController;
import smartplayer.controllers.PlayerController;
import smartplayer.controllers.PlaylistController;
import smartplayer.controllers.StatisticsController;
import smartplayer.utils.StatsManager;

/**
 * Panel de estadísticas con tema rosado premium.
 * Cards rosa oscuro, gráficos de barras rosas, historial con filas alternadas navy.
 * Botón Exportar HTML para guardar estadísticas.
 */
public class EstadisticasPanel extends JPanel {

    private static final Color BG_MAIN  = new Color(0x1A1A2E);
    private static final Color BG_CARD  = new Color(0x16213E);
    private static final Color BG_HOVER = new Color(0x2A2A4A);
    private static final Color ACCENT   = new Color(0xFF69B4);
    private static final Color TEXT_MAIN= Color.WHITE;
    private static final Color TEXT_SEC = new Color(0xC0C0C0);
    private static final Color BORDER   = new Color(0x4A4A6A);

    private LibraryController  libraryCtrl;
    private PlayerController   playerCtrl;
    private PlaylistController playlistCtrl;
    private StatsManager       statsManager;

    private JTextArea         txtStats;
    private BarChartPanel     chartTop10;
    private BarChartPanel     chartTop5Artistas;
    private JTable            tablaHistorial;
    private DefaultTableModel modelHistorial;

    public EstadisticasPanel(LibraryController libraryCtrl, PlayerController playerCtrl,
                              PlaylistController playlistCtrl, StatsManager statsManager) {
        this.libraryCtrl  = libraryCtrl;
        this.playerCtrl   = playerCtrl;
        this.playlistCtrl = playlistCtrl;
        this.statsManager = statsManager;

        setLayout(new BorderLayout(0, 0));
        setBackground(BG_MAIN);

        // ---- Panel superior con botones ----
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        topPanel.setBackground(BG_CARD);
        topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

        RoundedButton btnRefresh     = new RoundedButton("Actualizar Estadísticas");
        RoundedButton btnExportarHTML = new RoundedButton("Exportar HTML", RoundedButton.Variante.SECONDARY);
        RoundedButton btnExportarTXT  = new RoundedButton("Exportar TXT",  RoundedButton.Variante.SECONDARY);

        topPanel.add(btnRefresh);
        topPanel.add(btnExportarHTML);
        topPanel.add(btnExportarTXT);
        add(topPanel, BorderLayout.NORTH);

        // ---- Tabs ----
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(BG_MAIN);
        tabs.setForeground(TEXT_SEC);
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabs.setBorder(BorderFactory.createEmptyBorder());

        // Tab 1: Resumen
        txtStats = new JTextArea();
        txtStats.setEditable(false);
        txtStats.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtStats.setBackground(BG_CARD);
        txtStats.setForeground(TEXT_MAIN);
        txtStats.setMargin(new Insets(20, 24, 20, 24));
        txtStats.setLineWrap(true);
        txtStats.setWrapStyleWord(true);
        JScrollPane scrollResumen = new JScrollPane(txtStats);
        CustomScrollBarUI.aplicarA(scrollResumen);
        scrollResumen.getViewport().setBackground(BG_CARD);
        tabs.addTab("Resumen", scrollResumen);

        // Tab 2: Top 10 canciones
        chartTop10 = new BarChartPanel();
        chartTop10.setBackground(BG_MAIN);
        tabs.addTab("Top 10 Canciones", chartTop10);

        // Tab 3: Top 5 artistas
        chartTop5Artistas = new BarChartPanel();
        chartTop5Artistas.setBackground(BG_MAIN);
        chartTop5Artistas.setColorBarra(new Color(0xE91E8C)); // Rosa magenta para artistas
        tabs.addTab("Top 5 Artistas", chartTop5Artistas);

        // Tab 4: Historial
        String[] cols = {"Fecha/Hora", "Canción", "Artista"};
        modelHistorial = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaHistorial = new JTable(modelHistorial);
        tablaHistorial.setBackground(BG_CARD);
        tablaHistorial.setForeground(TEXT_MAIN);
        tablaHistorial.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablaHistorial.setRowHeight(36);
        tablaHistorial.setGridColor(new Color(0x2A2A4A));
        tablaHistorial.setShowGrid(false);
        tablaHistorial.setShowHorizontalLines(false);
        tablaHistorial.setIntercellSpacing(new Dimension(0, 0));
        tablaHistorial.setSelectionBackground(new Color(255, 105, 180, 80));
        tablaHistorial.setSelectionForeground(Color.WHITE);
        tablaHistorial.getTableHeader().setBackground(new Color(0x0D0D1F));
        tablaHistorial.getTableHeader().setForeground(TEXT_SEC);
        tablaHistorial.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));

        tablaHistorial.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) {
                    c.setBackground(row % 2 == 0 ? BG_CARD : new Color(0x1A1A2E));
                    c.setForeground(TEXT_MAIN);
                } else {
                    c.setBackground(new Color(255, 105, 180, 80));
                    c.setForeground(Color.WHITE);
                }
                ((JLabel)c).setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 8));
                return c;
            }
        });

        JScrollPane scrollHist = new JScrollPane(tablaHistorial);
        CustomScrollBarUI.aplicarA(scrollHist);
        scrollHist.getViewport().setBackground(BG_CARD);
        tabs.addTab("Historial", scrollHist);

        add(tabs, BorderLayout.CENTER);

        // Acciones
        btnRefresh.addActionListener(e -> actualizarStats());

        btnExportarHTML.addActionListener(e -> exportarHTML());

        btnExportarTXT.addActionListener(e -> exportarTXT());
    }

    private void actualizarStats() {
        StringBuilder sb = new StringBuilder();
        sb.append("BIBLIOTECA\n");
        sb.append("  Total canciones:       ").append(libraryCtrl.getBiblioteca().tamano).append("\n");
        sb.append("  Tamaño total:          ").append(StatisticsController.getTamanioTotal(libraryCtrl.getBiblioteca())).append("\n");
        sb.append("  Promedio duración:     ").append(StatisticsController.getPromedioDuracion(libraryCtrl.getBiblioteca())).append("\n");
        sb.append("  Género más frecuente:  ").append(StatisticsController.getGeneroMasFrecuente(libraryCtrl.getBiblioteca())).append("\n");
        sb.append("  Archivos duplicados:   ").append(StatisticsController.countArchivosDuplicados(libraryCtrl.getBiblioteca())).append("\n");
        sb.append("  Tamaño en duplicados:  ").append(StatisticsController.getTamanioTotalDuplicados(libraryCtrl.getBiblioteca())).append("\n");
        String[][] dups = StatisticsController.getArchivosDuplicadosDetalle(libraryCtrl.getBiblioteca());
        if (dups.length > 0) {
            sb.append("  Detalle duplicados:\n");
            int maxShow = Math.min(dups.length, 10);
            for (int i = 0; i < maxShow; i++) {
                sb.append("    • ").append(dups[i][0]).append(" - ").append(dups[i][1])
                  .append(" [").append(dups[i][3]).append("]\n");
            }
            if (dups.length > 10) sb.append("    ... y ").append(dups.length - 10).append(" más\n");
        }
        sb.append("\n");
        sb.append("REPRODUCCIÓN\n");
        sb.append("  Total reproducciones:  ").append(statsManager.getTotalReproducciones()).append("\n");
        sb.append("  Tiempo total:          ").append(statsManager.getTiempoTotalFormateado()).append("\n");
        sb.append("  Canción más rep.:      ").append(StatisticsController.getCancionMasReproducida(playerCtrl.getHistorial())).append("\n");
        sb.append("  Artista más escuchado: ").append(StatisticsController.getArtistaMasEscuchado(playerCtrl.getHistorial())).append("\n\n");
        sb.append("PLAYLISTS\n");
        sb.append("  Playlist más grande:   ").append(StatisticsController.getPlaylistMasGrande(playlistCtrl.getPlaylists())).append("\n\n");
        sb.append("RENDIMIENTO\n");
        sb.append("  Búsqueda ABB:          ").append(libraryCtrl.getLastSearchTimeABB()).append(" ns\n");
        sb.append("  Búsqueda AVL:          ").append(libraryCtrl.getLastSearchTimeAVL()).append(" ns\n");
        txtStats.setText(sb.toString());
        txtStats.setCaretPosition(0);

        String[][] top10 = statsManager.getTop10Canciones(libraryCtrl.getBiblioteca());
        if (top10.length > 0) {
            String[] labels = new String[top10.length]; int[] vals = new int[top10.length];
            for (int i = 0; i < top10.length; i++) { labels[i] = top10[i][0]; vals[i] = Integer.parseInt(top10[i][2]); }
            chartTop10.setDatos(labels, vals, "Top 10 Canciones Más Reproducidas");
        } else { chartTop10.setDatos(null, null, "Top 10 Canciones Más Reproducidas"); }

        String[][] top5 = statsManager.getTop5Artistas(libraryCtrl.getBiblioteca());
        if (top5.length > 0) {
            String[] labels = new String[top5.length]; int[] vals = new int[top5.length];
            for (int i = 0; i < top5.length; i++) { labels[i] = top5[i][0]; vals[i] = Integer.parseInt(top5[i][1]); }
            chartTop5Artistas.setDatos(labels, vals, "Top 5 Artistas Más Escuchados");
        } else { chartTop5Artistas.setDatos(null, null, "Top 5 Artistas Más Escuchados"); }

        modelHistorial.setRowCount(0);
        List<String[]> historial = statsManager.getHistorialCompleto();
        for (int i = historial.size() - 1; i >= 0; i--) {
            String[] entry = historial.get(i);
            if (entry.length >= 4) modelHistorial.addRow(new Object[]{entry[3], entry[1], entry[2]});
        }
    }

    /** Exporta estadísticas a archivo HTML con estilos rosados. */
    private void exportarHTML() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Exportar estadísticas como HTML");
        fc.setSelectedFile(new File("estadisticas_spotify_beth.html"));
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ex) {}
        int res = fc.showSaveDialog(this);
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception ex) {}

        if (res == JFileChooser.APPROVE_OPTION) {
            File archivo = fc.getSelectedFile();
            if (!archivo.getName().endsWith(".html"))
                archivo = new File(archivo.getAbsolutePath() + ".html");
            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(archivo), "UTF-8"))) {
                pw.println("<!DOCTYPE html><html lang='es'><head>");
                pw.println("<meta charset='UTF-8'>");
                pw.println("<title>Lumina — Estadísticas</title>");
                pw.println("<style>");
                pw.println("body { background:#1A1A2E; color:#fff; font-family:'Segoe UI',sans-serif; margin:0; padding:24px; }");
                pw.println("h1 { color:#FF69B4; font-size:28px; } h2 { color:#FF85C8; font-size:18px; margin-top:28px; }");
                pw.println(".card { background:#16213E; border-radius:12px; padding:20px 28px; margin-bottom:20px; }");
                pw.println("table { width:100%; border-collapse:collapse; margin-top:12px; }");
                pw.println("th { background:#0D0D1F; color:#C0C0C0; padding:10px 14px; text-align:left; font-size:13px; }");
                pw.println("td { padding:8px 14px; font-size:13px; color:#eee; }");
                pw.println("tr:nth-child(even) { background:#1A1A2E; } tr:nth-child(odd) { background:#16213E; }");
                pw.println(".stat-value { color:#FF69B4; font-weight:bold; }");
                pw.println("</style></head><body>");

                pw.println("<h1>Lumina - Estadisticas</h1>");

                // Resumen
                pw.println("<div class='card'><h2>Biblioteca</h2>");
                pw.println("<p>Total canciones: <span class='stat-value'>" + libraryCtrl.getBiblioteca().tamano + "</span></p>");
                pw.println("<p>Tamaño total: <span class='stat-value'>" + StatisticsController.getTamanioTotal(libraryCtrl.getBiblioteca()) + "</span></p>");
                pw.println("<p>Género más frecuente: <span class='stat-value'>" + StatisticsController.getGeneroMasFrecuente(libraryCtrl.getBiblioteca()) + "</span></p>");
                pw.println("</div>");

                pw.println("<div class='card'><h2>Reproducción</h2>");
                pw.println("<p>Total reproducciones: <span class='stat-value'>" + statsManager.getTotalReproducciones() + "</span></p>");
                pw.println("<p>Tiempo total escuchado: <span class='stat-value'>" + statsManager.getTiempoTotalFormateado() + "</span></p>");
                pw.println("<p>Canción más reproducida: <span class='stat-value'>" + StatisticsController.getCancionMasReproducida(playerCtrl.getHistorial()) + "</span></p>");
                pw.println("</div>");

                // Top 10 canciones
                String[][] top10 = statsManager.getTop10Canciones(libraryCtrl.getBiblioteca());
                pw.println("<div class='card'><h2>Top 10 Canciones</h2><table>");
                pw.println("<tr><th>#</th><th>Título</th><th>Artista</th><th>Reproducciones</th></tr>");
                for (int i = 0; i < top10.length; i++)
                    pw.printf("<tr><td>%d</td><td>%s</td><td>%s</td><td>%s</td></tr>%n", i+1, esc(top10[i][0]), esc(top10[i][1]), top10[i][2]);
                pw.println("</table></div>");

                // Historial
                pw.println("<div class='card'><h2>Historial de Reproducción</h2><table>");
                pw.println("<tr><th>Fecha/Hora</th><th>Canción</th><th>Artista</th></tr>");
                List<String[]> hist = statsManager.getHistorialCompleto();
                for (int i = hist.size() - 1; i >= Math.max(0, hist.size() - 30); i--) {
                    String[] entry = hist.get(i);
                    if (entry.length >= 4)
                        pw.printf("<tr><td>%s</td><td>%s</td><td>%s</td></tr>%n", esc(entry[3]), esc(entry[1]), esc(entry[2]));
                }
                pw.println("</table></div>");

                pw.println("</body></html>");
                JOptionPane.showMessageDialog(this, "Estadísticas exportadas a:\n" + archivo.getAbsolutePath(),
                    "Exportación exitosa", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error al exportar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** Exporta estadísticas a TXT. */
    private void exportarTXT() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Exportar estadísticas como TXT");
        fc.setSelectedFile(new File("estadisticas.txt"));
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ex) {}
        int res = fc.showSaveDialog(this);
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception ex) {}

        if (res == JFileChooser.APPROVE_OPTION) {
            File archivo = fc.getSelectedFile();
            if (!archivo.getName().endsWith(".txt"))
                archivo = new File(archivo.getAbsolutePath() + ".txt");
            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(archivo), "UTF-8"))) {
                pw.println(txtStats.getText());
                JOptionPane.showMessageDialog(this, "Estadísticas exportadas.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error al exportar.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
