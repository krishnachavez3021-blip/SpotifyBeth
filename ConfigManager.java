package smartplayer.utils;

import java.io.*;
import java.util.Properties;

/**
 * Gestor de configuración persistente de la aplicación.
 * Almacena preferencias del usuario en archivo .properties.
 */
public class ConfigManager {
    private static final String CONFIG_FILE = "smartplayer_config.properties";
    private Properties config;

    public ConfigManager() {
        config = new Properties();
        cargar();
    }

    /**
     * Obtiene un valor de configuración.
     */
    public String get(String key, String defaultValue) {
        return config.getProperty(key, defaultValue);
    }

    /**
     * Establece un valor de configuración.
     */
    public void set(String key, String value) {
        config.setProperty(key, value);
    }

    /**
     * Obtiene un entero de configuración.
     */
    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(config.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Obtiene un booleano de configuración.
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(config.getProperty(key, String.valueOf(defaultValue)));
    }

    /**
     * Guarda la configuración en archivo.
     */
    public void guardar() {
        try (OutputStream out = new FileOutputStream(CONFIG_FILE)) {
            config.store(out, "Smart Player - Configuración");
        } catch (IOException e) {
            System.err.println("Error guardando configuración: " + e.getMessage());
        }
    }

    /**
     * Carga la configuración desde archivo.
     */
    private void cargar() {
        File file = new File(CONFIG_FILE);
        if (!file.exists()) {
            // Valores por defecto
            config.setProperty("theme", "DARK");
            config.setProperty("volume", "80");
            config.setProperty("lastFolder", "");
            return;
        }
        
        try (InputStream in = new FileInputStream(file)) {
            config.load(in);
        } catch (IOException e) {
            System.err.println("Error cargando configuración: " + e.getMessage());
        }
    }
}
