package smartplayer.structures;
import smartplayer.models.Song;

public class ArbolAVL {
    public NodoArbol raiz;
    
    public ArbolAVL() {
        raiz = null;
    }
    
    private int altura(NodoArbol N) {
        if (N == null) return 0;
        return N.altura;
    }
    
    private int max(int a, int b) {
        return (a > b) ? a : b;
    }
    
    private NodoArbol rotacionDerecha(NodoArbol y) {
        NodoArbol x = y.izquierdo;
        NodoArbol T2 = x.derecho;
        
        x.derecho = y;
        y.izquierdo = T2;
        
        y.altura = max(altura(y.izquierdo), altura(y.derecho)) + 1;
        x.altura = max(altura(x.izquierdo), altura(x.derecho)) + 1;
        
        return x;
    }
    
    private NodoArbol rotacionIzquierda(NodoArbol x) {
        NodoArbol y = x.derecho;
        NodoArbol T2 = y.izquierdo;
        
        y.izquierdo = x;
        x.derecho = T2;
        
        x.altura = max(altura(x.izquierdo), altura(x.derecho)) + 1;
        y.altura = max(altura(y.izquierdo), altura(y.derecho)) + 1;
        
        return y;
    }
    
    private int getBalance(NodoArbol N) {
        if (N == null) return 0;
        return altura(N.izquierdo) - altura(N.derecho);
    }
    
    public void insertar(Song song) {
        raiz = insertarRec(raiz, song);
    }
    
    private NodoArbol insertarRec(NodoArbol nodo, Song song) {
        if (nodo == null) return new NodoArbol(song);
        
        if (song.getTitle().compareToIgnoreCase(nodo.song.getTitle()) < 0)
            nodo.izquierdo = insertarRec(nodo.izquierdo, song);
        else if (song.getTitle().compareToIgnoreCase(nodo.song.getTitle()) > 0)
            nodo.derecho = insertarRec(nodo.derecho, song);
        else
            nodo.derecho = insertarRec(nodo.derecho, song);
            
        nodo.altura = 1 + max(altura(nodo.izquierdo), altura(nodo.derecho));
        
        int balance = getBalance(nodo);
        
        if (balance > 1 && song.getTitle().compareToIgnoreCase(nodo.izquierdo.song.getTitle()) < 0)
            return rotacionDerecha(nodo);
            
        if (balance < -1 && song.getTitle().compareToIgnoreCase(nodo.derecho.song.getTitle()) > 0)
            return rotacionIzquierda(nodo);
            
        if (balance > 1 && song.getTitle().compareToIgnoreCase(nodo.izquierdo.song.getTitle()) > 0) {
            nodo.izquierdo = rotacionIzquierda(nodo.izquierdo);
            return rotacionDerecha(nodo);
        }
        
        if (balance < -1 && song.getTitle().compareToIgnoreCase(nodo.derecho.song.getTitle()) < 0) {
            nodo.derecho = rotacionDerecha(nodo.derecho);
            return rotacionIzquierda(nodo);
        }
        
        return nodo;
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
    
    /** Elimina un nodo por título manteniendo el balanceo AVL. */
    public void eliminar(String titulo) {
        raiz = eliminarRec(raiz, titulo);
    }

    private NodoArbol eliminarRec(NodoArbol nodo, String titulo) {
        if (nodo == null) return null;

        int cmp = titulo.compareToIgnoreCase(nodo.song.getTitle());
        if (cmp < 0) {
            nodo.izquierdo = eliminarRec(nodo.izquierdo, titulo);
        } else if (cmp > 0) {
            nodo.derecho = eliminarRec(nodo.derecho, titulo);
        } else {
            // Nodo a eliminar encontrado
            if (nodo.izquierdo == null) return nodo.derecho;
            if (nodo.derecho  == null) return nodo.izquierdo;
            // Sucesor in-order (menor del subárbol derecho)
            NodoArbol sucesor = nodo.derecho;
            while (sucesor.izquierdo != null) sucesor = sucesor.izquierdo;
            nodo.song = sucesor.song;
            nodo.derecho = eliminarRec(nodo.derecho, sucesor.song.getTitle());
        }

        // Actualizar altura
        nodo.altura = 1 + max(altura(nodo.izquierdo), altura(nodo.derecho));

        // Rebalancear
        int balance = getBalance(nodo);
        // LL
        if (balance > 1 && getBalance(nodo.izquierdo) >= 0)
            return rotacionDerecha(nodo);
        // LR
        if (balance > 1 && getBalance(nodo.izquierdo) < 0) {
            nodo.izquierdo = rotacionIzquierda(nodo.izquierdo);
            return rotacionDerecha(nodo);
        }
        // RR
        if (balance < -1 && getBalance(nodo.derecho) <= 0)
            return rotacionIzquierda(nodo);
        // RL
        if (balance < -1 && getBalance(nodo.derecho) > 0) {
            nodo.derecho = rotacionDerecha(nodo.derecho);
            return rotacionIzquierda(nodo);
        }
        return nodo;
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
     * Elimina el nodo original y reinsertar con los datos actualizados
     * para mantener el balanceo AVL.
     * @param tituloOriginal titulo actual de la cancion a modificar
     * @param songActualizada objeto Song con los nuevos datos
     * @return true si la cancion fue encontrada y modificada, false si no existe
     */
    public boolean modificar(String tituloOriginal, smartplayer.models.Song songActualizada) {
        if (buscar(tituloOriginal) == null) return false;
        eliminar(tituloOriginal);
        insertar(songActualizada);
        return true;
    }
}
