package smartplayer.structures;
import smartplayer.models.Song;

public class Pila {
    public Nodo tope;
    public int tamano;

    public Pila() {
        tope = null;
        tamano = 0;
    }

    public void push(Song song) {
        Nodo nuevo = new Nodo(song);
        if (tope != null) {
            nuevo.siguiente = tope;
        }
        tope = nuevo;
        tamano++;
    }

    public Song pop() {
        if (tope == null) return null;
        Song song = tope.song;
        tope = tope.siguiente;
        tamano--;
        return song;
    }

    /** Retorna la cancion en el tope sin eliminarla, o null si la pila esta vacia. */
    public Song peek() {
        return (tope != null) ? tope.song : null;
    }

    public boolean estaVacia() {
        return tope == null;
    }
}
