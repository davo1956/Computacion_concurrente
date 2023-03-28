class Sorter {

   static void printArray(int[] a) {
      for (int i = 0; i < a.length; i++) System.out.print(" " + a[i]);
      System.out.print("\n");
   }

   static void sortArray(int[] a) {
      for (int i = 0; i < a.length - 1; i++)
         for (int j = i + 1; j < a.length; j++)
            if (a[i] > a[j]) {
               int temp = a[i]; a[i] = a[j]; a[j] = temp;
            }
   }

   public static void main(String[] args) {
      int n;
      n = Integer.parseInt(args[0]);
      int[] nums = new int[n];
      for (int i = 0; i < n; i++) nums[i] = Integer.parseInt(args[i+1]);
      System.out.println("Original numbers:");
      printArray(nums);
      sortArray(nums);
      System.out.println("Sorted numbers:");
      printArray(nums);
   }
}

/* ............... Example compile and run(s)

D:\>javac sort.java

D:\>java Sorter 5 -10 2 0 100 -77
Original numbers:
 -10 2 0 100 -77
Sorted numbers:
 -77 -10 0 2 100

D:\>java Sorter 5 5 4 typo 2 1
java.lang.NumberFormatException: typo
        at java.lang.Integer.parseInt(Integer.java:229)
        at java.lang.Integer.parseInt(Integer.java:276)
        at Sorter.main(sort.java:20)
                                            ... end of example run(s)  */
