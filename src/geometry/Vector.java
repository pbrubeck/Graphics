package geometry;

import java.util.Arrays;
import static geometry.Matrix.*;

public class Vector implements java.io.Serializable, java.lang.Cloneable{
    private double[] comp;
    
    public Vector(double... x){
        comp=x;
    }
    
    public void set(double... x){
        comp=Arrays.copyOf(x, x.length);
    }
    
    //Transformations
    public Vector scale(double x){
        for(int i=0; i<comp.length; i++){
            comp[i]*=x;
        }
        return this;
    }
    public Vector translate(Vector v){
        for(int i=0; i<comp.length; i++) {
            comp[i]+=v.getComp(i);
        }
        return this;
    }
    public Vector rotate(Vector axis, double angle){
        set(transpose(Matrix.multiply(axisRotation(axis, angle), toColumn()))[0]);
        return this;
    }
    public Vector rotate(double alpha, double beta, double gamma){
        set(transpose(Matrix.multiply(eulerRotation(alpha, beta, gamma), toColumn(4)))[0]);
        return this;
    }
    public Vector normalize(){
        return scale(1/getLength());
    }
    
    //Accesors
    public int getDimension(){
        return comp.length;
    }
    public double getLength(){
        double x=0;
        for(int i=0; i<comp.length; i++){
            x+=comp[i]*comp[i];
        }
        return Math.sqrt(x);
    }
    public double getComp(int i){
        return i<comp.length? comp[i]: 0;
    }
    public double[] getComp(){
        return comp;
    }
    public double[][] toRow(){
        return new double[][]{comp};
    }
    public double[][] toColumn(){
        double[][] a=new double[comp.length][1];
        for(int i=0; i<a.length; i++){
            a[i][0]=comp[i];
        }
        return a;
    }
    public double[][] toColumn(int length){
        double[][] a=new double[length][1];
        int i=0;
        for(; i<comp.length; i++){
            a[i][0]=comp[i];
        }
        for(; i<length; i++){
            a[i][0]=i==length-1?1:0;
        }
        return a;
    }
    
    public double getAngle(Vector v){
        return Math.acos(dotP(this, v)/(getLength()*v.getLength()));
    }
    public double getAngle(Triangle t){
        Vector n=t.getNormal();
        return Math.asin(dotP(this, n)/(getLength()*n.getLength()));
    }

    //Binary operations
    public static Vector add(Vector... v){
        double[] a=new double[v[0].getDimension()];
        for(Vector u : v) {
            for(int i=0; i<a.length; i++) {
                a[i]+=u.getComp(i);
            }
        }
        return new Vector(a);
    }
    public static Vector subtract(Vector u, Vector v){
        double[] a=new double[u.getDimension()];
        double[] b=u.comp;
        double[] c=v.comp;
        for(int i=0; i<b.length; i++){
            a[i]=b[i]-c[i];
        }
        return new Vector(a);
    }
    public static double distance(Vector u, Vector v){
        return subtract(v,u).getLength();
    }
    public static Vector multiply(double x, Vector v){
        double[] a=new double[v.getDimension()];
        for(int i=0; i<a.length; i++){
            a[i]=x*v.getComp(i);
        }
        return new Vector(a);
    }
    public static double dotP(Vector u, Vector v){
        int p=0, d=Math.min(u.getDimension(), v.getDimension());
        for(int i=0; i<d; i++){
            p+=u.getComp(i)*v.getComp(i);
        }
        return p;
    }
    public static Vector crossP(Vector u, Vector v){
        if(u.getDimension()==3 && v.getDimension()==3){
            double[] a=new double[3];
            for(int i=0; i<3; i++){
                a[i]=u.getComp((i+1)%3)*v.getComp((i+2)%3)-u.getComp((i+2)%3)*v.getComp((i+1)%3);
            }
            return new Vector(a);  
        }
        return null;
    }
    public static Vector average(Vector... v){
        return add(v).scale(1.0/v.length);
    }
    public static Vector randomVector(double r){
        double[] a=new double[3];
        for(int i=0; i<a.length; i++){
            a[i]=r*(2*Math.random()-1);
        }
        return new Vector(a);
    }
    
    @Override
    public String toString(){
        return Arrays.toString(comp);
    }
}