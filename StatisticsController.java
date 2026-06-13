package smartplayer.controllers;

import smartplayer.models.Song;
import smartplayer.structures.ListaSimple;
import smartplayer.structures.Nodo;
import smartplayer.structures.ListaPlaylists;
import smartplayer.structures.Pila;

public class StatisticsController {
    
    public static String getArtistaMasEscuchado(Pila historial) {
        if (historial.tamano == 0) return "N/A";
        
        String[] artistas = new String[historial.tamano];
        int[] conteos = new int[historial.tamano];
        int uniqueCount = 0;
        
        Nodo actual = historial.tope;
        while (actual != null) {
            String artista = actual.song.getArtist();
            boolean found = false;
            for (int i = 0; i < uniqueCount; i++) {
                if (artistas[i].equals(artista)) {
                    conteos[i]++;
                    found = true;
                    break;
                }
            }
            if (!found) {
                artistas[uniqueCount] = artista;
                conteos[uniqueCount] = 1;
                uniqueCount++;
            }
            actual = actual.siguiente;
        }
        
        int max = 0;
        String maxArtista = "N/A";
        for (int i = 0; i < uniqueCount; i++) {
            if (conteos[i] > max) {
                max = conteos[i];
                maxArtista = artistas[i];
            }
        }
        return maxArtista;
    }
    
    public static String getCancionMasReproducida(Pila historial) {
        if (historial.tamano == 0) return "N/A";
        
        String[] canciones = new String[historial.tamano];
        int[] conteos = new int[historial.tamano];
        int uniqueCount = 0;
        
        Nodo actual = historial.tope;
        while (actual != null) {
            String titulo = actual.song.getTitle();
            boolean found = false;
            for (int i = 0; i < uniqueCount; i++) {
                if (canciones[i].equals(titulo)) {
                    conteos[i]++;
                    found = true;
                    break;
                }
            }
            if (!found) {
                canciones[uniqueCount] = titulo;
                conteos[uniqueCount] = 1;
                uniqueCount++;
            }
            actual = actual.siguiente;
        }
        
        int max = 0;
        String maxCancion = "N/A";
        for (int i = 0; i < uniqueCount; i++) {
            if (conteos[i] > max) {
                max = conteos[i];
                maxCancion = canciones[i];
            }
        }
        return maxCancion;
    }

    public static String getPlaylistMasGrande(ListaPlaylists playlists) {
        if (playlists.tamano == 0) return "N/A";
        int max = -1;
        String maxName = "N/A";
        
        ListaPlaylists.NodoPlaylist actual = playlists.cabeza;
        while (actual != null) {
            if (actual.playlist.getCanciones().tamano > max) {
                max = actual.playlist.getCanciones().tamano;
                maxName = actual.playlist.getNombre();
            }
            actual = actual.siguiente;
        }
        return maxName + " (" + max + " canciones)";
    }
    
    public static String getGeneroMasFrecuente(ListaSimple biblioteca) {
        if (biblioteca.tamano == 0) return "N/A";
        
        String[] generos = new String[biblioteca.tamano];
        int[] conteos = new int[biblioteca.tamano];
        int uniqueCount = 0;
        
        Nodo actual = biblioteca.getCabeza();
        while (actual != null) {
            String genero = actual.song.getGenre();
            boolean found = false;
            for (int i = 0; i < uniqueCount; i++) {
                if (generos[i].equals(genero)) {
                    conteos[i]++;
                    found = true;
                    break;
                }
            }
            if (!found) {
                generos[uniqueCount] = genero;
                conteos[uniqueCount] = 1;
                uniqueCount++;
            }
            actual = actual.siguiente;
        }
        
        int max = 0;
        String maxGenero = "N/A";
        for (int i = 0; i < uniqueCount; i++) {
            if (conteos[i] > max) {
                max = conteos[i];
                maxGenero = generos[i];
            }
        }
        return maxGenero;
    }
    
    public static String getPromedioDuracion(ListaSimple biblioteca) {
        if (biblioteca.tamano == 0) return "0s";
        long total = 0;
        Nodo actual = biblioteca.getCabeza();
        while (actual != null) {
            total += actual.song.getDuration();
            actual = actual.siguiente;
        }
        long promedio = total / biblioteca.tamano;
        long min = promedio / 60;
        long sec = promedio % 60;
        return min + "m " + sec + "s";
    }
    
    public static String getTamanioTotal(ListaSimple biblioteca) {
        long totalBytes = 0;
        Nodo actual = biblioteca.getCabeza();
        while (actual != null) {
            totalBytes += actual.song.getSize();
            actual = actual.siguiente;
        }
        double mb = totalBytes / (1024.0 * 1024.0);
        return String.format("%.2f MB", mb);
    }
    
    public static int countArchivosDuplicados(ListaSimple biblioteca) {
        if (biblioteca.tamano == 0) return 0;
        int duplicados = 0;
        
        String[] rutas = new String[biblioteca.tamano];
        int index = 0;
        
        Nodo actual = biblioteca.getCabeza();
        while (actual != null) {
            String ruta = actual.song.getPath();
            boolean found = false;
            for (int i = 0; i < index; i++) {
                if (rutas[i].equals(ruta)) {
                    duplicados++;
                    found = true;
                    break;
                }
            }
            if (!found) {
                rutas[index++] = ruta;
            }
            actual = actual.siguiente;
        }
        
        return duplicados;
    }

    /**
     * Retorna el detalle de archivos duplicados: cuales son y que tamanio tienen.
     * Cada elemento del arreglo contiene: [titulo, artista, ruta, tamanioFormateado].
     * Solo se incluyen las copias duplicadas (no la primera aparicion).
     */
    public static String[][] getArchivosDuplicadosDetalle(ListaSimple biblioteca) {
        if (biblioteca.tamano == 0) return new String[0][0];

        // Primera pasada: identificar rutas que aparecen mas de una vez
        String[] todasRutas = new String[biblioteca.tamano];
        int[] conteoRuta = new int[biblioteca.tamano];
        int uniqueCount = 0;

        Nodo actual = biblioteca.getCabeza();
        while (actual != null) {
            String ruta = actual.song.getPath();
            boolean found = false;
            for (int i = 0; i < uniqueCount; i++) {
                if (todasRutas[i].equals(ruta)) {
                    conteoRuta[i]++;
                    found = true;
                    break;
                }
            }
            if (!found) {
                todasRutas[uniqueCount] = ruta;
                conteoRuta[uniqueCount] = 1;
                uniqueCount++;
            }
            actual = actual.siguiente;
        }

        // Contar cuantos duplicados hay en total
        int totalDuplicados = 0;
        for (int i = 0; i < uniqueCount; i++) {
            if (conteoRuta[i] > 1) {
                totalDuplicados += (conteoRuta[i] - 1); // solo las copias extra
            }
        }

        if (totalDuplicados == 0) return new String[0][0];

        // Segunda pasada: recolectar la info de cada duplicado
        String[][] resultado = new String[totalDuplicados][4];
        int resultIdx = 0;

        // Para saber si ya incluimos la primera aparicion usamos un marcador
        boolean[] primeraYaVista = new boolean[uniqueCount];

        actual = biblioteca.getCabeza();
        while (actual != null && resultIdx < totalDuplicados) {
            String ruta = actual.song.getPath();
            for (int i = 0; i < uniqueCount; i++) {
                if (todasRutas[i].equals(ruta) && conteoRuta[i] > 1) {
                    if (!primeraYaVista[i]) {
                        primeraYaVista[i] = true; // primera aparicion: no la marcamos como dup
                    } else {
                        // Esta es una copia duplicada
                        long bytes = actual.song.getSize();
                        String tamanio;
                        if (bytes >= 1024 * 1024) {
                            tamanio = String.format("%.2f MB", bytes / (1024.0 * 1024.0));
                        } else {
                            tamanio = String.format("%.1f KB", bytes / 1024.0);
                        }
                        resultado[resultIdx][0] = actual.song.getTitle();
                        resultado[resultIdx][1] = actual.song.getArtist();
                        resultado[resultIdx][2] = ruta;
                        resultado[resultIdx][3] = tamanio;
                        resultIdx++;
                    }
                    break;
                }
            }
            actual = actual.siguiente;
        }

        return resultado;
    }

    /**
     * Retorna el tamanio total ocupado por archivos duplicados (las copias extra).
     */
    public static String getTamanioTotalDuplicados(ListaSimple biblioteca) {
        String[][] detalle = getArchivosDuplicadosDetalle(biblioteca);
        if (detalle.length == 0) return "0 KB";

        long totalBytes = 0;
        Nodo actual = biblioteca.getCabeza();

        // Reconstruir el mapa ruta->tamanio
        java.util.HashMap<String, Long> rutaSize = new java.util.HashMap<>();
        actual = biblioteca.getCabeza();
        while (actual != null) {
            rutaSize.put(actual.song.getPath(), actual.song.getSize());
            actual = actual.siguiente;
        }

        for (String[] dup : detalle) {
            Long sz = rutaSize.get(dup[2]);
            if (sz != null) totalBytes += sz;
        }

        if (totalBytes >= 1024 * 1024) {
            return String.format("%.2f MB", totalBytes / (1024.0 * 1024.0));
        }
        return String.format("%.1f KB", totalBytes / 1024.0);
    }
}
