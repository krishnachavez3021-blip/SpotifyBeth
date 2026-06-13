package smartplayer.controllers;

import smartplayer.models.Song;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONObject;

/**
 * Controlador para obtener y gestionar letras de canciones.
 * Busca en multiples fuentes con fallback:
 *   1. Archivo local junto al MP3 (_letra.txt, .lrc, .txt)
 *   2. lyrics.ovh API
 *   3. lyrist.vercel.app API
 *   4. some-random-api.com API
 *   5. Scraping web: Genius.com, AZLyrics.com
 * Limpia titulo y artista antes de buscar para mayor precision.
 * Auto-guarda la letra cuando la encuentra via API o scraping.
 */
public class LyricsController {

    private static final String API_LYRICS_OVH   = "https://api.lyrics.ovh/v1/";
    private static final String API_LYRIST       = "https://lyrist.vercel.app/api/";
    private static final String API_SOME_RANDOM  = "https://some-random-api.com/others/lyrics?title=";
    private static final String GOOGLE_SEARCH    = "https://www.google.com/search?q=";
    private static final int    TIMEOUT_MS       = 8000;  // 8 segundos por API JSON
    private static final int    TIMEOUT_SCRAPING = 12000; // 12 segundos para scraping web
    private static final int    TIMEOUT_GOOGLE   = 15000; // 15 segundos para Google

    public LyricsController() {
    }

    /**
     * Obtiene la letra de una cancion.
     * Prioridad: archivo local > APIs JSON > scraping web > null.
     * Auto-guarda la letra si la encuentra en red.
     */
    public String obtenerLetra(Song song) {
        if (song == null) return null;

        // 1. Intentar cargar letra local (archivo junto al MP3)
        String letraLocal = cargarLetraLocal(song);
        if (letraLocal != null && !letraLocal.trim().isEmpty()) {
            return letraLocal;
        }

        // Limpiar artista y titulo antes de buscar
        String artista = limpiarArtista(song.getArtist());
        String titulo  = limpiarTitulo(song.getTitle());

        // 2. API lyrics.ovh
        String letra = buscarEnLyricsOvh(artista, titulo);
        if (letra != null) {
            guardarLetraLocal(song, letra); // auto-guardar para proximas veces
            return letra;
        }

        // 3. API lyrist.vercel.app
        letra = buscarEnLyrist(artista, titulo);
        if (letra != null) {
            guardarLetraLocal(song, letra);
            return letra;
        }

        // 4. API some-random-api.com
        letra = buscarEnSomeRandomApi(artista, titulo);
        if (letra != null) {
            guardarLetraLocal(song, letra);
            return letra;
        }

        // 5. Scraping web: Genius y AZLyrics como fallback
        letra = buscarEnWeb(artista, titulo);
        if (letra != null) {
            guardarLetraLocal(song, letra);
            return letra;
        }

        // 6. Scraping de Google como ultimo recurso automatico
        letra = buscarEnGoogle(artista, titulo);
        if (letra != null) {
            guardarLetraLocal(song, letra);
            return letra;
        }

        // 7. Reintentar con nombres originales si cambiaron al limpiarlos
        if (!artista.equals(song.getArtist()) || !titulo.equals(song.getTitle())) {
            letra = buscarEnLyricsOvh(song.getArtist(), song.getTitle());
            if (letra != null) { guardarLetraLocal(song, letra); return letra; }
            letra = buscarEnLyrist(song.getArtist(), song.getTitle());
            if (letra != null) { guardarLetraLocal(song, letra); return letra; }
        }

        // 7. Ultimo intento: sin acentos ni caracteres especiales
        String artistaSinAcentos = quitarAcentos(artista);
        String tituloSinAcentos  = quitarAcentos(titulo);
        if (!artistaSinAcentos.equals(artista) || !tituloSinAcentos.equals(titulo)) {
            letra = buscarEnLyricsOvh(artistaSinAcentos, tituloSinAcentos);
            if (letra != null) { guardarLetraLocal(song, letra); return letra; }
            letra = buscarEnLyrist(artistaSinAcentos, tituloSinAcentos);
            if (letra != null) { guardarLetraLocal(song, letra); return letra; }
        }

        return null; // No se encontro la letra
    }

    /**
     * Busca la letra en las APIs y scraping (uso publico desde LyricsPanel).
     */
    public String buscarLetraEnAPI(String artista, String titulo) {
        String artistaLimpio = limpiarArtista(artista);
        String tituloLimpio  = limpiarTitulo(titulo);

        // Intento 1: lyrics.ovh con nombres limpios
        String result = buscarEnLyricsOvh(artistaLimpio, tituloLimpio);
        if (result != null) return result;

        // Intento 2: lyrist.vercel.app
        result = buscarEnLyrist(artistaLimpio, tituloLimpio);
        if (result != null) return result;

        // Intento 3: some-random-api.com
        result = buscarEnSomeRandomApi(artistaLimpio, tituloLimpio);
        if (result != null) return result;

        // Intento 4: scraping web (Genius, AZLyrics)
        result = buscarEnWeb(artistaLimpio, tituloLimpio);
        if (result != null) return result;

        // Intento 5: scraping automatico de Google
        result = buscarEnGoogle(artistaLimpio, tituloLimpio);
        if (result != null) return result;

        // Intento 6: nombres originales si son diferentes
        if (!artistaLimpio.equals(artista) || !tituloLimpio.equals(titulo)) {
            result = buscarEnLyricsOvh(artista, titulo);
            if (result != null) return result;
            result = buscarEnLyrist(artista, titulo);
            if (result != null) return result;
        }

        // Intento 6: sin acentos
        result = buscarEnLyricsOvh(quitarAcentos(artistaLimpio), quitarAcentos(tituloLimpio));
        if (result != null) return result;
        result = buscarEnLyrist(quitarAcentos(artistaLimpio), quitarAcentos(tituloLimpio));
        return result;
    }

    /**
     * Limpia el titulo de una cancion eliminando sufijos comunes no utiles.
     * - Extrae titulo real si el campo tiene formato 'Artista - Titulo'
     * - Quita '(Video Oficial)', '(Video Ofici...)', parentesis truncados
     * - Quita puntos suspensivos al final (..)
     */
    public String limpiarTitulo(String titulo) {
        if (titulo == null || titulo.trim().isEmpty()) {
            return titulo != null ? titulo : "";
        }
        String t = titulo;

        // Quitar extension .mp3 / .MP3
        t = t.replaceAll("(?i)\\.mp3$", "");

        // Si el titulo tiene formato 'Artista - Titulo' o 'Artista, Artista2 - Titulo',
        // usar solo la parte derecha como titulo real
        if (t.contains(" - ")) {
            String parteDerecha = t.substring(t.indexOf(" - ") + 3).trim();
            if (!parteDerecha.isEmpty()) {
                t = parteDerecha;
            }
        }

        // Quitar numeracion al inicio: "01 - ", "01. ", "1. ", "01 "
        t = t.replaceAll("^\\d{1,3}\\s*[-.]\\s*", "");
        t = t.replaceAll("^\\d{1,3}\\s+", "");

        // Quitar marcadores de video oficial (incluyendo texto truncado con '..')
        t = t.replaceAll("(?i)\\s*[\\(\\[]\\s*official\\s*(music\\s*)?video\\s*[\\)\\]]", "");
        t = t.replaceAll("(?i)\\s*[\\(\\[]\\s*official\\s*audio\\s*[\\)\\]]", "");
        t = t.replaceAll("(?i)\\s*[\\(\\[]\\s*official\\s*[\\)\\]]", "");
        t = t.replaceAll("(?i)\\s*[\\(\\[]\\s*audio\\s*[\\)\\]]", "");
        t = t.replaceAll("(?i)\\s*[\\(\\[]\\s*hd\\s*[\\)\\]]", "");
        t = t.replaceAll("(?i)\\s*[\\(\\[]\\s*hq\\s*[\\)\\]]", "");
        t = t.replaceAll("(?i)\\s*[\\(\\[]\\s*4k\\s*[\\)\\]]", "");
        t = t.replaceAll("(?i)\\s*[\\(\\[]\\s*lyrics?\\s*[\\)\\]]", "");
        t = t.replaceAll("(?i)\\s*[\\(\\[]\\s*with\\s+lyrics?\\s*[\\)\\]]", "");
        t = t.replaceAll("(?i)\\s*[\\(\\[]\\s*letras?\\s*[\\)\\]]", "");
        t = t.replaceAll("(?i)\\s*[\\(\\[]\\s*lyric\\s*video\\s*[\\)\\]]", "");
        t = t.replaceAll("(?i)\\s*[\\(\\[]\\s*visualizer\\s*[\\)\\]]", "");
        t = t.replaceAll("(?i)\\s*[\\(\\[]\\s*live\\s*[\\)\\]]", "");
        t = t.replaceAll("(?i)\\s*[\\(\\[]\\s*video\\s*oficial\\s*[\\)\\]]", "");
        t = t.replaceAll("(?i)\\s*[\\(\\[]\\s*clip\\s*oficial?\\s*[\\)\\]]", "");
        t = t.replaceAll("(?i)\\s*[\\(\\[]\\s*remastered?\\s*[\\)\\]]", "");
        t = t.replaceAll("(?i)\\s*[\\(\\[]\\s*version\\s*[\\)\\]]", "");

        // Quitar parentesis truncados que terminan en '..' (ej: "(Video Ofici..")
        t = t.replaceAll("\\s*\\([^)]*\\.\\.[^)]*\\)?\\s*", " ");
        // Quitar parentesis sin cerrar al final del titulo (truncados)
        t = t.replaceAll("\\s*\\([^)]*$", "");

        // Quitar "feat. xxx" o "ft. xxx" del titulo
        t = t.replaceAll("(?i)\\s*(feat\\.?|ft\\.?|featuring)\\s+[^\\(\\[\\n\\r]+", "");

        // Quitar puntos suspensivos al final del titulo (titulo truncado "..") 
        t = t.replaceAll("\\.{2,}$", "").trim();

        return t.trim();
    }

    /**
     * Limpia el nombre del artista eliminando VEVO, colaboraciones y sufijos.
     * - Quita 'VEVO' al final (ej: MartinwhiteVEVO -> Martinwhite)
     * - Quita feat., ft., &, vs., etc.
     */
    public String limpiarArtista(String artista) {
        if (artista == null || artista.trim().isEmpty()) {
            return artista != null ? artista : "";
        }
        String a = artista;

        // Quitar sufijo 'VEVO' al final del nombre (ej: MartinwhiteVEVO -> Martinwhite)
        a = a.replaceAll("(?i)VEVO$", "").trim();

        // Quitar "feat. xxx", "ft. xxx", "featuring xxx" al final
        a = a.replaceAll("(?i)\\s*(feat\\.?|ft\\.?|featuring)\\s+.+$", "");
        // Quitar lo que haya entre parentesis al final
        a = a.replaceAll("\\s*\\(.*?\\)\\s*$", "");
        // Si tiene " & " o " vs. " o coma, usar solo el primer artista
        if (a.contains(" & ")) a = a.substring(0, a.indexOf(" & ")).trim();
        if (a.contains(" vs.")) a = a.substring(0, a.indexOf(" vs.")).trim();
        if (a.contains(" vs ")) a = a.substring(0, a.indexOf(" vs ")).trim();
        if (a.contains(",")) a = a.substring(0, a.indexOf(",")).trim();

        return a.trim();
    }

    /**
     * Elimina acentos y diacriticos de un texto para mejorar la busqueda en APIs.
     */
    private String quitarAcentos(String texto) {
        if (texto == null) return "";
        String normalizado = Normalizer.normalize(texto, Normalizer.Form.NFD);
        return normalizado.replaceAll("[^\\p{ASCII}]", "");
    }

    // ==================== APIs JSON de letras ====================

    /**
     * Busca la letra en lyrics.ovh.
     * Formato: GET https://api.lyrics.ovh/v1/{artista}/{titulo}
     */
    private String buscarEnLyricsOvh(String artista, String titulo) {
        if (!parametrosValidos(artista, titulo)) return null;
        try {
            String encodedArtist = URLEncoder.encode(artista.trim(), "UTF-8");
            String encodedTitle  = URLEncoder.encode(titulo.trim(), "UTF-8");
            String urlStr = API_LYRICS_OVH + encodedArtist + "/" + encodedTitle;

            String respuesta = hacerGetRequest(urlStr, TIMEOUT_MS);
            if (respuesta == null) return null;

            JSONObject json = new JSONObject(respuesta);
            if (json.has("lyrics")) {
                String letra = json.getString("lyrics").trim();
                return letra.isEmpty() ? null : letra;
            }
        } catch (Exception e) {
            System.err.println("[LyricsOvh] Error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Busca la letra en lyrist.vercel.app.
     * Formato: GET https://lyrist.vercel.app/api/{titulo}/{artista}
     */
    private String buscarEnLyrist(String artista, String titulo) {
        if (!parametrosValidos(artista, titulo)) return null;
        try {
            String encodedTitle  = URLEncoder.encode(titulo.trim(), "UTF-8");
            String encodedArtist = URLEncoder.encode(artista.trim(), "UTF-8");
            String urlStr = API_LYRIST + encodedTitle + "/" + encodedArtist;

            String respuesta = hacerGetRequest(urlStr, TIMEOUT_MS);
            if (respuesta == null) return null;

            JSONObject json = new JSONObject(respuesta);
            if (json.has("lyrics")) {
                String letra = json.getString("lyrics").trim();
                return letra.isEmpty() ? null : letra;
            }
        } catch (Exception e) {
            System.err.println("[Lyrist] Error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Busca la letra en some-random-api.com.
     * Formato: GET https://some-random-api.com/others/lyrics?title={artista} {titulo}
     */
    private String buscarEnSomeRandomApi(String artista, String titulo) {
        if (!parametrosValidos(artista, titulo)) return null;
        try {
            String query = artista.trim() + " " + titulo.trim();
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String urlStr = API_SOME_RANDOM + encodedQuery;

            String respuesta = hacerGetRequest(urlStr, TIMEOUT_MS);
            if (respuesta == null) return null;

            JSONObject json = new JSONObject(respuesta);
            if (json.has("lyrics")) {
                String letra = json.getString("lyrics").trim();
                return letra.isEmpty() ? null : letra;
            }
        } catch (Exception e) {
            System.err.println("[SomeRandomApi] Error: " + e.getMessage());
        }
        return null;
    }

    // ==================== Scraping web como fallback ====================

    /**
     * Fallback de scraping web: intenta Genius primero, luego AZLyrics.
     * Solo se llama cuando todas las APIs JSON fallan.
     */
    public String buscarEnWeb(String artista, String titulo) {
        if (!parametrosValidos(artista, titulo)) return null;

        // Intentar Genius
        String letra = buscarEnGenius(artista, titulo);
        if (letra != null) {
            System.out.println("[Web] Letra encontrada en Genius para: " + artista + " - " + titulo);
            return letra;
        }

        // Si Genius falla, intentar AZLyrics
        letra = buscarEnAZLyrics(artista, titulo);
        if (letra != null) {
            System.out.println("[Web] Letra encontrada en AZLyrics para: " + artista + " - " + titulo);
            return letra;
        }

        return null;
    }

    /**
     * Scraping de Genius.com.
     * URL: https://genius.com/{artista}-{titulo}-lyrics
     * Extrae el contenido de los divs con data-lyrics-container="true".
     */
    private String buscarEnGenius(String artista, String titulo) {
        try {
            // Preparar slug: lowercase, caracteres especiales -> guion
            String artistaSlug = prepararSlugGenius(artista);
            String tituloSlug  = prepararSlugGenius(titulo);
            String urlStr = "https://genius.com/" + artistaSlug + "-" + tituloSlug + "-lyrics";

            System.out.println("[Genius] Intentando: " + urlStr);
            String html = hacerGetRequestWeb(urlStr);
            if (html == null) return null;

            // Extraer contenido de divs con data-lyrics-container="true"
            StringBuilder letraBuilder = new StringBuilder();
            Pattern pattern = Pattern.compile(
                "data-lyrics-container=\"true\"[^>]*>(.*?)</div>",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE
            );
            Matcher matcher = pattern.matcher(html);
            while (matcher.find()) {
                String fragmento = matcher.group(1);
                // Limpiar HTML del fragmento y agregar a la letra
                fragmento = limpiarHTML(fragmento);
                if (!fragmento.trim().isEmpty()) {
                    letraBuilder.append(fragmento.trim()).append("\n");
                }
            }

            String letra = letraBuilder.toString().trim();
            // Verificar que tiene contenido suficiente (al menos 50 caracteres)
            return (letra.length() > 50) ? letra : null;
        } catch (Exception e) {
            System.err.println("[Genius] Error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Scraping de AZLyrics.com.
     * URL: https://www.azlyrics.com/lyrics/{artista}/{titulo}.html
     * Extrae texto entre los comentarios de uso de contenido y el banner MxM.
     */
    private String buscarEnAZLyrics(String artista, String titulo) {
        try {
            // Preparar slug: solo alfanumerico lowercase, sin espacios ni caracteres especiales
            String artistaSlug = prepararSlugAZLyrics(artista);
            String tituloSlug  = prepararSlugAZLyrics(titulo);
            String urlStr = "https://www.azlyrics.com/lyrics/" + artistaSlug + "/" + tituloSlug + ".html";

            System.out.println("[AZLyrics] Intentando: " + urlStr);
            String html = hacerGetRequestWeb(urlStr);
            if (html == null) return null;

            // Buscar el bloque de letra entre los marcadores de AZLyrics
            String marcadorInicio = "<!-- Usage of azlyrics.com content";
            String marcadorFin    = "<!-- MxM banner";

            int inicio = html.indexOf(marcadorInicio);
            int fin    = (inicio >= 0) ? html.indexOf(marcadorFin, inicio) : -1;

            if (inicio < 0 || fin < 0 || fin <= inicio) {
                // Intentar marcador alternativo si no encontro los principales
                System.err.println("[AZLyrics] Marcadores principales no encontrados, intentando alternativo");
                return null;
            }

            // Saltar al primer '>' despues del marcador de inicio (fin del comentario)
            int inicioDiv = html.indexOf(">", inicio);
            if (inicioDiv < 0) return null;
            inicioDiv++; // saltar el '>'

            String fragmento = html.substring(inicioDiv, fin);
            String letra = limpiarHTML(fragmento).trim();
            return (letra.length() > 50) ? letra : null;
        } catch (Exception e) {
            System.err.println("[AZLyrics] Error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Scraping de Google Search como fallback automatico.
     * Busca "artista titulo lyrics" en Google y extrae el bloque de letra
     * que Google muestra en los resultados (knowledge panel / featured snippet).
     *
     * Patrones soportados:
     *  - class="hwc" o class="LGOjhe"
     *  - divs con class que contenga 'lyric' o 'lyrics'
     *  - spans con jsname="*lyric*" o data-lyric
     *  - bloques de texto largo (>200 chars) con saltos de linea
     */
    private String buscarEnGoogle(String artista, String titulo) {
        if (!parametrosValidos(artista, titulo)) return null;
        try {
            String query = artista.trim() + " " + titulo.trim() + " lyrics";
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String urlStr = GOOGLE_SEARCH + encodedQuery;

            System.out.println("[Google] Intentando: " + urlStr);
            String html = hacerGetRequestGoogle(urlStr);
            if (html == null || html.isEmpty()) return null;

            String letra = extraerLetraDeGoogle(html);
            if (letra != null && letra.length() > 80) {
                System.out.println("[Google] Letra encontrada para: " + artista + " - " + titulo);
                return letra;
            }
        } catch (Exception e) {
            System.err.println("[Google] Error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Realiza la peticion GET a Google con headers de navegador real.
     */
    private String hacerGetRequestGoogle(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(TIMEOUT_GOOGLE);
            conn.setReadTimeout(TIMEOUT_GOOGLE);
            // User-Agent REAL de Chrome para evitar bloqueos
            conn.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            conn.setRequestProperty("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8");
            conn.setRequestProperty("Accept-Language", "es-ES,es;q=0.9,en;q=0.8");
            conn.setRequestProperty("Accept-Encoding", "identity");
            conn.setRequestProperty("Cache-Control", "max-age=0");
            conn.setInstanceFollowRedirects(true);

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("[Google] HTTP " + responseCode + " para: " + urlStr);
                return null;
            }

            String contentType = conn.getContentType();
            String charset = "UTF-8";
            if (contentType != null && contentType.contains("charset=")) {
                String cs = contentType.substring(contentType.indexOf("charset=") + 8).trim();
                if (!cs.isEmpty()) charset = cs;
            }

            BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), charset));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            br.close();
            conn.disconnect();
            return sb.toString();
        } catch (Exception e) {
            System.err.println("[Google] Error de conexion: " + e.getMessage());
            return null;
        }
    }

    /**
     * Extrae la letra del HTML de Google probando varios patrones.
     */
    private String extraerLetraDeGoogle(String html) {
        if (html == null) return null;

        // Patron 1: class="hwc" o class="LGOjhe" (bloques de letra de Google)
        String letra = extraerConPatron(html,
            "<div[^>]*class=\"[^\"]*(?:hwc|LGOjhe)[^\"]*\"[^>]*>(.*?)</div>");
        if (letra != null && letra.length() > 80) return letra;

        // Patron 2: divs con class que contenga 'lyric' o 'lyrics'
        letra = extraerConPatron(html,
            "<div[^>]*class=\"[^\"]*(?:lyric|lyrics)[^\"]*\"[^>]*>(.*?)</div>");
        if (letra != null && letra.length() > 80) return letra;

        // Patron 3: spans con jsname que contenga lyric
        letra = extraerConPatron(html,
            "<span[^>]*jsname=\"[^\"]*(?:lyric|lyrics)[^\"]*\"[^>]*>(.*?)</span>");
        if (letra != null && letra.length() > 80) return letra;

        // Patron 4: data-lyric o data-attribute relacionado
        letra = extraerConPatron(html,
            "<[^>]*data-lyric[^>]*>(.*?)</[^>]*>");
        if (letra != null && letra.length() > 80) return letra;

        // Patron 5: buscar bloques de texto largo con saltos de linea <br> o <div>
        // que parezcan letra de cancion (mas de 200 chars)
        letra = buscarBloqueLargoLetra(html);
        if (letra != null && letra.length() > 80) return letra;

        return null;
    }

    /**
     * Extrae texto con un patron regex y limpia el HTML.
     */
    private String extraerConPatron(String html, String regex) {
        try {
            Pattern pattern = Pattern.compile(regex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(html);
            StringBuilder sb = new StringBuilder();
            while (matcher.find()) {
                String fragmento = matcher.group(1);
                fragmento = limpiarHTML(fragmento);
                if (fragmento.trim().length() > 20) {
                    sb.append(fragmento.trim()).append("\n\n");
                }
            }
            String resultado = sb.toString().trim();
            return resultado.isEmpty() ? null : resultado;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Busca bloques de texto largo dentro del HTML que parezcan letra.
     * Busca elementos con multiples <br> o <div> que sumen mas de 200 caracteres.
     */
    private String buscarBloqueLargoLetra(String html) {
        try {
            // Buscar divs que contengan multiples <br> y texto largo
            Pattern pattern = Pattern.compile(
                "<div[^>]*>(.*?)</div>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(html);
            String mejorCandidato = null;
            int mejorLongitud = 0;

            while (matcher.find()) {
                String contenido = matcher.group(1);
                // Debe tener al menos 3 saltos de linea (<br> o </div> internos)
                if (contenido.split("<br").length >= 3 || contenido.split("</div>").length >= 2) {
                    String limpio = limpiarHTML(contenido);
                    if (limpio.length() > 200 && limpio.length() > mejorLongitud) {
                        mejorLongitud = limpio.length();
                        mejorCandidato = limpio;
                    }
                }
            }
            return mejorCandidato;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Prepara un slug para URL de Genius:
     * lowercase, caracteres no alfanumericos -> guion, sin guiones al inicio/final.
     */
    private String prepararSlugGenius(String texto) {
        if (texto == null) return "";
        String slug = texto.toLowerCase().trim();
        slug = quitarAcentos(slug);
        // Reemplazar caracteres no alfanumericos por guion
        slug = slug.replaceAll("[^a-z0-9]+", "-");
        // Quitar guiones al inicio y final
        slug = slug.replaceAll("^-+|-+$", "");
        return slug;
    }

    /**
     * Prepara un slug para URL de AZLyrics:
     * solo letras y numeros, lowercase, sin espacios ni caracteres especiales.
     */
    private String prepararSlugAZLyrics(String texto) {
        if (texto == null) return "";
        String slug = texto.toLowerCase().trim();
        slug = quitarAcentos(slug);
        // Quitar todo lo que no sea letra o numero
        slug = slug.replaceAll("[^a-z0-9]", "");
        return slug;
    }

    /**
     * Limpia tags HTML de un fragmento y devuelve texto plano con saltos de linea.
     * Convierte <br> y </p> en saltos de linea, luego elimina los demas tags.
     */
    private String limpiarHTML(String html) {
        if (html == null) return "";
        String texto = html;
        // <br> y <br/> -> salto de linea
        texto = texto.replaceAll("(?i)<br\\s*/?>", "\n");
        // </p> -> salto de linea
        texto = texto.replaceAll("(?i)</p>", "\n");
        // Quitar todos los demas tags HTML
        texto = texto.replaceAll("<[^>]+>", "");
        // Decodificar entidades HTML comunes
        texto = texto.replace("&amp;", "&")
                     .replace("&lt;", "<")
                     .replace("&gt;", ">")
                     .replace("&quot;", "\"")
                     .replace("&#39;", "'")
                     .replace("&apos;", "'")
                     .replace("&#x27;", "'")
                     .replace("&nbsp;", " ")
                     .replace("&#10;", "\n")
                     .replace("&#13;", "\r");
        // Limpiar multiples lineas vacias consecutivas (mas de 2)
        texto = texto.replaceAll("(\r?\n){3,}", "\n\n");
        return texto;
    }

    /**
     * Realiza un GET request HTTP con User-Agent de navegador (para scraping web).
     * Usa un timeout mayor y headers de navegador real para evitar bloqueos.
     */
    private String hacerGetRequestWeb(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(TIMEOUT_SCRAPING);
            conn.setReadTimeout(TIMEOUT_SCRAPING);
            // User-Agent de navegador real para evitar bloqueos de scraping
            conn.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            conn.setRequestProperty("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            conn.setRequestProperty("Accept-Language", "es-ES,es;q=0.9,en;q=0.8");
            // Sin gzip para simplificar la lectura del stream
            conn.setRequestProperty("Accept-Encoding", "identity");
            conn.setInstanceFollowRedirects(true);

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("[Web] HTTP " + responseCode + " para: " + urlStr);
                return null;
            }

            // Detectar charset de la respuesta para leer correctamente
            String contentType = conn.getContentType();
            String charset = "UTF-8";
            if (contentType != null && contentType.contains("charset=")) {
                String cs = contentType.substring(contentType.indexOf("charset=") + 8).trim();
                if (!cs.isEmpty()) charset = cs;
            }

            BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), charset));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            br.close();
            conn.disconnect();
            return sb.toString();
        } catch (Exception e) {
            System.err.println("[Web] Error de conexion: " + e.getMessage());
            return null;
        }
    }

    /**
     * Realiza un GET request HTTP JSON y retorna el cuerpo de la respuesta.
     * Retorna null si hay error, timeout o codigo != 200.
     */
    private String hacerGetRequest(String urlStr, int timeoutMs) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("User-Agent", "SmartPlayer/1.0 (Java)");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) return null;

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            br.close();
            conn.disconnect();
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /** Valida que artista y titulo sean utiles para buscar. */
    private boolean parametrosValidos(String artista, String titulo) {
        return artista != null && titulo != null
                && !artista.trim().isEmpty() && !titulo.trim().isEmpty()
                && !"Desconocido".equalsIgnoreCase(artista.trim())
                && !"Desconocido".equalsIgnoreCase(titulo.trim());
    }

    // ==================== Archivo local ====================

    /**
     * Carga la letra desde un archivo local junto al MP3.
     * Prioridad: {nombre}_letra.txt -> {nombre}.lrc -> {nombre}.txt
     */
    public String cargarLetraLocal(Song song) {
        if (song == null || song.getPath() == null) return null;

        File fileTxt = new File(obtenerRutaLetra(song));
        if (fileTxt.exists()) return leerArchivo(fileTxt);

        File fileLrc = new File(obtenerRutaBase(song) + ".lrc");
        if (fileLrc.exists()) return leerArchivoLrc(fileLrc);

        File fileTxtSimple = new File(obtenerRutaBase(song) + ".txt");
        if (fileTxtSimple.exists()) return leerArchivo(fileTxtSimple);

        return null;
    }

    private String leerArchivo(File file) {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            return null;
        }
    }

    private String leerArchivoLrc(File file) {
        String contenido = leerArchivo(file);
        if (contenido == null) return null;
        StringBuilder sb = new StringBuilder();
        for (String linea : contenido.split("\n")) {
            String limpia = linea.replaceAll("^\\[\\d+:\\d+\\.\\d+\\]\\s*", "").trim();
            if (!limpia.isEmpty()) sb.append(limpia).append("\n");
        }
        return sb.toString().trim();
    }

    private String obtenerRutaBase(Song song) {
        String path = song.getPath();
        int lastDot = path.lastIndexOf('.');
        return lastDot > 0 ? path.substring(0, lastDot) : path;
    }

    /**
     * Guarda la letra en un archivo _letra.txt junto al MP3.
     * Permite carga instantanea en futuras reproducciones sin buscar en red.
     */
    public void guardarLetraLocal(Song song, String letra) {
        if (song == null || song.getPath() == null || letra == null) return;
        String lyricsPath = obtenerRutaLetra(song);
        try (PrintWriter out = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(lyricsPath), StandardCharsets.UTF_8))) {
            out.print(letra);
        } catch (IOException e) {
            System.err.println("Error guardando letra: " + e.getMessage());
        }
    }

    private String obtenerRutaLetra(Song song) {
        String mp3Path = song.getPath();
        if (mp3Path.toLowerCase().endsWith(".mp3")) {
            return mp3Path.substring(0, mp3Path.length() - 4) + "_letra.txt";
        }
        return mp3Path + "_letra.txt";
    }
}
