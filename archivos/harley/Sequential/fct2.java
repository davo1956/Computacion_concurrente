import java.io.*;

class Factorial {

   public static int compute(int k) {
      if (k < 0) return -1;
      else if (k == 0 || k == 1) return 1;
      else return k*compute(k-1);
   }
}

class SomeFactorials {

   public static void main(String[] args) {
      int n;
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
      String line = null;
      while (true) {
         System.out.print("What factorial to compute? "); System.out.flush();
         try {
            line = in.readLine();
         } catch (IOException e) {
            System.err.println("readLine: " + e);
            System.exit(1);
         }
         if (line == null || line.equals("")) { // CR gives "", ^D gives null
            System.out.println("No more input.");
            break;
         }
         try {
            n = Integer.parseInt(line.trim()); // trim leading trailing blanks
         } catch (NumberFormatException e) {
            System.err.println(e + " (try again)");
            continue;
         }
         System.out.println(n + " factorial is " + Factorial.compute(n));
      }
      System.exit(0);
   }
}

/* ............... Example compile and run(s)

D:\>javac fct2.java

D:\>java SomeFactorials
What factorial to compute? 5
5 factorial is 120
What factorial to compute? abc
java.lang.NumberFormatException: abc (try again)
What factorial to compute? 3
3 factorial is 6
What factorial to compute?
No more input.
                                            ... end of example run(s)  */
