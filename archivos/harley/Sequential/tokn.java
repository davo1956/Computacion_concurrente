import java.io.*;

class Tokenize {

   public static void main(String[] args) {

      System.out.println("Enter some numbers and words.");
      StreamTokenizer st = new StreamTokenizer(
         new BufferedReader(new InputStreamReader(System.in)));
      try {    // case TT_EOL occurs only if execute st.eolIsSignificant(true);
         while (st.nextToken() != st.TT_EOF) {
            switch(st.ttype) {
               case st.TT_NUMBER:
                  System.out.println("double number=" + st.nval);
                  break;
               case st.TT_WORD:
                  System.out.println("word=" + st.sval);
                  break;
               case st.TT_EOL:
                  System.out.println("end of line");
                  break;
               default:
                  System.out.println("unrecognized character");
                  break;
            }
         }
         System.out.println("end of file");
      } catch (IOException e) {
         System.err.println("Tokenize: " + e);
      }
   }
}

/* ............... Example compile and run(s)

% javac tokn.java

% java Tokenize
Enter some numbers and words.
1 2 3
double number=1.0
double number=2.0
double number=3.0
4.0 5.5 6.667
double number=4.0
double number=5.5
double number=6.667
1 abc def,ghi (jkl) !@#
double number=1.0
word=abc
word=def
unrecognized character
word=ghi
unrecognized character
word=jkl
unrecognized character
unrecognized character
unrecognized character
unrecognized character
^D
end of file
                                            ... end of example run(s)  */
