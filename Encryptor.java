package smartplayer;

import java.util.List;

/**
 * Encriptación de playlists usando recorridos de árboles.
 * Algoritmo propio: Caesar-shift con clave derivada del recorrido del AVL.
 */
public class Encryptor {

    private static final int BASE_SHIFT = 7;

    // ── Generar clave desde recorrido ──────────────────────────────────
    private static int[] buildKeyFromTraversal(List<Song> traversal) {
        int[] key = new int[traversal.size() == 0 ? 1 : traversal.size()];
        for (int i = 0; i < traversal.size(); i++) {
            key[i] = (traversal.get(i).getTitle().length() + BASE_SHIFT) % 95;
            if (key[i] == 0) key[i] = BASE_SHIFT;
        }
        return key;
    }

    // ── Encriptar ─────────────────────────────────────────────────────
    public static String encrypt(String text, List<Song> traversal) {
        int[] key = buildKeyFromTraversal(traversal);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            int shift = key[i % key.length];
            if (c >= 32 && c <= 126) {
                c = (char) (((c - 32 + shift) % 95) + 32);
            }
            sb.append(c);
        }
        return sb.toString();
    }

    // ── Desencriptar ──────────────────────────────────────────────────
    public static String decrypt(String text, List<Song> traversal) {
        int[] key = buildKeyFromTraversal(traversal);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            int shift = key[i % key.length];
            if (c >= 32 && c <= 126) {
                c = (char) (((c - 32 - shift + 95) % 95) + 32);
            }
            sb.append(c);
        }
        return sb.toString();
    }

    // ── Serializar playlist a texto ────────────────────────────────────
    public static String serializePlaylist(Playlist pl) {
        StringBuilder sb = new StringBuilder();
        sb.append("PLAYLIST:").append(pl.getName()).append("\n");
        for (Song s : pl.getSongs()) {
            sb.append(s.toCSV()).append("\n");
        }
        return sb.toString();
    }

    // ── Deserializar ──────────────────────────────────────────────────
    public static Playlist deserializePlaylist(String text) {
        String[] lines = text.split("\n");
        if (lines.length == 0 || !lines[0].startsWith("PLAYLIST:")) return null;
        String name = lines[0].substring(9);
        Playlist pl = new Playlist(name);
        for (int i = 1; i < lines.length; i++) {
            if (lines[i].trim().isEmpty()) continue;
            Song s = Song.fromCSV(lines[i]);
            if (s != null) pl.addSong(s);
        }
        return pl;
    }
}
