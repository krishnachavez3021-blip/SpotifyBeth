package smartplayer.structures;
import smartplayer.models.Playlist;

public class ListaPlaylists {
    public class NodoPlaylist {
        public Playlist playlist;
        public NodoPlaylist siguiente;
        public NodoPlaylist(Playlist p) {
            this.playlist = p;
            this.siguiente = null;
        }
    }
    
    public NodoPlaylist cabeza;
    public int tamano;
    
    public ListaPlaylists() {
        cabeza = null;
        tamano = 0;
    }
    
    public void insertar(Playlist p) {
        NodoPlaylist nuevo = new NodoPlaylist(p);
        if (cabeza == null) {
            cabeza = nuevo;
        } else {
            NodoPlaylist temp = cabeza;
            while (temp.siguiente != null) temp = temp.siguiente;
            temp.siguiente = nuevo;
        }
        tamano++;
    }
    
    public void eliminar(String nombre) {
        if (cabeza == null) return;
        if (cabeza.playlist.getNombre().equals(nombre)) {
            cabeza = cabeza.siguiente;
            tamano--;
            return;
        }
        NodoPlaylist actual = cabeza;
        while (actual.siguiente != null) {
            if (actual.siguiente.playlist.getNombre().equals(nombre)) {
                actual.siguiente = actual.siguiente.siguiente;
                tamano--;
                return;
            }
            actual = actual.siguiente;
        }
    }
}
