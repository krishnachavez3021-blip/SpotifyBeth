package smartplayer.utils;

import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.JavaSoundAudioDevice;

/**
 * Dispositivo de audio personalizado para JLayer que aplica control de volumen
 * real escalando las muestras PCM antes de enviarlas al dispositivo de sonido.
 * Envuelve JavaSoundAudioDevice y modifica las muestras segun el volumen configurado.
 */
public class VolumeAudioDevice implements AudioDevice {

    private JavaSoundAudioDevice device;
    private volatile float volume; // 0.0 = silencio, 1.0 = maximo

    /**
     * Crea el dispositivo con un volumen inicial.
     * @param initialVolume valor entre 0.0 y 1.0
     */
    public VolumeAudioDevice(float initialVolume) {
        this.device = new JavaSoundAudioDevice();
        this.volume = Math.max(0f, Math.min(1f, initialVolume));
    }

    /**
     * Ajusta el volumen en tiempo real.
     * @param vol valor entre 0.0 (silencio) y 1.0 (maximo)
     */
    public void setVolume(float vol) {
        this.volume = Math.max(0f, Math.min(1f, vol));
    }

    /** Retorna el volumen actual. */
    public float getVolume() {
        return volume;
    }

    @Override
    public void open(Decoder decoder) throws JavaLayerException {
        device.open(decoder);
    }

    @Override
    public void close() {
        device.close();
    }

    /**
     * Escala las muestras PCM por el factor de volumen antes de enviarlas al
     * dispositivo real. Soporta mute (vol=0) y paso directo (vol~1).
     */
    @Override
    public void write(short[] samples, int offs, int len) throws JavaLayerException {
        float v = volume;
        if (v >= 0.99f) {
            // Volumen maximo: pasar muestras sin modificar
            device.write(samples, offs, len);
        } else if (v <= 0.0f) {
            // Silencio total: enviar muestras en cero
            short[] silent = new short[len];
            device.write(silent, 0, len);
        } else {
            // Escalar cada muestra por el factor de volumen
            short[] scaled = new short[len];
            for (int i = 0; i < len; i++) {
                int s = (int)(samples[offs + i] * v);
                // Clamp al rango valido de short
                if (s > 32767)  s = 32767;
                else if (s < -32768) s = -32768;
                scaled[i] = (short) s;
            }
            device.write(scaled, 0, len);
        }
    }

    @Override
    public void flush() {
        try { device.flush(); } catch (Exception e) { /* ignorar */ }
    }

    @Override
    public boolean isOpen() {
        return device.isOpen();
    }

    @Override
    public int getPosition() {
        return device.getPosition();
    }
}
