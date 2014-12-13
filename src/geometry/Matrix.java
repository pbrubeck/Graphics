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
    public static double[][] givensRotation(int n, int i, int j, double theta){
        double[][] G=new double[n][n];
        for(int k=0; k<n; k++){
            if(k!=i && k!=j){
                G[k][k]=1;
            }
        }
        double cos=Math.cos(theta);
        double sin=Math.signum(i-j)*Math.sin(theta);
        G[i][i]=cos;
        G[j][j]=cos;
        G[i][j]=sin;
        G[j][i]=-sin;
        return G;
    }
    public static double[][] eulerRotation(double alpha, double beta, double gamma){
        double[][] Rx=givensRotation(4, 2, 1, -alpha);
        double[][] Ry=givensRotation(4, 2, 0, beta);
        double[][] Rz=givensRotation(4, 1, 0, -gamma);
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
        int p, r;
        double t;
        for(int i=0; i<M.length; i++){
            r=i;
            do{
                t=M[r++][i];
            }while(t==0 && r<M.length);
            r--;
            if(i!=r){
                double[] temp=M[i];
                M[i]=M[r];
                M[r]=temp;
            }
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
    
    public static double[][] merge(double[][][][] T){
        int rows=0, cols=0;
        for(double[][][] R:T){
            rows+=R[0].length;
        }
        for(double[][] C:T[0]){
            cols+=C[0].length;
        }
        double[][] H=new double[rows][cols];
        rows=0;
        for(double[][][] R:T) {
            cols=0;
            for(double[][] M:R) {
                for(int k=0; k<M.length; k++) {
                    System.arraycopy(M[k], 0, H[rows+k], cols, M[k].length);
                }
                cols+=M[0].length;
            }
            rows+=R[0].length;
        }
        return H;
    }
    public static double[][] mergeH(double[][][] T){
        int cols=0;
        for (double[][] M:T) {
            cols+=M[0].length;
        }
        double[][] H=new double[T[0].length][cols];
        cols=0;
        for(double[][] M:T) {
            for(int k=0; k<M.length; k++){
                System.arraycopy(M[k], 0, H[k], cols, M[k].length);
            }
            cols+=M[0].length;
        }
        return H;
    }
    public static double[][] mergeV(double[][][] T){
        int rows=0;
        for(double[][] M:T){
            rows+=M.length;
        }
        double[][] V=new double[rows][T[0][0].length];
        rows=0;
        for(double[][] M:T){
            System.arraycopy(M, 0, V, rows, M.length);
            rows+=M.length;
        }
        return V;
    }
    
    public static void main(String[] args){
        double[][] A={{0,-18,-3},{0,3,4},{1,5,5}};
        double[][] I={{1,0,0},{0,1,0},{0,0,1}};
        double[][] M=mergeH(new double[][][]{A, I});
        printMatrix(M);
        System.out.println();
        GaussJordan(M);
        
        printMatrix(M);
        System.out.println();
        printMatrix(multiply(A, M));
    }
}