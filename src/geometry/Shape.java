package geometry;

import java.awt.Color;
import java.util.Arrays;
import java.util.ArrayList;
import static util.ArrayOp.*;
import static geometry.Vector.*;

public class Shape implements java.io.Serializable, java.lang.Cloneable{
    public Vector[] vertex;
    public Face[] faces;
    private double[][] R={{1,0,0,0},{0,1,0,0},{0,0,1,0},{0,0,0,1}};
    
    public Shape(){
        vertex=new Vector[0];
        faces=new Face[0];
    }
    public Shape(Vector[] v, Face[] f){
        vertex=Arrays.copyOf(v, v.length);
        faces=Arrays.copyOf(f, f.length);
    }
    
    public Vector[] getVertex(){
        return Arrays.copyOf(vertex, vertex.length);
    }
    public Triangle[] getTriangles(){
        Triangle[][] matrix=new Triangle[faces.length][];
        for(int i=0; i<faces.length; i++){
            matrix[i]=faces[i].getTriangles();
        }
        return merge(matrix);
    }
    public Face[] getFaces(){
        return Arrays.copyOf(faces, faces.length);
    }
    public void invert(){
        for(Face f: faces){
            f.invert();
        }
    }
    public void setColor(Color c){
        for(Face face : faces) {
            face.setColor(c);
        }
    }
    
    public void translate(Vector v){
        for(int i=0; i<3; i++){
            R[i][3]+=v.getComp(i);
        }
    }
    public void setPosition(Vector v){
        for(int i=0; i<3; i++){
            R[i][3]=v.getComp(i);
        }
    }
    public Vector getPosition(){
        return new Vector(R[0][3], R[1][3], R[2][3]);
    }
    
    public void rotate(Vector axis, double angle){
        R=Matrix.multiply(R, Matrix.axisRotation(axis, angle));
    }
    public void rotate(double alpha, double beta, double gamma){
        R=Matrix.multiply(R, Matrix.eulerRotation(alpha, beta, gamma));
    }
    public void setRotationMatrix(double[][] matrix){
        R=matrix;
    }
    public double[][] getRotationMatrix(){
        return R;
    }
    
    public int[] mapEdges(int p){
        int k=0;
        int[] map=new int[vertex.length];
        for(Face face : faces){
            int b=0;
            int[] fi=face.getIndex();
            while(b<fi.length){
                b+=(fi[b]==p)? fi.length+1: 1;
            }
            if(b>fi.length){
                map[k++]=fi[b%fi.length];
            }
        }
        map=Arrays.copyOfRange(map, 0, k);
        return map;
    }
    public int[] mapFaces(int p){
        int k=0;
        int[] map=new int[faces.length];
        for(int i=0; i<faces.length; i++){
            int b=0;
            int[] fi=faces[i].getIndex();
            while(b<fi.length){
                b+=(fi[b]==p)? fi.length+1: 1;
            }
            if(b>fi.length){
                map[k++]=i;
            }
        }
        map=Arrays.copyOfRange(map, 0, k);
        return map;
    }
    public int[] commonFaces(int p, int q){
        int[] mp=mapFaces(p), mq=mapFaces(q);
        int k=0;
        for(int i=0; i<mp.length; i++){
            int j=0;
            while(j<mq.length){
                if(mp[i]==mq[j]){
                    mp[k++]=mp[i];
                    j+=mq.length;
                }
                j++;
            }
        }
        return Arrays.copyOfRange(mp, 0, k);
    }
    
    public Shape subdivide(){
        Vector[] facePoint=new Vector[faces.length];
        int[][] indexMap=new int[faces.length][];
        //For each face, add a face point
        for(int i=0; i<faces.length; i++){
            facePoint[i]=faces[i].getCenter();
            indexMap[i]=new int[faces[i].getIndex().length];
        }
        ArrayList<Vector> edgePoint=new ArrayList();
        //For each edge, add an edge point
        //Map each edge point on all adjacent original faces
        int e=0;
        for(int i=0; i<vertex.length; i++){
            for(int j: mapEdges(i)){
                if(i<j){
                    int[] cf=commonFaces(i, j);
                    edgePoint.add(average(vertex[i], vertex[j], facePoint[cf[0]], facePoint[cf[1]]));
                    for(int f: cf){
                        int k=0;
                        int[] b=faces[f].getIndex();
                        while(k<b.length){
                            if(i==b[k]){
                                if(b[(k+1)%b.length]==j){
                                    indexMap[f][k]=e;
                                }else{
                                    indexMap[f][(k-1+b.length)%b.length]=e; 
                                }
                                k+=b.length;
                            }
                            k++;
                        }
                    }
                    e++;
                }
            }
        }
        /* 
         * For each original point P, take the average F of all n (recently created) 
         * face points for faces touching P, and take the average R of all n edge midpoints
         * for edges touching P, where each edge midpoint is the average of its two endpoint 
         * vertices. Move each original point to the point (F+2R+(n-3)P)/n.
         */
        for(int i=0; i<vertex.length; i++){
            int[] m=mapFaces(i);
            int n=m.length;
            Vector[] temp=new Vector[n];
            int k=0;
            for(int j: m){
                temp[k++]=facePoint[j];
            }
            Vector F=average(temp);
            m=mapEdges(i);
            temp=new Vector[m.length];
            k=0;
            for(int j: m){
                temp[k++]=average(vertex[i], vertex[j]);
            }
            Vector R=average(temp);
            vertex[i]=add(F, R.scale(2), vertex[i].scale(n-3)).scale(1.0/n);
        }
        /* 
         * For each face point, add an edge for every edge of the face, 
         * connecting the face point to each edge point for the face.
         * Connect each new vertex point to the new edge points of all original
         * edges incident on the original vertex.
         */
        Vector[] v=merge(vertex, facePoint, edgePoint.toArray(new Vector[0]));
        ArrayList<Face> faceList=new ArrayList();
        for(int i=0; i<faces.length; i++){
            int[] a=indexMap[i];
            for(int j=0; j<a.length; j++){
                Face f=new Face(v, vertex.length+i,
                    vertex.length+facePoint.length+a[(j+a.length-1)%a.length],
                    faces[i].getIndex(j), vertex.length+facePoint.length+a[j]);
                f.setColor(faces[i].getColor());
                faceList.add(f);
            }  
        }       
        return new Shape(v, faceList.toArray(new Face[0]));
    }
}