package com.progra3.smartplayer.servicio;

import com.progra3.smartplayer.modelo.Cancion;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class BibliotecaMusical {
    private final List<Cancion> canciones = new ArrayList<Cancion>();

    public List<Cancion> cargarDesdeCsv(Path ruta) {
        canciones.clear();
        if (!Files.exists(ruta)) {
            return canciones;
        }
        try {
            List<String> lineas = Files.readAllLines(ruta, StandardCharsets.UTF_8);
            for (int i = 1; i < lineas.size(); i++) {
                String[] partes = lineas.get(i).split("\\|");
                if (partes.length >= 8) {
                    canciones.add(new Cancion(
                            partes[0], partes[1], partes[2], partes[3],
                            entero(partes[4]), entero(partes[5]), partes[6], largo(partes[7])));
                }
            }
        } catch (IOException ex) {
            System.out.println("No se pudo leer CSV: " + ex.getMessage());
        }
        return canciones;
    }

    public List<Cancion> crearDatosDeEjemplo() {
        canciones.clear();
        canciones.add(new Cancion("Noches de Mazate", "Grupo Aurora", "Local Sessions", "Pop", 215, 2023, "demo/noches.mp3", 5120000));
        canciones.add(new Cancion("Codigo Azul", "Byte Band", "Algoritmos", "Rock", 198, 2024, "demo/codigo_azul.mp3", 4980000));
        canciones.add(new Cancion("Arbol AVL", "Los Recursivos", "Estructuras", "Electronica", 240, 2026, "demo/arbol_avl.mp3", 6200000));
        canciones.add(new Cancion("Cola de Espera", "DJ Nodo", "Estructuras", "Urbano", 188, 2025, "demo/cola_espera.mp3", 4550000));
        canciones.add(new Cancion("InOrden", "Binary Trio", "Recorridos", "Jazz", 260, 2022, "demo/inorden.mp3", 7000000));
        return canciones;
    }

    public void imprimir() {
        for (int i = 0; i < canciones.size(); i++) {
            System.out.println((i + 1) + ". " + canciones.get(i));
        }
    }

    private int entero(String texto) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private long largo(String texto) {
        try {
            return Long.parseLong(texto.trim());
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }
}
