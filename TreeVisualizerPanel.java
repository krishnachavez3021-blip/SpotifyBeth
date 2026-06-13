package smartplayer.views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import smartplayer.structures.NodoArbol;
import smartplayer.models.Song;

/**
 * Visualizador de arbol binario con Graphics2D.
 *
 * Layout: recorrido in-order para asignar columnas X (sin solapamientos garantizados),
 * profundidad del nodo para Y. Nodos como rectángulos redondeados con titulo + artista.
 * Líneas curvas bezier. Scroll y animación de resaltado.
 */
public class TreeVisualizerPanel extends JPanel implements Scrollable {

    // ---- Paleta ----
    private static final Color BG_TOP        = new Color(0x12122A);
    private static final Color BG_BOTTOM     = new Color(0x0A0A1E);
    private static final Color GRID_COLOR    = new Color(0x1C1C38);
    private static final Color NODE_BG       = new Color(0x1E2252);
    private static final Color NODE_BG2      = new Color(0x14163C);
    private static final Color NODE_BORDER   = new Color(0xFF69B4);
    private static final Color NODE_SEL_BG   = new Color(0xFF3090);
    private static final Color NODE_SEL_BG2  = new Color(0xB01860);
    private static final Color NODE_ANIM_BG  = new Color(0xFFD060);
    private static final Color NODE_ANIM_BG2 = new Color(0xFF8820);
    private static final Color LINE_COLOR    = new Color(0xFF69B4);
    private static final Color LINE_COLOR2   = new Color(0x7A2850);
    private static final Color TEXT_TITLE    = Color.WHITE;
    private static final Color TEXT_ARTIST   = new Color(0xC0C8FF);
    private static final Color TEXT_BAL_OK   = new Color(0x60FF60);
    private static final Color TEXT_BAL_WARN = new Color(0xFF6030);
    private static final Color SHADOW        = new Color(0, 0, 0, 90);

    // ---- Dimensiones de nodo ----
    private static final int NW     = 72;   // ancho del nodo
    private static final int NH     = 36;   // alto del nodo
    private static final int ARC    = 10;   // radio esquinas redondeadas
    private static final int GAP_X  = 10;   // espacio horizontal entre nodos
    private static final int GAP_Y  = 48;   // espacio vertical entre niveles
    private static final int MARGIN = 24;   // margen externo

    // ---- Estado ----
    private NodoArbol raiz;
    private final Map<NodoArbol, Point> posiciones = new LinkedHashMap<>();
    private NodoArbol nodoSeleccionado;
    private NodoArbol nodoAnimado;
    private int totalW = 400;
    private int totalH = 300;
    private double zoom = 1.0;          // zoom automático para que quepa todo
    private boolean mostrarBalanceo = false;
    private String  tituloArbol    = "Árbol";
    private String  recorridoActual = "InOrden"; // InOrden | PreOrden | PostOrden

    // Animación de resaltado (pulso dorado)
    private final javax.swing.Timer animTimer;
    private int     animAlpha = 0;
    private boolean animSube  = true;

    // Callback al hacer clic en un nodo
    private java.util.function.Consumer<Song> onNodeClick;

    // ---- Constructor ----
    public TreeVisualizerPanel() {
        setBackground(BG_TOP);
        setOpaque(true);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                NodoArbol n = getNodoEnPunto(e.getX(), e.getY());
                nodoSeleccionado = n;
                repaint();
                if (n != null && onNodeClick != null) onNodeClick.accept(n.song);
            }
        });

        animTimer = new javax.swing.Timer(35, e -> {
            if (animSube) { animAlpha += 15; if (animAlpha >= 255) { animAlpha = 255; animSube = false; } }
            else          { animAlpha -= 15; if (animAlpha <=  70) { animAlpha =  70; animSube = true;  } }
            repaint();
        });
    }

    // ---- API pública ----

    public void setMostrarBalanceo(boolean v) { this.mostrarBalanceo = v; repaint(); }
    public void setTituloArbol(String t)       { this.tituloArbol = t;    repaint(); }
    public void setOnNodeClick(java.util.function.Consumer<Song> cb) { this.onNodeClick = cb; }
    public void setRecorridoActual(String recorrido) {
        if (recorrido != null && !recorrido.equals(this.recorridoActual)) {
            this.recorridoActual = recorrido;
            calcularLayout();
            revalidate();
        }
        repaint();
    }

    // ---- Zoom manual (exponemos métodos públicos para que ArbolesPanel los llame) ----
    public void zoomIn()   { zoom = Math.min(zoom + 0.15, 3.0); revalidate(); repaint(); }
    public void zoomOut()  { zoom = Math.max(zoom - 0.15, 0.15); revalidate(); repaint(); }
    public void zoomFit()  { // recalcula zoom para que quepa todo
        if (raiz == null) return;
        int vpW = getParent() != null ? getParent().getWidth()  : 500;
        int vpH = getParent() != null ? getParent().getHeight() : 400;
        if (vpW <= 0) vpW = 500; if (vpH <= 0) vpH = 400;
        zoom = Math.max(0.15, Math.min(1.0, Math.min((double)vpW / totalW, (double)vpH / totalH)));
        revalidate(); repaint();
    }
    public double getZoom() { return zoom; }

    /** Establece la raíz del árbol y recalcula el layout. */
    public void setRaiz(NodoArbol r) {
        this.raiz = r;
        this.nodoSeleccionado = null;
        this.nodoAnimado = null;
        animTimer.stop();
        calcularLayout();
        revalidate();
        repaint();
    }

    /** Recalcula el zoom cuando el componente cambia de tamaño. */
    @Override
    public void setBounds(int x, int y, int w, int h) {
        super.setBounds(x, y, w, h);
        if (raiz != null && !posiciones.isEmpty()) {
            // Recalcular zoom con el nuevo tamaño
            double zx = (double) w / totalW;
            double zy = (double) h / totalH;
            zoom = Math.max(0.25, Math.min(1.0, Math.min(zx, zy)));
        }
    }

    /** Resalta el nodo de la canción dada con animación dorada. */
    public void resaltarNodo(Song song) {
        if (song == null || raiz == null) return;
        nodoAnimado = buscarNodoPorTitulo(raiz, song.getTitle());
        if (nodoAnimado != null) {
            animAlpha = 70; animSube = true;
            animTimer.restart();
            javax.swing.Timer stop = new javax.swing.Timer(2500, ev -> {
                animTimer.stop(); nodoAnimado = null; repaint();
            });
            stop.setRepeats(false);
            stop.start();
            repaint();
        }
    }

    // ---- LAYOUT: in-order para X, profundidad para Y ----
    // Garantiza que no haya solapamientos: cada nodo ocupa una "columna" única.

    private void calcularLayout() {
        posiciones.clear();
        if (raiz == null) { totalW = 400; totalH = 300; zoom = 1.0; return; }

        // Asignar columna a cada nodo via in-order (indices 0, 1, 2, ...)
        int[] col = {0};
        asignarColumnas(raiz, 0, col);

        // Calcular dimensiones totales
        int maxCol = 0, maxDepth = 0;
        for (Map.Entry<NodoArbol, Point> e : posiciones.entrySet()) {
            maxCol   = Math.max(maxCol,   e.getValue().x);
            maxDepth = Math.max(maxDepth, e.getValue().y);
        }

        // Convertir (col, depth) a coordenadas de píxeles
        for (Map.Entry<NodoArbol, Point> e : posiciones.entrySet()) {
            int c = e.getValue().x;
            int d = e.getValue().y;
            e.getValue().x = MARGIN + c * (NW + GAP_X);
            e.getValue().y = MARGIN + d * (NH + GAP_Y);
        }

        totalW = MARGIN + (maxCol + 1) * (NW + GAP_X) + MARGIN;
        totalH = MARGIN + (maxDepth + 1) * (NH + GAP_Y) + MARGIN;

        // Calcular zoom automático para que quepa en el viewport visible
        // Usamos el tamaño preferido del viewport como referencia (500 x 400)
        int vpW = getParent() != null ? getParent().getWidth()  : 500;
        int vpH = getParent() != null ? getParent().getHeight() : 400;
        if (vpW <= 0) vpW = 500;
        if (vpH <= 0) vpH = 400;

        double zoomX = (double) vpW  / totalW;
        double zoomY = (double) vpH  / totalH;
        // Usar el zoom más restrictivo, con mínimo 0.25 y máximo 1.0
        zoom = Math.min(1.0, Math.min(zoomX, zoomY));
        zoom = Math.max(0.25, zoom);
    }

    /** Recorrido in-order: asigna col++ al nodo y registra (col, depth) en posiciones. */
    private void asignarColumnas(NodoArbol nodo, int depth, int[] col) {
        if (nodo == null) return;
        if ("PreOrden".equals(recorridoActual)) {
            // PreOrden: raiz, izq, der
            posiciones.put(nodo, new Point(col[0], depth));
            col[0]++;
            asignarColumnas(nodo.izquierdo, depth + 1, col);
            asignarColumnas(nodo.derecho, depth + 1, col);
        } else if ("PostOrden".equals(recorridoActual)) {
            // PostOrden: izq, der, raiz
            asignarColumnas(nodo.izquierdo, depth + 1, col);
            asignarColumnas(nodo.derecho, depth + 1, col);
            posiciones.put(nodo, new Point(col[0], depth));
            col[0]++;
        } else {
            // InOrden (default): izq, raiz, der
            asignarColumnas(nodo.izquierdo, depth + 1, col);
            posiciones.put(nodo, new Point(col[0], depth));
            col[0]++;
            asignarColumnas(nodo.derecho, depth + 1, col);
        }
    }

    // ---- Detección de clic ----
    private NodoArbol getNodoEnPunto(int mx, int my) {
        // Las coordenadas del clic vienen en espacio de pantalla; convertir a espacio del árbol
        int ax = (int)(mx / zoom);
        int ay = (int)(my / zoom);
        for (Map.Entry<NodoArbol, Point> e : posiciones.entrySet()) {
            Point p = e.getValue();
            if (ax >= p.x && ax <= p.x + NW && ay >= p.y && ay <= p.y + NH)
                return e.getKey();
        }
        return null;
    }

    private NodoArbol buscarNodoPorTitulo(NodoArbol n, String titulo) {
        if (n == null) return null;
        if (n.song.getTitle().equalsIgnoreCase(titulo)) return n;
        NodoArbol r = buscarNodoPorTitulo(n.izquierdo, titulo);
        return r != null ? r : buscarNodoPorTitulo(n.derecho, titulo);
    }

    // ---- Pintura ----

    @Override
    public Dimension getPreferredSize() {
        int scaledW = (int)(totalW * zoom);
        int scaledH = (int)(totalH * zoom);
        int vpW = getParent() != null ? getParent().getWidth()  : 500;
        int vpH = getParent() != null ? getParent().getHeight() : 400;
        // Si el árbol escalado cabe, llenar el viewport; si no, permitir scroll
        return new Dimension(Math.max(scaledW, vpW > 0 ? vpW : 400),
                             Math.max(scaledH, vpH > 0 ? vpH : 300));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);

        int w = getWidth(), h = getHeight();

        // Fondo con degradado
        g2.setPaint(new GradientPaint(0, 0, BG_TOP, 0, h, BG_BOTTOM));
        g2.fillRect(0, 0, w, h);

        // Cuadrícula de fondo
        g2.setColor(GRID_COLOR);
        g2.setStroke(new BasicStroke(0.5f));
        for (int x = 0; x < w; x += 40) g2.drawLine(x, 0, x, h);
        for (int y = 0; y < h; y += 40) g2.drawLine(0, y, w, y);

        if (raiz == null || posiciones.isEmpty()) {
            // Mensaje árbol vacío
            String msg = "Árbol vacío — importa canciones y haz clic en \"Visualizar Árboles\"";
            g2.setColor(new Color(0x7070A0));
            g2.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, h / 2);
            g2.dispose();
            return;
        }

        // Aplicar zoom centrado en el origen
        g2.scale(zoom, zoom);

        // 1. Líneas conectoras (debajo de los nodos)
        dibujarLineas(g2, raiz);

        // 2. Nodos encima
        for (Map.Entry<NodoArbol, Point> e : posiciones.entrySet())
            dibujarNodo(g2, e.getKey(), e.getValue());

        g2.dispose();
    }

    /** Dibuja líneas curvas de padre a cada hijo. */
    private void dibujarLineas(Graphics2D g2, NodoArbol nodo) {
        if (nodo == null) return;
        Point p = posiciones.get(nodo);
        if (p == null) return;

        int parentCX = p.x + NW / 2;
        int parentCY = p.y + NH;       // base del nodo padre

        g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        for (NodoArbol hijo : new NodoArbol[]{nodo.izquierdo, nodo.derecho}) {
            if (hijo == null) continue;
            Point ph = posiciones.get(hijo);
            if (ph == null) continue;
            int hCX = ph.x + NW / 2;
            int hCY = ph.y;             // tope del nodo hijo
            int ctrlY = parentCY + (hCY - parentCY) / 2;
            g2.setPaint(new GradientPaint(parentCX, parentCY, LINE_COLOR,
                                          hCX, hCY, LINE_COLOR2));
            g2.draw(new QuadCurve2D.Float(parentCX, parentCY, parentCX, ctrlY, hCX, hCY));
            dibujarLineas(g2, hijo);
        }
    }

    /** Dibuja un nodo como rectángulo redondeado con título y artista. */
    private void dibujarNodo(Graphics2D g2, NodoArbol nodo, Point p) {
        boolean sel  = (nodo == nodoSeleccionado);
        boolean anim = (nodo == nodoAnimado);
        int x = p.x, y = p.y;

        // Sombra
        g2.setColor(SHADOW);
        g2.fillRoundRect(x + 3, y + 5, NW, NH, ARC, ARC);

        // Relleno con degradado vertical
        Color top, bot;
        if (anim) {
            float t = animAlpha / 255.0f;
            top = blend(NODE_ANIM_BG,  NODE_BG,  1 - t);
            bot = blend(NODE_ANIM_BG2, NODE_BG2, 1 - t);
        } else if (sel) {
            top = NODE_SEL_BG;  bot = NODE_SEL_BG2;
        } else {
            top = NODE_BG;      bot = NODE_BG2;
        }
        g2.setPaint(new GradientPaint(x, y, top, x, y + NH, bot));
        g2.fillRoundRect(x, y, NW, NH, ARC, ARC);

        // Borde
        g2.setColor(anim ? NODE_ANIM_BG : NODE_BORDER);
        g2.setStroke(new BasicStroke(sel || anim ? 2.0f : 1.4f));
        g2.drawRoundRect(x, y, NW, NH, ARC, ARC);

        // Divisor interno (línea horizontal en el centro)
        g2.setColor(new Color(255, 255, 255, 20));
        g2.setStroke(new BasicStroke(1f));
        int midY = y + NH / 2;
        g2.drawLine(x + 10, midY, x + NW - 10, midY);

        // ---- Texto: título (arriba) ----
        String titulo  = cortar(nodo.song.getTitle(),  12);
        String artista = cortar(nodo.song.getArtist(), 13);

        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        FontMetrics fmT = g2.getFontMetrics();
        g2.setColor(TEXT_TITLE);
        g2.drawString(titulo,
            x + (NW - fmT.stringWidth(titulo)) / 2,
            y + NH / 4 + fmT.getAscent() / 2 + 1);

        // ---- Texto: artista (abajo) ----
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        FontMetrics fmA = g2.getFontMetrics();
        g2.setColor(TEXT_ARTIST);
        g2.drawString(artista,
            x + (NW - fmA.stringWidth(artista)) / 2,
            midY + fmA.getAscent() + 2);

        // ---- Factor de balanceo AVL (esquina sup. derecha) ----
        if (mostrarBalanceo) {
            int bal = profundidad(nodo.izquierdo) - profundidad(nodo.derecho);
            String bs = (bal > 0 ? "+" : "") + bal;
            g2.setFont(new Font("Segoe UI", Font.BOLD, 8));
            FontMetrics fmB = g2.getFontMetrics();
            g2.setColor(Math.abs(bal) <= 1 ? TEXT_BAL_OK : TEXT_BAL_WARN);
            g2.drawString(bs, x + NW - fmB.stringWidth(bs) - 5, y + 9);
        }
    }

    private String cortar(String s, int max) {
        if (s == null || s.isEmpty()) return "—";
        s = s.trim();
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }

    private int profundidad(NodoArbol n) {
        if (n == null) return 0;
        return 1 + Math.max(profundidad(n.izquierdo), profundidad(n.derecho));
    }

    private Color blend(Color a, Color b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        return new Color(
            (int)(a.getRed()   * t + b.getRed()   * (1 - t)),
            (int)(a.getGreen() * t + b.getGreen() * (1 - t)),
            (int)(a.getBlue()  * t + b.getBlue()  * (1 - t))
        );
    }

    // ---- Scrollable ----
    @Override public Dimension getPreferredScrollableViewportSize()              { return new Dimension(500, 400); }
    @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d)  { return 30;  }
    @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 120; }
    // Si el árbol cabe con zoom (zoom == 1.0 o el árbol es pequeño), no mostrar scroll
    @Override public boolean getScrollableTracksViewportWidth()  { return zoom >= 1.0 || totalW * zoom <= (getParent() != null ? getParent().getWidth()  : 500); }
    @Override public boolean getScrollableTracksViewportHeight() { return zoom >= 1.0 || totalH * zoom <= (getParent() != null ? getParent().getHeight() : 400); }
}
