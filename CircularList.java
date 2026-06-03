package smartplayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Lista Circular para modo repetición infinita */
public class CircularList {

    private static class Node {
        Song data;
        Node next;
        Node(Song d) { this.data = d; }
    }

    private Node tail;   // tail.next = head
    private Node current;
    private int size;

    public void add(Song s) {
        Node n = new Node(s);
        if (tail == null) { n.next = n; tail = n; current = n; }
        else { n.next = tail.next; tail.next = n; tail = n; }
        size++;
    }

    public Song next() {
        if (current == null) return null;
        current = current.next;
        return current.data;
    }

    public Song getCurrent() { return current == null ? null : current.data; }

    public void shuffle() {
        List<Song> l = toList();
        Collections.shuffle(l);
        tail = null; current = null; size = 0;
        for (Song s : l) add(s);
    }

    public int size()  { return size; }
    public boolean isEmpty() { return size == 0; }

    public List<Song> toList() {
        List<Song> l = new ArrayList<>();
        if (tail == null) return l;
        Node start = tail.next;
        Node cur   = start;
        do { l.add(cur.data); cur = cur.next; } while (cur != start);
        return l;
    }
}
