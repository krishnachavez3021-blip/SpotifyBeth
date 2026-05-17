package com.progra3.smartplayer.estructuras;

import com.progra3.smartplayer.modelo.Cancion;

public class ColaReproduccion {
    private Nodo frente;
    private Nodo fin;
    private int tamanio;

    public void encolar(Cancion cancion) {
        Nodo nuevo = new Nodo(cancion);
        if (fin == null) {
            frente = nuevo;
        } else {
            fin.siguiente = nuevo;
        }
        fin = nuevo;
        tamanio++;
    }

    public Cancion desencolar() {
        if (frente == null) {
            return null;
        }
        Cancion cancion = frente.cancion;
        frente = frente.siguiente;
        if (frente == null) {
            fin = null;
        }
        tamanio--;
        return cancion;
    }

    public int getTamanio() {
        return tamanio;
    }

    private static class Nodo {
        private Cancion cancion;
        private Nodo siguiente;

        private Nodo(Cancion cancion) {
            this.cancion = cancion;
        }
    }
}
