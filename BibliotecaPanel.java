package smartplayer.views;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;
import smartplayer.controllers.LibraryController;
import smartplayer.controllers.SearchController;
import smartplayer.controllers.CoverArtController;
import smartplayer.structures.Nodo;
import smartplayer.structures.ListaSimple;
import smartplayer.models.Song;
import smartplayer.utils.FileChooserUtil;

public class BibliotecaPanel extends JPanel {
    private static final int COL_COVER=0,COL_FAV=1,COL_NUM=2,COL_TITLE=3,COL_ARTIST=4,COL_ALBUM=5,COL_GENRE=6,COL_DUR=7,COL_YEAR=8,COL_PLAYS=9;
    private static final Color BG_MAIN=new Color(0x1A1A2E),BG_ROW1=new Color(0x16213E),BG_ROW2=new Color(0x1A1A2E),BG_TOP=new Color(0x16213E),ACCENT=new Color(0xFF69B4),TEXT_MAIN=Color.WHITE,TEXT_SEC=new Color(0xC0C0C0);
    private LibraryController libraryCtrl;
    private SearchController searchCtrl;
    private CoverArtController coverArtCtrl;
    private ReproductorPanel reproductorPanel;
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private RoundedTextField txtBusqueda;
    private JComboBox<String> comboFiltro;
    private JPopupMenu popupHistorial;
    private SwingWorker<Void,Object[]> coverWorker;
    
    public BibliotecaPanel(LibraryController libraryCtrl,ReproductorPanel reproductorPanel){
        this.libraryCtrl=libraryCtrl;
        this.reproductorPanel=reproductorPanel;
        this.searchCtrl=new SearchController();
        this.coverArtCtrl=new CoverArtController();
        setLayout(new BorderLayout());
        setBackground(BG_MAIN);
        add(crearPanelSuperior(),BorderLayout.NORTH);
        crearTabla();
        JScrollPane sp=new JScrollPane(table);
        sp.getViewport().setBackground(BG_MAIN);
        CustomScrollBarUI.aplicarA(sp);
        add(sp,BorderLayout.CENTER);
        configurarListeners();
    }
    
    private JPanel crearPanelSuperior(){
        JPanel panel=new JPanel(new BorderLayout(12,0));
        panel.setBackground(BG_TOP);
        panel.setBorder(BorderFactory.createEmptyBorder(12,20,12,20));
        JPanel leftBtns=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        leftBtns.setOpaque(false);

        // Boton principal: Importar Carpeta
        RoundedButton btnImportar=new RoundedButton("\u271A Importar Carpeta");
        btnImportar.addActionListener(e->importarCarpeta());

        // Boton: Agregar a Cola
        RoundedButton btnAgregarCola=crearBotonVisible("\u25B6 Agregar a Cola",new Color(0x7B68EE));
        btnAgregarCola.addActionListener(e->agregarCancionACola());

        // Boton desplegable: Archivo ▾  (agrupa Exportar CSV, Exportar TXT y Desencriptar)
        JButton btnArchivo = new JButton("\u2699 Archivo  \u25BE") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg=getModel().isArmed()?new Color(0xB01870):
                         getModel().isRollover()?new Color(0xD0207A):new Color(0xFF69B4);
                g2.setColor(bg);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.setColor(new Color(255,255,255,60));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,14,14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnArchivo.setForeground(Color.WHITE);
        btnArchivo.setFont(new Font("Segoe UI",Font.BOLD,13));
        btnArchivo.setContentAreaFilled(false);
        btnArchivo.setBorderPainted(false);
        btnArchivo.setFocusPainted(false);
        btnArchivo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnArchivo.setPreferredSize(new Dimension(148,36));

        // Menu emergente del boton Archivo
        JPopupMenu menuArchivo = new JPopupMenu();
        menuArchivo.setBackground(new Color(0x1E1E3A));
        menuArchivo.setBorder(BorderFactory.createLineBorder(new Color(0xFF69B4,true),1));

        JMenuItem itemCSV = crearMenuItem("\u2193  Exportar como CSV",new Color(0xADD8E6));
        JMenuItem itemTXT = crearMenuItem("\u2193  Exportar como TXT",new Color(0xADD8E6));
        JMenuItem itemDescifrar = crearMenuItem("\uD83D\uDD13  Desencriptar e Importar",new Color(0xFF69B4));

        itemCSV.addActionListener(e->exportarCSV());
        itemTXT.addActionListener(e->exportarTXT());
        itemDescifrar.addActionListener(e->desencriptarEImportar());

        menuArchivo.add(itemCSV);
        menuArchivo.add(itemTXT);
        menuArchivo.addSeparator();
        menuArchivo.add(itemDescifrar);

        btnArchivo.addActionListener(e->
            menuArchivo.show(btnArchivo,0,btnArchivo.getHeight()));

        leftBtns.add(btnImportar);
        leftBtns.add(btnAgregarCola);
        leftBtns.add(btnArchivo);

        JPanel searchRow=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));
        searchRow.setOpaque(false);
        String[] filtros={"Todos","Titulo","Artista","Album","Genero","Ano"};
        comboFiltro=new JComboBox<>(filtros);
        comboFiltro.setBackground(new Color(0x2A2A4A));
        comboFiltro.setForeground(Color.WHITE);
        comboFiltro.setFont(new Font("Segoe UI",Font.PLAIN,12));
        comboFiltro.setPreferredSize(new Dimension(110,34));
        txtBusqueda=new RoundedTextField("Buscar canciones...");
        txtBusqueda.setPreferredSize(new Dimension(280,34));
        txtBusqueda.setBackground(new Color(0x2A2A4A));
        txtBusqueda.setForeground(Color.WHITE);
        txtBusqueda.setCaretColor(Color.WHITE);
        searchRow.add(comboFiltro);
        searchRow.add(txtBusqueda);
        panel.add(leftBtns,BorderLayout.WEST);
        panel.add(searchRow,BorderLayout.EAST);
        return panel;
    }

    /** Crea un RoundedButton con fondo de color visible y texto blanco en negrita. */
    private RoundedButton crearBotonVisible(String texto, Color colorFondo){
        // Reutilizamos RoundedButton.SECONDARY y sobreponemos color manualmente
        RoundedButton btn=new RoundedButton(texto,RoundedButton.Variante.SECONDARY){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg=getModel().isArmed()?colorFondo.darker():
                         getModel().isRollover()?colorFondo.brighter():colorFondo;
                g2.setColor(bg);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.setColor(new Color(255,255,255,50));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,14,14);
                g2.dispose();
                // Dibujar texto manualmente para asegurar visibilidad
                Graphics2D g3=(Graphics2D)g.create();
                g3.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g3.setFont(getFont());
                g3.setColor(Color.WHITE);
                FontMetrics fm=g3.getFontMetrics();
                int tx=(getWidth()-fm.stringWidth(getText()))/2;
                int ty=(getHeight()+fm.getAscent()-fm.getDescent())/2;
                g3.drawString(getText(),tx,ty);
                g3.dispose();
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI",Font.BOLD,13));
        return btn;
    }

    /** Crea un JMenuItem estilizado con fondo oscuro y texto de color. */
    private JMenuItem crearMenuItem(String texto, Color colorTexto){
        JMenuItem item=new JMenuItem(texto){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg=getModel().isArmed()?new Color(0x2E2E50):new Color(0x1E1E3A);
                g2.setColor(bg);
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        item.setBackground(new Color(0x1E1E3A));
        item.setForeground(colorTexto);
        item.setFont(new Font("Segoe UI",Font.BOLD,13));
        item.setBorder(BorderFactory.createEmptyBorder(8,14,8,14));
        item.setOpaque(false);
        return item;
    }

    /** Abre un archivo encriptado (.enc, .txt o cualquiera), lo desencripta y agrega las canciones a la biblioteca. */
    private void desencriptarEImportar(){
        File archivo=FileChooserUtil.abrirArchivo(this,"Desencriptar e Importar — seleccionar archivo");
        if(archivo==null)return;

        int importadas=0, errores=0, duplicadas=0;
        java.util.List<String> rutas=new java.util.ArrayList<>();

        // 1) Leer y desencriptar todas las lineas del archivo
        try(java.io.BufferedReader br=new java.io.BufferedReader(
                new java.io.InputStreamReader(new java.io.FileInputStream(archivo),"UTF-8"))){
            String linea;
            boolean primeraLinea=true;
            while((linea=br.readLine())!=null){
                if(linea.trim().isEmpty()) continue;
                String desc;
                try{ desc=smartplayer.utils.Encriptacion.desencriptar(linea); }
                catch(Exception ex){ errores++; continue; }
                // La primera linea suele ser el nombre de playlist/album — ignorar si no es ruta de archivo
                if(primeraLinea){
                    primeraLinea=false;
                    if(!desc.contains(File.separator) && !desc.contains("/") && !desc.toLowerCase().endsWith(".mp3")) continue;
                }
                // Ignorar marcadores de album
                if(desc.startsWith("__ALBUM__:")) continue;
                rutas.add(desc);
            }
        }catch(Exception ex){
            JOptionPane.showMessageDialog(this,"Error al leer el archivo:\n"+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2) Para cada ruta desencriptada, intentar cargar el archivo MP3 y agregarlo a la biblioteca
        for(String ruta : rutas){
            File f=new File(ruta);
            if(f.exists() && f.getName().toLowerCase().endsWith(".mp3")){
                // Escanear la carpeta padre para obtener metadatos completos del MP3
                smartplayer.structures.ListaSimple mini=smartplayer.utils.FileManager.scanDirectory(f.getParentFile());
                Song cancionEncontrada=null;
                smartplayer.structures.Nodo n=mini.getCabeza();
                while(n!=null){
                    if(n.song.getPath().equals(ruta)){
                        cancionEncontrada=n.song;
                        break;
                    }
                    n=n.siguiente;
                }
                if(cancionEncontrada!=null){
                    boolean agregada=libraryCtrl.agregarCancion(cancionEncontrada);
                    if(agregada) importadas++;
                    else duplicadas++;
                } else {
                    errores++;
                }
            } else {
                // Archivo no existe o no es MP3
                errores++;
            }
        }

        actualizarTabla();
        String msg=String.format(
            "Desencriptado completado.%n%n" +
            "  Canciones importadas: %d%n" +
            "  Ya existían (duplicadas): %d%n" +
            "  No encontradas / errores: %d",
            importadas, duplicadas, errores);
        JOptionPane.showMessageDialog(this,msg,"Desencriptar e Importar",JOptionPane.INFORMATION_MESSAGE);
    }

    private void crearTabla(){
        String[] columnas={"","\u2665","#","Titulo","Artista","Album","Genero","Duracion","Ano","Plays"};
        tableModel=new DefaultTableModel(columnas,0){
            public boolean isCellEditable(int r,int c){return false;}
            public Class<?> getColumnClass(int c){
                if(c==COL_COVER)return ImageIcon.class;
                if(c==COL_FAV)return String.class;
                if(c==COL_NUM||c==COL_PLAYS)return Integer.class;
                return String.class;
            }
        };
        table=new JTable(tableModel){
            public Component prepareRenderer(TableCellRenderer renderer,int row,int col){
                Component comp=super.prepareRenderer(renderer,row,col);
                if(!isRowSelected(row)){
                    comp.setBackground(row%2==0?BG_ROW1:BG_ROW2);
                    comp.setForeground(col==COL_ARTIST||col==COL_ALBUM?TEXT_SEC:TEXT_MAIN);
                }else{
                    comp.setBackground(new Color(255,105,180,60));
                    comp.setForeground(Color.WHITE);
                }
                return comp;
            }
        };
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setBackground(BG_MAIN);
        table.setForeground(TEXT_MAIN);
        table.setGridColor(new Color(0x2A2A4A));
        table.setSelectionBackground(new Color(255,105,180,60));
        table.setSelectionForeground(Color.WHITE);
        table.setRowHeight(44);
        table.setFont(new Font("Segoe UI",Font.PLAIN,13));
        JTableHeader header=table.getTableHeader();
        header.setBackground(new Color(0x0D0D1F));
        header.setForeground(TEXT_SEC);
        header.setFont(new Font("Segoe UI",Font.BOLD,11));
        header.setPreferredSize(new Dimension(0,36));
        table.getColumnModel().getColumn(COL_COVER).setMaxWidth(48);
        table.getColumnModel().getColumn(COL_FAV).setMaxWidth(36);
        table.getColumnModel().getColumn(COL_NUM).setMaxWidth(42);
        table.getColumnModel().getColumn(COL_DUR).setMaxWidth(80);
        table.getColumnModel().getColumn(COL_YEAR).setMaxWidth(60);
        table.getColumnModel().getColumn(COL_PLAYS).setMaxWidth(55);
        sorter=new TableRowSorter<>(tableModel);
        sorter.setSortable(COL_COVER,false);
        table.setRowSorter(sorter);
        popupHistorial=new JPopupMenu();
        // Renderer para portadas
        table.getColumnModel().getColumn(COL_COVER).setCellRenderer((tbl,val,sel,foc,row,col)->{
            JLabel lbl=new JLabel();
            lbl.setHorizontalAlignment(JLabel.CENTER);
            lbl.setOpaque(true);
            lbl.setBackground(row%2==0?BG_ROW1:BG_ROW2);
            if(val instanceof javax.swing.ImageIcon)lbl.setIcon((javax.swing.ImageIcon)val);
            return lbl;
        });
        // Renderer para corazon favorito
        table.getColumnModel().getColumn(COL_FAV).setCellRenderer((tbl,val,sel,foc,row,col)->{
            JLabel lbl=new JLabel();
            lbl.setHorizontalAlignment(JLabel.CENTER);
            lbl.setFont(new Font("Segoe UI",Font.PLAIN,16));
            lbl.setOpaque(true);
            if(sel){lbl.setBackground(new Color(255,105,180,60));}
            else{lbl.setBackground(row%2==0?BG_ROW1:BG_ROW2);}
            String v=val!=null?val.toString():"";
            lbl.setText(v);
            lbl.setForeground("♥".equals(v)?new Color(0xFF69B4):TEXT_SEC);
            return lbl;
        });
    }
    
    private void configurarListeners(){
        txtBusqueda.getDocument().addDocumentListener(new DocumentListener(){
            public void insertUpdate(DocumentEvent e){filtrar();}
            public void removeUpdate(DocumentEvent e){filtrar();}
            public void changedUpdate(DocumentEvent e){filtrar();}
        });
        table.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent evt){
                int col=table.columnAtPoint(evt.getPoint());
                int row=table.rowAtPoint(evt.getPoint());
                if(col==COL_FAV&&row>=0&&evt.getClickCount()==1){
                    int modelRow=table.convertRowIndexToModel(row);
                    String titulo=(String)tableModel.getValueAt(modelRow,COL_TITLE);
                    String artista=(String)tableModel.getValueAt(modelRow,COL_ARTIST);
                    Song song=buscarSong(titulo,artista);
                    if(song!=null){
                        song.setFavorite(!song.isFavorite());
                        tableModel.setValueAt(song.isFavorite()?"♥":"",modelRow,COL_FAV);
                    }
                    return;
                }
                if(evt.getClickCount()==2&&col!=COL_FAV&&row>=0){
                    int modelRow=table.convertRowIndexToModel(row);
                    String titulo=(String)tableModel.getValueAt(modelRow,COL_TITLE);
                    Song song=buscarSong(titulo,null);
                    if(song!=null){
                        smartplayer.structures.ListaDoble listaNav=libraryCtrl.getListaDoble();
                        reproductorPanel.getPlayerCtrl().setCurrentSongInList(song,listaNav);
                        reproductorPanel.playSingle(song);
                    }
                }
            }
        });
        comboFiltro.addActionListener(e->filtrar());
    }
    
    private Song buscarSong(String titulo,String artista){
        Nodo n=libraryCtrl.getBiblioteca().getCabeza();
        while(n!=null){
            if(n.song.getTitle().equalsIgnoreCase(titulo)&&(artista==null||n.song.getArtist().equalsIgnoreCase(artista)))
                return n.song;
            n=n.siguiente;
        }
        return null;
    }
    
    private void filtrar(){
        String query=txtBusqueda.getText().trim();
        if(query.isEmpty()){actualizarTabla();return;}
        ListaSimple resultados=searchCtrl.buscarGeneral(libraryCtrl.getBiblioteca(),query);
        llenarTabla(resultados);
    }
    
    public void actualizarTabla(){llenarTabla(libraryCtrl.getBiblioteca());}
    
    private void llenarTabla(ListaSimple lista){
        tableModel.setRowCount(0);
        if(coverWorker!=null&&!coverWorker.isDone())coverWorker.cancel(true);
        int num=1;
        java.util.List<Song> songs=new java.util.ArrayList<>();
        Nodo actual=lista.getCabeza();
        while(actual!=null){
            Song s=actual.song;
            tableModel.addRow(new Object[]{null,s.isFavorite()?"♥":"",num++,s.getTitle(),s.getArtist(),
                s.getAlbum(),s.getGenre(),s.getDurationFormatted(),s.getYear(),s.getPlayCount()});
            songs.add(s);
            actual=actual.siguiente;
        }
        coverWorker=new SwingWorker<Void,Object[]>(){
            protected Void doInBackground(){
                for(int i=0;i<songs.size();i++){
                    if(isCancelled())break;
                    javax.swing.ImageIcon icon=coverArtCtrl.getMiniatura(songs.get(i),40);
                    publish(new Object[]{i,icon});
                }
                return null;
            }
            protected void process(java.util.List<Object[]> chunks){
                for(Object[] chunk:chunks){
                    int row=(int)chunk[0];
                    if(row<tableModel.getRowCount())
                        tableModel.setValueAt(chunk[1],row,COL_COVER);
                }
            }
        };
        coverWorker.execute();
    }
    
    private void importarCarpeta(){
        File carpeta=FileChooserUtil.seleccionarCarpeta(this);
        if(carpeta==null)return;
        smartplayer.structures.ListaSimple resultado=smartplayer.utils.FileManager.scanDirectory(carpeta);
        libraryCtrl.importarDesdeListaEscaneada(resultado);
        actualizarTabla();
        JOptionPane.showMessageDialog(this,"Importacion completada.\n"+resultado.tamano+" canciones cargadas.","Listo",JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void exportarCSV(){
        JFileChooser fc=new JFileChooser();
        fc.setDialogTitle("Guardar como CSV");
        fc.setSelectedFile(new File("biblioteca.csv"));
        if(fc.showSaveDialog(this)==JFileChooser.APPROVE_OPTION){
            File archivo=fc.getSelectedFile();
            if(!archivo.getName().endsWith(".csv"))archivo=new File(archivo.getAbsolutePath()+".csv");
            try(PrintWriter pw=new PrintWriter(new OutputStreamWriter(new FileOutputStream(archivo),"UTF-8"))){
                pw.println("Numero,Titulo,Artista,Album,Genero,Duracion,Ano,Plays");
                int num=1;
                Nodo actual=libraryCtrl.getBiblioteca().getCabeza();
                while(actual!=null){
                    Song s=actual.song;
                    pw.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\",%s,%s,%d%n",num++,s.getTitle(),s.getArtist(),s.getAlbum(),s.getGenre(),s.getDurationFormatted(),s.getYear(),s.getPlayCount());
                    actual=actual.siguiente;
                }
                JOptionPane.showMessageDialog(this,"Biblioteca exportada correctamente","Exito",JOptionPane.INFORMATION_MESSAGE);
            }catch(IOException ex){
                JOptionPane.showMessageDialog(this,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void exportarTXT(){
        JFileChooser fc=new JFileChooser();
        fc.setDialogTitle("Guardar como TXT");
        fc.setSelectedFile(new File("biblioteca.txt"));
        if(fc.showSaveDialog(this)==JFileChooser.APPROVE_OPTION){
            File archivo=fc.getSelectedFile();
            if(!archivo.getName().endsWith(".txt"))archivo=new File(archivo.getAbsolutePath()+".txt");
            try(PrintWriter pw=new PrintWriter(new OutputStreamWriter(new FileOutputStream(archivo),"UTF-8"))){
                pw.println("LUMINA - Biblioteca");
                int num=1;
                Nodo actual=libraryCtrl.getBiblioteca().getCabeza();
                while(actual!=null){
                    Song s=actual.song;
                    pw.printf("%d. %s - %s%n",num++,s.getTitle(),s.getArtist());
                    actual=actual.siguiente;
                }
                JOptionPane.showMessageDialog(this,"Biblioteca exportada correctamente","Exito",JOptionPane.INFORMATION_MESSAGE);
            }catch(IOException ex){
                JOptionPane.showMessageDialog(this,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void agregarCancionACola(){
        int row=table.getSelectedRow();
        if(row<0){
            JOptionPane.showMessageDialog(this,"Selecciona una cancion","Lumina",JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow=table.convertRowIndexToModel(row);
        String titulo=(String)tableModel.getValueAt(modelRow,COL_TITLE);
        String artista=(String)tableModel.getValueAt(modelRow,COL_ARTIST);
        Song song=buscarSong(titulo,artista);
        if(song!=null){
            reproductorPanel.getPlayerCtrl().getColaReproduccion().encolar(song);
            JOptionPane.showMessageDialog(this,"Agregada a la cola","Lumina",JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    public void enfocarBusqueda(){txtBusqueda.requestFocusInWindow();}
    public SearchController getSearchCtrl(){return searchCtrl;}
}

