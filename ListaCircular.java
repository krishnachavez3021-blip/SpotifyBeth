package smartplayer.structures;
import smartplayer.models.Song;

public class ListaCircular {
    public Nodo cabeza;
    public Nodo cola;
    public int tamano;

    public ListaCircular() {
        cabeza = null;
        cola = null;
        tamano = 0;
    }

    public void insertar(Song song) {
        Nodo nuevo = new Nodo(song);
        if (cabeza == null) {
            cabeza = nuevo;
            cola = nuevo;
            cabeza.siguiente = cabeza;
            cabeza.anterior = cabeza;
        } else {
            cola.siguiente = nuevo;
            nuevo.anterior = cola;
            nuevo.siguiente = cabeza;
            cabeza.anterior = nuevo;
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
