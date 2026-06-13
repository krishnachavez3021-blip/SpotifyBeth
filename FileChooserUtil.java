package smartplayer.utils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilidad para abrir JFileChooser con colores claros visibles,
 * independientemente del tema oscuro activo en la aplicacion.
 *
 * Guarda los colores actuales del UIManager, aplica colores claros
 * antes de mostrar el dialogo y los restaura al cerrarlo.
 */
public class FileChooserUtil {

    // Claves de UIManager que afectan la apariencia del JFileChooser
    private static final String[] CLAVES_UI = {
        "Panel.background",
        "Panel.foreground",
        "Label.foreground",
        "List.background",
        "List.foreground",
        "List.selectionBackground",
        "List.selectionForeground",
        "Table.background",
        "Table.foreground",
        "Table.selectionBackground",
        "Table.selectionForeground",
        "Table.gridColor",
        "TextField.background",
        "TextField.foreground",
        "TextField.selectionBackground",
        "TextField.selectionForeground",
        "ComboBox.background",
        "ComboBox.foreground",
        "ComboBox.selectionBackground",
        "ComboBox.selectionForeground",
        "Button.background",
        "Button.foreground",
        "ScrollPane.background",
        "Tree.background",
        "Tree.foreground",
        "Tree.selectionBackground",
        "Tree.selectionForeground",
        "OptionPane.background",
        "ToolTip.background",
        "ToolTip.foreground",
        "FileChooser.background",
        "SplitPane.background",
        "PopupMenu.background",
        "PopupMenu.foreground",
        "MenuItem.background",
        "MenuItem.foreground",
        "CheckBox.background",
        "CheckBox.foreground"
    };

    /**
     * Guarda los valores actuales de las claves de UIManager relevantes.
     *
     * @return mapa con los valores originales
     */
    private static Map<String, Object> guardarColoresOriginales() {
        Map<String, Object> coloresOriginales = new HashMap<>();
        for (String clave : CLAVES_UI) {
            coloresOriginales.put(clave, UIManager.get(clave));
        }
        return coloresOriginales;
    }

    /**
     * Aplica colores claros (fondo blanco, texto negro) al UIManager
     * para que el JFileChooser sea completamente visible.
     */
    private static void aplicarColoresDialogoClaro() {
        Color fondoBlanco    = Color.WHITE;
        Color fondoGrisClaro = new Color(0xF0F0F0);
        Color fondoSeleccion = new Color(0x3884E0);
        Color textoNegro     = Color.BLACK;
        Color textoBlanco    = Color.WHITE;

        UIManager.put("Panel.background",                fondoBlanco);
        UIManager.put("Panel.foreground",                textoNegro);
        UIManager.put("Label.foreground",                textoNegro);
        UIManager.put("List.background",                 fondoBlanco);
        UIManager.put("List.foreground",                 textoNegro);
        UIManager.put("List.selectionBackground",        fondoSeleccion);
        UIManager.put("List.selectionForeground",        textoBlanco);
        UIManager.put("Table.background",                fondoBlanco);
        UIManager.put("Table.foreground",                textoNegro);
        UIManager.put("Table.selectionBackground",       fondoSeleccion);
        UIManager.put("Table.selectionForeground",       textoBlanco);
        UIManager.put("Table.gridColor",                 new Color(0xD0D0D0));
        UIManager.put("TextField.background",            fondoBlanco);
        UIManager.put("TextField.foreground",            textoNegro);
        UIManager.put("TextField.selectionBackground",   fondoSeleccion);
        UIManager.put("TextField.selectionForeground",   textoBlanco);
        UIManager.put("ComboBox.background",             fondoBlanco);
        UIManager.put("ComboBox.foreground",             textoNegro);
        UIManager.put("ComboBox.selectionBackground",    fondoSeleccion);
        UIManager.put("ComboBox.selectionForeground",    textoBlanco);
        UIManager.put("Button.background",               fondoGrisClaro);
        UIManager.put("Button.foreground",               textoNegro);
        UIManager.put("ScrollPane.background",           fondoBlanco);
        UIManager.put("Tree.background",                 fondoBlanco);
        UIManager.put("Tree.foreground",                 textoNegro);
        UIManager.put("Tree.selectionBackground",        fondoSeleccion);
        UIManager.put("Tree.selectionForeground",        textoBlanco);
        UIManager.put("OptionPane.background",           fondoBlanco);
        UIManager.put("ToolTip.background",              new Color(0xFFFFE1));
        UIManager.put("ToolTip.foreground",              textoNegro);
        UIManager.put("FileChooser.background",          fondoBlanco);
        UIManager.put("SplitPane.background",            fondoGrisClaro);
        UIManager.put("PopupMenu.background",            fondoBlanco);
        UIManager.put("PopupMenu.foreground",            textoNegro);
        UIManager.put("MenuItem.background",             fondoBlanco);
        UIManager.put("MenuItem.foreground",             textoNegro);
        UIManager.put("CheckBox.background",             fondoBlanco);
        UIManager.put("CheckBox.foreground",             textoNegro);
    }

    /**
     * Restaura los colores originales del UIManager guardados previamente.
     *
     * @param coloresOriginales mapa devuelto por guardarColoresOriginales()
     */
    private static void restaurarColoresOriginales(Map<String, Object> coloresOriginales) {
        for (Map.Entry<String, Object> entry : coloresOriginales.entrySet()) {
            if (entry.getValue() != null) {
                UIManager.put(entry.getKey(), entry.getValue());
            } else {
                // Si no existia el valor, lo removemos para dejar el default
                UIManager.put(entry.getKey(), null);
            }
        }
    }

    /**
     * Abre un dialogo para seleccionar una CARPETA con colores claros visibles.
     * Restaura automaticamente el tema oscuro al cerrarse.
     *
     * @param parent componente padre para el dialogo (puede ser null)
     * @return la carpeta seleccionada, o null si el usuario cancelo
     */
    public static File seleccionarCarpeta(Component parent) {
        Map<String, Object> coloresOriginales = guardarColoresOriginales();
        try {
            aplicarColoresDialogoClaro();
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setDialogTitle("Seleccionar carpeta de musica");
            int resultado = chooser.showOpenDialog(parent);
            if (resultado == JFileChooser.APPROVE_OPTION) {
                return chooser.getSelectedFile();
            }
            return null;
        } finally {
            // Siempre restaurar los colores, incluso si ocurre una excepcion
            restaurarColoresOriginales(coloresOriginales);
        }
    }

    /**
     * Abre un dialogo para GUARDAR un archivo con colores claros visibles.
     * Restaura automaticamente el tema oscuro al cerrarse.
     *
     * @param parent componente padre para el dialogo (puede ser null)
     * @param titulo titulo del dialogo
     * @return el archivo destino seleccionado, o null si el usuario cancelo
     */
    public static File guardarArchivo(Component parent, String titulo) {
        Map<String, Object> coloresOriginales = guardarColoresOriginales();
        try {
            aplicarColoresDialogoClaro();
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle(titulo != null ? titulo : "Guardar archivo");
            int resultado = chooser.showSaveDialog(parent);
            if (resultado == JFileChooser.APPROVE_OPTION) {
                return chooser.getSelectedFile();
            }
            return null;
        } finally {
            restaurarColoresOriginales(coloresOriginales);
        }
    }

    /**
     * Abre un dialogo para ABRIR un archivo con colores claros visibles.
     * Restaura automaticamente el tema oscuro al cerrarse.
     *
     * @param parent componente padre para el dialogo (puede ser null)
     * @param titulo titulo del dialogo
     * @return el archivo seleccionado, o null si el usuario cancelo
     */
    public static File abrirArchivo(Component parent, String titulo) {
        Map<String, Object> coloresOriginales = guardarColoresOriginales();
        try {
            aplicarColoresDialogoClaro();
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle(titulo != null ? titulo : "Abrir archivo");
            int resultado = chooser.showOpenDialog(parent);
            if (resultado == JFileChooser.APPROVE_OPTION) {
                return chooser.getSelectedFile();
            }
            return null;
        } finally {
            restaurarColoresOriginales(coloresOriginales);
        }
    }
}
