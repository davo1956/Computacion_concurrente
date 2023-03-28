class Factorials {

   static int computeFactorial(int k) {
      if (k < 0) return -1;
      else if (k == 0 || k == 1) return 1;
      else return k*computeFactorial(k-1);
   }

   public static void main(String[] args) {
      int n;
      n = Integer.parseInt(args[0]);
      for (int i = 1; i <= n; i++)
         System.out.println(i + " factorial is " + computeFactorial(i));
   }
}

/* ............... Example compile and run(s)

D:\>javac fact.java

D:\>java Factorials 5
1 factorial is 1
2 factorial is 2
3 factorial is 6
4 factorial is 24
5 factorial is 120

D:\>java Factorials
java.lang.ArrayIndexOutOfBoundsException: 0
        at Factorials.main(fact.java:11)
                                            ... end of example run(s)  */
