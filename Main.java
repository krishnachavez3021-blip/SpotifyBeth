package smartplayer;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import smartplayer.utils.ConfigManager;
import smartplayer.views.MainFrame;

/**
 * Punto de entrada de SmartPlayer.
 * Carga ConfigManager al inicio y lanza la interfaz gráfica en el EDT.
 * El StatsManager es inicializado automáticamente dentro de PlayerController
 * y se comparte con EstadisticasPanel a través de playerCtrl.getStatsManager().
 */
public class Main {

    public static void main(String[] args) {

        // Cargar configuración persistente al inicio (volumen, tema, última carpeta, etc.)
        ConfigManager configManager = new ConfigManager();

        // Intentar usar el Look and Feel del sistema para mejor integración visual con el SO.
        // Si falla, Swing usa su propio L&F por defecto, sin afectar la funcionalidad.
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Se usa el L&F por defecto de Swing
        }

        // Lanzar la interfaz gráfica en el hilo de despacho de eventos (EDT).
        // MainFrame inicializa PlayerController (que crea su StatsManager interno),
        // aplica la configuración guardada y muestra la ventana principal.
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(configManager);
            frame.setVisible(true);
        });
    }
}
