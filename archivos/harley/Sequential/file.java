import java.io.*;

class FileIO {

   public static void main(String[] args) {

      System.out.println("Enter some numbers.");
      StreamTokenizer st = new StreamTokenizer(
         new BufferedReader(new InputStreamReader(System.in)));
      File f = new File("temp.out");
      int numberCount = 0;
      try {
         DataOutputStream dos = new DataOutputStream(
            new BufferedOutputStream(new FileOutputStream(f)));
         while (st.nextToken() != st.TT_EOF) {
            if (st.ttype == st.TT_NUMBER) {
               dos.writeDouble(st.nval);
               numberCount++;
            }
         }
         System.out.println("numberCount=" + numberCount);
         dos.flush();
         dos.close();
         DataInputStream dis = new DataInputStream(
            new BufferedInputStream(new FileInputStream(f)));
         for (int i = 0; i < numberCount; i++) {
            System.out.println("number=" + dis.readDouble());
         }
         dis.close();
      } catch (IOException e) {
         System.err.println("FileIO: " + e);
      } finally {
         f.delete();
      }
   }
}

/* ............... Example compile and run(s)

% javac file.java

% java FileIO
Enter some numbers.
1 2 3
4.4 5.5
6.67
^D
numberCount=6
number=1.0
number=2.0
number=3.0
number=4.4
number=5.5
number=6.67
                                            ... end of example run(s)  */
