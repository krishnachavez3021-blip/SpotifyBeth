package smartplayer;

import java.util.ArrayList;
import java.util.List;

/** Árbol Binario de Búsqueda (ABB) para canciones */
public class BST {

    public static class Node {
        public Song data;
        public Node left, right;
        public Node(Song d) { this.data = d; }
    }

    private Node root;
    private int size;

    // ── INSERTAR ──────────────────────────────────────────────────────
    public void insert(Song s) { root = insertRec(root, s); size++; }

    private Node insertRec(Node n, Song s) {
        if (n == null) return new Node(s);
        int cmp = s.getTitle().compareToIgnoreCase(n.data.getTitle());
        if (cmp < 0)      n.left  = insertRec(n.left, s);
        else if (cmp > 0) n.right = insertRec(n.right, s);
        return n;
    }

    // ── BUSCAR ────────────────────────────────────────────────────────
    public Song search(String title) {
        Node n = searchNode(root, title);
        return n == null ? null : n.data;
    }

    private Node searchNode(Node n, String title) {
        if (n == null) return null;
        int cmp = title.compareToIgnoreCase(n.data.getTitle());
        if (cmp == 0) return n;
        return cmp < 0 ? searchNode(n.left, title) : searchNode(n.right, title);
    }

    // ── ELIMINAR ──────────────────────────────────────────────────────
    public void delete(String title) { root = deleteRec(root, title); size--; }

    private Node deleteRec(Node n, String title) {
        if (n == null) return null;
        int cmp = title.compareToIgnoreCase(n.data.getTitle());
        if (cmp < 0)      n.left  = deleteRec(n.left, title);
        else if (cmp > 0) n.right = deleteRec(n.right, title);
        else {
            if (n.left == null)  return n.right;
            if (n.right == null) return n.left;
            Node min = findMin(n.right);
            n.data = min.data;
            n.right = deleteRec(n.right, min.data.getTitle());
        }
        return n;
    }

    private Node findMin(Node n) {
        while (n.left != null) n = n.left;
        return n;
    }

    // ── RECORRIDOS ────────────────────────────────────────────────────
    public List<Song> inOrder()   { List<Song> l = new ArrayList<>(); inOrderRec(root, l); return l; }
    public List<Song> preOrder()  { List<Song> l = new ArrayList<>(); preOrderRec(root, l); return l; }
    public List<Song> postOrder() { List<Song> l = new ArrayList<>(); postOrderRec(root, l); return l; }

    private void inOrderRec(Node n, List<Song> l)   { if (n==null) return; inOrderRec(n.left,l); l.add(n.data); inOrderRec(n.right,l); }
    private void preOrderRec(Node n, List<Song> l)  { if (n==null) return; l.add(n.data); preOrderRec(n.left,l); preOrderRec(n.right,l); }
    private void postOrderRec(Node n, List<Song> l) { if (n==null) return; postOrderRec(n.left,l); postOrderRec(n.right,l); l.add(n.data); }

    public int size()           { return size; }
    public int height()         { return heightRec(root); }
    public Node getRoot()       { return root; }
    public boolean isEmpty()    { return root == null; }

    private int heightRec(Node n) {
        if (n == null) return 0;
        return 1 + Math.max(heightRec(n.left), heightRec(n.right));
    }

    // Búsqueda parcial (contiene)
    public List<Song> searchContains(String keyword) {
        List<Song> results = new ArrayList<>();
        searchContainsRec(root, keyword.toLowerCase(), results);
        return results;
    }

    private void searchContainsRec(Node n, String kw, List<Song> res) {
        if (n == null) return;
        Song s = n.data;
        if (s.getTitle().toLowerCase().contains(kw)  ||
            s.getArtist().toLowerCase().contains(kw) ||
            s.getAlbum().toLowerCase().contains(kw)  ||
            s.getGenre().toLowerCase().contains(kw))
            res.add(s);
        searchContainsRec(n.left, kw, res);
        searchContainsRec(n.right, kw, res);
    }
}
