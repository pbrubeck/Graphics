package geometry;

import java.awt.Color;
import java.util.Arrays;
import java.util.LinkedList;

import static geometry.Vector.*;

public class Face implements java.io.Serializable, java.lang.Cloneable{
    public Color color;
    public int[] index;
    public Vector[] vertex;
    public Triangle[] triangles;
    
    public Face(){
        
    }
    public Face(Vector... v){
        index=new int[v.length];
        for(int i=0; i<v.length; i++){
            index[i]=i;
        }
        vertex=Arrays.copyOf(v, v.length);
        triangles=triangulation();
    }
    public Face(Vector[] v, int... p){
        index=p;
        vertex=v;
        triangles=new Triangle[p.length-2];
        for(int i=0; i<triangles.length; i++){
            triangles[i]=new Triangle(vertex, p[(i+2)%p.length], p[0], p[(i+1)%p.length]);
        }
    }
    public final Triangle[] triangulation(){
        LinkedList <Integer> V=new LinkedList();
        LinkedList <Integer> E=new LinkedList();
        LinkedList <Triangle> T=new LinkedList();
        int n=index.length;
        for(int i=0; i<n; i++){
            V.add(index[i]);
        }
        for(int i=0; i<n; i++){
            classify(i, V, E);
        }
        while(n>3){
            int i=V.indexOf(E.getFirst());
            Integer K=V.get(i);
            T.add(new Triangle(vertex, V.get((i+n-1)%n), K, V.get((i+1)%n)));
            classify((i+1)%n, V, E);
            classify((i+n-1)%n, V, E);
            V.remove(K);
            E.removeFirst();
            n--;
        }
        T.add(new Triangle(vertex, V.get(0), V.get(1), V.get(2)));
        return T.toArray(new Triangle[]{});
    }
    public void classify(int i, LinkedList<Integer> V, LinkedList E){
        int n=V.size();
        Integer K=V.get(i);
        Vector a=vertex[K];
        Vector AB=subtract(vertex[V.get((i+1)%n)], a);
        Vector AC=subtract(vertex[V.get((i+n-1)%n)], a);
        Vector norm=new Vector(0,0,1);
        if(dotP(crossP(AB, AC), norm)>=0){
            double dot00=dotP(AC,AC), dot01=dotP(AC,AB), dot11=dotP(AB,AB);
            double denom=(dot00*dot11-dot01*dot01);
            int j=2;
            while(j<n-1){
                Vector AP=subtract(vertex[V.get((i+j)%n)], a);
                double dot02=dotP(AC,AP), dot12=dotP(AB,AP);
                double u=(dot11*dot02-dot01*dot12)/denom;
                double v=(dot00*dot12-dot01*dot02)/denom;
                j+=(u>=0 && v>=0 && u+v<1)? n:1;
            }if(j<n){
                if(E.indexOf(K)<0){
                    E.add(K); 
                }
            }else if(E.indexOf(K)>=0){
                E.remove(K);
            }
        }else{
            if(E.indexOf(K)>=0){
                E.remove(K);
            }
        }
    }
    public static boolean inside(Vector p, Vector a, Vector b, Vector c){
        Vector AB=subtract(b,a);
        Vector AC=subtract(c,a);
        Vector AP=subtract(p,a);
        double dot00=dotP(AC,AC);
        double dot01=dotP(AC,AB);
        double dot02=dotP(AC,AP);
        double dot11=dotP(AB,AB);
        double dot12=dotP(AB,AP);
        double denom=(dot00*dot11-dot01*dot01);
        double u=(dot11*dot02-dot01*dot12)/denom;
        double v=(dot00*dot12-dot01*dot02)/denom;
        return (u>=0) && (v>=0) && (u+v<1);
    }
    
    public void invert(){
        int[] arr=new int[index.length];
        for(int i=0; i<arr.length; i++){
            arr[i]=index[arr.length-i-1];
        }
        setIndex(arr);
        for(Triangle t: triangles){
            t.invert();
        }
    }
    
    public void setColor(Color c){
        color=c;
        for(Triangle t: triangles){
            t.setColor(c);
        }
    }
    public void setVertex(Vector[] v){
        vertex=v;
        for(Triangle t: triangles){
            t.setVertex(v);
        }
    }
    public void setIndex(int[] p){
        index=p;
        triangles=new Triangle[p.length-2];
        for(int i=0; i<triangles.length; i++){
            triangles[i]=new Triangle(vertex, p[(i+2)%p.length], p[0], p[(i+1)%p.length]);
        }
    }
    public void set(Vector[] v, int[] i){
        setVertex(v);
        setIndex(i);
    }
    
    public int[] getIndex(){
        return Arrays.copyOf(index, index.length);
    }
    public int getIndex(int i){
        return index[i];
    }
    public Color getColor(){
        return color;
    }
    public Vector[] getVertex(){
        Vector[] points=new Vector[index.length];
        for(int i=0; i<index.length; i++){
            points[i]=vertex[index[i]];
        }
        return points;
    }
    public Vector getVertex(int i){
        return vertex[index[i]];
    }
    public Triangle[] getTriangles(){
        return triangles;
    }
    public Vector getNormal(){
        return triangles[0].getNormal();
    }
    public Vector getCenter(){
        return average(getVertex());
    }
}