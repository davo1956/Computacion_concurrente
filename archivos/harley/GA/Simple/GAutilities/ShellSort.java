package GAutilities;

public class ShellSort {
   public static void sort(int[] a) {
      int n = a.length;
      int incr = n/2;
      while (incr >= 1) {
         for (int i = incr; i < n; i++) {
            int temp = a[i];
            int j = i;
            while (j >= incr && temp < a[j-incr]) {
               a[j] = a[j-incr];
               j -= incr;
            }
            a[j] = temp;
         }
         incr /= 2;
      }
   }
}
