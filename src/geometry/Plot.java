package geometry;

import gui.Viewer;
import mathematics.*;

public class Plot extends Shape{
    Viewer viewer;
    Calculus u;
    public Plot(Viewer v, Calculus f){
        viewer=v;
        u=f;
        calculatePoints();
    }
    public void calculatePoints(){
        int n=viewer.mesh;
        double xmin=viewer.xmin, xmax=viewer.xmax, ymin=viewer.ymin, ymax=viewer.ymax;
        vertex=new Vector[n*n];
        double x, y;
        double plane=200;
        double dx=xmax-xmin;
        double dy=ymax-ymin;
        for(int i=0; i<n; i++){
            x=(double)i/n;
            for(int j=0; j<n; j++){
                y=(double)j/n;
                vertex[i*n+j]=new Vector(plane*(x-0.5), plane*(0.5-y), Math.min(60*point(u, x*dx+xmin, y*dy+ymin), plane));
            }
        }
        Face[] t=new Face[2*(n-1)*(n-1)];
        for(int i=0; i<n-1; i++){
            for(int j=0; j<n-1; j++){
                int k=i*(n-1)+j;
                t[2*k]=new Face(vertex, i*n+j, i*n+j+1, (i+1)*n+j+1, (i+1)*n+j);
                t[2*k+1]=new Face(vertex, (i+1)*n+j, (i+1)*n+j+1, i*n+j+1, i*n+j);
            }
        }
        faces=t;
    }
    public static double point(Calculus f, double x, double y){
        return ((Complex)f.evaluate(new String[]{"x", "y"}, new Fraction(x), new Fraction(y)).simplify()).getRe().toDouble();
    }
}
