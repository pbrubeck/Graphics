package geometry;

import java.awt.Color;
import static geometry.Vector.*;

public class Triangle extends Face{
    public Triangle(Vector[] v, int a, int b, int c){
        vertex=v;
        index=new int[]{a,b,c};
        triangles=new Triangle[]{this};
    }
    
    @Override
    public void invert(){
        int[] arr=new int[index.length];
        for(int i=0; i<arr.length; i++){
            arr[i]=index[arr.length-i-1];
        }
        setIndex(arr);
    }
    @Override
    public void setColor(Color c){
        color=c;
    }
    @Override
    public void setVertex(Vector[] v){
        vertex=v;
    }
    @Override
    public Vector getNormal(){
        Vector u=subtract(vertex[index[1]], vertex[index[0]]);
        Vector v=subtract(vertex[index[2]], vertex[index[0]]);
        return crossP(u, v);
    }
}