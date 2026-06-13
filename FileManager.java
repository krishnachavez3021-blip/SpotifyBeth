package smartplayer.utils;

import java.io.File;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import smartplayer.models.Song;
import smartplayer.structures.ListaSimple;
import smartplayer.structures.Nodo;
import smartplayer.models.Playlist;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FileManager {

    /** Interfaz funcional para reportar progreso de escaneo. */
    @FunctionalInterface
    public interface ProgresoCallback {
        /** Se llama cada vez que se procesa un archivo. nombreArchivo = nombre corto del MP3. */
        void onArchivoProcesado(String nombreArchivo, int totalHastaAhora);
    }

    public static ListaSimple scanDirectory(File directory) {
        ListaSimple canciones = new ListaSimple();
        scanDirectoryRec(directory, canciones);
        return canciones;
    }

    /**
     * Escanea un directorio de forma recursiva reportando progreso.
     * Nunca lanza excepción — los archivos con error se saltan y se loguean.
     */
    public static ListaSimple scanDirectoryConProgreso(File directory, ProgresoCallback callback) {
        ListaSimple canciones = new ListaSimple();
        int[] totalProcesados = {0};
        scanDirectoryRecConProgreso(directory, canciones, callback, totalProcesados);
        return canciones;
    }

    private static void scanDirectoryRecConProgreso(File directory, ListaSimple canciones,
                                                     ProgresoCallback callback, int[] totalProcesados) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) return;

        File[] files = directory.listFiles();
        if (files == null) return;

        // Ordenar para que el escaneo sea determinista
        java.util.Arrays.sort(files, (a, b) -> {
            if (a.isDirectory() && !b.isDirectory()) return 1;
            if (!a.isDirectory() && b.isDirectory()) return -1;
            return a.getName().compareToIgnoreCase(b.getName());
        });

        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectoryRecConProgreso(file, canciones, callback, totalProcesados);
            } else if (file.getName().toLowerCase().endsWith(".mp3")) {
                Song song = createSongFromFile(file);
                // createSongFromFile nunca retorna null — siempre crea al menos un fallback
                if (song != null) {
                    canciones.insertar(song);
                    totalProcesados[0]++;
                    if (callback != null) {
                        callback.onArchivoProcesado(file.getName(), totalProcesados[0]);
                    }
                }
            }
        }
    }

    /** Limpia el nombre del archivo para usarlo como título. */
    private static String limpiarNombreArchivo(String nombre) {
        if (nombre == null) return "Desconocido";
        // Quitar extensión
        String sin = nombre.replaceAll("(?i)\\.mp3$", "").trim();
        // Si tiene "Artista - Titulo", usar solo la parte derecha
        if (sin.contains(" - ")) {
            sin = sin.substring(sin.lastIndexOf(" - ") + 3).trim();
        }
        return sin.isEmpty() ? nombre : sin;
    }

    private static void scanDirectoryRec(File directory, ListaSimple canciones) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) return;
        File[] files = directory.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectoryRec(file, canciones);
            } else if (file.getName().toLowerCase().endsWith(".mp3")) {
                Song song = createSongFromFile(file);
                if (song != null) canciones.insertar(song);
            }
        }
    }

    private static Song createSongFromFile(File file) {
        // Intento 1: parseo completo con mp3agic
        try {
            Mp3File mp3file = new Mp3File(file.getAbsolutePath());
            String title  = limpiarNombreArchivo(file.getName());
            String artist = "Desconocido";
            String album  = "Desconocido";
            String genre  = "Desconocido";
            String year   = "Desconocido";

            if (mp3file.hasId3v2Tag()) {
                ID3v2 tag = mp3file.getId3v2Tag();
                if (tag.getTitle()            != null && !tag.getTitle().trim().isEmpty())            title  = tag.getTitle().trim();
                if (tag.getArtist()           != null && !tag.getArtist().trim().isEmpty())           artist = tag.getArtist().trim();
                if (tag.getAlbum()            != null && !tag.getAlbum().trim().isEmpty())            album  = tag.getAlbum().trim();
                if (tag.getGenreDescription() != null && !tag.getGenreDescription().trim().isEmpty()) genre  = tag.getGenreDescription().trim();
                if (tag.getYear()             != null && !tag.getYear().trim().isEmpty())             year   = tag.getYear().trim();
            } else if (mp3file.hasId3v1Tag()) {
                ID3v1 tag = mp3file.getId3v1Tag();
                if (tag.getTitle()            != null && !tag.getTitle().trim().isEmpty())            title  = tag.getTitle().trim();
                if (tag.getArtist()           != null && !tag.getArtist().trim().isEmpty())           artist = tag.getArtist().trim();
                if (tag.getAlbum()            != null && !tag.getAlbum().trim().isEmpty())            album  = tag.getAlbum().trim();
                if (tag.getGenreDescription() != null && !tag.getGenreDescription().trim().isEmpty()) genre  = tag.getGenreDescription().trim();
                if (tag.getYear()             != null && !tag.getYear().trim().isEmpty())             year   = tag.getYear().trim();
            }

            long duration = mp3file.getLengthInSeconds();
            return new Song(title, artist, album, genre, duration, file.length(), file.getAbsolutePath(), year);

        } catch (Exception e) {
            System.err.println("[FileManager] mp3agic falló para: " + file.getName() + " — " + e.getMessage());
        }

        // Intento 2: mp3agic con modo lenient (ignora errores de frame)
        try {
            Mp3File mp3file = new Mp3File(file.getAbsolutePath(), false); // false = no lanzar excepcion en frames corruptos
            String title  = limpiarNombreArchivo(file.getName());
            String artist = "Desconocido";
            String album  = "Desconocido";
            String genre  = "Desconocido";
            String year   = "Desconocido";

            if (mp3file.hasId3v2Tag()) {
                ID3v2 tag = mp3file.getId3v2Tag();
                if (tag.getTitle()  != null && !tag.getTitle().trim().isEmpty())  title  = tag.getTitle().trim();
                if (tag.getArtist() != null && !tag.getArtist().trim().isEmpty()) artist = tag.getArtist().trim();
                if (tag.getAlbum()  != null && !tag.getAlbum().trim().isEmpty())  album  = tag.getAlbum().trim();
            } else if (mp3file.hasId3v1Tag()) {
                ID3v1 tag = mp3file.getId3v1Tag();
                if (tag.getTitle()  != null && !tag.getTitle().trim().isEmpty())  title  = tag.getTitle().trim();
                if (tag.getArtist() != null && !tag.getArtist().trim().isEmpty()) artist = tag.getArtist().trim();
                if (tag.getAlbum()  != null && !tag.getAlbum().trim().isEmpty())  album  = tag.getAlbum().trim();
            }

            long duration = mp3file.getLengthInSeconds();
            System.out.println("[FileManager] Intento 2 OK: " + title);
            return new Song(title, artist, album, genre, duration, file.length(), file.getAbsolutePath(), year);

        } catch (Exception e2) {
            System.err.println("[FileManager] Intento 2 también falló: " + file.getName());
        }

        // Intento 3: sin mp3agic — extraer metadatos del nombre del archivo
        // y estimar duración por tamaño (bitrate 128kbps típico)
        return crearSongDesdNombreArchivo(file);
    }

    /**
     * Crea un Song desde el nombre del archivo cuando mp3agic no puede leerlo.
     * Extrae artista y título del patrón "Artista - Título.mp3".
     * Estima la duración con: duración ≈ tamaño_bytes / (128000 / 8) segundos.
     */
    private static Song crearSongDesdNombreArchivo(File file) {
        String nombre = file.getName().replaceAll("(?i)\\.mp3$", "").trim();
        String title  = nombre;
        String artist = "Desconocido";

        // Patrón más común: "Artista - Título" o "Artista, Artista2 - Título"
        if (nombre.contains(" - ")) {
            int idx = nombre.indexOf(" - ");
            String parteIzq = nombre.substring(0, idx).trim();
            String parteDer = nombre.substring(idx + 3).trim();
            if (!parteIzq.isEmpty() && !parteDer.isEmpty()) {
                artist = parteIzq;
                title  = parteDer;
            }
        }

        // Estimar duración: bitrate promedio 128 kbps = 16000 bytes/seg
        long sizeBytes = file.length();
        long duracionEstimada = sizeBytes > 0 ? sizeBytes / 16000L : 0L;

        System.out.println("[FileManager] Fallback nombre para: " + title + " (" + duracionEstimada + "s estimados)");
        return new Song(title, artist, "Desconocido", "Desconocido",
                        duracionEstimada, sizeBytes, file.getAbsolutePath(), "Desconocido");
    }

    public static void savePlaylist(Playlist playlist, String filename) {
        try (PrintWriter out = new PrintWriter(new FileWriter(filename))) {
            out.println(playlist.getNombre());
            Nodo actual = playlist.getCanciones().getCabeza();
            while (actual != null) {
                out.println(actual.song.getPath());
                actual = actual.siguiente;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static Playlist loadPlaylist(String filename, ListaSimple biblioteca) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String nombre = br.readLine();
            if (nombre == null) return null;
            
            Playlist playlist = new Playlist(nombre);
            String path;
            while ((path = br.readLine()) != null) {
                Nodo actual = biblioteca.getCabeza();
                while(actual != null) {
                    if (actual.song.getPath().equals(path)) {
                        playlist.getCanciones().insertar(actual.song);
                        break;
                    }
                    actual = actual.siguiente;
                }
            }
            return playlist;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
