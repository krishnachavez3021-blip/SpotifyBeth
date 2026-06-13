package smartplayer.controllers;

import smartplayer.models.Playlist;
import smartplayer.models.Song;
import smartplayer.structures.ListaPlaylists;
import smartplayer.structures.ListaSimple;
import smartplayer.structures.Nodo;
import smartplayer.utils.Encriptacion;
import smartplayer.structures.ArbolABB;
import smartplayer.utils.FileManager;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlaylistController {
    private ListaPlaylists playlists;

    public PlaylistController() {
        playlists = new ListaPlaylists();
    }

    public void crearPlaylist(String nombre) {
        playlists.insertar(new Playlist(nombre));
    }
    
    public void eliminarPlaylist(String nombre) {
        playlists.eliminar(nombre);
    }
    
    public ListaPlaylists getPlaylists() {
        return playlists;
    }
    
    public Playlist getPlaylist(String nombre) {
        ListaPlaylists.NodoPlaylist actual = playlists.cabeza;
        while (actual != null) {
            if (actual.playlist.getNombre().equals(nombre)) {
                return actual.playlist;
            }
            actual = actual.siguiente;
        }
        return null;
    }
    
    public void encriptarYGuardar(String nombrePlaylist, String archivo, String tipoRecorrido) {
        Playlist p = getPlaylist(nombrePlaylist);
        if (p == null) return;
        
        ArbolABB arbol = new ArbolABB();
        smartplayer.structures.Nodo actual = p.getCanciones().getCabeza();
        while (actual != null) {
            arbol.insertar(actual.song);
            actual = actual.siguiente;
        }
        
        ListaSimple encriptada = Encriptacion.obtenerListaPorRecorrido(arbol, tipoRecorrido);
        Encriptacion.guardarPlaylistEncriptada(encriptada, archivo, nombrePlaylist);
    }

    /**
     * Resultado de la importacion de una playlist.
     */
    public static class ResultadoImportacion {
        public final boolean exito;
        public final String nombrePlaylist;
        public final int cancionesEncontradas;
        public final int cancionesFaltantes;
        public final List<String> nombresFaltantes;
        public final String mensaje;

        public ResultadoImportacion(boolean exito, String nombrePlaylist,
                                     int cancionesEncontradas, int cancionesFaltantes,
                                     List<String> nombresFaltantes, String mensaje) {
            this.exito = exito;
            this.nombrePlaylist = nombrePlaylist;
            this.cancionesEncontradas = cancionesEncontradas;
            this.cancionesFaltantes = cancionesFaltantes;
            this.nombresFaltantes = nombresFaltantes;
            this.mensaje = mensaje;
        }
    }
    
    /**
     * Recupera y desencripta una playlist desde archivo.
     * Busca las canciones en la biblioteca por path; si no las encuentra,
     * intenta buscar por titulo + artista.
     * Retorna un ResultadoImportacion con el estado de la operacion.
     */
    public ResultadoImportacion recuperarYDesencriptar(String archivo, ListaSimple biblioteca) {
        List<String> faltantes = new ArrayList<>();
        int encontradas = 0;
        String nombrePlaylist = null;

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String nombreEnc = br.readLine();
            if (nombreEnc == null) {
                return new ResultadoImportacion(false, null, 0, 0, faltantes,
                    "El archivo esta vacio o es invalido.");
            }
            
            nombrePlaylist = Encriptacion.desencriptar(nombreEnc);
            Playlist p = new Playlist(nombrePlaylist);
            
            String pathEnc;
            int lineaNum = 0;
            while ((pathEnc = br.readLine()) != null) {
                lineaNum++;
                if (pathEnc.trim().isEmpty()) continue;
                String path = Encriptacion.desencriptar(pathEnc);
                
                // Buscar por path exacto
                Nodo actual = biblioteca.getCabeza();
                Song encontrada = null;
                while (actual != null) {
                    if (actual.song.getPath().equals(path)) {
                        encontrada = actual.song;
                        break;
                    }
                    actual = actual.siguiente;
                }

                // Si no se encontro por path, intentar buscar por titulo/artista
                // El path puede contener algo como "artista - titulo"
                if (encontrada == null) {
                    // Extraer nombre de archivo sin extension como posible titulo
                    File f = new File(path);
                    String nombreArchivo = f.getName();
                    String tituloBusqueda = nombreArchivo.replaceAll("(?i)\\.mp3$", "").trim();
                    
                    actual = biblioteca.getCabeza();
                    while (actual != null) {
                        Song s = actual.song;
                        String candidato = s.getTitle();
                        if (candidato == null) candidato = "";
                        // Comparacion flexible: titulo exacto o titulo+path
                        if (candidato.equalsIgnoreCase(tituloBusqueda) ||
                            s.getPath().endsWith(nombreArchivo)) {
                            encontrada = s;
                            break;
                        }
                        actual = actual.siguiente;
                    }
                }
                
                if (encontrada != null) {
                    p.getCanciones().insertar(encontrada);
                    encontradas++;
                } else {
                    // Último recurso: intentar cargar directamente del disco
                    File f = new File(path);
                    if (f.exists() && f.getName().toLowerCase().endsWith(".mp3")) {
                        ListaSimple mini = FileManager.scanDirectory(f.getParentFile());
                        Nodo m = mini.getCabeza();
                        while (m != null) {
                            if (m.song.getPath().equals(path)) { encontrada = m.song; break; }
                            m = m.siguiente;
                        }
                    }
                    if (encontrada != null) {
                        p.getCanciones().insertar(encontrada);
                        encontradas++;
                    } else {
                        faltantes.add(path);
                    }
                }
            }
            
            playlists.insertar(p);

            String mensaje;
            if (faltantes.isEmpty()) {
                mensaje = "Playlist importada exitosamente.\nCanciones: " + encontradas;
            } else {
                mensaje = "Playlist importada parcialmente.\nEncontradas: " + encontradas +
                          " | Faltantes: " + faltantes.size() +
                          "\nAlgunas canciones no estan en la biblioteca.";
            }
            return new ResultadoImportacion(true, nombrePlaylist, encontradas,
                faltantes.size(), faltantes, mensaje);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultadoImportacion(false, nombrePlaylist, encontradas,
                faltantes.size(), faltantes,
                "Error al importar: " + e.getMessage());
        }
    }

    /**
     * Último nombre de álbum importado (se almacena durante importarAlbumComoCancionesSeparadas).
     */
    private String ultimoNombreAlbum = "";

    public String ultimoNombreAlbumImportado() {
        return ultimoNombreAlbum;
    }

    /**
     * Importa un álbum encriptado y retorna cada canción como un elemento Song[] separado.
     * Permite crear playlists individuales por canción en la vista.
     * Retorna null si falla, o lista vacía si no hay canciones encontradas.
     */
    public java.util.List<Song[]> importarAlbumComoCancionesSeparadas(String archivo, ListaSimple biblioteca) {
        java.util.List<Song[]> resultado = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new java.io.InputStreamReader(new java.io.FileInputStream(archivo), "UTF-8"))) {
            String primeraLinea = br.readLine();
            if (primeraLinea == null) return null;

            String primeraDesc = Encriptacion.desencriptar(primeraLinea);
            if (primeraDesc.startsWith("__ALBUM__:")) {
                ultimoNombreAlbum = primeraDesc.substring("__ALBUM__:".length()).trim();
            } else {
                ultimoNombreAlbum = primeraDesc.trim();
            }

            String pathEnc;
            while ((pathEnc = br.readLine()) != null) {
                if (pathEnc.trim().isEmpty()) continue;
                String path = Encriptacion.desencriptar(pathEnc);

                Song cancion = null;
                // Buscar por path exacto
                Nodo actual = biblioteca.getCabeza();
                while (actual != null) {
                    if (actual.song.getPath().equals(path)) {
                        cancion = actual.song;
                        break;
                    }
                    actual = actual.siguiente;
                }
                // Fallback: buscar por nombre de archivo
                if (cancion == null) {
                    String nombreArchivo = new File(path).getName();
                    actual = biblioteca.getCabeza();
                    while (actual != null) {
                        if (new File(actual.song.getPath()).getName().equalsIgnoreCase(nombreArchivo)) {
                            cancion = actual.song;
                            break;
                        }
                        actual = actual.siguiente;
                    }
                }
                if (cancion != null) {
                    resultado.add(new Song[]{cancion});
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return resultado;
    }

    // ==================== Álbumes ====================

    /**
     * Exporta todas las canciones de un álbum como archivo encriptado.
     * Formato: primera línea = "__ALBUM__:nombreAlbum" encriptado, luego paths encriptados.
     */
    public void exportarAlbumEncriptado(String nombreAlbum, java.util.List<Song> canciones, String archivo) {
        try (java.io.PrintWriter out = new java.io.PrintWriter(
                new java.io.OutputStreamWriter(new java.io.FileOutputStream(archivo), "UTF-8"))) {
            out.println(Encriptacion.encriptar("__ALBUM__:" + nombreAlbum));
            for (Song s : canciones) {
                out.println(Encriptacion.encriptar(s.getPath()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Importa un álbum encriptado y lo agrega como playlist.
     * Retorna el nombre del álbum/playlist creada, o null si falló.
     */
    public String importarAlbumEncriptado(String archivo, ListaSimple biblioteca) {
        try (BufferedReader br = new BufferedReader(
                new java.io.InputStreamReader(new java.io.FileInputStream(archivo), "UTF-8"))) {
            String primeraLinea = br.readLine();
            if (primeraLinea == null) return null;

            String primeraDesc = Encriptacion.desencriptar(primeraLinea);

            // Detectar si es archivo de álbum o playlist normal encriptada
            String nombreAlbum;
            if (primeraDesc.startsWith("__ALBUM__:")) {
                nombreAlbum = primeraDesc.substring("__ALBUM__:".length());
            } else {
                nombreAlbum = primeraDesc;
            }

            Playlist p = new Playlist(nombreAlbum);
            int encontradas = 0;

            String pathEnc;
            while ((pathEnc = br.readLine()) != null) {
                if (pathEnc.trim().isEmpty()) continue;
                String path = Encriptacion.desencriptar(pathEnc);

                // Buscar por path exacto
                Nodo actual = biblioteca.getCabeza();
                boolean hallada = false;
                while (actual != null) {
                    if (actual.song.getPath().equals(path)) {
                        p.getCanciones().insertar(actual.song);
                        encontradas++;
                        hallada = true;
                        break;
                    }
                    actual = actual.siguiente;
                }

                // Fallback: buscar por nombre de archivo
                if (!hallada) {
                    String nombreArchivo = new File(path).getName();
                    actual = biblioteca.getCabeza();
                    while (actual != null) {
                        if (new File(actual.song.getPath()).getName().equalsIgnoreCase(nombreArchivo)) {
                            p.getCanciones().insertar(actual.song);
                            encontradas++;
                            break;
                        }
                        actual = actual.siguiente;
                    }
                }
            }

            if (encontradas > 0) {
                playlists.insertar(p);
                return nombreAlbum;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
