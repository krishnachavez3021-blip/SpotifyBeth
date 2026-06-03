package smartplayer;

import java.util.ArrayList;
import java.util.List;

/** Árbol AVL balanceado para índice optimizado de canciones */
public class AVLTree {

    public static class Node {
        public Song data;
        public Node left, right;
        public int height;
        public Node(Song d) { this.data = d; this.height = 1; }
    }

    private Node root;
    private int size;
    private int rotations;

    // ── ALTURA ────────────────────────────────────────────────────────
    private int h(Node n)    { return n == null ? 0 : n.height; }
    private int bf(Node n)   { return n == null ? 0 : h(n.left) - h(n.right); }
    private void updateH(Node n) { n.height = 1 + Math.max(h(n.left), h(n.right)); }

    // ── ROTACIONES ────────────────────────────────────────────────────
    private Node rotateRight(Node y) {   // RD
        rotations++;
        Node x = y.left, T = x.right;
        x.right = y; y.left = T;
        updateH(y); updateH(x);
        return x;
    }

    private Node rotateLeft(Node x) {    // RI
        rotations++;
        Node y = x.right, T = y.left;
        y.left = x; x.right = T;
        updateH(x); updateH(y);
        return y;
    }

    // ── BALANCEO ──────────────────────────────────────────────────────
    private Node balance(Node n) {
        updateH(n);
        int b = bf(n);
        if (b > 1) {
            if (bf(n.left) < 0) n.left = rotateLeft(n.left);   // RDI
            return rotateRight(n);                               // RD o RDI
        }
        if (b < -1) {
            if (bf(n.right) > 0) n.right = rotateRight(n.right); // RID
            return rotateLeft(n);                                  // RI o RID
        }
        return n;
    }

    // ── INSERTAR ──────────────────────────────────────────────────────
    public void insert(Song s) { root = insertRec(root, s); size++; }

    private Node insertRec(Node n, Song s) {
        if (n == null) return new Node(s);
        int cmp = s.getTitle().compareToIgnoreCase(n.data.getTitle());
        if (cmp < 0)      n.left  = insertRec(n.left, s);
        else if (cmp > 0) n.right = insertRec(n.right, s);
        return balance(n);
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
    public void delete(String title) { root = deleteRec(root, title); if (size > 0) size--; }

    private Node deleteRec(Node n, String title) {
        if (n == null) return null;
        int cmp = title.compareToIgnoreCase(n.data.getTitle());
        if (cmp < 0)      n.left  = deleteRec(n.left, title);
        else if (cmp > 0) n.right = deleteRec(n.right, title);
        else {
            if (n.left == null)  return n.right;
            if (n.right == null) return n.left;
            Node min = findMin(n.right);
            n.data  = min.data;
            n.right = deleteRec(n.right, min.data.getTitle());
        }
        return balance(n);
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

    // Búsqueda parcial
    public List<Song> searchContains(String kw) {
        List<Song> res = new ArrayList<>();
        searchContainsRec(root, kw.toLowerCase(), res);
        return res;
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

    public int size()        { return size; }
    public int height()      { return h(root); }
    public int getRotations(){ return rotations; }
    public Node getRoot()    { return root; }
    public boolean isEmpty() { return root == null; }
}
