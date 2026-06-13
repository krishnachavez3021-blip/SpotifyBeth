package smartplayer.structures;
import smartplayer.models.Song;

public class Nodo {
    public Song song;
    public Nodo siguiente;
    public Nodo anterior;

    public Nodo(Song song) {
        this.song = song;
        this.siguiente = null;
        this.anterior = null;
    }
}
