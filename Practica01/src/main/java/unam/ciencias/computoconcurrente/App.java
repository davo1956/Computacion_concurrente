package unam.ciencias.computoconcurrente;

public class App {

    public static void main(String[] a) throws InterruptedException {
        
        long iniSec = System.currentTimeMillis();
        PrimeNumberCalculator secuencial = new PrimeNumberCalculator(1);
        secuencial.isPrime(104543);
        long finSec = System.currentTimeMillis();

        long tiempo01 = finSec - iniSec;


        long iniPar = System.currentTimeMillis();
        PrimeNumberCalculator paralelo = new PrimeNumberCalculator(8);
        paralelo.isPrime(104543);
        long finPar = System.currentTimeMillis();

        long tiempo02 = finPar -iniPar;

      
        System.out.println("Comparacion ejercicio 01");
        System.out.println("El primo a comparar es el numero: 104543");
        System.out.println("El tiempo en el algoritmo secuencial tarda: " + tiempo01);
        System.out.println("El tiempo en el algoritmo paralelo tarda: " + tiempo02);


        int[][] matrix = {
            {4, 29, -6, 0},
            {15, 6, 0, 4},
            {25, 41, -10, 4},
            {0, 0, -1, 39},
            };



        long iniSec2 = System.currentTimeMillis();
        MatrixUtils secuencial2 = new MatrixUtils();
        secuencial2.findMinimum(matrix);
        long finSec2 = System.currentTimeMillis();

        long tiempo011 = finSec2 - iniSec2;


        
        long iniPar2 = System.currentTimeMillis();
        MatrixUtils paralelo2 = new MatrixUtils(4);
        paralelo2.findMinimum(matrix);
        long finPar2 = System.currentTimeMillis();

        long tiempo022 = finPar2 -iniPar2;

        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println("Comparacion ejercicio 02");
        System.out.println("Se uso una matriz de:  ");
        System.out.println("El tiempo en el algoritmo secuencial tarda: " + tiempo011);
        System.out.println("El tiempo en el algoritmo paralelo tarda: " + tiempo022);














    }
}
