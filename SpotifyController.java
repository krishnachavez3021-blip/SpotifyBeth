package smartplayer.controllers;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import smartplayer.structures.ListaSimple;
import smartplayer.structures.Nodo;
import smartplayer.models.Song;

/**
 * Controlador para integración con la API de Spotify.
 * Usa OAuth 2.0 Authorization Code Flow para autenticarse.
 * Permite importar playlists de Spotify y emparejarlas con archivos locales.
 */
public class SpotifyController {
    
    private static final String AUTH_URL = "https://accounts.spotify.com/authorize";
    private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";
    private static final String API_BASE = "https://api.spotify.com/v1";
    private static final String REDIRECT_URI = "http://localhost:8888/callback";
    private static final int CALLBACK_PORT = 8888;
    
    private String clientId;
    private String clientSecret;
    private String accessToken;
    
    public SpotifyController(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.accessToken = null;
    }
    
    /**
     * Inicia el flujo OAuth: abre el navegador y espera el callback con el código.
     * @return true si la autenticación fue exitosa
     */
    public boolean autenticar() {
        try {
            // Construir URL de autorización
            String scope = "playlist-read-private playlist-read-collaborative";
            String authUrl = AUTH_URL + "?client_id=" + clientId
                    + "&response_type=code"
                    + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8")
                    + "&scope=" + URLEncoder.encode(scope, "UTF-8");
            
            // Abrir navegador
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(authUrl));
            }
            
            // Iniciar servidor local para recibir el callback
            String code = esperarCallback();
            if (code == null) return false;
            
            // Intercambiar código por token
            accessToken = obtenerToken(code);
            return accessToken != null;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Espera la respuesta de Spotify en el servidor local (puerto 8888).
     */
    private String esperarCallback() {
        try (ServerSocket serverSocket = new ServerSocket(CALLBACK_PORT)) {
            serverSocket.setSoTimeout(120000); // 2 minutos de timeout
            Socket socket = serverSocket.accept();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line = reader.readLine();
            
            // Enviar respuesta HTML al navegador
            String response = "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n"
                    + "<html><body><h2>Autenticación exitosa. Puedes cerrar esta ventana.</h2></body></html>";
            OutputStream os = socket.getOutputStream();
            os.write(response.getBytes());
            os.flush();
            socket.close();
            
            // Extraer el código de la URL
            if (line != null && line.contains("code=")) {
                String codeParam = line.split("code=")[1];
                codeParam = codeParam.split(" ")[0]; // Quitar HTTP/1.1
                codeParam = codeParam.split("&")[0]; // Quitar otros params
                return codeParam;
            }
            return null;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Intercambia el authorization code por un access token.
     */
    private String obtenerToken(String code) {
        try {
            URL url = new URL(TOKEN_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            
            // Header de autorización Basic (client_id:client_secret en Base64)
            String credentials = clientId + ":" + clientSecret;
            String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
            conn.setRequestProperty("Authorization", "Basic " + encoded);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            
            // Body
            String body = "grant_type=authorization_code"
                    + "&code=" + code
                    + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8");
            
            OutputStream os = conn.getOutputStream();
            os.write(body.getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();
            
            // Leer respuesta
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            
            // Parsear JSON manualmente (sin dependencias externas)
            String json = sb.toString();
            return extraerValorJSON(json, "access_token");
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Obtiene las playlists del usuario autenticado.
     * @return Arreglo de String[] donde cada elemento es {id, nombre}
     */
    public String[][] obtenerPlaylists() {
        try {
            String json = hacerGetRequest(API_BASE + "/me/playlists?limit=50");
            if (json == null) return new String[0][0];
            
            // Parsear items del JSON
            return parsearPlaylists(json);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new String[0][0];
        }
    }
    
    /**
     * Obtiene las canciones de una playlist de Spotify por su ID.
     * @return ListaSimple con objetos Song (solo con título y artista, sin archivo local)
     */
    public ListaSimple obtenerCancionesDePlaylist(String playlistId) {
        ListaSimple canciones = new ListaSimple();
        try {
            String json = hacerGetRequest(API_BASE + "/playlists/" + playlistId + "/tracks?limit=100");
            if (json == null) return canciones;
            
            // Parsear tracks
            parsearTracks(json, canciones);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return canciones;
    }
    
    /**
     * Empareja canciones de Spotify con archivos locales de la biblioteca.
     * Busca coincidencias por título (ignorando mayúsculas/minúsculas).
     */
    public ListaSimple emparejarConBiblioteca(ListaSimple cancionesSpotify, ListaSimple bibliotecaLocal) {
        ListaSimple emparejadas = new ListaSimple();
        
        Nodo actualSpotify = cancionesSpotify.getCabeza();
        while (actualSpotify != null) {
            String tituloSpotify = actualSpotify.song.getTitle().toLowerCase().trim();
            String artistaSpotify = actualSpotify.song.getArtist().toLowerCase().trim();
            
            Nodo actualLocal = bibliotecaLocal.getCabeza();
            while (actualLocal != null) {
                String tituloLocal = actualLocal.song.getTitle().toLowerCase().trim();
                String artistaLocal = actualLocal.song.getArtist().toLowerCase().trim();
                
                // Coincidencia por título o por título + artista
                if (tituloLocal.contains(tituloSpotify) || tituloSpotify.contains(tituloLocal)) {
                    emparejadas.insertar(actualLocal.song);
                    break;
                }
                if (tituloLocal.equals(tituloSpotify) && artistaLocal.contains(artistaSpotify)) {
                    emparejadas.insertar(actualLocal.song);
                    break;
                }
                actualLocal = actualLocal.siguiente;
            }
            actualSpotify = actualSpotify.siguiente;
        }
        return emparejadas;
    }
    
    /**
     * Realiza una petición GET autenticada a la API de Spotify.
     */
    private String hacerGetRequest(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestProperty("Accept", "application/json");
            
            if (conn.getResponseCode() != 200) {
                return null;
            }
            
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            return sb.toString();
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Extrae un valor simple de un JSON (parser manual básico).
     */
    private String extraerValorJSON(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int idx = json.indexOf(searchKey);
        if (idx == -1) return null;
        
        int colonIdx = json.indexOf(":", idx);
        int startQuote = json.indexOf("\"", colonIdx + 1);
        int endQuote = json.indexOf("\"", startQuote + 1);
        
        if (startQuote == -1 || endQuote == -1) return null;
        return json.substring(startQuote + 1, endQuote);
    }
    
    /**
     * Parsea la lista de playlists desde el JSON de Spotify.
     */
    private String[][] parsearPlaylists(String json) {
        // Contar cuántos "id" hay en items
        String[] items = json.split("\"id\"");
        int count = 0;
        
        // Buscar sección "items"
        int itemsIdx = json.indexOf("\"items\"");
        if (itemsIdx == -1) return new String[0][0];
        
        String itemsJson = json.substring(itemsIdx);
        String[] nameSegments = itemsJson.split("\"name\"");
        String[] idSegments = itemsJson.split("\"id\"");
        
        // El máximo de playlists es min(nameSegments, idSegments) - 1
        int maxPlaylists = Math.min(nameSegments.length, idSegments.length) - 1;
        if (maxPlaylists <= 0) return new String[0][0];
        
        String[][] result = new String[maxPlaylists][2];
        
        for (int i = 1; i <= maxPlaylists; i++) {
            // Extraer ID
            if (i < idSegments.length) {
                String seg = idSegments[i];
                int startQ = seg.indexOf("\"");
                if (startQ != -1) {
                    int endQ = seg.indexOf("\"", startQ + 1);
                    if (endQ != -1) {
                        result[i - 1][0] = seg.substring(startQ + 1, endQ);
                    }
                }
            }
            // Extraer nombre
            if (i < nameSegments.length) {
                String seg = nameSegments[i];
                int startQ = seg.indexOf("\"");
                if (startQ != -1) {
                    int endQ = seg.indexOf("\"", startQ + 1);
                    if (endQ != -1) {
                        result[i - 1][1] = seg.substring(startQ + 1, endQ);
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * Parsea los tracks de una playlist desde el JSON de Spotify.
     */
    private void parsearTracks(String json, ListaSimple canciones) {
        // Buscar cada "track" y extraer name y artists
        String[] trackSegments = json.split("\"track\"");
        
        for (int i = 1; i < trackSegments.length; i++) {
            String seg = trackSegments[i];
            
            // Extraer nombre de la canción
            String nombre = extraerPrimerValor(seg, "name");
            
            // Extraer artista (primer artista encontrado después de "artists")
            String artista = "Desconocido";
            int artistIdx = seg.indexOf("\"artists\"");
            if (artistIdx != -1) {
                String artistSeg = seg.substring(artistIdx);
                String artistName = extraerPrimerValor(artistSeg, "name");
                if (artistName != null) artista = artistName;
            }
            
            if (nombre != null) {
                Song spotifySong = new Song(nombre, artista, "Spotify", "Desconocido", 0, 0, "", "");
                canciones.insertar(spotifySong);
            }
        }
    }
    
    /**
     * Extrae el primer valor de una clave en un segmento JSON.
     */
    private String extraerPrimerValor(String segment, String key) {
        String searchKey = "\"" + key + "\"";
        int idx = segment.indexOf(searchKey);
        if (idx == -1) return null;
        
        int colonIdx = segment.indexOf(":", idx);
        if (colonIdx == -1) return null;
        
        // Buscar siguiente comilla de apertura
        int startQ = segment.indexOf("\"", colonIdx + 1);
        if (startQ == -1) return null;
        int endQ = segment.indexOf("\"", startQ + 1);
        if (endQ == -1) return null;
        
        return segment.substring(startQ + 1, endQ);
    }
    
    public boolean isAutenticado() {
        return accessToken != null;
    }
}
