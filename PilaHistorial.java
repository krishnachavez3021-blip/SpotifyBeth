package com.progra3.smartplayer.estructuras;

import com.progra3.smartplayer.modelo.Cancion;

public class PilaHistorial {
    private Nodo cima;
    private int tamanio;

    public void apilar(Cancion cancion) {
        cancion.reproducir();
        Nodo nuevo = new Nodo(cancion);
        nuevo.siguiente = cima;
        cima = nuevo;
        tamanio++;
    }

    public Cancion desapilar() {
        if (cima == null) {
            return null;
        }
        Cancion cancion = cima.cancion;
        cima = cima.siguiente;
        tamanio--;
        return cancion;
    }

    public void imprimir() {
        System.out.println("Historial (" + tamanio + "):");
        Nodo actual = cima;
        while (actual != null) {
            System.out.println("- " + actual.cancion);
            actual = actual.siguiente;
        }
    }

    private static class Nodo {
        private Cancion cancion;
        private Nodo siguiente;

        private Nodo(Cancion cancion) {
            this.cancion = cancion;
        }
    }
}
