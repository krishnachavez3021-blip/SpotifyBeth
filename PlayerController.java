package smartplayer.controllers;

import javazoom.jl.player.Player;
import smartplayer.models.Song;
import smartplayer.structures.Pila;
import smartplayer.structures.Cola;
import smartplayer.structures.ListaDoble;
import smartplayer.structures.ListaCircular;
import smartplayer.structures.Nodo;
import smartplayer.utils.StatsManager;
import smartplayer.utils.VolumeAudioDevice;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;

/**
 * Controlador principal de reproduccion de musica.
 * Gestiona play, pause, stop, siguiente, anterior, volumen real y seek.
 * Usa VolumeAudioDevice para control de volumen real en JLayer.
 */
public class PlayerController {
    private Player player;
    private Thread playerThread;
    private Song currentSong;
    private Pila historial;
    private Cola colaReproduccion;
    private boolean isPlaying = false;
    private boolean isPaused = false;
    private long pauseLocation = 0;
    private float volume = 0.8f; // Volumen 0.0 a 1.0
    private VolumeAudioDevice currentVolumeDevice; // Para control real de volumen
    private StatsManager statsManager;
    private long playStartTime; // Para calcular tiempo de escucha

    public enum PlayMode { NORMAL, CIRCULAR, RANDOM }
    private PlayMode currentMode = PlayMode.NORMAL;

    private ListaDoble listaNormal;
    private ListaCircular listaCircular;
    private Nodo nodoActualNormal;
    private Nodo nodoActualCircular;

    private Runnable onSongEndCallback;
    private Runnable onSongStartCallback;

    public PlayerController() {
        historial = new Pila();
        colaReproduccion = new Cola();
        listaNormal = new ListaDoble();
        listaCircular = new ListaCircular();
        statsManager = new StatsManager();
        playStartTime = 0;
    }

    public void setOnSongEndCallback(Runnable callback) {
        this.onSongEndCallback = callback;
    }

    public void setOnSongStartCallback(Runnable cb) { this.onSongStartCallback = cb; }

    public void setPlaylistNormal(ListaDoble lista) {
        this.listaNormal = lista;
        this.currentMode = PlayMode.NORMAL;
        if (lista.cabeza != null) {
            this.nodoActualNormal = lista.cabeza;
        }
    }

    public void setPlaylistCircular(ListaCircular lista) {
        this.listaCircular = lista;
        this.currentMode = PlayMode.CIRCULAR;
        if (lista.cabeza != null) {
            this.nodoActualCircular = lista.cabeza;
        }
    }

    /**
     * Asigna la playlist de navegacion y posiciona el nodo actual en la cancion dada.
     * Debe llamarse antes de playSong cuando se selecciona una cancion desde la biblioteca.
     *
     * @param song  cancion que se va a reproducir
     * @param lista lista doble con todas las canciones de la fuente
     */
    public void setCurrentSongInList(Song song, ListaDoble lista) {
        this.listaNormal = lista;
        this.currentMode = PlayMode.NORMAL;
        if (song == null || lista == null || lista.cabeza == null) {
            this.nodoActualNormal = (lista != null) ? lista.cabeza : null;
            return;
        }
        // 1. Buscar por ruta de archivo (más preciso)
        Nodo n = lista.cabeza;
        while (n != null) {
            if (n.song != null && n.song.getPath() != null
                    && n.song.getPath().equals(song.getPath())) {
                this.nodoActualNormal = n;
                return;
            }
            n = n.siguiente;
        }
        // 2. Fallback: buscar por título exacto
        n = lista.cabeza;
        while (n != null) {
            if (n.song != null && n.song.getTitle() != null
                    && n.song.getTitle().equalsIgnoreCase(song.getTitle())) {
                this.nodoActualNormal = n;
                return;
            }
            n = n.siguiente;
        }
        // 3. Último recurso: usar cabeza
        this.nodoActualNormal = lista.cabeza;
    }

    public void setMode(PlayMode mode) {
        this.currentMode = mode;
        System.out.println("[DEBUG] Modo cambiado a: " + mode);
        // Si se activa modo circular y la lista circular esta vacia, copiar listaNormal
        if (mode == PlayMode.CIRCULAR && listaCircular.tamano == 0 && listaNormal.cabeza != null) {
            listaCircular = new ListaCircular();
            Nodo n = listaNormal.cabeza;
            while (n != null) {
                listaCircular.insertar(n.song);
                n = n.siguiente;
            }
            nodoActualCircular = listaCircular.cabeza;
            System.out.println("[DEBUG] Lista circular creada desde listaNormal (" + listaCircular.tamano + " canciones)");
        }
    }

    public PlayMode getMode() { return currentMode; }

    public void playSong(Song song) {
        if (song == null) return;

        // Registrar tiempo de escucha de la cancion anterior
        registrarTiempoEscucha();

        stop();
        currentSong = song;
        historial.push(song);

        if (onSongStartCallback != null)
            javax.swing.SwingUtilities.invokeLater(onSongStartCallback);

        // Registrar reproduccion en estadisticas
        statsManager.registrarReproduccion(song);

        System.out.println("[DEBUG] Reproduciendo: " + song.getTitle() + " - " + song.getArtist());

        File archivoMp3 = new File(song.getPath());
        if (!archivoMp3.exists()) {
            System.err.println("[ERROR] Archivo no encontrado: " + song.getPath());
            // Intentar siguiente canción automáticamente
            javax.swing.SwingUtilities.invokeLater(() -> {
                if (onSongEndCallback != null) onSongEndCallback.run();
            });
            return;
        }

        try {
            FileInputStream fis = new FileInputStream(archivoMp3);
            BufferedInputStream bis = new BufferedInputStream(fis, 65536);

            // Crear dispositivo de audio con control de volumen real
            currentVolumeDevice = new VolumeAudioDevice(this.volume);
            player = new Player(bis, currentVolumeDevice);

            isPlaying = true;
            isPaused = false;
            playStartTime = System.currentTimeMillis();

            final Player currentPlayer = player;
            final Song   songRef       = song;
            playerThread = new Thread(() -> {
                try {
                    currentPlayer.play();
                    if (currentPlayer.isComplete() && isPlaying && !isPaused) {
                        registrarTiempoEscucha();
                        if (onSongEndCallback != null) {
                            onSongEndCallback.run();
                        }
                    }
                } catch (Exception e) {
                    System.err.println("[ERROR] Error durante reproduccion de '"
                        + songRef.getTitle() + "': " + e.getMessage());
                    // Saltar a la siguiente canción automáticamente si falla
                    if (isPlaying && !isPaused) {
                        isPlaying = false;
                        if (onSongEndCallback != null) {
                            onSongEndCallback.run();
                        }
                    }
                }
            });
            playerThread.setDaemon(true);
            playerThread.setName("PlayerThread-" + song.getTitle());
            playerThread.start();
        } catch (Exception e) {
            System.err.println("[ERROR] No se pudo abrir: " + song.getPath() + " — " + e.getMessage());
            // Saltar a la siguiente canción automáticamente
            isPlaying = false;
            javax.swing.SwingUtilities.invokeLater(() -> {
                if (onSongEndCallback != null) onSongEndCallback.run();
            });
        }
    }

    public void pause() {
        if (isPlaying && player != null) {
            System.out.println("[DEBUG] Pausa");
            registrarTiempoEscucha();
            isPaused = true;
            isPlaying = false;
            player.close();
        }
    }

    public void resume() {
        if (isPaused && currentSong != null) {
            System.out.println("[DEBUG] Reanudando: " + currentSong.getTitle());
            playSong(currentSong);
        }
    }

    public void stop() {
        registrarTiempoEscucha();
        isPlaying = false;
        isPaused = false;
        if (player != null) {
            player.close();
            player = null;
        }
        currentVolumeDevice = null;
        if (playerThread != null && playerThread.isAlive()) {
            playerThread.interrupt();
        }
        System.out.println("[DEBUG] Detenido");
    }

    /** Avanza a la siguiente cancion segun el modo actual. */
    public void next() {
        System.out.println("[DEBUG] Boton: Siguiente (modo=" + currentMode + ")");
        if (currentMode == PlayMode.NORMAL) {
            if (nodoActualNormal != null && nodoActualNormal.siguiente != null) {
                nodoActualNormal = nodoActualNormal.siguiente;
                playSong(nodoActualNormal.song);
            } else if (listaNormal != null && listaNormal.cabeza != null) {
                // Llegó al final: volver al inicio
                nodoActualNormal = listaNormal.cabeza;
                playSong(nodoActualNormal.song);
            } else {
                System.out.println("[DEBUG] No hay siguiente cancion (lista vacía)");
            }
        } else if (currentMode == PlayMode.CIRCULAR) {
            if (nodoActualCircular != null && nodoActualCircular.siguiente != null) {
                nodoActualCircular = nodoActualCircular.siguiente;
                playSong(nodoActualCircular.song);
            } else if (listaCircular.cabeza != null) {
                nodoActualCircular = listaCircular.cabeza;
                playSong(nodoActualCircular.song);
            }
        } else if (currentMode == PlayMode.RANDOM) {
            Song randomSong = colaReproduccion.desencolar();
            if (randomSong != null) {
                playSong(randomSong);
            } else if (listaNormal != null && listaNormal.cabeza != null) {
                // Cola vacía: elegir aleatoriamente de la lista normal
                int total = listaNormal.tamano;
                if (total > 0) {
                    int target = (int)(Math.random() * total);
                    Nodo n = listaNormal.cabeza;
                    for (int i = 0; i < target && n.siguiente != null; i++) n = n.siguiente;
                    nodoActualNormal = n;
                    playSong(n.song);
                }
            }
        }
    }

    /** Retrocede a la cancion anterior usando el historial. */
    public void prev() {
        System.out.println("[DEBUG] Boton: Anterior (modo=" + currentMode + ")");
        Song last = historial.pop();
        if (last != null && last.equals(currentSong)) {
            last = historial.pop();
        }
        if (last != null) {
            playSong(last);
            // Actualizar puntero de lista
            if (currentMode == PlayMode.NORMAL) {
                if (nodoActualNormal != null && nodoActualNormal.anterior != null) {
                    nodoActualNormal = nodoActualNormal.anterior;
                }
            } else if (currentMode == PlayMode.CIRCULAR) {
                if (nodoActualCircular != null && nodoActualCircular.anterior != null) {
                    nodoActualCircular = nodoActualCircular.anterior;
                }
            }
        } else {
            System.out.println("[DEBUG] No hay cancion anterior en el historial");
        }
    }

    /** Alias de next() para compatibilidad. */
    public void siguiente() { next(); }

    /** Alias de prev() para compatibilidad. */
    public void anterior() { prev(); }

    /**
     * Establece el volumen real de reproduccion (0.0 a 1.0).
     * Aplica inmediatamente al VolumeAudioDevice activo.
     */
    public void setVolume(float vol) {
        this.volume = Math.max(0.0f, Math.min(1.0f, vol));
        if (currentVolumeDevice != null) {
            currentVolumeDevice.setVolume(this.volume);
        }
        System.out.println("[DEBUG] Volumen: " + (int)(this.volume * 100) + "%");
    }

    public float getVolume() {
        return volume;
    }

    /**
     * Realiza un seek a una posicion de la cancion actual.
     * Detiene la reproduccion actual y la reinicia desde el byte aproximado.
     * Para MP3 CBR la precision es buena; para VBR es aproximada.
     *
     * @param percentage posicion deseada entre 0.0 y 1.0
     * @return segundos calculados de la nueva posicion, o -1 si falla
     */
    public int seekTo(double percentage) {
        if (currentSong == null) {
            System.out.println("[DEBUG] SeekTo: no hay cancion activa");
            return -1;
        }
        if (percentage < 0) percentage = 0;
        if (percentage > 1) percentage = 1;

        int newSecs = (int)(percentage * currentSong.getDuration());
        System.out.println("[DEBUG] SeekTo " + (int)(percentage * 100) + "% -> " + newSecs + "s");

        // Detener reproduccion actual sin resetear currentSong
        isPlaying = false;
        if (player != null) {
            player.close();
            player = null;
        }
        currentVolumeDevice = null;
        if (playerThread != null && playerThread.isAlive()) {
            playerThread.interrupt();
        }

        try {
            File f = new File(currentSong.getPath());
            long totalBytes = f.length();
            long skipBytes = (long)(percentage * totalBytes);

            FileInputStream fis = new FileInputStream(currentSong.getPath());
            long skipped = fis.skip(skipBytes);
            if (skipped < skipBytes) {
                System.out.println("[DEBUG] SeekTo: skip parcial (" + skipped + "/" + skipBytes + ")");
            }

            BufferedInputStream bis = new BufferedInputStream(fis);

            // Crear nuevo player con dispositivo de volumen
            currentVolumeDevice = new VolumeAudioDevice(this.volume);
            player = new Player(bis, currentVolumeDevice);

            isPlaying = true;
            isPaused = false;

            final Player currentPlayer = player;
            playerThread = new Thread(() -> {
                try {
                    currentPlayer.play();
                    if (currentPlayer.isComplete() && isPlaying && !isPaused) {
                        registrarTiempoEscucha();
                        if (onSongEndCallback != null) {
                            onSongEndCallback.run();
                        }
                    }
                } catch (Exception e) {
                    // Silenciar errores de reproduccion
                }
            });
            playerThread.setDaemon(true);
            playerThread.start();

        } catch (Exception e) {
            System.err.println("[ERROR] SeekTo fallido: " + e.getMessage());
            return -1;
        }

        return newSecs;
    }

    /**
     * Registra el tiempo de escucha transcurrido desde el inicio de reproduccion.
     */
    private void registrarTiempoEscucha() {
        if (playStartTime > 0 && isPlaying) {
            long elapsed = (System.currentTimeMillis() - playStartTime) / 1000;
            if (elapsed > 0) {
                statsManager.registrarTiempoEscucha(elapsed);
            }
            playStartTime = 0;
        }
    }

    public Song getCurrentSong() { return currentSong; }
    public boolean isPlaying() { return isPlaying; }
    public boolean isPaused() { return isPaused; }
    public Pila getHistorial() { return historial; }
    public Cola getColaReproduccion() { return colaReproduccion; }
    public StatsManager getStatsManager() { return statsManager; }

    /** Devuelve la siguiente cancion segun el modo actual sin avanzar. */
    public Song getNextSong() {
        if (currentMode == PlayMode.NORMAL) {
            if (nodoActualNormal != null && nodoActualNormal.siguiente != null)
                return nodoActualNormal.siguiente.song;
            if (listaNormal != null && listaNormal.cabeza != null)
                return listaNormal.cabeza.song; // vuelta al inicio
        } else if (currentMode == PlayMode.CIRCULAR) {
            if (nodoActualCircular != null && nodoActualCircular.siguiente != null)
                return nodoActualCircular.siguiente.song;
            if (listaCircular != null && listaCircular.cabeza != null)
                return listaCircular.cabeza.song;
        } else if (currentMode == PlayMode.RANDOM) {
            if (colaReproduccion != null && colaReproduccion.frente != null)
                return colaReproduccion.frente.song;
        }
        return null;
    }

    public int getPosition() {
        if (player != null && isPlaying) {
            return player.getPosition();
        }
        return pauseLocation > 0 ? (int)pauseLocation : 0;
    }
}
