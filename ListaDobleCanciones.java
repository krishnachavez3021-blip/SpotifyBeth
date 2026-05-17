package com.progra3.smartplayer.estructuras;

import com.progra3.smartplayer.modelo.Cancion;

public class ListaDobleCanciones {
    private Nodo cabeza;
    private Nodo cola;
    private Nodo actual;

    public void agregar(Cancion cancion) {
        Nodo nuevo = new Nodo(cancion);
        if (cabeza == null) {
            cabeza = nuevo;
            actual = nuevo;
        } else {
            cola.siguiente = nuevo;
            nuevo.anterior = cola;
        }
        cola = nuevo;
    }

    public Cancion siguiente() {
        if (actual == null) {
            return null;
        }
        if (actual.siguiente != null) {
            actual = actual.siguiente;
        }
        return actual.cancion;
    }

    public Cancion anterior() {
        if (actual == null) {
            return null;
        }
        if (actual.anterior != null) {
            actual = actual.anterior;
        }
        return actual.cancion;
    }

    public Cancion actual() {
        return actual == null ? null : actual.cancion;
    }

    private static class Nodo {
        private Cancion cancion;
        private Nodo anterior;
        private Nodo siguiente;

        private Nodo(Cancion cancion) {
            this.cancion = cancion;
        }
    }
}
