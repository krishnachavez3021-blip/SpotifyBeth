package com.progra3.smartplayer.estructuras;

import com.progra3.smartplayer.modelo.Cancion;

public class ArbolAVL {
    private Nodo raiz;

    public void insertar(Cancion cancion) {
        raiz = insertar(raiz, cancion);
    }

    private Nodo insertar(Nodo nodo, Cancion cancion) {
        if (nodo == null) {
            return new Nodo(cancion);
        }
        int comparacion = cancion.compareTo(nodo.cancion);
        if (comparacion < 0) {
            nodo.izquierda = insertar(nodo.izquierda, cancion);
        } else if (comparacion > 0) {
            nodo.derecha = insertar(nodo.derecha, cancion);
        } else {
            return nodo;
        }

        actualizarAltura(nodo);
        return balancear(nodo);
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

    private Nodo balancear(Nodo nodo) {
        int balance = balance(nodo);
        if (balance > 1) {
            if (balance(nodo.izquierda) < 0) {
                nodo.izquierda = rotarIzquierda(nodo.izquierda);
            }
            return rotarDerecha(nodo);
        }
        if (balance < -1) {
            if (balance(nodo.derecha) > 0) {
                nodo.derecha = rotarDerecha(nodo.derecha);
            }
            return rotarIzquierda(nodo);
        }
        return nodo;
    }

    private Nodo rotarDerecha(Nodo y) {
        Nodo x = y.izquierda;
        Nodo temporal = x.derecha;
        x.derecha = y;
        y.izquierda = temporal;
        actualizarAltura(y);
        actualizarAltura(x);
        return x;
    }

    private Nodo rotarIzquierda(Nodo x) {
        Nodo y = x.derecha;
        Nodo temporal = y.izquierda;
        y.izquierda = x;
        x.derecha = temporal;
        actualizarAltura(x);
        actualizarAltura(y);
        return y;
    }

    private void actualizarAltura(Nodo nodo) {
        nodo.altura = 1 + Math.max(altura(nodo.izquierda), altura(nodo.derecha));
    }

    private int altura(Nodo nodo) {
        return nodo == null ? 0 : nodo.altura;
    }

    private int balance(Nodo nodo) {
        return nodo == null ? 0 : altura(nodo.izquierda) - altura(nodo.derecha);
    }

    private static class Nodo {
        private Cancion cancion;
        private Nodo izquierda;
        private Nodo derecha;
        private int altura = 1;

        private Nodo(Cancion cancion) {
            this.cancion = cancion;
        }
    }
}
