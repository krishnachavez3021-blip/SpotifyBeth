package com.proyectofinal.spotify;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.awt.Desktop;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class AudioPlayer {
    private Clip clip;
    private Object mediaPlayer;
    private Process externalPlayer;
    private long startMillis;
    private long pausedMicros;
    private int seconds = 1;
    private String message = "Listo";

    public boolean play(Song song) {
        stop();
        seconds = Math.max(1, song.getSeconds());
        if (isMp3(song.getPath())) {
            return playWithJavaFx(song);
        }
        try {
            AudioInputStream stream = open(song.getPath());
            clip = AudioSystem.getClip();
            clip.open(stream);
            seconds = Math.max(1, (int) (clip.getMicrosecondLength() / 1000000L));
            clip.start();
            startMillis = System.currentTimeMillis();
            pausedMicros = 0;
            message = "Reproduciendo: " + song.getTitle();
            return true;
        } catch (Exception ex) {
            message = "No se pudo reproducir. Use WAV, AIFF o AU compatibles con Java: " + ex.getMessage();
            return false;
        }
    }

    public void pause() {
        if (clip != null && clip.isRunning()) {
            pausedMicros = clip.getMicrosecondPosition();
            clip.stop();
            message = "Pausado";
        } else if (mediaPlayer != null) {
            call(mediaPlayer, "pause");
            message = "Pausado";
        } else if (externalPlayer != null && externalPlayer.isAlive()) {
            message = "Pausa no disponible para MP3 externo. Usa Detener.";
        }
    }

    public void resume() {
        if (clip != null && !clip.isRunning()) {
            clip.setMicrosecondPosition(pausedMicros);
            clip.start();
            startMillis = System.currentTimeMillis() - (pausedMicros / 1000L);
            message = "Reproduciendo";
        } else if (mediaPlayer != null) {
            call(mediaPlayer, "play");
            startMillis = System.currentTimeMillis();
            message = "Reproduciendo";
        } else if (externalPlayer != null && externalPlayer.isAlive()) {
            message = "Ya se esta reproduciendo con Windows Media Player";
        }
    }

    public void stop() {
        if (externalPlayer != null) {
            externalPlayer.destroy();
            externalPlayer = null;
        }
        if (mediaPlayer != null) {
            call(mediaPlayer, "stop");
            call(mediaPlayer, "dispose");
            mediaPlayer = null;
        }
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }
        pausedMicros = 0;
    }

    public boolean isPlaying() {
        if (clip != null) {
            return clip.isRunning();
        }
        if (externalPlayer != null) {
            return externalPlayer.isAlive();
        }
        return mediaPlayer != null && "PLAYING".equals(String.valueOf(call(call(mediaPlayer, "getStatus"), "name")));
    }

    public boolean isPaused() {
        if (clip != null) {
            return !clip.isRunning() && pausedMicros > 0;
        }
        return mediaPlayer != null && "PAUSED".equals(String.valueOf(call(call(mediaPlayer, "getStatus"), "name")));
    }

    public boolean hasClip() {
        return clip != null || mediaPlayer != null || externalPlayer != null;
    }

    public int progress() {
        if (externalPlayer != null) {
            long elapsed = Math.max(0, (System.currentTimeMillis() - startMillis) / 1000L);
            return (int) Math.min(100, (elapsed * 100) / seconds);
        }
        if (mediaPlayer != null) {
            long elapsed = Math.max(0, (System.currentTimeMillis() - startMillis) / 1000L);
            return (int) Math.min(100, (elapsed * 100) / seconds);
        }
        if (clip == null) {
            return 0;
        }
        long elapsed = clip.getMicrosecondPosition() / 1000000L;
        return (int) Math.min(100, (elapsed * 100) / seconds);
    }

    public String currentTime() {
        if (externalPlayer != null) {
            int value = (int) Math.max(0, (System.currentTimeMillis() - startMillis) / 1000L);
            return (value / 60) + ":" + String.format("%02d", value % 60);
        }
        if (mediaPlayer != null) {
            int value = (int) Math.max(0, (System.currentTimeMillis() - startMillis) / 1000L);
            return (value / 60) + ":" + String.format("%02d", value % 60);
        }
        if (clip == null) {
            return "0:00";
        }
        int value = clip == null ? 0 : (int) (clip.getMicrosecondPosition() / 1000000L);
        return (value / 60) + ":" + String.format("%02d", value % 60);
    }

    public String getMessage() {
        return message;
    }

    private AudioInputStream open(String path) throws Exception {
        if (path.startsWith("resource:")) {
            InputStream in = getClass().getResourceAsStream(path.substring("resource:".length()));
            if (in == null) {
                throw new IllegalArgumentException("No existe el recurso " + path);
            }
            return AudioSystem.getAudioInputStream(new BufferedInputStream(in));
        }
        return AudioSystem.getAudioInputStream(new File(path));
    }

    private boolean playWithJavaFx(Song song) {
        try {
            Class.forName("javafx.embed.swing.JFXPanel").getConstructor().newInstance();
            Class<?> mediaClass = Class.forName("javafx.scene.media.Media");
            Class<?> playerClass = Class.forName("javafx.scene.media.MediaPlayer");
            Constructor<?> mediaConstructor = mediaClass.getConstructor(String.class);
            Object media = mediaConstructor.newInstance(new File(song.getPath()).toURI().toString());
            mediaPlayer = playerClass.getConstructor(mediaClass).newInstance(media);
            call(mediaPlayer, "play");
            startMillis = System.currentTimeMillis();
            message = "Reproduciendo MP3: " + song.getTitle();
            return true;
        } catch (Exception ex) {
            mediaPlayer = null;
            return playWithWindowsMediaPlayer(song);
        }
    }

    private boolean playWithWindowsMediaPlayer(Song song) {
        try {
            File player = new File("C:\\Program Files\\Windows Media Player\\wmplayer.exe");
            if (!player.exists()) {
                player = new File("C:\\Program Files (x86)\\Windows Media Player\\wmplayer.exe");
            }
            File audio = new File(song.getPath());
            if (player.exists()) {
                externalPlayer = new ProcessBuilder(player.getAbsolutePath(), "/play", audio.getAbsolutePath()).start();
            } else if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(audio);
                externalPlayer = null;
            } else {
                message = "No se encontro Windows Media Player para reproducir MP3/MPEG.";
                return false;
            }
            startMillis = System.currentTimeMillis();
            message = "Reproduciendo con el reproductor de Windows: " + song.getTitle();
            return true;
        } catch (Exception ex) {
            externalPlayer = null;
            message = "No se pudo reproducir MP3/MPEG. Convierte la cancion a WAV o instala JavaFX.";
            return false;
        }
    }

    private boolean isMp3(String path) {
        String p = path.toLowerCase();
        return p.endsWith(".mp3") || p.endsWith(".mpeg");
    }

    private Object call(Object target, String methodName) {
        if (target == null) {
            return null;
        }
        try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (Exception ex) {
            return null;
        }
    }
}
