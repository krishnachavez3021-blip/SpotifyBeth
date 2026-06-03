# Smart Player – Sistema Inteligente de Reproducción Musical
**Programación III (Estructura de Datos) – UMG Mazatenango 2026**

---

## Estructura del Proyecto

```
SmartPlayer/
└── src/
    └── smartplayer/
        ├── Main.java            ← Punto de entrada
        ├── SmartPlayerUI.java   ← Interfaz gráfica principal
        ├── Song.java            ← Modelo de canción
        ├── BST.java             ← Árbol Binario de Búsqueda
        ├── AVLTree.java         ← Árbol AVL con rotaciones
        ├── PlayStack.java       ← Pila (historial)
        ├── PlayQueue.java       ← Cola de reproducción
        ├── DoublyLinkedList.java← Lista doble (navegación)
        ├── CircularList.java    ← Lista circular (modo infinito)
        ├── Playlist.java        ← Gestión de playlists
        ├── SearchHistory.java   ← Historial de búsquedas
        ├── MusicLoader.java     ← Carga de archivos de música
        ├── Statistics.java      ← Estadísticas musicales
        └── Encryptor.java       ← Encriptación con recorridos de árbol
```

---

## Cómo abrir en NetBeans

1. Abrir NetBeans IDE
2. File → New Project → Java → Java Application → Next
3. Nombre del proyecto: `SmartPlayer`
4. Desmarcar "Create Main Class"
5. Finish
6. Eliminar los archivos `.java` que NetBeans crea por defecto
7. Copiar todos los archivos `.java` de esta carpeta a `src/smartplayer/`
8. Click derecho en el proyecto → Run

---

## Estructuras de Datos Implementadas

| Estructura | Clase | Uso |
|---|---|---|
| Árbol ABB | `BST.java` | Búsqueda e índice de canciones |
| Árbol AVL | `AVLTree.java` | Índice balanceado y optimizado |
| Pila | `PlayStack.java` | Historial de reproducción |
| Cola | `PlayQueue.java` | Cola de reproducción automática |
| Lista Doble | `DoublyLinkedList.java` | Navegación siguiente/anterior |
| Lista Circular | `CircularList.java` | Modo repetición infinita |
| Lista Simple | `MusicLoader.java` | Biblioteca musical en carga |
| Arreglos | `Statistics.java` | Comparativas y resúmenes |

---

## Funcionalidades

### Biblioteca Musical
- Carga automática desde carpeta (recursiva en subcarpetas)
- Filtrado rápido por cualquier campo
- Vista de detalle al hacer clic en canción
- Ícono de álbum por canción
- Contador de reproducciones por canción

### Búsqueda
- Búsqueda por: Título, Artista, Álbum, Género o Todo
- Selección de árbol: ABB, AVL o Ambos
- Tiempo de búsqueda mostrado en ms
- **Historial de búsquedas** con frecuencia y fecha
- Chips de búsqueda reciente con un clic

### Reproducción
- Reproducir / Pausar / Siguiente / Anterior
- Modo Aleatorio (Shuffle)
- Modo Repetir
- Modo Circular Infinito
- Barra de progreso simulada
- Control de volumen

### Playlists
- Crear / Eliminar playlists
- Agregar / quitar canciones
- Reproducción: Normal, Aleatoria, Circular
- Exportar playlist (.splist)
- Encriptar y exportar (.spenc)

### Cola de Reproducción
- Agregar canciones a la cola
- Vista de la cola actual
- Limpiar cola

### Historial
- Historial de canciones reproducidas (pila)
- Historial de búsquedas con frecuencia
- Cantidad de veces escuchada cada canción

### Estadísticas
- Canción más reproducida
- Artista más escuchado
- Playlist más grande
- Género más frecuente
- Duración promedio
- Tamaño total de la biblioteca
- Tiempo promedio de búsqueda ABB vs AVL
- Archivos duplicados (cuántos, cuáles, tamaño)

### Encriptación
- Encriptar playlists usando recorridos: InOrden, PreOrden, PostOrden
- Exportar archivo encriptado (.spenc)
- Desencriptar y cargar playlist
- Algoritmo propio basado en Caesar-shift con clave derivada del árbol

### Letra de canciones
- Ver/editar la letra de cada canción
- Guardar letra en la sesión actual

---

## Formatos de música soportados
`.mp3`, `.wav`, `.flac`, `.ogg`, `.m4a`, `.aac`, `.wma`

---

## Recorridos del Árbol AVL
- **InOrden**: Canciones ordenadas alfabéticamente (A→Z)
- **PreOrden**: Raíz primero, luego ramas
- **PostOrden**: Hojas primero, luego raíz

---

## Rotaciones AVL implementadas
- **RI** – Rotación Izquierda
- **RD** – Rotación Derecha
- **RID** – Rotación Izquierda-Derecha
- **RDI** – Rotación Derecha-Izquierda

---

*Universidad Mariano Gálvez de Guatemala – Facultad de Ingeniería en Sistemas*
*Curso: Programación III – 2026*
