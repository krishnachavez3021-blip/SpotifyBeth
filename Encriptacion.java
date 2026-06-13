package smartplayer.utils;
import smartplayer.structures.ArbolABB;
import smartplayer.structures.ListaSimple;
import smartplayer.structures.Nodo;

public class Encriptacion {
    public static String encriptar(String texto) {
        StringBuilder sb = new StringBuilder();
        for (char c : texto.toCharArray()) {
            sb.append((char)(c + 3));
        }
        return sb.toString();
    }
    
    public static String desencriptar(String texto) {
        StringBuilder sb = new StringBuilder();
        for (char c : texto.toCharArray()) {
            sb.append((char)(c - 3));
        }
        return sb.toString();
    }
    
    public static ListaSimple obtenerListaPorRecorrido(ArbolABB arbol, String tipoRecorrido) {
        ListaSimple resultado = new ListaSimple();
        if (tipoRecorrido.equalsIgnoreCase("InOrden")) {
            arbol.inOrden(resultado);
        } else if (tipoRecorrido.equalsIgnoreCase("PreOrden")) {
            arbol.preOrden(resultado);
        } else if (tipoRecorrido.equalsIgnoreCase("PostOrden")) {
            arbol.postOrden(resultado);
        }
        return resultado;
    }
    
    public static void guardarPlaylistEncriptada(ListaSimple canciones, String archivo, String nombrePlaylist) {
        try (java.io.PrintWriter out = new java.io.PrintWriter(new java.io.FileWriter(archivo))) {
            out.println(encriptar(nombrePlaylist));
            Nodo actual = canciones.getCabeza();
            while (actual != null) {
                out.println(encriptar(actual.song.getPath()));
                actual = actual.siguiente;
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}
