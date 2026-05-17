package com.progra3.smartplayer.estructuras;

import com.progra3.smartplayer.modelo.Cancion;

public class ArbolABB {
    private Nodo raiz;

    public void insertar(Cancion cancion) {
        raiz = insertar(raiz, cancion);
    }

    private Nodo insertar(Nodo actual, Cancion cancion) {
        if (actual == null) {
            return new Nodo(cancion);
        }
        if (cancion.compareTo(actual.cancion) < 0) {
            actual.izquierda = insertar(actual.izquierda, cancion);
        } else if (cancion.compareTo(actual.cancion) > 0) {
            actual.derecha = insertar(actual.derecha, cancion);
        }
        return actual;
    }

    public Cancion buscar(String nombre) {
        Nodo actual = raiz;
        while (actual != null) {
            int comparacion = actual.cancion.compararPorNombre(nombre);
            if (comparacion == 0) {
                return actual.cancion;
            }
            actual = comparacion > 0 ? actual.izquierda : actual.derecha;
        }
        return null;
    }

    public void imprimirInOrden() {
        inOrden(raiz);
    }

    public void imprimirPreOrden() {
        preOrden(raiz);
    }

    public void imprimirPostOrden() {
        postOrden(raiz);
    }

    private void inOrden(Nodo nodo) {
        if (nodo != null) {
            inOrden(nodo.izquierda);
            System.out.println(nodo.cancion);
            inOrden(nodo.derecha);
        }
    }

    private void preOrden(Nodo nodo) {
        if (nodo != null) {
            System.out.println(nodo.cancion);
            preOrden(nodo.izquierda);
            preOrden(nodo.derecha);
        }
    }

    private void postOrden(Nodo nodo) {
        if (nodo != null) {
            postOrden(nodo.izquierda);
            postOrden(nodo.derecha);
            System.out.println(nodo.cancion);
        }
    }

    private static class Nodo {
        private Cancion cancion;
        private Nodo izquierda;
        private Nodo derecha;

        private Nodo(Cancion cancion) {
            this.cancion = cancion;
        }
    }
}
