package smartplayer;

import java.util.ArrayList;
import java.util.List;

/** Cola de reproducción implementada manualmente */
public class PlayQueue {

    private static class Node {
        Song data;
        Node next;
        Node(Song d) { this.data = d; }
    }

    private Node head, tail;
    private int size;

    public void enqueue(Song s) {
        Node n = new Node(s);
        if (tail != null) tail.next = n;
        tail = n;
        if (head == null) head = n;
        size++;
    }

    public Song dequeue() {
        if (isEmpty()) return null;
        Song s = head.data;
        head = head.next;
        if (head == null) tail = null;
        size--;
        return s;
    }

    public Song peek()   { return isEmpty() ? null : head.data; }
    public boolean isEmpty() { return head == null; }
    public int size()    { return size; }
    public void clear()  { head = tail = null; size = 0; }

    public List<Song> toList() {
        List<Song> l = new ArrayList<>();
        Node cur = head;
        while (cur != null) { l.add(cur.data); cur = cur.next; }
        return l;
    }
}
