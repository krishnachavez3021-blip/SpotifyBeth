package smartplayer.controllers;

import smartplayer.models.Song;
import smartplayer.structures.ArbolABB;
import smartplayer.structures.ArbolAVL;
import smartplayer.structures.ListaSimple;
import smartplayer.structures.ListaDoble;
import smartplayer.structures.Nodo;
import smartplayer.utils.FileManager;
import java.io.File;

public class LibraryController {
    private ListaSimple biblioteca;
    private ArbolABB arbolABB;
    private ArbolAVL arbolAVL;
    private long lastSearchTimeABB;
    private long lastSearchTimeAVL;

    public LibraryController() {
        biblioteca = new ListaSimple();
        arbolABB = new ArbolABB();
        arbolAVL = new ArbolAVL();
        lastSearchTimeABB = 0;
        lastSearchTimeAVL = 0;
    }

    public void importarCarpeta(File folder) {
        biblioteca = FileManager.scanDirectory(folder);
        arbolABB = new ArbolABB();
        arbolAVL = new ArbolAVL();
        
        smartplayer.structures.Nodo actual = biblioteca.getCabeza();
        while (actual != null) {
            arbolABB.insertar(actual.song);
            arbolAVL.insertar(actual.song);
            actual = actual.siguiente;
        }
    }

    /**
     * Recibe una lista ya escaneada (desde SwingWorker en segundo plano)
     * y la establece como la biblioteca, reconstruyendo los árboles.
     * Se llama desde el hilo de fondo — los árboles se reconstruyen aquí
     * para no bloquear el EDT.
     */
    public void importarDesdeListaEscaneada(ListaSimple lista) {
        biblioteca = lista;
        arbolABB = new ArbolABB();
        arbolAVL = new ArbolAVL();
        Nodo actual = biblioteca.getCabeza();
        while (actual != null) {
            arbolABB.insertar(actual.song);
            arbolAVL.insertar(actual.song);
            actual = actual.siguiente;
        }
    }

    public ListaSimple getBiblioteca() {
        return biblioteca;
    }

    /**
     * Agrega una sola cancion a la biblioteca sin reemplazarla.
     * Evita duplicados por path. Actualiza arboles ABB y AVL.
     */
    public boolean agregarCancion(Song song) {
        if (song == null) return false;
        // Evitar duplicados por path exacto
        Nodo n = biblioteca.getCabeza();
        while (n != null) {
            if (n.song.getPath().equals(song.getPath())) return false; // ya existe
            n = n.siguiente;
        }
        biblioteca.insertar(song);
        arbolABB.insertar(song);
        arbolAVL.insertar(song);
        return true;
    }

    /**
     * Agrega todas las canciones de una lista a la biblioteca sin reemplazarla.
     * Retorna el numero de canciones nuevas agregadas.
     */
    public int agregarDesdeListaEscaneada(ListaSimple lista) {
        int agregadas = 0;
        Nodo actual = lista.getCabeza();
        while (actual != null) {
            if (agregarCancion(actual.song)) agregadas++;
            actual = actual.siguiente;
        }
        return agregadas;
    }

    public Song buscarEnABB(String titulo) {
        long startTime = System.nanoTime();
        Song song = arbolABB.buscar(titulo);
        long endTime = System.nanoTime();
        lastSearchTimeABB = endTime - startTime;
        return song;
    }

    public Song buscarEnAVL(String titulo) {
        long startTime = System.nanoTime();
        Song song = arbolAVL.buscar(titulo);
        long endTime = System.nanoTime();
        lastSearchTimeAVL = endTime - startTime;
        return song;
    }

    public long getLastSearchTimeABB() { return lastSearchTimeABB; }
    public long getLastSearchTimeAVL() { return lastSearchTimeAVL; }
    
    public ArbolABB getArbolABB() { return arbolABB; }
    public ArbolAVL getArbolAVL() { return arbolAVL; }

    /**
     * Construye y retorna una ListaDoble con todos las canciones de la biblioteca.
     * Se usa para la navegacion siguiente/anterior desde la vista de biblioteca.
     */
    public ListaDoble getListaDoble() {
        ListaDoble listaDoble = new ListaDoble();
        Nodo actual = biblioteca.getCabeza();
        while (actual != null) {
            listaDoble.insertar(actual.song);
            actual = actual.siguiente;
        }
        return listaDoble;
    }
}
