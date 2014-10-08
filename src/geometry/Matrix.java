package geometry;

public class Matrix {
    public static String ArraytoString(double[] A){
        String result="[";
        for(double t: A){
            result+=t+"\t";
        }
        return result+"]";
    }
    public static void printMatrix(double[][] M){
        for(double[] row: M){
            System.out.println(ArraytoString(row));
        }
    }
    public static double[][] add(double[][] A, double[][] B){
        if(A.length==B.length && A[0].length==B[0].length){
            for(int i=0; i<A.length; i++){
                for(int j=0; j<A.length; j++){
                    A[i][j]+=B[i][j];
                }
            }
        }else{
            A=new double[1][0];
        }
        return A;
    }
    public static double[][] multiply(double[][] X, double[][] Y){
        double[][] Z={{}};
        if(X[0].length==Y.length){
            Z=new double[X.length][Y[0].length];
            for(int i=0; i<Z.length; i++){
                for(int j=0; j<Z[0].length; j++){
                    Z[i][j]=0;
                    for(int k=0; k<Y.length; k++){
                        Z[i][j]+=X[i][k]*Y[k][j];
                    }
                }
            }
        }
        return Z;
    }
    public static double[][] multiply(double[][] M, double x){
        double[][] Z=new double[M.length][];
        for(int i=0; i<Z.length; i++){
            Z[i]=new double[M[i].length];
            for(int j=0; j<Z[i].length; j++){
                Z[i][j]=M[i][j]*x;
            }
        }
        return Z;
    }
    public static double[][] transpose(double[][] M){
        double[][] T=new double[M[0].length][M.length];
        for(int i=0; i<M.length; i++){
            for(int j=0; j<M[0].length; j++){
                T[j][i]=M[i][j];
            }
        }
        return T;
    }
    public static double[][] identity(int n){
        double[][] I=new double[n][n];
        for(int k=0; k<n; k++){
            I[k][k]=1;
        }
        return I;
    }
    public static double[][] eulerRotation(double alpha, double beta, double gamma){
        double cos=Math.cos(-alpha);
        double sin=Math.sin(-alpha);
        double[][] Rx={{1,0,0,0},{0,cos,-sin,0},{0,sin,cos,0},{0,0,0,1}};
        cos=Math.cos(-beta);
        sin=Math.sin(-beta);
        double[][] Ry={{cos,0,sin,0},{0,1,0,0},{-sin,0,cos,0},{0,0,0,1}};
        cos=Math.cos(-gamma);
        sin=Math.sin(-gamma);
        double[][] Rz={{cos,-sin,0,0},{sin,cos,0,0},{0,0,1,0},{0,0,0,1}};
        return multiply(multiply(Rx, Ry), Rz);
    }
    public static double[][] axisRotation(Vector axis, double angle){
        double cos=Math.cos(angle), sin=Math.sin(angle);
        double[] u=axis.normalize().getComp();
        double[][] R=new double[3][3];
        int k=1;
        for(int i=0; i<3; i++){
            for(int j=0; j<3; j++){
                R[i][j]=u[i]*u[j]*(1-cos)+(i==j? cos: k*sin*u[3-i-j]);
                k*=(i==j || j==2)? 1:-1;
            }
        }
        return R;
    }
    public static void GaussJordan(double[][] M){
        int p;
        double t;
        for(int i=0; i<M.length; i++){
            t=M[i][i];
            for(int j=i; j<M[i].length; j++){
                M[i][j]/=t;
            }
            for(int k=1; k<M.length; k++){
                p=(k+i)%M.length;
                t=M[p][i];
                for(int h=0; h<M[p].length; h++){
                    M[p][h]-=t*M[i][h];
                }
            }
        }
    }
}