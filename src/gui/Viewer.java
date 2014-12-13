package gui;

import geometry.*;
import geometry.Matrix;
import gui.MyComponent.*;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;
import static geometry.Matrix.*;
import java.awt.event.MouseWheelListener;
import java.util.Arrays;
import java.util.HashMap;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import mathematics.Calculus;
import static util.ArrayOp.merge;

public class Viewer extends JPanel implements Runnable{
    
    public int h, k, xtemp, ytemp, button=0;
    public long fps=0, dt=10, ms;
    public double zoom=1;
    public double[][] R={{1,0,0,0},{0,1,0,0},{0,0,1,0},{0,0,0,1}};
    public Shape[] shapes;
    public Vector[][] model, transform;
    public Triangle[] triangles;
    public Vector light=new Vector(0,0,1);
    public static final Vector[] axis={new Vector(1,0,0), new Vector(0,1,0), new Vector(0,0,1)};
    public boolean stats=true, fill=true, showAxis=true, numbers=false, centers=false;
    
    public int mesh=100;
    public double xmin=-10, xmax=10, ymin=-10, ymax=10;    
    
    private Thread thread=new Thread(this);
    private final ArrayList<Thread> threadList=new ArrayList();
    private final JFileChooser fc=new JFileChooser();
    private final String[] menuLabels = {"File", "Edit", "View", "Tools", "Help"};
    
    public Viewer(Shape... s){
        setShapes(s);
        initcomp();
    }
    
    private void initcomp(){
        setOpaque(true);
        setFocusable(true);
        setBackground(Color.BLACK);
        addKeyListener(new KeyAdapter(){
            @Override
            public void keyPressed(KeyEvent ke){
                switch(ke.getKeyCode()){
                    default:
                        return;
                    case KeyEvent.VK_W:
                        rotateMatrix(Math.PI/32, 0, 0);
                        return;
                    case KeyEvent.VK_S:
                        rotateMatrix(-Math.PI/32, 0, 0);
                        return;
                    case KeyEvent.VK_D:
                        rotateMatrix(0, -Math.PI/32, 0);
                        return;
                    case KeyEvent.VK_A:
                        rotateMatrix(0, Math.PI/32, 0);
                        return;
                    case KeyEvent.VK_Q:
                        rotateMatrix(0, 0, -Math.PI/32);
                        return;
                    case KeyEvent.VK_E:
                        rotateMatrix(0, 0, Math.PI/32);
                        return;
                    case KeyEvent.VK_UP:
                        k--;
                        break;
                    case KeyEvent.VK_DOWN:
                        k++;
                        break;
                    case KeyEvent.VK_RIGHT:
                        h++;
                        break;
                    case KeyEvent.VK_LEFT:
                        h--;
                        break;
                }
                synchronized(Viewer.this){
                    Viewer.this.notifyAll();
                }
            }
        });
        addMouseListener(new MouseAdapter(){
            @Override
            public void mousePressed(MouseEvent me){
                button=me.getButton();
                xtemp=me.getX()-getWidth()/2-h;
                ytemp=me.getY()-getHeight()/2-k;
            }
            @Override
            public void mouseReleased(MouseEvent me){
                xtemp=me.getX()-getWidth()/2;
                ytemp=me.getY()-getHeight()/2;
            }
        });
        addMouseMotionListener(new MouseAdapter(){
            @Override
            public void mouseDragged(MouseEvent me){
                me.translatePoint(-getWidth()/2-h, -getHeight()/2-k);
                double yaw=0, pitch=0, roll=0;
                switch(button){
                    default:
                        return;
                    case 1:
                        yaw=2*Math.PI*(ytemp-me.getY())/getHeight();
                        pitch=2*Math.PI*(xtemp-me.getX())/getWidth();
                        break;
                    case 3:
                        roll=Math.atan2(me.getY()*xtemp-me.getX()*ytemp, me.getX()*xtemp+me.getY()*ytemp);
                }
                rotateMatrix(yaw, pitch, roll);
                xtemp=me.getX();
                ytemp=me.getY();
            }
        });
        addMouseWheelListener(new MouseWheelListener(){
            @Override
            public void mouseWheelMoved(MouseWheelEvent mwe) {
                double r=mwe.getWheelRotation();
                r= r>0? Math.min(r, 49): Math.max(r, -49);
                zoom*=(1-r/50);
                synchronized(Viewer.this){
                    Viewer.this.notifyAll();
                }
            }
        });
        
        String dir=getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        dir=dir.replaceAll("/build/classes/", "");
        fc.setCurrentDirectory(new File(dir));
        fc.setFileFilter(new FileFilter(){
            @Override
            public boolean accept(File file){
                return file.isDirectory() || file.getName().endsWith(".dat");
            }
            @Override
            public String getDescription(){
                return "Binary file";
            }
        });
        new JFrame(){
            {
                setTitle("3D Viewer");
                setDefaultCloseOperation(EXIT_ON_CLOSE);
                setMinimumSize(new Dimension(400,400));
                setSize(new Dimension(1000,625));
                setJMenuBar(new MyMenuBar(menuLabels, new JMenuItem[][]{
                    {new MyMenuItem("Open", "control O", () -> loadShape()),
                        new MyMenuItem("Save", "control S", () -> System.out.println("Save")),
                        new MyMenuItem("Exit", "alt F4", () -> System.exit(0))},
                    {new MyMenuItem("Cut", "control X", () -> System.out.println("Cut")), 
                        new MyMenuItem("Copy", "control C", () -> System.out.println("Copy")), 
                        new MyMenuItem("Paste", "control V", () -> System.out.println("Paste"))},
                    {new MyRadioMenuItem("Show vertex numbers", "control K", Viewer.this, "numbers"), 
                        new MyRadioMenuItem("Show shape centers", "control J", Viewer.this, "centers"),
                        new MyRadioMenuItem("Show axes", "control R", Viewer.this, "showAxis"),
                        new MyRadioMenuItem("Show stats", "control T", Viewer.this, "stats"),
                        new MyRadioMenuItem("Fill surfaces", "control F", Viewer.this, "fill")},
                    {new MyMenuItem("Options", null, () -> System.out.println("Options")),
                        new MyMenuItem("Input function", "F6", () -> input())},
                    {new MyMenuItem("Help Contents", "F1", () ->  System.out.println("Help Contents")),
                        new MyMenuItem("About", null, () -> System.out.println("About"))}
                }));
                setContentPane(Viewer.this);
                setOpaque(true);
                setVisible(true);
            }
        };
    }
    private void setShapes(Shape... array){
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        shapes=array;
        model=new Vector[shapes.length][];
        transform=new Vector[shapes.length][];
        Triangle[][] M=new Triangle[shapes.length][]; 
        for(int i=0; i<shapes.length; i++){
            model[i]=shapes[i].getVertex();
            int m=model[i].length;
            transform[i]=new Vector[m];
            for(int j=0; j<m; j++){
                transform[i][j]=new Vector(0,0,0);
            }
            Triangle[] original=shapes[i].getTriangles();
            M[i]=new Triangle[original.length];
            for(int j=0; j<original.length; j++){
                int[] index=original[j].getIndex();
                M[i][j]=new Triangle(transform[i], index[0], index[1], index[2]);
                Color color=original[j].getColor();
                if(color==null){
                    double[] P=original[j].getCenter().getComp();
                    M[i][j].setColor(createColor(P[0], P[1], P[2]));
                }else{
                    M[i][j].setColor(color);
                }
            }
        }
        triangles=merge(M);
        for(Shape s:shapes){
            if(s instanceof Runnable){
                Thread t=new Thread((Runnable)s);
                threadList.add(t);
                t.start();
            }
        }
        thread.interrupt();
        thread=new Thread(this, "Paint Thread");
        thread.start();
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
    private void loadShape(){        
        if(fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION){
            setCursor(new Cursor(Cursor.WAIT_CURSOR));
            for(Thread t: threadList){
                t.interrupt();
            }
            threadList.clear();
            try(ObjectInputStream ois=new ObjectInputStream(new FileInputStream(fc.getSelectedFile()))){
                Object obj=ois.readObject();
                if(obj instanceof Shape[]){
                    setShapes((Shape[])obj);
                }else if(obj instanceof Shape){
                    setShapes((Shape)obj);
                }
            }catch(NullPointerException | IOException | ClassNotFoundException e){
            }
        }
    }
    private void input(){
        String s=JOptionPane.showInputDialog("Enter function");
        try{
            Calculus c=Calculus.parseCalculus(s);
            setShapes(new Plot(this, c));
        }catch(StackOverflowError ex){
            input();
        }
    }
    
    public synchronized void rotateMatrix(double yaw, double pitch, double roll){
        R=multiply(eulerRotation(yaw, pitch, roll), R);
        notifyAll();
    }
    
    public static float colorRange(float x){
        return x>0? Math.min(1, x): Math.min(0, x);
    }
    public static Color createColor(double x, double y, double z){
        double m=Math.sqrt(x*x+y*y+z*z);
        double xt=x/m;
        double yt=y/m;
        double zt=z/m;
        float r=0, g=0, b=0;
        if(x>0){
            r+=xt;
        }else{
            g-=xt;
            b-=xt;
        }
        if(y>0){
            b+=yt;
        }else{
            g-=yt;
            r-=yt;
        }
        if(z>0){
            g+=zt;
        }else{
            r-=zt;
            b-=zt;
        }
        return new Color(colorRange(r),colorRange(g),colorRange(b)); 
    }
    
    @Override
    public synchronized void paint(Graphics g){
        long temp=System.currentTimeMillis();
        fps=1000/(temp-ms);
        ms=temp;
        g.clearRect(0, 0, getWidth(), getHeight());
        paintComponent(g);
        if(stats){
            g.setColor(Color.WHITE);
            g.drawString(String.format("yaw=%.2f",Math.toDegrees(Math.atan2(R[2][1], R[2][2]))), 10, 12);
            g.drawString(String.format("pitch=%.2f",Math.toDegrees(Math.atan2(-R[2][0], Math.sqrt(R[2][1]*R[2][1]+R[2][2]*R[2][2])))), 10, 27);
            g.drawString(String.format("roll=%.2f",Math.toDegrees(Math.atan2(R[1][0], R[0][0]))), 10, 42);
            g.drawString(String.format("zoom=%.2f",zoom), 10, 57);
            g.drawString(String.format("fps=%d",fps), 10, 72);
        }
        g.translate(getWidth()/2+h, getHeight()/2+k);
        for(final Triangle t: triangles){
            double depth=0;
            int[] x=new int[3], y=new int[3];
            for(int j=0; j<3; j++){
                double[] q=t.getVertex(j).getComp();
                x[j]=(int)q[0];
                y[j]=-(int)q[1];
                depth+=q[2];
            }
            double angle=axis[2].getAngle(t);
            if(angle>-Math.PI/32 && depth<850/zoom){
                Color c=t.getColor();
                if(fill){
                    double b=(130+250*(angle/Math.PI))/255;
                    c=new Color((int)(b*c.getRed()),(int)(b*c.getGreen()),(int)(b*c.getBlue()));
                    g.setColor(c);
                    g.fillPolygon(x, y, 3);
                }else{
                    g.setColor(c);
                    g.drawPolygon(x, y, 3);
                }if(numbers){
                    g.setColor(Color.WHITE);
                    for(int n=0; n<3; n++){
                        g.drawString(String.valueOf(t.getIndex(n)), x[n], y[n]);
                    }
                }
            }
        }
        if(showAxis){
            g.setColor(Color.WHITE);
            for(int i=0; i<axis.length; i++) {
                double[] D=transpose(multiply(R, axis[i].toColumn(4)))[0];
                double r=0.4375*Math.min(getWidth(), getHeight());
                int y=(int)(r*D[1]);
                int x=(int)(r*D[0]);
                g.drawLine(x, -y, -x, y);
                g.drawString(String.valueOf((char)(120+i)), x+10*(int)Math.signum(x), -y-10*(int)Math.signum(y));
            }
        }
    }
    @Override
    public synchronized void run(){        
        while(true){
            try{
                double r, P[][];
                for(int i=0; i<shapes.length; i++){
                    int m=model[i].length;
                    double[][] rotation=Matrix.multiply(R, shapes[i].getRotationMatrix());
                    for(int j=0; j<m; j++){
                        P=Matrix.multiply(rotation, model[i][j].toColumn(4));
                        r=1/zoom-P[2][0]/300;
                        transform[i][j].set(P[0][0]/r, P[1][0]/r, P[2][0]);
                    }
                }
                HashMap<Triangle, Double> map=new HashMap();
                for(Triangle t : triangles) {
                    map.put(t, t.getCenter().getComp(2));
                }
                Arrays.parallelSort(triangles, (t1, t2)->Double.compare(map.get(t1), map.get(t2)));
                repaint();
                wait(threadList.isEmpty()? 0: dt);
            }catch(InterruptedException ex){
                return;
            }
        }
    }
    
    public static void main(String[] args){
        try{
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        }catch(ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex){
            Logger.getLogger(Viewer.class.getName()).log(Level.SEVERE, null, ex);
        }
        Viewer v=new Viewer(ShapeFactory.kochSnowflake(4, 100));
    }
}