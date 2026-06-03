package smartplayer.models;
import smartplayer.structures.ListaSimple;

public class Playlist {
    private String nombre;
    private ListaSimple canciones;

    public Playlist(String nombre) {
        this.nombre = nombre;
        this.canciones = new ListaSimple();
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public ListaSimple getCanciones() { return canciones; }
}
