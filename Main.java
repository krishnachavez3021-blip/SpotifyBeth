package com.progra3.smartplayer;

import com.progra3.smartplayer.estructuras.ArbolABB;
import com.progra3.smartplayer.estructuras.ArbolAVL;
import com.progra3.smartplayer.estructuras.ColaReproduccion;
import com.progra3.smartplayer.estructuras.ListaCircularCanciones;
import com.progra3.smartplayer.estructuras.ListaDobleCanciones;
import com.progra3.smartplayer.estructuras.PilaHistorial;
import com.progra3.smartplayer.modelo.Cancion;
import com.progra3.smartplayer.servicio.BibliotecaMusical;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class Main {
    private final BibliotecaMusical biblioteca = new BibliotecaMusical();
    private final ArbolABB abb = new ArbolABB();
    private final ArbolAVL avl = new ArbolAVL();
    private final PilaHistorial historial = new PilaHistorial();
    private final ColaReproduccion cola = new ColaReproduccion();
    private final ListaDobleCanciones navegacion = new ListaDobleCanciones();
    private final ListaCircularCanciones playlistCircular = new ListaCircularCanciones();
    private final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        new Main().iniciar();
    }

    private void iniciar() {
        cargarDatos();
        int opcion;
        do {
            mostrarMenu();
            opcion = leerEntero("Seleccione una opcion: ");
            ejecutar(opcion);
        } while (opcion != 0);
        System.out.println("Smart Player finalizado.");
    }

    private void cargarDatos() {
        List<Cancion> canciones = biblioteca.cargarDesdeCsv(Paths.get("data", "canciones.csv"));
        if (canciones.isEmpty()) {
            canciones = biblioteca.crearDatosDeEjemplo();
        }
        for (Cancion cancion : canciones) {
            abb.insertar(cancion);
            avl.insertar(cancion);
            navegacion.agregar(cancion);
            playlistCircular.agregar(cancion);
            cola.encolar(cancion);
        }
        System.out.println("Canciones cargadas: " + canciones.size());
    }

    private void mostrarMenu() {
        System.out.println("\n=== SMART PLAYER - AVANCE ===");
        System.out.println("1. Listar biblioteca");
        System.out.println("2. Buscar cancion por nombre");
        System.out.println("3. Reproducir siguiente en cola");
        System.out.println("4. Ver historial");
        System.out.println("5. Navegar lista doble");
        System.out.println("6. Mostrar playlist circular");
        System.out.println("7. Recorridos ABB");
        System.out.println("8. Comparar busqueda ABB vs AVL");
        System.out.println("0. Salir");
    }

    private void ejecutar(int opcion) {
        switch (opcion) {
            case 1:
                biblioteca.imprimir();
                break;
            case 2:
                buscar();
                break;
            case 3:
                reproducirSiguiente();
                break;
            case 4:
                historial.imprimir();
                break;
            case 5:
                navegar();
                break;
            case 6:
                playlistCircular.imprimirUnaVuelta();
                break;
            case 7:
                mostrarRecorridos();
                break;
            case 8:
                compararBusqueda();
                break;
            case 0:
                break;
            default:
                System.out.println("Opcion no valida.");
        }
    }

    private void buscar() {
        System.out.print("Nombre de la cancion: ");
        String nombre = scanner.nextLine();
        Cancion encontrada = avl.buscar(nombre);
        System.out.println(encontrada == null ? "No encontrada." : encontrada);
    }

    private void reproducirSiguiente() {
        Cancion cancion = cola.desencolar();
        if (cancion == null) {
            System.out.println("La cola esta vacia.");
            return;
        }
        historial.apilar(cancion);
        System.out.println("Reproduciendo: " + cancion.getNombre() + " - " + cancion.getArtista());
    }

    private void navegar() {
        System.out.println("Actual: " + navegacion.actual());
        System.out.println("1. Siguiente");
        System.out.println("2. Anterior");
        int opcion = leerEntero("Movimiento: ");
        Cancion cancion = opcion == 2 ? navegacion.anterior() : navegacion.siguiente();
        System.out.println("Ahora: " + cancion);
    }

    private void mostrarRecorridos() {
        System.out.println("InOrden:");
        abb.imprimirInOrden();
        System.out.println("PreOrden:");
        abb.imprimirPreOrden();
        System.out.println("PostOrden:");
        abb.imprimirPostOrden();
    }

    private void compararBusqueda() {
        System.out.print("Nombre a buscar: ");
        String nombre = scanner.nextLine();

        long inicioAbb = System.nanoTime();
        Cancion r1 = abb.buscar(nombre);
        long tiempoAbb = System.nanoTime() - inicioAbb;

        long inicioAvl = System.nanoTime();
        Cancion r2 = avl.buscar(nombre);
        long tiempoAvl = System.nanoTime() - inicioAvl;

        System.out.println("ABB: " + tiempoAbb + " ns -> " + (r1 != null ? "encontrada" : "no encontrada"));
        System.out.println("AVL: " + tiempoAvl + " ns -> " + (r2 != null ? "encontrada" : "no encontrada"));
    }

    private int leerEntero(String mensaje) {
        System.out.print(mensaje);
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException ex) {
            return -1;
        }
    }
}
