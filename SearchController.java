package smartplayer.controllers;

import smartplayer.models.Song;
import smartplayer.structures.ListaSimple;
import smartplayer.structures.Nodo;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador de búsqueda avanzada.
 * Soporta búsqueda por múltiples campos, filtros combinados,
 * y mantiene un historial de las últimas 20 búsquedas.
 */
public class SearchController {
    private static final int MAX_HISTORIAL = 20;
    private static final String HISTORIAL_FILE = "smartplayer_search_history.txt";
    
    private List<String> historialBusquedas;

    public SearchController() {
        historialBusquedas = new ArrayList<>();
        cargarHistorial();
    }

    /**
     * Búsqueda general en todos los campos de la canción.
     */
    public ListaSimple buscarGeneral(ListaSimple biblioteca, String query) {
        ListaSimple resultados = new ListaSimple();
        if (query == null || query.trim().isEmpty()) return resultados;
        
        String queryLower = query.toLowerCase().trim();
        Nodo actual = biblioteca.getCabeza();
        
        while (actual != null) {
            Song s = actual.song;
            if (s.getTitle().toLowerCase().contains(queryLower) ||
                s.getArtist().toLowerCase().contains(queryLower) ||
                s.getAlbum().toLowerCase().contains(queryLower) ||
                s.getGenre().toLowerCase().contains(queryLower) ||
                s.getYear().toLowerCase().contains(queryLower) ||
                s.getDurationFormatted().contains(queryLower)) {
                resultados.insertar(s);
            }
            actual = actual.siguiente;
        }
        return resultados;
    }

    /**
     * Búsqueda por campo específico.
     * @param campo puede ser: "titulo", "artista", "album", "genero", "año", "duracion"
     */
    public ListaSimple buscarPorCampo(ListaSimple biblioteca, String query, String campo) {
        ListaSimple resultados = new ListaSimple();
        if (query == null || query.trim().isEmpty()) return resultados;
        
        String queryLower = query.toLowerCase().trim();
        Nodo actual = biblioteca.getCabeza();
        
        while (actual != null) {
            Song s = actual.song;
            boolean match = false;
            
            switch (campo.toLowerCase()) {
                case "titulo":
                    match = s.getTitle().toLowerCase().contains(queryLower);
                    break;
                case "artista":
                    match = s.getArtist().toLowerCase().contains(queryLower);
                    break;
                case "album":
                case "álbum":
                    match = s.getAlbum().toLowerCase().contains(queryLower);
                    break;
                case "genero":
                case "género":
                    match = s.getGenre().toLowerCase().contains(queryLower);
                    break;
                case "año":
                case "year":
                    match = s.getYear().toLowerCase().contains(queryLower);
                    break;
                case "duracion":
                case "duración":
                    match = s.getDurationFormatted().contains(queryLower);
                    break;
                default:
                    match = s.getTitle().toLowerCase().contains(queryLower);
                    break;
            }
            
            if (match) {
                resultados.insertar(s);
            }
            actual = actual.siguiente;
        }
        return resultados;
    }

    /**
     * Búsqueda con múltiples filtros combinados (AND lógico).
     * @param filtros arreglo de pares [campo, valor]
     */
    public ListaSimple buscarMultiFiltro(ListaSimple biblioteca, String[][] filtros) {
        ListaSimple resultados = new ListaSimple();
        if (filtros == null || filtros.length == 0) return resultados;
        
        Nodo actual = biblioteca.getCabeza();
        while (actual != null) {
            Song s = actual.song;
            boolean matchAll = true;
            
            for (String[] filtro : filtros) {
                if (filtro.length < 2 || filtro[1] == null || filtro[1].trim().isEmpty()) continue;
                
                String campo = filtro[0];
                String valor = filtro[1].toLowerCase().trim();
                
                boolean matchCampo = false;
                switch (campo.toLowerCase()) {
                    case "titulo": matchCampo = s.getTitle().toLowerCase().contains(valor); break;
                    case "artista": matchCampo = s.getArtist().toLowerCase().contains(valor); break;
                    case "album": matchCampo = s.getAlbum().toLowerCase().contains(valor); break;
                    case "genero": matchCampo = s.getGenre().toLowerCase().contains(valor); break;
                    case "año": matchCampo = s.getYear().toLowerCase().contains(valor); break;
                    default: matchCampo = true; break;
                }
                
                if (!matchCampo) {
                    matchAll = false;
                    break;
                }
            }
            
            if (matchAll) {
                resultados.insertar(s);
            }
            actual = actual.siguiente;
        }
        return resultados;
    }

    /**
     * Agrega una búsqueda al historial (máximo 20).
     */
    public void agregarAlHistorial(String query) {
        if (query == null || query.trim().isEmpty()) return;
        
        // Eliminar si ya existe para moverla al frente
        historialBusquedas.remove(query.trim());
        
        // Agregar al inicio
        historialBusquedas.add(0, query.trim());
        
        // Limitar a MAX_HISTORIAL
        while (historialBusquedas.size() > MAX_HISTORIAL) {
            historialBusquedas.remove(historialBusquedas.size() - 1);
        }
        
        guardarHistorial();
    }

    /**
     * Obtiene el historial de búsquedas recientes.
     */
    public List<String> getHistorial() {
        return historialBusquedas;
    }

    /**
     * Limpia todo el historial de búsquedas.
     */
    public void limpiarHistorial() {
        historialBusquedas.clear();
        guardarHistorial();
    }

    /**
     * Guarda el historial en archivo.
     */
    private void guardarHistorial() {
        try (PrintWriter out = new PrintWriter(new FileWriter(HISTORIAL_FILE))) {
            for (String query : historialBusquedas) {
                out.println(query);
            }
        } catch (IOException e) {
            // Silenciar error de IO
        }
    }

    /**
     * Carga el historial desde archivo.
     */
    private void cargarHistorial() {
        File file = new File(HISTORIAL_FILE);
        if (!file.exists()) return;
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null && historialBusquedas.size() < MAX_HISTORIAL) {
                if (!line.trim().isEmpty()) {
                    historialBusquedas.add(line.trim());
                }
            }
        } catch (IOException e) {
            // Silenciar error de IO
        }
    }
}
