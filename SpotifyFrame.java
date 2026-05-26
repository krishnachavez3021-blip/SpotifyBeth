package com.proyectofinal.spotify;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class SpotifyFrame extends JFrame {
    private final SimpleSongList library = new SimpleSongList();
    private final SongStack history = new SongStack();
    private final SongQueue queue = new SongQueue();
    private final DoubleSongList doubleList = new DoubleSongList();
    private final CircularSongList circularList = new CircularSongList();
    private final SongTree abb = new SongTree();
    private final SongAvlTree avl = new SongAvlTree();
    private final AudioPlayer audio = new AudioPlayer();
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"#", "Cancion", "Artista", "Album", "Genero", "Duracion", "Veces"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final JLabel now = new JLabel("Selecciona una cancion");
    private final JLabel sub = new JLabel("Carga una carpeta o reproduce la demo");
    private final JLabel status = new JLabel("Listo");
    private final JLabel cover = new JLabel();
    private final JProgressBar progress = new JProgressBar(0, 100);
    private final JTextField search = new JTextField();
    private final JButton play = new JButton("Play");
    private final JButton repeat = new JButton("Repetir: no");
    private final JButton shuffle = new JButton("Aleatorio");
    private final List<Song> visibleSongs = new ArrayList<Song>();
    private Song current;
    private boolean repeatCircular;
    private boolean shuffleMode;

    public SpotifyFrame() {
        setTitle("Spotify Proyecto Final");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1220, 760));
        setContentPane(root());
        loadDemo();
        refresh();
        new Timer(350, e -> tick()).start();
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel root() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(18, 18, 18));
        root.add(sidebar(), BorderLayout.WEST);
        root.add(center(), BorderLayout.CENTER);
        root.add(player(), BorderLayout.SOUTH);
        return root;
    }

    private JPanel sidebar() {
        JPanel p = new JPanel(new GridLayout(15, 1, 0, 8));
        p.setPreferredSize(new Dimension(245, 0));
        p.setBackground(new Color(7, 7, 7));
        p.setBorder(new EmptyBorder(18, 16, 18, 16));
        p.add(title("Spotify", 32, new Color(30, 215, 96)));
        p.add(button("Inicio", e -> showHome()));
        p.add(button("Cargar carpeta", e -> loadFolder()));
        p.add(button("Cargar ZIP", e -> loadZip()));
        p.add(button("Cargar canciones", e -> loadFiles()));
        p.add(button("Buscar ABB/AVL", e -> searchSong()));
        p.add(button("Agregar a cola", e -> enqueueSelected()));
        p.add(button("Cola", e -> showQueue()));
        p.add(button("Historial", e -> showHistory()));
        p.add(button("Biblioteca", e -> showLibrary()));
        p.add(button("ABB", e -> JOptionPane.showMessageDialog(this, abb.horizontal("ARBOL ABB"))));
        p.add(button("AVL", e -> JOptionPane.showMessageDialog(this, avl.horizontalAvl())));
        p.add(button("Estadisticas", e -> showStats()));
        p.add(button("Exportar", e -> exportPlaylist()));
        p.add(button("Encriptar", e -> encryptDialog()));
        return p;
    }

    private JPanel center() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setBackground(new Color(18, 18, 18));
        p.setBorder(new EmptyBorder(20, 22, 14, 22));
        p.add(header(), BorderLayout.NORTH);
        p.add(tableScroll(), BorderLayout.CENTER);
        return p;
    }

    private JPanel header() {
        JPanel p = new JPanel(new BorderLayout(16, 0));
        p.setOpaque(false);
        JLabel h = title("Tu biblioteca", 34, Color.WHITE);
        p.add(h, BorderLayout.WEST);
        search.setText("Buscar por cancion, artista, album o genero");
        search.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        search.setBackground(new Color(38, 38, 38));
        search.setForeground(Color.WHITE);
        search.setCaretColor(Color.WHITE);
        search.setBorder(BorderFactory.createEmptyBorder(11, 15, 11, 15));
        search.addActionListener(e -> refresh());
        search.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (search.getText().startsWith("Buscar por")) {
                    search.setText("");
                }
            }
        });
        p.add(search, BorderLayout.CENTER);
        p.add(button("Filtrar", e -> refresh()), BorderLayout.EAST);
        return p;
    }

    private JScrollPane tableScroll() {
        table.setRowHeight(38);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setBackground(new Color(18, 18, 18));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(35, 35, 35));
        table.setSelectionBackground(new Color(30, 115, 65));
        table.setSelectionForeground(Color.WHITE);
        table.getSelectionModel().addListSelectionListener(e -> select());
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    playCurrent();
                }
            }
        });
        table.getTableHeader().setBackground(new Color(34, 34, 34));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(6).setMaxWidth(70);
        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(new Color(18, 18, 18));
        sp.setBorder(BorderFactory.createLineBorder(new Color(38, 38, 38)));
        return sp;
    }

    private JPanel player() {
        JPanel p = new JPanel(new BorderLayout(18, 0));
        p.setBackground(new Color(10, 10, 10));
        p.setBorder(new EmptyBorder(14, 22, 14, 22));

        JPanel left = new JPanel(new BorderLayout(12, 0));
        left.setOpaque(false);
        cover.setPreferredSize(new Dimension(76, 76));
        cover.setHorizontalAlignment(SwingConstants.CENTER);
        cover.setIcon(new ImageIcon(defaultCover("SP", 76)));
        left.add(cover, BorderLayout.WEST);
        JPanel info = new JPanel(new GridLayout(2, 1));
        info.setOpaque(false);
        now.setForeground(Color.WHITE);
        now.setFont(new Font("Segoe UI", Font.BOLD, 16));
        sub.setForeground(new Color(185, 185, 185));
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        info.add(now);
        info.add(sub);
        left.add(info, BorderLayout.CENTER);
        p.add(left, BorderLayout.WEST);

        JPanel controls = new JPanel(new BorderLayout(0, 8));
        controls.setOpaque(false);
        JPanel row = new JPanel(new GridLayout(1, 8, 10, 0));
        row.setOpaque(false);
        row.add(button("Anterior", e -> playSong(doubleList.previous())));
        play.addActionListener(e -> playOrPause());
        styleButton(play, true);
        row.add(play);
        row.add(button("Siguiente", e -> nextSong()));
        row.add(button("Cola Play", e -> playSong(queue.dequeue())));
        repeat.addActionListener(e -> toggleCircular());
        styleButton(repeat, false);
        row.add(repeat);
        shuffle.addActionListener(e -> toggleShuffle());
        styleButton(shuffle, false);
        row.add(shuffle);
        row.add(button("Detener", e -> stop()));
        row.add(button("Limpiar busqueda", e -> clearSearch()));
        controls.add(row, BorderLayout.NORTH);
        progress.setForeground(new Color(30, 215, 96));
        progress.setBackground(new Color(55, 55, 55));
        controls.add(progress, BorderLayout.SOUTH);
        p.add(controls, BorderLayout.CENTER);

        status.setForeground(Color.WHITE);
        status.setHorizontalAlignment(SwingConstants.RIGHT);
        status.setPreferredSize(new Dimension(190, 28));
        p.add(status, BorderLayout.EAST);
        return p;
    }

    private void loadDemo() {
        add(new Song("Intro Spotify", "Proyecto Final", "Demo", "Pop", 12, "resource:/audio/song1.wav"));
        add(new Song("Arbol ABB", "Estructuras", "Datos", "Rock", 12, "resource:/audio/song2.wav"));
        add(new Song("AVL Balanceado", "Progra III", "Datos", "Electronica", 12, "resource:/audio/song3.wav"));
        add(new Song("Pila Historial", "Java Swing", "Demo", "Pop", 12, "resource:/audio/song4.wav"));
        add(new Song("Cola Reproduccion", "NetBeans", "Demo", "Dance", 12, "resource:/audio/song5.wav"));
    }

    private void add(Song s) {
        library.add(s);
        queue.enqueue(s);
        doubleList.add(s);
        circularList.add(s);
        abb.insert(s);
        avl.insertAvl(s);
    }

    private void refresh() {
        model.setRowCount(0);
        visibleSongs.clear();
        String q = search.getText().trim().toLowerCase(Locale.ROOT);
        if (q.startsWith("buscar por")) {
            q = "";
        }
        List<Song> songs = library.all();
        for (int i = 0; i < songs.size(); i++) {
            Song s = songs.get(i);
            if (q.isEmpty() || matches(s, q)) {
                visibleSongs.add(s);
                model.addRow(new Object[]{visibleSongs.size(), s.getTitle(), s.getArtist(), s.getAlbum(), s.getGenre(), s.duration(), s.getPlays()});
            }
        }
        if (!visibleSongs.isEmpty()) {
            table.setRowSelectionInterval(0, 0);
        }
        status.setText(visibleSongs.size() + " canciones");
    }

    private boolean matches(Song s, String q) {
        return s.getTitle().toLowerCase(Locale.ROOT).contains(q)
                || s.getArtist().toLowerCase(Locale.ROOT).contains(q)
                || s.getAlbum().toLowerCase(Locale.ROOT).contains(q)
                || s.getGenre().toLowerCase(Locale.ROOT).contains(q);
    }

    private void select() {
        int r = table.getSelectedRow();
        if (r >= 0 && r < visibleSongs.size()) {
            current = visibleSongs.get(r);
            doubleList.setIndex(library.all().indexOf(current));
            paintSong(current);
        }
    }

    private void playOrPause() {
        if (audio.isPlaying()) {
            audio.pause();
            play.setText("Play");
            status.setText("Pausado");
        } else if (audio.isPaused()) {
            audio.resume();
            play.setText("Pausa");
            status.setText(audio.getMessage());
        } else {
            playCurrent();
        }
    }

    private void playCurrent() {
        playSong(current);
    }

    private void playSong(Song s) {
        if (s == null) {
            status.setText("Selecciona una cancion");
            return;
        }
        current = s;
        current.addPlay();
        history.push(s);
        paintSong(s);
        boolean ok = audio.play(s);
        play.setText(ok ? "Pausa" : "Play");
        status.setText(audio.getMessage());
        refreshRowPlays();
        if (!ok) {
            JOptionPane.showMessageDialog(this, audio.getMessage());
        }
    }

    private void nextSong() {
        if (shuffleMode && !library.all().isEmpty()) {
            List<Song> copy = new ArrayList<Song>(library.all());
            Collections.shuffle(copy);
            playSong(copy.get(0));
        } else {
            playSong(doubleList.next());
        }
    }

    private void stop() {
        audio.stop();
        play.setText("Play");
        status.setText("Detenido");
        progress.setValue(0);
    }

    private void tick() {
        progress.setValue(audio.progress());
        if (audio.hasClip()) {
            status.setText(audio.currentTime() + "  " + audio.getMessage());
        }
        if (audio.hasClip() && !audio.isPlaying() && !audio.isPaused() && progress.getValue() >= 99) {
            if (repeatCircular) {
                playSong(circularList.nextCircular());
            } else if (shuffleMode) {
                nextSong();
            } else {
                play.setText("Play");
            }
        }
    }

    private void searchSong() {
        String q = search.getText().trim();
        if (q.length() == 0 || q.startsWith("Buscar por")) {
            JOptionPane.showMessageDialog(this, "Escribe el nombre exacto de una cancion para buscar en ABB/AVL.");
            return;
        }
        Song s = avl.searchAvl(q);
        if (s == null) {
            s = abb.search(q);
        }
        if (s == null) {
            JOptionPane.showMessageDialog(this, "No encontrada en ABB/AVL.");
        } else {
            playSong(s);
        }
    }

    private void loadFolder() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Elige la carpeta donde esta tu musica");
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            int count = addFolder(fc.getSelectedFile());
            refresh();
            JOptionPane.showMessageDialog(this, "Se cargaron " + count + " canciones compatibles.\nFormatos: WAV, AIFF y AU. Las portadas se toman de jpg/png con el mismo nombre, cover, folder o front.");
        }
    }

    private void loadFiles() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Elige canciones compatibles");
        fc.setMultiSelectionEnabled(true);
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            int count = 0;
            for (File f : fc.getSelectedFiles()) {
                if (isAudio(f)) {
                    add(songFromFile(f));
                    count++;
                }
            }
            refresh();
            JOptionPane.showMessageDialog(this, "Se cargaron " + count + " canciones.");
        }
    }

    private void loadZip() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Elige un ZIP con musica");
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File zip = fc.getSelectedFile();
            if (!zip.getName().toLowerCase(Locale.ROOT).endsWith(".zip")) {
                JOptionPane.showMessageDialog(this, "Selecciona un archivo .zip.");
                return;
            }
            try {
                File extracted = extractZip(zip);
                int count = addFolder(extracted);
                refresh();
                JOptionPane.showMessageDialog(this, "ZIP cargado: " + count + " canciones encontradas.\nSe aceptan MP3, MPEG, WAV, AIFF y AU.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "No se pudo cargar el ZIP: " + ex.getMessage());
            }
        }
    }

    private File extractZip(File zip) throws Exception {
        File dir = Files.createTempDirectory("spotify_musica_").toFile();
        ZipInputStream zin = new ZipInputStream(Files.newInputStream(zip.toPath()));
        ZipEntry entry;
        byte[] buffer = new byte[8192];
        while ((entry = zin.getNextEntry()) != null) {
            File out = new File(dir, entry.getName());
            String root = dir.getCanonicalPath() + File.separator;
            String target = out.getCanonicalPath();
            if (!target.startsWith(root)) {
                throw new IllegalArgumentException("ZIP invalido");
            }
            if (entry.isDirectory()) {
                out.mkdirs();
            } else {
                out.getParentFile().mkdirs();
                FileOutputStream fout = new FileOutputStream(out);
                int read;
                while ((read = zin.read(buffer)) >= 0) {
                    fout.write(buffer, 0, read);
                }
                fout.close();
            }
            zin.closeEntry();
        }
        zin.close();
        return dir;
    }

    private int addFolder(File folder) {
        int count = 0;
        File[] files = folder.listFiles();
        if (files == null) {
            return 0;
        }
        for (File f : files) {
            if (f.isDirectory()) {
                count += addFolder(f);
            } else if (isAudio(f)) {
                add(songFromFile(f));
                count++;
            }
        }
        return count;
    }

    private Song songFromFile(File f) {
        String base = stripExtension(f.getName());
        int seconds = durationSeconds(f);
        String coverPath = findCover(f);
        return new Song(base, "Archivo local", f.getParentFile().getName(), extension(f).toUpperCase(Locale.ROOT), seconds, f.getAbsolutePath(), coverPath);
    }

    private int durationSeconds(File f) {
        int parsed = durationFromName(f.getName());
        if (parsed > 0) {
            return parsed;
        }
        try {
            long micros = AudioSystem.getAudioFileFormat(f).getFrameLength() * 1000000L / (long) AudioSystem.getAudioFileFormat(f).getFormat().getFrameRate();
            return Math.max(1, (int) (micros / 1000000L));
        } catch (Exception ex) {
            return 180;
        }
    }

    private int durationFromName(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        int m = lower.indexOf('m');
        int s = lower.indexOf('s', m + 1);
        if (m > 0 && s > m) {
            try {
                int start = m - 1;
                while (start > 0 && Character.isDigit(lower.charAt(start - 1))) {
                    start--;
                }
                int minutes = Integer.parseInt(lower.substring(start, m));
                int seconds = Integer.parseInt(lower.substring(m + 1, s));
                return minutes * 60 + seconds;
            } catch (Exception ex) {
                return 0;
            }
        }
        return 0;
    }

    private String findCover(File audioFile) {
        File dir = audioFile.getParentFile();
        String base = stripExtension(audioFile.getName()).toLowerCase(Locale.ROOT);
        File[] files = dir.listFiles();
        if (files == null) {
            return null;
        }
        for (File f : files) {
            String name = stripExtension(f.getName()).toLowerCase(Locale.ROOT);
            if (isImage(f) && name.equals(base)) {
                return f.getAbsolutePath();
            }
        }
        for (File f : files) {
            String name = stripExtension(f.getName()).toLowerCase(Locale.ROOT);
            if (isImage(f) && (name.equals("cover") || name.equals("folder") || name.equals("front") || name.equals("portada"))) {
                return f.getAbsolutePath();
            }
        }
        return null;
    }

    private boolean isAudio(File f) {
        String e = extension(f);
        return e.equals("mp3") || e.equals("mpeg") || e.equals("wav") || e.equals("aiff") || e.equals("aif") || e.equals("au");
    }

    private boolean isImage(File f) {
        String e = extension(f);
        return e.equals("jpg") || e.equals("jpeg") || e.equals("png") || e.equals("gif");
    }

    private void paintSong(Song s) {
        now.setText(s.getTitle());
        sub.setText(s.getArtist() + " - " + s.getAlbum() + " - " + s.getGenre());
        cover.setIcon(new ImageIcon(loadCover(s, 76)));
    }

    private Image loadCover(Song s, int size) {
        try {
            if (s.getCoverPath() != null) {
                BufferedImage img = ImageIO.read(new File(s.getCoverPath()));
                if (img != null) {
                    return img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
                }
            }
        } catch (Exception ex) {
            status.setText("No se pudo abrir portada");
        }
        String initials = s.getTitle().length() >= 2 ? s.getTitle().substring(0, 2).toUpperCase(Locale.ROOT) : s.getTitle().toUpperCase(Locale.ROOT);
        return defaultCover(initials, size);
    }

    private Image defaultCover(String text, int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(new GradientPaint(0, 0, new Color(30, 215, 96), size, size, new Color(45, 45, 45)));
        g.fillRect(0, 0, size, size);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI", Font.BOLD, Math.max(20, size / 3)));
        int w = g.getFontMetrics().stringWidth(text);
        g.drawString(text, (size - w) / 2, size / 2 + g.getFontMetrics().getAscent() / 3);
        g.dispose();
        return img;
    }

    private void enqueueSelected() {
        if (current != null) {
            queue.enqueue(current);
            status.setText("Agregada a cola: " + current.getTitle());
        }
    }

    private void showHome() {
        clearSearch();
        status.setText("Inicio listo");
    }

    private void clearSearch() {
        search.setText("");
        refresh();
    }

    private void showLibrary() {
        JOptionPane.showMessageDialog(this, "Lista simple - Biblioteca:\n" + lines(library.all()));
    }

    private void showQueue() {
        JOptionPane.showMessageDialog(this, "Cola de reproduccion:\n" + lines(queue.all()));
    }

    private void showHistory() {
        JOptionPane.showMessageDialog(this, "Pila historial:\n" + lines(history.all()));
    }

    private String lines(List<Song> songs) {
        StringBuilder sb = new StringBuilder();
        for (Song s : songs) {
            sb.append(s).append(" (").append(s.duration()).append(")").append("\n");
        }
        return sb.length() == 0 ? "Sin canciones" : sb.toString();
    }

    private void showStats() {
        int total = library.all().size();
        int seconds = 0;
        Song top = null;
        for (Song s : library.all()) {
            seconds += s.getSeconds();
            if (top == null || s.getPlays() > top.getPlays()) {
                top = s;
            }
        }
        JOptionPane.showMessageDialog(this,
                "Estadisticas\nCanciones: " + total
                        + "\nDuracion total: " + (seconds / 60) + " min"
                        + "\nDuracion promedio: " + (total == 0 ? 0 : seconds / total) + "s"
                        + "\nMas reproducida: " + (top == null ? "-" : top + " (" + top.getPlays() + ")"));
    }

    private void exportPlaylist() {
        try {
            File out = new File("playlist_exportada.txt");
            FileWriter fw = new FileWriter(out);
            fw.write(lines(library.all()));
            fw.close();
            JOptionPane.showMessageDialog(this, "Playlist exportada en: " + out.getAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void encryptDialog() {
        String text = lines(library.all());
        String enc = caesar(text, 3);
        JOptionPane.showMessageDialog(this, "Encriptado:\n" + enc + "\n\nDesencriptado:\n" + caesar(enc, -3));
    }

    private String caesar(String text, int shift) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            sb.append((char) (c + shift));
        }
        return sb.toString();
    }

    private void toggleCircular() {
        repeatCircular = !repeatCircular;
        repeat.setText(repeatCircular ? "Repetir: si" : "Repetir: no");
        status.setText(repeatCircular ? "Lista circular activada" : "Lista circular desactivada");
    }

    private void toggleShuffle() {
        shuffleMode = !shuffleMode;
        shuffle.setText(shuffleMode ? "Aleatorio: si" : "Aleatorio");
        status.setText(shuffleMode ? "Aleatorio activado" : "Aleatorio desactivado");
    }

    private void refreshRowPlays() {
        for (int i = 0; i < visibleSongs.size(); i++) {
            model.setValueAt(visibleSongs.get(i).getPlays(), i, 6);
        }
    }

    private String stripExtension(String name) {
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }

    private String extension(File f) {
        String name = f.getName().toLowerCase(Locale.ROOT);
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(dot + 1) : "";
    }

    private JLabel title(String t, int size, Color color) {
        JLabel l = new JLabel(t);
        l.setForeground(color);
        l.setFont(new Font("Segoe UI", Font.BOLD, size));
        return l;
    }

    private JButton button(String text, java.awt.event.ActionListener a) {
        JButton b = new JButton(text);
        b.addActionListener(a);
        styleButton(b, false);
        return b;
    }

    private void styleButton(JButton b, boolean primary) {
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBackground(primary ? new Color(30, 215, 96) : new Color(32, 32, 32));
        b.setForeground(primary ? Color.BLACK : Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBorder(BorderFactory.createEmptyBorder(9, 10, 9, 10));
    }
}
