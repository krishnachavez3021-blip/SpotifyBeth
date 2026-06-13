package smartplayer.structures;
import smartplayer.models.Song;

public class ArbolABB {
    public NodoArbol raiz;
    
    public ArbolABB() {
        raiz = null;
    }
    
    public void insertar(Song song) {
        raiz = insertarRec(raiz, song);
    }
    
    private NodoArbol insertarRec(NodoArbol raiz, Song song) {
        if (raiz == null) {
            return new NodoArbol(song);
        }
        if (song.getTitle().compareToIgnoreCase(raiz.song.getTitle()) < 0) {
            raiz.izquierdo = insertarRec(raiz.izquierdo, song);
        } else if (song.getTitle().compareToIgnoreCase(raiz.song.getTitle()) > 0) {
            raiz.derecho = insertarRec(raiz.derecho, song);
        } else {
            raiz.derecho = insertarRec(raiz.derecho, song);
        }
        return raiz;
    }
    
    public Song buscar(String titulo) {
        NodoArbol res = buscarRec(raiz, titulo);
        return res != null ? res.song : null;
    }
    
    private NodoArbol buscarRec(NodoArbol raiz, String titulo) {
        if (raiz == null || raiz.song.getTitle().equalsIgnoreCase(titulo)) {
            return raiz;
        }
        if (titulo.compareToIgnoreCase(raiz.song.getTitle()) < 0) {
            return buscarRec(raiz.izquierdo, titulo);
        }
        return buscarRec(raiz.derecho, titulo);
    }
    
    public void eliminar(String titulo) {
        raiz = eliminarRec(raiz, titulo);
    }
    
    private NodoArbol eliminarRec(NodoArbol raiz, String titulo) {
        if (raiz == null) return raiz;
        
        if (titulo.compareToIgnoreCase(raiz.song.getTitle()) < 0) {
            raiz.izquierdo = eliminarRec(raiz.izquierdo, titulo);
        } else if (titulo.compareToIgnoreCase(raiz.song.getTitle()) > 0) {
            raiz.derecho = eliminarRec(raiz.derecho, titulo);
        } else {
            if (raiz.izquierdo == null) return raiz.derecho;
            else if (raiz.derecho == null) return raiz.izquierdo;
            
            raiz.song = minValue(raiz.derecho);
            raiz.derecho = eliminarRec(raiz.derecho, raiz.song.getTitle());
        }
        return raiz;
    }
    
    private Song minValue(NodoArbol raiz) {
        Song minv = raiz.song;
        while (raiz.izquierdo != null) {
            minv = raiz.izquierdo.song;
            raiz = raiz.izquierdo;
        }
        return minv;
    }
    
    public void inOrden(ListaSimple resultado) { inOrdenRec(raiz, resultado); }
    private void inOrdenRec(NodoArbol nodo, ListaSimple resultado) {
        if (nodo != null) {
            inOrdenRec(nodo.izquierdo, resultado);
            resultado.insertar(nodo.song);
            inOrdenRec(nodo.derecho, resultado);
        }
    }
    
    public void preOrden(ListaSimple resultado) { preOrdenRec(raiz, resultado); }
    private void preOrdenRec(NodoArbol nodo, ListaSimple resultado) {
        if (nodo != null) {
            resultado.insertar(nodo.song);
            preOrdenRec(nodo.izquierdo, resultado);
            preOrdenRec(nodo.derecho, resultado);
        }
    }
    
    public void postOrden(ListaSimple resultado) { postOrdenRec(raiz, resultado); }
    private void postOrdenRec(NodoArbol nodo, ListaSimple resultado) {
        if (nodo != null) {
            postOrdenRec(nodo.izquierdo, resultado);
            postOrdenRec(nodo.derecho, resultado);
            resultado.insertar(nodo.song);
        }
    }

    /**
     * Modifica los metadatos de una cancion identificada por su titulo.
     * Elimina el nodo original y lo reinserta con los datos actualizados.
     * @param tituloOriginal titulo actual de la cancion a modificar
     * @param songActualizada objeto Song con los nuevos datos
     * @return true si la cancion fue encontrada y modificada, false si no existe
     */
    public boolean modificar(String tituloOriginal, Song songActualizada) {
        if (buscar(tituloOriginal) == null) return false;
        eliminar(tituloOriginal);
        insertar(songActualizada);
        return true;
    }
}
