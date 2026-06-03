package smartplayer;

import java.io.*;
import java.util.*;

/** Carga canciones desde carpetas, extrae metadata básica */
public class MusicLoader {

    private static final String[] EXTENSIONS = {".mp3", ".wav", ".flac", ".ogg", ".m4a", ".aac", ".wma"};

    public interface ProgressCallback {
        void onProgress(int found, String currentFile);
    }

    /** Recorre carpeta y subcarpetas recursivamente */
    public static List<Song> loadFromDirectory(File dir, ProgressCallback callback) {
        List<Song> songs = new ArrayList<>();
        if (dir == null || !dir.exists() || !dir.isDirectory()) return songs;
        scanDirectory(dir, songs, callback);
        return songs;
    }

    private static void scanDirectory(File dir, List<Song> songs, ProgressCallback cb) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                scanDirectory(f, songs, cb);
            } else if (isMusicFile(f)) {
                Song s = extractMetadata(f);
                if (s != null) {
                    songs.add(s);
                    if (cb != null) cb.onProgress(songs.size(), f.getName());
                }
            }
        }
    }

    private static boolean isMusicFile(File f) {
        String name = f.getName().toLowerCase();
        for (String ext : EXTENSIONS) if (name.endsWith(ext)) return true;
        return false;
    }

    private static Song extractMetadata(File f) {
        try {
            String name = f.getName();
            // Quita extensión
            String noExt = name.contains(".") ? name.substring(0, name.lastIndexOf('.')) : name;

            String title, artist = "Desconocido", album = "Desconocido";
            String genre = detectGenreFromPath(f);
            int year = detectYearFromPath(f);

            // Intenta parsear "Artista - Titulo" o solo "Titulo"
            if (noExt.contains(" - ")) {
                String[] parts = noExt.split(" - ", 2);
                artist = parts[0].trim();
                title  = parts[1].trim();
            } else {
                title = noExt.trim();
            }

            // Intenta usar nombre de carpeta padre como álbum
            File parent = f.getParentFile();
            if (parent != null) {
                album = parent.getName();
                // Si hay dos niveles: artista/album/cancion
                File gp = parent.getParentFile();
                if (gp != null && !gp.getName().isEmpty()) {
                    if (artist.equals("Desconocido")) artist = gp.getName();
                }
            }

            long fileSize = f.length();
            // Duración estimada (3 MB ≈ 3 min para MP3 128kbps)
            long duration = estimateDuration(f, fileSize);

            return new Song(title, artist, album, genre, year, duration, fileSize, f.getAbsolutePath());
        } catch (Exception e) {
            return null;
        }
    }

    private static String detectGenreFromPath(File f) {
        String path = f.getAbsolutePath().toLowerCase();
        String[] genres = {"rock", "pop", "jazz", "classical", "hiphop", "hip-hop",
                           "electronic", "reggaeton", "salsa", "cumbia", "latin",
                           "blues", "country", "metal", "r&b", "soul", "folk"};
        for (String g : genres) if (path.contains(g)) return capitalize(g);
        return "Desconocido";
    }

    private static int detectYearFromPath(File f) {
        String path = f.getAbsolutePath();
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(19|20)\\d{2}").matcher(path);
        if (m.find()) { try { return Integer.parseInt(m.group()); } catch (Exception ignored) {} }
        return 0;
    }

    private static long estimateDuration(File f, long size) {
        String name = f.getName().toLowerCase();
        if (name.endsWith(".mp3")) return size / (128 * 1024 / 8);      // 128 kbps
        if (name.endsWith(".flac")) return size / (700 * 1024 / 8);     // ~700 kbps
        if (name.endsWith(".wav"))  return size / (1411 * 1024 / 8);    // PCM 44.1k
        return size / (192 * 1024 / 8);                                   // default
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    // ── Generar canciones de demo cuando no hay archivos ──────────────
    public static List<Song> generateDemoSongs(int count) {
        String[] artists = {"The Beatles","Queen","Michael Jackson","Adele","Bad Bunny",
                            "Taylor Swift","Coldplay","Eminem","Shakira","Rihanna",
                            "Ed Sheeran","Billie Eilish","Post Malone","Drake","BTS"};
        String[] albums  = {"Greatest Hits","World Tour","Anthology","Classics","New Era",
                            "Platinum","Gold Edition","Black Album","Revolution","Legacy"};
        String[] genres  = {"Rock","Pop","Hip-Hop","Latin","Electronic","R&B","Jazz","Classical","Reggaeton","Country"};
        String[] titles  = {"Yesterday","Bohemian Rhapsody","Thriller","Hello","Dákiti",
                            "Shape of You","Fix You","Lose Yourself","Hips Don't Lie",
                            "We Found Love","Perfect","Bad Guy","Rockstar","God's Plan","Dynamite",
                            "Let It Be","Don't Stop Me Now","Beat It","Rolling in the Deep","Tití Me Preguntó"};

        List<Song> songs = new ArrayList<>();
        Random rnd = new Random(42);

        for (int i = 0; i < count; i++) {
            String title  = titles[i % titles.length] + (i < titles.length ? "" : " " + (i/titles.length+1));
            String artist = artists[rnd.nextInt(artists.length)];
            String album  = albums[rnd.nextInt(albums.length)];
            String genre  = genres[rnd.nextInt(genres.length)];
            int    year   = 1990 + rnd.nextInt(35);
            long   dur    = 120 + rnd.nextInt(180);   // 2–5 min
            long   size   = dur * 128 * 1024 / 8;     // estimado MP3
            songs.add(new Song(title, artist, album, genre, year, dur, size,
                               "/demo/music/" + artist + "/" + album + "/" + title + ".mp3"));
        }
        return songs;
    }
}
