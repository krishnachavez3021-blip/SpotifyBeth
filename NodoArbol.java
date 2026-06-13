package smartplayer.structures;
import smartplayer.models.Song;

public class NodoArbol {
    public Song song;
    public NodoArbol izquierdo;
    public NodoArbol derecho;
    public int altura;

    public NodoArbol(Song song) {
        this.song = song;
        this.izquierdo = null;
        this.derecho = null;
        this.altura = 1;
    }
}
