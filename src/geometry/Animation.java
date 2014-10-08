package geometry;

import java.util.Arrays;

public class Animation extends Shape implements Runnable{
    @FunctionalInterface
    public interface DoubleFunction extends java.io.Serializable{
        double calculate(double x, double y, double t);
    }
    
    private final int mesh;
    private double time=0;
    private static final long timeout=10;
    private final double x0, y0, dx, dy;
    private final DoubleFunction lambda;
    
    public Animation(int n, double xmin, double xmax, double ymin, double ymax, DoubleFunction f) {
        super();
        mesh=n;
        x0=xmin;
        y0=ymin;
        dx=xmax-xmin;
        dy=ymax-ymin;
        lambda=f;
        vertex=new Vector[mesh*mesh];
        Arrays.parallelSetAll(vertex, i->new Vector(0,0,0));
        calcPoints(200, 200, 200, 0);
        setFaces();
    }
    private void setFaces(){
        faces=new Face[2*(mesh-1)*(mesh-1)];
        int k=0;
        for(int i=0; i<mesh-1; i++){
            for(int j=0; j<mesh-1; j++){
                faces[k++]=new Face(vertex, i*mesh+j, (i+1)*mesh+j, (i+1)*mesh+j+1, i*mesh+j+1);
                faces[k++]=new Face(vertex, i*mesh+j+1, (i+1)*mesh+j+1, (i+1)*mesh+j, i*mesh+j);
            }
        }
    }
    private void calcPoints(double lx, double ly, double lz, double t){
        double x, y;
        for(int i=0; i<mesh; i++){
            x=(double)i/mesh;
            for(int j=0; j<mesh; j++){
                y=(double)j/mesh;
                vertex[i*mesh+j].set(lx*(x-0.5), ly*(0.5-y), lz/2*lambda.calculate(dx*x+x0, dy*y+y0, t));
            }
        }
    }
    
    @Override
    public synchronized void run(){
        while(true){
            if(lambda!=null){
                calcPoints(200, 200, 200, time);
            }
            try{
                wait(timeout);
            }catch(InterruptedException ex){
                return;
            }
            time+=timeout/1E3;
        }
    }
}