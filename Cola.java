package smartplayer.structures;
import smartplayer.models.Song;

public class Cola {
    public Nodo frente;
    public Nodo fin;
    public int tamano;

    public Cola() {
        frente = null;
        fin = null;
        tamano = 0;
    }

    public void encolar(Song song) {
        Nodo nuevo = new Nodo(song);
        if (fin == null) {
            frente = nuevo;
            fin = nuevo;
        } else {
            fin.siguiente = nuevo;
            fin = nuevo;
        }
        tamano++;
    }

    public Song desencolar() {
        if (frente == null) return null;
        Song song = frente.song;
        frente = frente.siguiente;
        if (frente == null) {
            fin = null;
        }
        tamano--;
        return song;
    }
    
    public boolean estaVacia() {
        return frente == null;
    }
}
