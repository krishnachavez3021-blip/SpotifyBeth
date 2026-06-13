package smartplayer.structures;
import smartplayer.models.Song;

public class ListaDoble {
    public Nodo cabeza;
    public Nodo cola;
    public int tamano;

    public ListaDoble() {
        cabeza = null;
        cola = null;
        tamano = 0;
    }

    public void insertar(Song song) {
        Nodo nuevo = new Nodo(song);
        if (cabeza == null) {
            cabeza = nuevo;
            cola = nuevo;
        } else {
            cola.siguiente = nuevo;
            nuevo.anterior = cola;
            cola = nuevo;
        }
        tamano++;
    }

    public void vaciar() {
        cabeza = null;
        cola = null;
        tamano = 0;
    }
}
