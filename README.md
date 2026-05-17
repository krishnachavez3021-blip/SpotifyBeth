# Smart Player - Avance Proyecto Final

Avance en Java para abrir en NetBeans. Implementa una version de consola del sistema solicitado:

- Biblioteca musical con carga desde archivo CSV o datos de ejemplo.
- Arbol Binario de Busqueda (ABB) para busqueda por nombre.
- Arbol AVL con balanceo automatico.
- Pila para historial de reproduccion.
- Cola para cola de reproduccion.
- Lista doble para navegar siguiente/anterior.
- Lista circular para modo repeticion infinita.
- Playlist, recorridos y comparacion basica de tiempos ABB vs AVL.

## Como abrir en NetBeans

1. Abrir NetBeans.
2. File > Open Project.
3. Seleccionar la carpeta `SmartPlayerAvance`.
4. Ejecutar la clase `com.progra3.smartplayer.Main`.

## Archivo CSV opcional

El programa puede cargar `data/canciones.csv`. Formato:

```text
nombre|artista|album|genero|duracionSegundos|anio|ruta|tamanoBytes
```

Si el archivo no existe, carga canciones de ejemplo para demostrar las estructuras.

## Pendiente para la siguiente entrega

- Lectura real de carpetas con miles de archivos.
- Interfaz grafica Swing.
- Reproduccion MP3 real si el ingeniero permite librerias externas.
- Exportacion, encriptacion y desencriptacion de playlists.
- Reportes completos de estadisticas.
