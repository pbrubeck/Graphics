package util;

import java.util.Arrays;
import java.lang.reflect.Array;

public class ArrayOp {
    public static <T> boolean compareArrays(T[] a, T[] b){
        if(a.length!=b.length){
            return false;
        }
        T[][] result=(T[][])Array.newInstance(a.getClass(), 2);
        result[0]=a;
        result[1]=b;
        T[] c=merge(result);
        int i=0, j, k=0;
        while(i<a.length){
            if(c[i]!=null){
                k=1;
                j=i+1;
                while(j<c.length){
                    if(((T)c[i]).equals((T)c[j])){
                        c[j]=null;
                        k+= j<a.length? 1: -1;
                    }
                    j++;
                }
            }
            i+= k==0? 1: a.length+1;
        }
        return i==a.length;
    }
    public static <T> T[] merge(T[]... arrays){
        int i=0;
        for(T[] a : arrays) {
            if(a!=null){
                i+=a.length;
            }
        }
        T[] result=(T[])Array.newInstance(arrays.getClass().getComponentType().getComponentType(), i);
        i=0;
        for(T[] a : arrays) {
            if(a!=null){
                System.arraycopy(a, 0, result, i, a.length);
                i+=a.length;
            }
        }
        return result;
    }
    public static <T> T[] append(T[] array, T element, int i){
        T[] result=Arrays.copyOf(array, array.length+1);
        result[i]=element;
        System.arraycopy(array, i, result, i+1, array.length-i);
        return result;
    }
    public static <T> T[] append(T[] array, T[] subset, int i){
        return merge(Arrays.copyOfRange(array, 0, i), subset, Arrays.copyOfRange(array, i, array.length));
    }
    public static <T> T[] removeRepeated(T[] array){
        int i,j,k=0;
        for(i=0; i<array.length; i++){
            if(array[i]!=null){
                k=i+1;
                for(j=k; j<array.length; j++){
                    if(array[j]!=null){
                        if(array[j].equals(array[i])){
                            array[j]=null;
                        }else{
                            if(j!=k){
                                array[k]=array[j];
                                array[j]=null;
                            }
                            k++;
                        }
                    }
                }   
            }
        }
        return Arrays.copyOfRange(array, 0, k);
    }
}
