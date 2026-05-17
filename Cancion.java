package com.progra3.smartplayer.modelo;

public class Cancion implements Comparable<Cancion> {
    private String nombre;
    private String artista;
    private String album;
    private String genero;
    private int duracionSegundos;
    private int anio;
    private String ruta;
    private long tamanoBytes;
    private int reproducciones;

    public Cancion(String nombre, String artista, String album, String genero,
                   int duracionSegundos, int anio, String ruta, long tamanoBytes) {
        this.nombre = nombre;
        this.artista = artista;
        this.album = album;
        this.genero = genero;
        this.duracionSegundos = duracionSegundos;
        this.anio = anio;
        this.ruta = ruta;
        this.tamanoBytes = tamanoBytes;
    }

    public String getNombre() {
        return nombre;
    }

    public String getArtista() {
        return artista;
    }

    public String getAlbum() {
        return album;
    }

    public String getGenero() {
        return genero;
    }

    public int getDuracionSegundos() {
        return duracionSegundos;
    }

    public int getAnio() {
        return anio;
    }

    public String getRuta() {
        return ruta;
    }

    public long getTamanoBytes() {
        return tamanoBytes;
    }

    public int getReproducciones() {
        return reproducciones;
    }

    public void reproducir() {
        reproducciones++;
    }

    @Override
    public int compareTo(Cancion otra) {
        return normalizar(nombre).compareTo(normalizar(otra.nombre));
    }

    public int compararPorNombre(String texto) {
        return normalizar(nombre).compareTo(normalizar(texto));
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.trim().toLowerCase();
    }

    @Override
    public String toString() {
        return nombre + " | " + artista + " | " + album + " | " + genero
                + " | " + duracionSegundos + "s | " + anio;
    }
}
