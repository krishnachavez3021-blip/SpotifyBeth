package smartplayer.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Generador de recursos de iconos para SmartPlayer.
 * Crea el icono genérico de nota musical (note_icon.png) en el directorio
 * de recursos si no existe, y lo expone como BufferedImage o ImageIcon.
 */
public class IconGenerator {

    private static final String ICON_CLASSPATH = "/icons/note_icon.png";

    private IconGenerator() {
        // Clase utilitaria, no se instancia
    }

    /**
     * Genera el archivo note_icon.png en src/main/resources/icons/ si no existe.
     * Útil durante el desarrollo para crear el recurso inicial.
     *
     * @param outputPath ruta absoluta donde guardar el PNG
     */
    public static void generarNoteIconPNG(String outputPath) {
        File file = new File(outputPath);
        if (file.exists()) return;

        try {
            file.getParentFile().mkdirs();
            BufferedImage img = crearImagenNota(64, 64);
            ImageIO.write(img, "PNG", file);
        } catch (IOException e) {
            System.err.println("No se pudo generar note_icon.png: " + e.getMessage());
        }
    }

    /**
     * Carga el icono de nota musical desde el classpath.
     * Si no lo encuentra, lo genera programáticamente.
     *
     * @param size tamaño deseado en píxeles
     * @return BufferedImage con la imagen del icono
     */
    public static BufferedImage cargarOGenerarIcono(int size) {
        // Intentar cargar desde classpath
        try {
            URL url = IconGenerator.class.getResource(ICON_CLASSPATH);
            if (url != null) {
                try (InputStream is = url.openStream()) {
                    BufferedImage img = ImageIO.read(is);
                    if (img != null) {
                        // Escalar si el tamaño no coincide
                        if (img.getWidth() != size || img.getHeight() != size) {
                            BufferedImage scaled = new BufferedImage(size, size,
                                    BufferedImage.TYPE_INT_ARGB);
                            Graphics2D g2d = scaled.createGraphics();
                            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                            g2d.drawImage(img, 0, 0, size, size, null);
                            g2d.dispose();
                            return scaled;
                        }
                        return img;
                    }
                }
            }
        } catch (IOException e) {
            // Recurso no encontrado en classpath, generarlo
        }

        // Generar programáticamente
        return crearImagenNota(size, size);
    }

    /**
     * Crea una imagen de nota musical con fondo oscuro.
     *
     * @param width  ancho en píxeles
     * @param height alto en píxeles
     * @return BufferedImage generado
     */
    public static BufferedImage crearImagenNota(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Fondo gris oscuro con esquinas redondeadas
        g2d.setColor(new Color(40, 40, 40));
        g2d.fillRoundRect(0, 0, width, height, 12, 12);

        // Letra M centrada (por Music)
        g2d.setColor(new Color(30, 215, 96)); // Verde Spotify
        int fontSize = Math.max(width / 3, 12);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
        FontMetrics fm = g2d.getFontMetrics();
        String letra = "M";
        int textX = (width - fm.stringWidth(letra)) / 2;
        int textY = (height + fm.getAscent() - fm.getDescent()) / 2;
        g2d.drawString(letra, textX, textY);

        g2d.dispose();
        return img;
    }
}
