package smartplayer.structures;
import smartplayer.models.Song;

public class ListaSimple {
    public Nodo cabeza;
    public int tamano;

    public ListaSimple() {
        cabeza = null;
        tamano = 0;
    }

    public void insertar(Song song) {
        Nodo nuevo = new Nodo(song);
        if (cabeza == null) {
            cabeza = nuevo;
        } else {
            Nodo temp = cabeza;
            while (temp.siguiente != null) {
                temp = temp.siguiente;
            }
            temp.siguiente = nuevo;
        }
        tamano++;
    }
    
    public void eliminar(Song song) {
        if (cabeza == null) return;
        if (cabeza.song.getPath().equals(song.getPath())) {
            cabeza = cabeza.siguiente;
            tamano--;
            return;
        }
        Nodo actual = cabeza;
        while (actual.siguiente != null) {
            if (actual.siguiente.song.getPath().equals(song.getPath())) {
                actual.siguiente = actual.siguiente.siguiente;
                tamano--;
                return;
            }
            actual = actual.siguiente;
        }
    }

    public void vaciar() {
        cabeza = null;
        tamano = 0;
    }
    
    public Nodo getCabeza() {
        return cabeza;
    }
}
