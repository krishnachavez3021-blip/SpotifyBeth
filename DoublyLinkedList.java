package smartplayer;

import java.util.ArrayList;
import java.util.List;

/** Lista Doble para navegación entre canciones */
public class DoublyLinkedList {

    public static class Node {
        public Song data;
        public Node prev, next;
        public Node(Song d) { this.data = d; }
    }

    private Node head, tail, current;
    private int size;

    public void add(Song s) {
        Node n = new Node(s);
        if (tail != null) { tail.next = n; n.prev = tail; }
        tail = n;
        if (head == null) { head = n; current = n; }
        size++;
    }

    public Song next() {
        if (current == null) return null;
        if (current.next != null) current = current.next;
        return current.data;
    }

    public Song prev() {
        if (current == null) return null;
        if (current.prev != null) current = current.prev;
        return current.data;
    }

    public Song getCurrent() { return current == null ? null : current.data; }

    public void setCurrent(Song s) {
        Node cur = head;
        while (cur != null) {
            if (cur.data == s) { current = cur; return; }
            cur = cur.next;
        }
    }

    public boolean hasNext() { return current != null && current.next != null; }
    public boolean hasPrev() { return current != null && current.prev != null; }
    public int size()        { return size; }
    public void clear()      { head = tail = current = null; size = 0; }

    public List<Song> toList() {
        List<Song> l = new ArrayList<>();
        Node cur = head;
        while (cur != null) { l.add(cur.data); cur = cur.next; }
        return l;
    }
}
