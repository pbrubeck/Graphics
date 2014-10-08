package util;

import geometry.*;
import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import static geometry.ShapeFactory.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileManager {
    public static void saveFile(Object obj, String filename){
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename+".dat"))) {
            oos.writeObject(obj);
        }catch(IOException ex){
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, "Unable to save "+filename+".dat", ex);
            return;
        }
        System.out.println(filename+".dat Succesfully saved");
    }
    public static void main(String[] args){
        Shape s;
        int n=80;
        double r=100;
        saveFile(Particle.solarSystem(), "SolarSystem");
        s=tetrahedron(r);
        saveFile(s, "tetrahedron");
        s=octahedron(r);
        saveFile(s, "octahedron");
        s=dodecahedron(r);
        saveFile(s, "dodecahedron");
        s=icosahedron(r);
        saveFile(s, "icosahedron");
        s=spikey(r);
        s.setColor(Color.red);
        saveFile(s, "spikey");
        s=star(r);
        s.setColor(Color.blue);
        saveFile(s, "star");
        s=sphere(n, r);
        saveFile(s, "sphere01");
        s=antisphere(n, r);
        saveFile(s, "sphere02");
        for(int i=0; i<3; i++){
            s=torus(n, r, r/2*(i+1));
            saveFile(s, "torus0"+(i+1));
        }
        s=paramSurface(n, (double u, double v) -> new Vector(r*v*Math.cos(2*u),r*v*Math.sin(2*u),r*v*Math.cos(u)));
        saveFile(s, "Parametric");
        s=mobiusStrip(n, r, 3*r/2);
        saveFile(s, "MobiusStrip");
        s=boySurface(n, r);
        saveFile(s, "BoySurface");
        s=briantSurface(n, r);
        saveFile(s, "BriantSurface");
        s=kleinBottle(n, r);
        saveFile(s, "KleinBottle");
        s=figureEight(n, r, 3);
        saveFile(s, "FigureEight");
        s=jello(n, 6);
        saveFile(s, "Gelatina");
        s=new Animation(n, -10, 10, -10, 10, (x,y,t)->Math.cos(x)*Math.sin(y)*Math.cos(t));
        saveFile(s, "WavePlot");
        s=cube(r);
        saveFile(s, "cube");
        for(int i=0; i<5; i++){
           s=s.subdivide();
           saveFile(s, "subdivision0"+(i+1)); 
        }
        
    }
}