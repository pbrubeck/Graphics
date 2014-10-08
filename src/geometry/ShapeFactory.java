package geometry;

import static geometry.Vector.*;
import static geometry.Matrix.*;
import java.awt.Color;
import static util.ArrayOp.*;
import java.util.Arrays;
import mathematics.*;
import static mathematics.Calculus.divide;

public class ShapeFactory {
    @FunctionalInterface
    public interface DoubleFunction{
        double calculate(double x, double y, double t);
    }
    @FunctionalInterface
    public interface Parametric{
        Vector calculate(double u, double v);
    }
    
    //util
    public static Shape combine(Shape... array){
        Triangle[][] m=new Triangle[array.length][];
        Vector[][] v=new Vector[array.length][];       
        for(int i=0; i<array.length; i++){
            v[i]=array[i].getVertex();
            m[i]=array[i].getTriangles();
        }
        int d=0;
        Vector[] p=merge(v);
        for(int i=0; i<m.length; i++){
            for(Triangle t : m[i]) {
                int[] index=t.getIndex();
                for(int k=0; k<index.length; k++){
                    index[k]+=d;
                }
                t.set(p, index);
            }
            d+=v[i].length;
        }
        return new Shape(p, merge(m));
    }
    
    //Regular shapes
    public static Shape polygon(int n, double r){
        double angle=2*Math.PI/n;
        Vector[] v=new Vector[n];
        for(int i=0; i<n; i++){
            v[i]=new Vector(r*Math.cos(angle*i), r*Math.sin(angle*i), 0);
        }
        return new Shape(v, new Face[]{new Face(v)});
    }
    public static Shape pyramid(int n, double r, double h){
        Shape p=polygon(n,r);
        Vector[] v=new Vector[n+2];
        System.arraycopy(p.vertex, 0, v, 0, n);
        v[n]=new Vector(0,0,-h/2);
        v[n+1]=new Vector(0,0,h/2);
        Face[] t=new Face[2*n];
        for(int i=0; i<n; i++){
            t[i]=p.faces[i];
            t[i+n]=new Triangle(v, i, (i+1)%n, n+1);
        }
        return new Shape(v, t);
    }
    public static Shape prism(int n, double r, double h){
        double angle=2*Math.PI/n;
        Vector[] v=new Vector[2*n+2];
        double sin, cos;
        for(int i=0; i<n; i++){
            cos=Math.cos(angle*i);
            sin=Math.sin(angle*i);
            v[i]=new Vector(r*cos,r*sin,h/2);
            v[i+n]=new Vector(r*cos,r*sin,-h/2);
        }
        v[2*n]=new Vector(0,0,h/2);
        v[2*n+1]=new Vector(0,0,h/2);
        Triangle[] t=new Triangle[4*n];
        for(int i=0; i<n; i++){
            t[i]=new Triangle(v,  i, (i+1)%n, 2*n);
            t[i+n]=new Triangle(v, 2*n+1, (i+1)%n+n, i+n);
            t[i+2*n]=new Triangle(v, i,  i+n,(i+1)%n);
            t[i+3*n]=new Triangle(v, i+n, (i+1)%n+n, (i+1)%n);
        }
        return new Shape(v,t);
    }
    public static Shape tube(int n, double r, Vector... p){
        Vector[] vertex=new Vector[n*p.length];
        Face[] f=new Face[n*(p.length-1)];
        Shape poly=polygon(n, r);
        Vector u=poly.getFaces()[0].getNormal();
        Vector norm=null;
        int m=0, k=0;
        for(int i=0;  i<p.length; i++){
            if(i==0){
                norm=subtract(p[i+1], p[i]);
            }else if(i==p.length-1){
                norm=subtract(p[i], p[i-1]);
            }else{
                norm=average(norm, subtract(p[i+1], p[i]));
            }
            Vector axis=crossP(norm, u);
            double angle=norm.getAngle(u);
            double[][] M=axisRotation(axis, angle);
            for(Vector v: poly.getVertex()){
                vertex[k++]=add(p[i], new Vector(transpose(multiply(M, v.toColumn()))[0]));
            }
            if(i>0){
                for(int j=0; j<n; j++){
                   f[m++]=new Face(vertex, (i-1)*n+j, (i-1)*n+(j+1)%n, i*n+(j+1)%n, i*n+j);
                }
            }
        }
        return new Shape(vertex, f);
    }
    public static Shape curl(int n, double R, double r){
        Vector[] v=new Vector[n];
        double dt=4*Math.PI/n;
        for(int i=0; i<n; i++){
            v[i]=new Vector(R*Math.cos(dt*i), R*Math.sin(dt*i), 4*r*Math.sin(2*i*Math.PI/n));
        }
        return tube(n, r, v);
    }
    
    //Platonic solids
    public static Shape tetrahedron(double r){
        double h=r/Math.sqrt(2);
        Vector v[]={new Vector(r,0,-h), new Vector(-r,0,-h), new Vector(0,r,h), new Vector(0,-r,h)};
        Triangle t[]={new Triangle(v,0,1,2), new Triangle(v,0,3,1), new Triangle(v,0,2,3), new Triangle(v,1,3,2)};
        return new Shape(v, t);
    }
    public static Shape cube(double r){
        double h=r/2;
        Vector v[]={new Vector(-h,-h,-h), new Vector(-h,-h,h), new Vector(-h,h,h), new Vector(-h,h,-h),
            new Vector(h,h,-h), new Vector(h,-h,-h), new Vector(h,-h,h), new Vector(h,h,h)};
        Face f[]={new Face(v,0,1,2,3), new Face(v,7,6,5,4), new Face(v,1,6,7,2), 
            new Face(v,7,4,3,2), new Face(v,0,5,6,1), new Face(v,0,3,4,5)};
        return new Shape(v, f);
    }
    public static Shape octahedron(double r){
        Vector v[]={new Vector(0,0,-r), new Vector(0,-r,0), new Vector(-r,0,0),
            new Vector(0,r,0), new Vector(r,0,0), new Vector(0,0,r)};
        Triangle t[]=new Triangle[8];
        for(int i=0; i<4; i++){
            t[2*i]=new Triangle(v,  0, i+1,(i+1)%4+1);
            t[2*i+1]=new Triangle(v, 5, (i+1)%4+1, i+1);
        }
        return new Shape(v, t);
    }
    public static Shape dodecahedron(double r){
        double f=r*(1+Math.sqrt(5))/2, g=r*2/(1+Math.sqrt(5));
        double[][] M={{g,f,0}, {-g,f,0}, {-r,r,-r}, {0,g,-f}, {r,r,-r},
            {r,r,r}, {0,g,f}, {-r,r,r}, {-f,0,g}, {-f,0,-g},
            {-r,-r,-r}, {0,-g,-f}, {r,-r,-r}, {f,0,-g}, {f,0,g},
            {-g,-f,0}, {g,-f,0}, {r,-r,r}, {0,-g,f}, {-r,-r,r}};
        Vector v[]=new Vector[M.length];
        for(int i=0; i<M.length; i++){
            v[i]=new Vector(M[i]);
        }
        Face[] t=new Face[12];
        for(int i=0; i<5; i++){
            t[i]=new Face(v, 2*i+5, i, (i+1)%5, 2*((i+1)%5)+5, 2*i+6);
            t[i+5]=new Face(v, 2*i+6, 15+(i+3)%5, 15+(i+2)%5,  2*((i+4)%5)+6, 2*i+5);
        }
        t[10]=new Face(v, 4, 3, 2, 1, 0);
        t[11]=new Face(v, 15, 16, 17, 18, 19);
        return new Shape(v, t);
    }
    public static Shape icosahedron(double r){
        double h=r*(Math.sqrt(5)+1)/2;
        //key: xyyxzz
        Vector v[]={new Vector(0,r,h), new Vector(-r,h,0), new Vector(r,h,0),
            new Vector(h,0,r), new Vector(0,-r,h), new Vector(-h,0,r),
            new Vector(-h,0,-r), new Vector(0,r,-h),new Vector(h,0,-r),
            new Vector(r,-h,0), new Vector(-r,-h,0), new Vector(0,-r,-h)};
        Triangle t[]=new Triangle[20];
        for(int i=1; i<6; i++){
            t[i-1]=new Triangle(v, 0, i%5+1, i);
            t[i+4]=new Triangle(v, i%5+6, i+5, i);
            t[i+9]=new Triangle(v, i, i%5+1, i%5+6);
            t[i+14]=new Triangle(v, 6+i%5, 11, i+5);
        }
        return new Shape(v, t);
    }
    
    //Tesselations
    public static Shape spikey(double r){
        Shape s=icosahedron(r);
        Vector[] v=Arrays.copyOf(s.vertex, 32);
        Triangle[] t=new Triangle[60];
        int i=0;
        for(Face f: s.getFaces()){
            int[] index=((Triangle)f).getIndex();
            v[12+i]=f.getNormal().normalize().scale(3*r);
            t[3*i]=new Triangle(v, 12+i, index[0], index[1]);
            t[3*i+1]=new Triangle(v, 12+i, index[1], index[2]);
            t[3*i+2]=new Triangle(v, 12+i, index[2], index[0]);
            i++;
        }
        return new Shape(v,t);
    }
    public static Shape star(double r){
        Shape s=dodecahedron(r);
        Vector[] v=Arrays.copyOf(s.vertex, 32);
        Triangle[] t=new Triangle[60];
        Face[] f=s.getFaces();
        for(int i=0; i<12; i++){
            int[] index=f[i].getIndex();
            v[20+i]=f[i].getNormal().normalize().scale(3*r);
            for(int j=0; j<5; j++){
                t[5*i+j]=new Triangle(v, 20+i, index[j], index[(j+1)%5]);
            }
        }
        return new Shape(v,t);
    }
    
    //Orientable
    public static Shape sphere(int n, double r){
        double th, cos, sin;
        double[] zh=new double[n], rh=new double[n];
        for(int i=0; i<n; i++){
            th=(2*i+1)*Math.PI/(2*n);
            zh[i]=r*Math.cos(th);
            rh[i]=r*Math.sin(th);
        }
        int k=n*n;
        Vector[] v=new Vector[k+2];
        for(int i=0; i<n; i++){
            th=2*i*Math.PI/n;
            cos=Math.cos(th);
            sin=Math.sin(th);
            for(int j=0; j<n; j++){
                v[j*n+i]=new Vector(rh[j]*cos, rh[j]*sin, zh[j]);
            }
        }
        v[k]=new Vector(0,0,r);
        v[k+1]=new Vector(0,0,-r);
        Triangle[] t=new Triangle[2*k];
        for(int i=0; i<n; i++){
            for(int j=0; j<n; j++){
                t[2*(i*n+j)]=new Triangle(v,  i*n+(j+1)%n, i==0? k: (i-1)*n+(j+1)%n, i*n+j);
                t[2*(i*n+j)+1]=new Triangle(v, i*n+j, i+1==n? k+1: (i+1)*n+j, i*n+(j+1)%n);
            }
        }
        return new Shape(v, t);
    }
    public static Shape antisphere(int n, double r){
        double th, zh, rh;
        int k=n*n;
        Vector[] points=new Vector[k+2];
        for(int i=0; i<n; i++){
            th=(2*i+1)*Math.PI/(2*n);
            zh=r*Math.cos(th);
            rh=r*Math.sin(th);
            for(int j=0; j<n; j++){
                th=(2*j+i)*Math.PI/n;
                points[i*n+j]=new Vector(rh*Math.cos(th), rh*Math.sin(th), zh);
            }
        }
        points[k]=new Vector(0,0,r);
        points[k+1]=new Vector(0,0,-r);
        Triangle[] faces=new Triangle[2*k];
        for(int i=0; i<n; i++){
            for(int j=0; j<n; j++){
                faces[2*(i*n+j)]=new Triangle(points,  i*n+(j+1)%n, i==0? k: (i-1)*n+(j+1)%n, i*n+j);
                faces[2*(i*n+j)+1]=new Triangle(points, i*n+j, i+1==n? k+1: (i+1)*n+j, i*n+(j+1)%n);
            }
        }
        return new Shape(points, faces);
    }
    public static Shape torus(int n, double c, double a){
        return paramHull(n,(u,v)->{
            double z=a*Math.sin(2*v);
            double t=c+a*Math.cos(2*v);
            return new Vector(t*Math.cos(u),t*Math.sin(u),z);
        });
    }
    
    //Nonorientable
    public static Shape mobiusStrip(int n, double w, double r){
        double x, y, z, t, s;
        Vector[] points=new Vector[n*n];
        Face[] faces=new Face[2*n*(n-1)];
        for(int i=0; i<n; i++){
            s=(2*i-n+1)*w/(n-1);
            for(int j=0; j<n; j++){
                t=2*j*Math.PI/n;
                double p=r+s*Math.cos(t/2);
                x=p*Math.cos(t);
                y=p*Math.sin(t);
                z=s*Math.sin(t/2);
                points[i*n+j]=new Vector(x,y,z);
            }
        }
        for(int i=0; i<n-1; i++){
            for(int j=0; j<n; j++){
                int k=i*n+j;
                boolean b=(j+1==n);
                faces[2*k]=new Face(points, k+n, b? n*(n-i-2): k+n+1,  b? n*(n-i-1):k+1, k);
                faces[2*k+1]=new Face(points, k+n, k, b? n*(n-i-1):k+1, b? n*(n-i-2): k+n+1);
            }
        }
        return new Shape(points, faces);
    }
    public static Shape boySurface(int n, double r){
        Vector[] points=new Vector[n*n];
        Face[] faces=new Face[2*n*n];
        for(int i=0; i<n; i++){
            double v=i*Math.PI/(n-1);
            double a=(Math.cos(2*v)+1)/2;
            double b=Math.sin(2*v);
            for(int j=0; j<n; j++){
                double u=(2*j-n)*Math.PI/(2*n-2);
                double cos=Math.cos(u);
                double sin=Math.sin(u);
                double den=2-Math.sqrt(2)*Math.sin(3*u)*b;
                double x=Math.sqrt(2)*a*(2*cos*cos-1)+cos*b;
                double y=Math.sqrt(2)*a*(2*sin*cos)-sin*b;
                double z=3*a;
                points[i*n+j]=new Vector(r*x/den, r*y/den, r*z/den);
            }
        }
        for(int i=0; i<n; i++){
            for(int j=0; j<n; j++){
                int k=i*n+j;
                faces[2*k]=new Face(points, k, i*n+(j+1)%n, n*((i+1)%n)+(j+1)%n, n*((i+1)%n)+j);
                faces[2*k+1]=new Face(points, k, n*((i+1)%n)+j, n*((i+1)%n)+(j+1)%n, i*n+(j+1)%n);
            }
        }
        return new Shape(points, faces);
    }
    public static Shape briantSurface(int n, double r){
        Vector[] points=new Vector[n*n];
        Face[] faces=new Face[2*n*n];
        Polynomial den=new Polynomial(null, -1,0,0,Math.sqrt(5),0,0,1);
        Polynomial p1=new Polynomial(null, 0,1,0,0,0,-1);
        Polynomial p2=new Polynomial(null, 0,1,0,0,0,1);
        Polynomial p3=new Polynomial(null, 1,0,0,0,0,0,1);
        for(int i=0; i<n; i++){
            double v=2*i*Math.PI/n;
            double cos=Math.cos(v);
            double sin=Math.sin(v);
            for(int j=0; j<n; j++){
                double u=(double)j/(n-1);
                Complex z=new Rectangular(u*cos,u*sin);
                Complex d=(Complex)den.evaluate(z);
                double g1=-3*(divide((Complex)p1.evaluate(z),d).getIm()).toDouble()/2;
                double g2=-3*(divide((Complex)p2.evaluate(z),d).getRe()).toDouble()/2;
                double g3=(divide((Complex)p3.evaluate(z),d).getIm()).toDouble()-0.5;
                double g=g1*g1+g2*g2+g3*g3;
                points[i*n+j]=new Vector(r*g1/g, r*g2/g, r*g3/g);
            }
        }
        for(int i=0; i<n; i++){
            for(int j=0; j<n; j++){
                int k=i*n+j;
                faces[2*k]=new Face(points, k, i*n+(j+1)%n, n*((i+1)%n)+(j+1)%n, n*((i+1)%n)+j);
                faces[2*k+1]=new Face(points, k, n*((i+1)%n)+j, n*((i+1)%n)+(j+1)%n, i*n+(j+1)%n);
            }
        }
        return new Shape(points, faces);
    }
    public static Shape kleinBottle(int n, double r){
        return paramSurface(n, (u,v) -> {
            u*=n/(n-1);
            double a=Math.sqrt(2)+Math.cos(2*v);
            double b=Math.sin(4*v)/2;
            double cos=Math.cos(u);
            double sin=Math.sin(u);
            double t=r*(a*cos+b*sin);
            double z=r*(b*cos-a*sin);
            return new Vector(t*Math.cos(2*u), t*Math.sin(2*u), z);
        });
    }
    public static Shape figureEight(int n, double r, double c){
        return paramHull(n, (u,v)->{
            v*=2*(n-1)/n;
            double a=Math.sin(v);
            double b=Math.sin(2*v);
            double cos=Math.cos(-u);
            double sin=Math.sin(-u);
            double t=r*(c+a*cos-b*sin);
            return new Vector(t*Math.cos(-2*u), t*Math.sin(-2*u), r*(a*sin+b*cos));
        });
    }
    public static Shape crossCappedDisk(int n, double r){
        return paramSurface(n, (u,v)->{
            double s=r*(1+Math.cos(2*v));
            return new Vector(s*Math.cos(u), s*Math.sin(u), -Math.tanh(u-Math.PI)*r*Math.sin(2*v));
        });
    }
    
    //Fractal
    public static Shape kochSnowflake(int n, double r){
        double h=Math.sqrt(3)/2;
        Vector norm=new Vector(0,0,h/3.001);
        Vector[] u={new Vector(0,r,0), new Vector(-r*h, -r/2,0), new Vector(r*h, -r/2,0)};
        while(n>0){
            Vector[] g=new Vector[4*u.length];
            for(int i=0; i<u.length; i++){
                Vector a=u[i], b=u[(i+1)%u.length];
                g[4*i]=a;
                g[4*i+1]=average(a, a, b);
                g[4*i+2]=add(crossP(subtract(b, a), norm), average(a, b));
                g[4*i+3]=average(a, b, b);
            }
            u=g;
            n--;
        }
        Face[] f={new Face(u)};
        return new Shape(u, f);
    }
    
    //Functional
    public static Shape paramSurface(int n, Parametric p){
        double u, v;
        Vector[] points=new Vector[n*n];
        Face[] faces=new Face[2*n*(n-1)];
        for(int i=0; i<n; i++){
            v=i*Math.PI/(n-1);
            for(int j=0; j<n; j++){
                u=2*j*Math.PI/n;
                points[i*n+j]=p.calculate(u,v);
            }
        }
        for(int i=0; i<n-1; i++){
            for(int j=0; j<n; j++){
                int k=i*n+j;
                faces[2*k]=new Face(points, k, i*n+(j+1)%n, n*((i+1)%n)+(j+1)%n, n*((i+1)%n)+j);
                faces[2*k+1]=new Face(points, k, n*((i+1)%n)+j, n*((i+1)%n)+(j+1)%n, i*n+(j+1)%n);
            }
        }
        return new Shape(points, faces);
    }
    public static Shape paramHull(int n, Parametric p){
        double u, v;
        Vector[] points=new Vector[n*n];
        Face[] faces=new Face[n*(n-1)];
        for(int i=0; i<n; i++){
            v=i*Math.PI/(n-1);
            for(int j=0; j<n; j++){
                u=2*j*Math.PI/n;
                points[i*n+j]=p.calculate(u,v);
            }
        }
        for(int i=0; i<n-1; i++){
            for(int j=0; j<n; j++){
                int k=i*n+j;
                faces[k]=new Face(points, k, i*n+(j+1)%n, n*((i+1)%n)+(j+1)%n, n*((i+1)%n)+j);
            }
        }
        return new Shape(points, faces);
    }
    public static Shape plot(int n, double xmin, double xmax, double ymin, double ymax, DoubleFunction f){
        Vector[] points=new Vector[n*n];
        double x, y, t;
        double length=200;
        double dx=xmax-xmin;
        double dy=ymax-ymin;
        t=System.currentTimeMillis();
        for(int i=0; i<n; i++){
            x=(double)i/n;
            for(int j=0; j<n; j++){
                y=(double)j/n;
                points[i*n+j]=new Vector(length*(x-0.5), length*(0.5-y), Math.min(length/2*f.calculate(x*dx+xmin, y*dy+ymin, t), length));
            }
        }
        Face[] faces=new Face[2*(n-1)*(n-1)];
        for(int i=0; i<n-1; i++){
            for(int j=0; j<n-1; j++){
                int k=i*(n-1)+j;
                faces[2*k]=new Face(points, i*n+j, i*n+j+1, (i+1)*n+j+1, (i+1)*n+j);
                faces[2*k+1]=new Face(points, (i+1)*n+j, (i+1)*n+j+1, i*n+j+1, i*n+j);
            }
        }
        return new Shape(points, faces);
    }
    public static Shape jello(int n, int m){
        Shape s= paramSurface(n, (u,v)->{
            double r=5*(Math.cos(2*m*u)+15)*(v+1);
            return new Vector(r*Math.sin(u), r*Math.cos(u), 200*Math.sin(v));
        });
        return s;
    }
    
    
    public static double[] curve(Vector[] points, double[] slopes){
        //Let k be the order of the polynomial and n the number of coefficients
        int n=2*points.length;
        double[] curve=new double[n+1];
        double[][] matrix=new double[n][];
        for(int i=0; i<points.length; i++){
            double x=points[i].getComp(0);
            double y=points[i].getComp(1);
            double m=slopes[i];
            double[] a={1, x, y};
            double[] b={0, 1, m};
            a=Arrays.copyOf(a, n+1);
            b=Arrays.copyOf(b, n+1);
            int k=2;
            for(int j=3; j<n+1; j+=2){
                a[j]=a[j-2]*x;
                a[j+1]=a[j-1]*y;
                b[j]=k*a[j-2];
                b[j+1]=k*a[j-1]*m;
                k++;
            }
            a[n]=-a[n];
            b[n]=-b[n];
            matrix[2*i]=a;
            matrix[2*i+1]=b;
        }
        GaussJordan(matrix);
        curve[n]=1;
        for(int i=0; i<n; i++){
            curve[i]=matrix[i][n];
        }
        return curve;
    }
}