package smartplayer.controllers;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import smartplayer.models.Song;
import smartplayer.utils.IconGenerator;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Controlador para extracción y caché de carátulas de canciones.
 * Extrae la imagen embebida del MP3 usando mp3agic y la escala al tamaño deseado.
 * Usa IconGenerator para el icono genérico de nota musical.
 */
public class CoverArtController {

    // Caché de miniaturas por ruta de archivo (para la tabla de biblioteca)
    private Map<String, ImageIcon> cacheMiniatura;
    // Caché de carátulas grandes (para el panel del reproductor)
    private Map<String, ImageIcon> cacheGrande;
    // Ícono genérico de nota musical cuando el MP3 no tiene carátula
    private ImageIcon iconoGenerico;
    private ImageIcon miniaturaGenerica;

    public CoverArtController() {
        cacheMiniatura = new HashMap<>();
        cacheGrande = new HashMap<>();
        // Cargar el icono desde classpath o generarlo si no existe
        iconoGenerico = new ImageIcon(IconGenerator.cargarOGenerarIcono(90));
        miniaturaGenerica = new ImageIcon(IconGenerator.cargarOGenerarIcono(32));
    }

    /**
     * Extrae los bytes de la carátula embebida en el archivo MP3.
     * Si el MP3 no tiene carátula embebida, busca archivos de imagen en la misma carpeta.
     *
     * @param mp3Path ruta absoluta del archivo MP3
     * @return bytes de la imagen o null si no hay carátula disponible
     */
    public byte[] extraerCaratula(String mp3Path) {
        // 1. Intentar extraer del ID3v2 tag del MP3
        try {
            Mp3File mp3file = new Mp3File(mp3Path);
            if (mp3file.hasId3v2Tag()) {
                ID3v2 tag = mp3file.getId3v2Tag();
                byte[] albumImage = tag.getAlbumImage();
                if (albumImage != null && albumImage.length > 0) {
                    return albumImage;
                }
            }
        } catch (Exception e) {
            // Silenciar: el archivo puede no tener etiquetas o estar corrupto
        }

        // 2. Si no hay carátula embebida, buscar archivos de imagen en la carpeta
        return buscarCaratulaDesdeArchivo(mp3Path);
    }

    /**
     * Busca archivos de carátula comunes en la carpeta del MP3.
     * Busca: cover.jpg/png, folder.jpg/png, album.jpg/png, front.jpg/png, artwork.jpg/png.
     *
     * @param mp3Path ruta absoluta del MP3
     * @return bytes de la imagen encontrada o null si no hay ninguna
     */
    private byte[] buscarCaratulaDesdeArchivo(String mp3Path) {
        if (mp3Path == null) return null;

        java.io.File mp3File  = new java.io.File(mp3Path);
        java.io.File carpeta  = mp3File.getParentFile();
        if (carpeta == null || !carpeta.exists()) return null;

        String[] nombres = {
            "cover.jpg",   "cover.jpeg",   "cover.png",
            "folder.jpg",  "folder.jpeg",  "folder.png",
            "album.jpg",   "album.jpeg",   "album.png",
            "front.jpg",   "front.jpeg",   "front.png",
            "artwork.jpg", "artwork.jpeg", "artwork.png"
        };

        for (String nombre : nombres) {
            java.io.File archivo = new java.io.File(carpeta, nombre);
            if (archivo.exists() && archivo.isFile()) {
                try {
                    return java.nio.file.Files.readAllBytes(archivo.toPath());
                } catch (Exception e) {
                    // Continuar con el siguiente archivo
                }
            }
        }
        return null;
    }

    /**
     * Obtiene la carátula grande escalada al tamaño indicado.
     * Primero consulta la caché; si no la tiene, extrae del MP3.
     *
     * @param song   canción cuya carátula se desea
     * @param width  ancho deseado en píxeles
     * @param height alto deseado en píxeles
     * @return ImageIcon con la carátula o el ícono genérico
     */
    public ImageIcon getCaratulaGrande(Song song, int width, int height) {
        if (song == null) return iconoGenerico;

        String path = song.getPath();
        if (cacheGrande.containsKey(path)) {
            return cacheGrande.get(path);
        }

        byte[] imageData = song.getCoverArt();
        if (imageData == null) {
            imageData = extraerCaratula(path);
            if (imageData != null) {
                song.setCoverArt(imageData);
            }
        }

        if (imageData != null) {
            ImageIcon icon = crearIconDesdeBytes(imageData, width, height);
            if (icon != null) {
                cacheGrande.put(path, icon);
                return icon;
            }
        }

        // Sin carátula: usar ícono genérico y cachear el resultado
        cacheGrande.put(path, iconoGenerico);
        return iconoGenerico;
    }

    /**
     * Obtiene la miniatura cuadrada de la canción para la tabla de biblioteca.
     *
     * @param song canción
     * @param size tamaño del lado en píxeles
     * @return ImageIcon miniatura
     */
    public ImageIcon getMiniatura(Song song, int size) {
        if (song == null) return miniaturaGenerica;

        String path = song.getPath();
        if (cacheMiniatura.containsKey(path)) {
            return cacheMiniatura.get(path);
        }

        byte[] imageData = song.getCoverArt();
        if (imageData == null) {
            imageData = extraerCaratula(path);
            if (imageData != null) {
                song.setCoverArt(imageData);
            }
        }

        if (imageData != null) {
            ImageIcon icon = crearIconDesdeBytes(imageData, size, size);
            if (icon != null) {
                cacheMiniatura.put(path, icon);
                return icon;
            }
        }

        cacheMiniatura.put(path, miniaturaGenerica);
        return miniaturaGenerica;
    }

    /**
     * Devuelve el ícono genérico de nota musical (90×90 px).
     */
    public ImageIcon getIconoGenerico() {
        return iconoGenerico;
    }

    /**
     * Crea un ImageIcon escalado a partir de los bytes de una imagen.
     *
     * @param data   bytes de la imagen (JPEG, PNG, etc.)
     * @param width  ancho deseado
     * @param height alto deseado
     * @return ImageIcon escalado o null si los bytes no son válidos
     */
    private ImageIcon crearIconDesdeBytes(byte[] data, int width, int height) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            BufferedImage img = ImageIO.read(bais);
            if (img != null) {
                Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }
        } catch (Exception e) {
            // Datos de imagen inválidos
        }
        return null;
    }

    /**
     * Limpia las cachés de imágenes para liberar memoria.
     */
    public void limpiarCache() {
        cacheMiniatura.clear();
        cacheGrande.clear();
    }
}
