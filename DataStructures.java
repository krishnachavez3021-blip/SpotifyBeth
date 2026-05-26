package com.proyectofinal.spotify;

import java.util.ArrayList;
import java.util.List;

class SimpleSongList {
    private final List<Song> songs = new ArrayList<Song>();
    public void add(Song s) { songs.add(s); }
    public void clear() { songs.clear(); }
    public List<Song> all() { return songs; }
}

class SongStack {
    private final List<Song> stack = new ArrayList<Song>();
    public void push(Song s) { stack.add(0, s); }
    public List<Song> all() { return stack; }
}

class SongQueue {
    private final List<Song> queue = new ArrayList<Song>();
    public void enqueue(Song s) { queue.add(s); }
    public Song dequeue() { return queue.isEmpty() ? null : queue.remove(0); }
    public void clear() { queue.clear(); }
    public List<Song> all() { return queue; }
}

class DoubleSongList {
    private final List<Song> list = new ArrayList<Song>();
    private int index;
    public void add(Song s) { list.add(s); }
    public void clear() { list.clear(); index = 0; }
    public Song next() {
        if (list.isEmpty()) return null;
        index = Math.min(list.size() - 1, index + 1);
        return list.get(index);
    }
    public Song previous() {
        if (list.isEmpty()) return null;
        index = Math.max(0, index - 1);
        return list.get(index);
    }
    public void setIndex(int index) { this.index = index; }
}

class CircularSongList {
    private final List<Song> list = new ArrayList<Song>();
    private int index;
    public void add(Song s) { list.add(s); }
    public void clear() { list.clear(); index = 0; }
    public Song nextCircular() {
        if (list.isEmpty()) return null;
        Song s = list.get(index);
        index = (index + 1) % list.size();
        return s;
    }
}

class SongTree {
    private Node root;
    static class Node {
        Song song;
        Node left;
        Node right;
        int height = 1;
        Node(Song song) { this.song = song; }
    }
    public void insert(Song song) { root = insert(root, song); }
    private Node insert(Node n, Song s) {
        if (n == null) return new Node(s);
        if (s.compareTo(n.song) < 0) n.left = insert(n.left, s);
        else if (s.compareTo(n.song) > 0) n.right = insert(n.right, s);
        return n;
    }
    public Song search(String title) {
        Node n = root;
        while (n != null) {
            int c = title.toLowerCase().compareTo(n.song.getTitle().toLowerCase());
            if (c == 0) return n.song;
            n = c < 0 ? n.left : n.right;
        }
        return null;
    }
    public String horizontal(String title) {
        StringBuilder sb = new StringBuilder(title).append("\n\n");
        draw(root, 0, sb);
        return sb.toString();
    }
    private void draw(Node n, int level, StringBuilder sb) {
        if (n != null) {
            draw(n.right, level + 1, sb);
            for (int i = 0; i < level; i++) sb.append("      ");
            sb.append(n.song.getTitle()).append("\n");
            draw(n.left, level + 1, sb);
        }
    }
}

class SongAvlTree extends SongTree {
    private AvlNode root;
    static class AvlNode {
        Song song;
        AvlNode left;
        AvlNode right;
        int height = 1;
        AvlNode(Song song) { this.song = song; }
    }
    public void insertAvl(Song song) { root = insert(root, song); }
    private AvlNode insert(AvlNode n, Song s) {
        if (n == null) return new AvlNode(s);
        if (s.compareTo(n.song) < 0) n.left = insert(n.left, s);
        else if (s.compareTo(n.song) > 0) n.right = insert(n.right, s);
        else return n;
        update(n);
        return balance(n);
    }
    public Song searchAvl(String title) {
        AvlNode n = root;
        while (n != null) {
            int c = title.toLowerCase().compareTo(n.song.getTitle().toLowerCase());
            if (c == 0) return n.song;
            n = c < 0 ? n.left : n.right;
        }
        return null;
    }
    public String horizontalAvl() {
        StringBuilder sb = new StringBuilder("ARBOL AVL\n\n");
        draw(root, 0, sb);
        return sb.toString();
    }
    private void draw(AvlNode n, int level, StringBuilder sb) {
        if (n != null) {
            draw(n.right, level + 1, sb);
            for (int i = 0; i < level; i++) sb.append("      ");
            sb.append(n.song.getTitle()).append("\n");
            draw(n.left, level + 1, sb);
        }
    }
    private AvlNode balance(AvlNode n) {
        int b = h(n.left) - h(n.right);
        if (b > 1) {
            if (h(n.left.left) < h(n.left.right)) n.left = rotateLeft(n.left);
            return rotateRight(n);
        }
        if (b < -1) {
            if (h(n.right.right) < h(n.right.left)) n.right = rotateRight(n.right);
            return rotateLeft(n);
        }
        return n;
    }
    private AvlNode rotateRight(AvlNode y) {
        AvlNode x = y.left;
        AvlNode t = x.right;
        x.right = y;
        y.left = t;
        update(y); update(x);
        return x;
    }
    private AvlNode rotateLeft(AvlNode x) {
        AvlNode y = x.right;
        AvlNode t = y.left;
        y.left = x;
        x.right = t;
        update(x); update(y);
        return y;
    }
    private void update(AvlNode n) { n.height = 1 + Math.max(h(n.left), h(n.right)); }
    private int h(AvlNode n) { return n == null ? 0 : n.height; }
}
