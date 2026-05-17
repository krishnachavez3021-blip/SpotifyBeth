package com.progra3.smartplayer.estructuras;

import com.progra3.smartplayer.modelo.Cancion;

public class ListaCircularCanciones {
    private Nodo ultimo;
    private int tamanio;

    public void agregar(Cancion cancion) {
        Nodo nuevo = new Nodo(cancion);
        if (ultimo == null) {
            ultimo = nuevo;
            ultimo.siguiente = ultimo;
        } else {
            nuevo.siguiente = ultimo.siguiente;
            ultimo.siguiente = nuevo;
            ultimo = nuevo;
        }
        tamanio++;
    }

    public void imprimirUnaVuelta() {
        if (ultimo == null) {
            System.out.println("Playlist circular vacia.");
            return;
        }
        Nodo primero = ultimo.siguiente;
        Nodo actual = primero;
        int contador = 1;
        do {
            System.out.println(contador + ". " + actual.cancion);
            actual = actual.siguiente;
            contador++;
        } while (actual != primero);
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
