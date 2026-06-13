package smartplayer.utils;

import smartplayer.models.Song;
import smartplayer.structures.ListaSimple;
import smartplayer.structures.Nodo;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Gestor de estadísticas con persistencia entre sesiones.
 * Mantiene contadores de reproducción, historial con fechas,
 * y tiempo total de escucha.
 */
public class StatsManager {
    private static final String STATS_FILE = "smartplayer_stats.dat";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    // Contadores de reproducción: path -> conteo
    private Map<String, Integer> contadorReproducciones;
    // Historial completo: lista de [path, titulo, artista, fecha]
    private List<String[]> historialCompleto;
    // Tiempo total de escucha en segundos
    private long tiempoTotalEscucha;

    public StatsManager() {
        contadorReproducciones = new HashMap<>();
        historialCompleto = new ArrayList<>();
        tiempoTotalEscucha = 0;
        cargarEstadisticas();
    }

    /**
     * Registra una reproducción de canción.
     */
    public void registrarReproduccion(Song song) {
        if (song == null) return;
        
        String path = song.getPath();
        contadorReproducciones.put(path, contadorReproducciones.getOrDefault(path, 0) + 1);
        song.setPlayCount(contadorReproducciones.get(path));
        
        // Agregar al historial completo
        String fecha = DATE_FORMAT.format(new Date());
        historialCompleto.add(new String[]{path, song.getTitle(), song.getArtist(), fecha});
        
        guardarEstadisticas();
    }

    /**
     * Registra tiempo de escucha acumulado.
     */
    public void registrarTiempoEscucha(long segundos) {
        tiempoTotalEscucha += segundos;
    }

    /**
     * Obtiene el top 10 de canciones más reproducidas.
     * @return arreglo de [titulo, artista, conteo]
     */
    public String[][] getTop10Canciones(ListaSimple biblioteca) {
        // Crear lista ordenada por reproducciones
        List<Object[]> lista = new ArrayList<>();
        
        Nodo actual = biblioteca.getCabeza();
        while (actual != null) {
            String path = actual.song.getPath();
            int count = contadorReproducciones.getOrDefault(path, 0);
            if (count > 0) {
                lista.add(new Object[]{actual.song.getTitle(), actual.song.getArtist(), count});
            }
            actual = actual.siguiente;
        }
        
        // Ordenar por conteo descendente
        lista.sort((a, b) -> Integer.compare((int) b[2], (int) a[2]));
        
        int size = Math.min(10, lista.size());
        String[][] result = new String[size][3];
        for (int i = 0; i < size; i++) {
            result[i][0] = (String) lista.get(i)[0];
            result[i][1] = (String) lista.get(i)[1];
            result[i][2] = String.valueOf(lista.get(i)[2]);
        }
        return result;
    }

    /**
     * Obtiene el top 5 de artistas más escuchados.
     * @return arreglo de [artista, conteo]
     */
    public String[][] getTop5Artistas(ListaSimple biblioteca) {
        Map<String, Integer> artistaConteo = new HashMap<>();
        
        for (Map.Entry<String, Integer> entry : contadorReproducciones.entrySet()) {
            String path = entry.getKey();
            int count = entry.getValue();
            
            // Buscar artista por path
            Nodo actual = biblioteca.getCabeza();
            while (actual != null) {
                if (actual.song.getPath().equals(path)) {
                    String artista = actual.song.getArtist();
                    artistaConteo.put(artista, artistaConteo.getOrDefault(artista, 0) + count);
                    break;
                }
                actual = actual.siguiente;
            }
        }
        
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(artistaConteo.entrySet());
        sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        
        int size = Math.min(5, sorted.size());
        String[][] result = new String[size][2];
        for (int i = 0; i < size; i++) {
            result[i][0] = sorted.get(i).getKey();
            result[i][1] = String.valueOf(sorted.get(i).getValue());
        }
        return result;
    }

    /**
     * Obtiene el tiempo total de escucha formateado.
     */
    public String getTiempoTotalFormateado() {
        long horas = tiempoTotalEscucha / 3600;
        long minutos = (tiempoTotalEscucha % 3600) / 60;
        long segundos = tiempoTotalEscucha % 60;
        
        if (horas > 0) {
            return String.format("%dh %dm %ds", horas, minutos, segundos);
        } else if (minutos > 0) {
            return String.format("%dm %ds", minutos, segundos);
        }
        return String.format("%ds", segundos);
    }

    /**
     * Obtiene el historial completo de reproducciones.
     */
    public List<String[]> getHistorialCompleto() {
        return historialCompleto;
    }

    /**
     * Obtiene el total de reproducciones registradas.
     */
    public int getTotalReproducciones() {
        int total = 0;
        for (int count : contadorReproducciones.values()) {
            total += count;
        }
        return total;
    }

    /**
     * Sincroniza los contadores de reproducción con los objetos Song.
     */
    public void sincronizarConBiblioteca(ListaSimple biblioteca) {
        Nodo actual = biblioteca.getCabeza();
        while (actual != null) {
            int count = contadorReproducciones.getOrDefault(actual.song.getPath(), 0);
            actual.song.setPlayCount(count);
            actual = actual.siguiente;
        }
    }

    /**
     * Guarda las estadísticas en archivo.
     */
    public void guardarEstadisticas() {
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(STATS_FILE), "UTF-8"))) {
            // Línea 1: tiempo total
            out.println("TIEMPO:" + tiempoTotalEscucha);
            
            // Contadores
            out.println("CONTADORES:" + contadorReproducciones.size());
            for (Map.Entry<String, Integer> entry : contadorReproducciones.entrySet()) {
                out.println(entry.getKey() + "|" + entry.getValue());
            }
            
            // Historial (últimos 500 registros máximo)
            int histSize = Math.min(500, historialCompleto.size());
            out.println("HISTORIAL:" + histSize);
            int startIdx = historialCompleto.size() - histSize;
            for (int i = startIdx; i < historialCompleto.size(); i++) {
                String[] entry = historialCompleto.get(i);
                out.println(entry[0] + "|" + entry[1] + "|" + entry[2] + "|" + entry[3]);
            }
        } catch (IOException e) {
            System.err.println("Error guardando estadísticas: " + e.getMessage());
        }
    }

    /**
     * Carga las estadísticas desde archivo.
     */
    private void cargarEstadisticas() {
        File file = new File(STATS_FILE);
        if (!file.exists()) return;
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), "UTF-8"))) {
            String line;
            
            // Tiempo total
            line = br.readLine();
            if (line != null && line.startsWith("TIEMPO:")) {
                tiempoTotalEscucha = Long.parseLong(line.substring(7));
            }
            
            // Contadores
            line = br.readLine();
            if (line != null && line.startsWith("CONTADORES:")) {
                int count = Integer.parseInt(line.substring(11));
                for (int i = 0; i < count; i++) {
                    line = br.readLine();
                    if (line != null) {
                        int sep = line.lastIndexOf('|');
                        if (sep > 0) {
                            String path = line.substring(0, sep);
                            int valor = Integer.parseInt(line.substring(sep + 1));
                            contadorReproducciones.put(path, valor);
                        }
                    }
                }
            }
            
            // Historial
            line = br.readLine();
            if (line != null && line.startsWith("HISTORIAL:")) {
                int count = Integer.parseInt(line.substring(10));
                for (int i = 0; i < count; i++) {
                    line = br.readLine();
                    if (line != null) {
                        String[] parts = line.split("\\|", 4);
                        if (parts.length >= 4) {
                            historialCompleto.add(parts);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error cargando estadísticas: " + e.getMessage());
        }
    }

    public long getTiempoTotalEscucha() {
        return tiempoTotalEscucha;
    }

    public Map<String, Integer> getContadorReproducciones() {
        return contadorReproducciones;
    }
}
