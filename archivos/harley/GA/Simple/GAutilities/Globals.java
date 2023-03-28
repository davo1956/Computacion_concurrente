package GAutilities;

import java.io.*;

public class Globals {
   private static PrintWriter temp = null;
   static {
      if (Defaults.logFileName == null)
         temp = new PrintWriter(System.out, Defaults.autoFlush);
      else {
         try {
            temp = new PrintWriter(new BufferedWriter(new FileWriter(
               new File(Defaults.logFileName))), Defaults.autoFlush);
         } catch (IOException e) {
            System.err.println("Globals: IOException involving file "
               + Defaults.logFileName);
            System.exit(1);
         }
      }
   }
   public static final PrintWriter stdout = temp;
}
