import java.io.*;

class GiftGivers {

   static final int MAX = 100;

   static Object tokenize(StreamTokenizer st)
         throws NumberFormatException, IOException {
      Object token = null;
      try {
         if (st.nextToken() == st.TT_EOF) {
            throw new EOFException();
         }
      } catch (IOException e) {
         throw e;
      }
      switch(st.ttype) {
         case st.TT_NUMBER:
            token = new Double(st.nval);
            break;
         case st.TT_WORD:
            token = st.sval;
            break;
         case st.TT_EOL:
            throw new NumberFormatException();
         default:
            throw new NumberFormatException();
      }
      return token;
   }

   static int tokenizeInt(StreamTokenizer st)
         throws NumberFormatException, IOException {
      try {
         return (int) ((Double) tokenize(st)).doubleValue();
      } catch (ClassCastException e) {
         throw new NumberFormatException();
      }
   }

   static double tokenizeDouble(StreamTokenizer st)
         throws NumberFormatException, IOException {
      try {
         return ((Double) tokenize(st)).doubleValue();
      } catch (ClassCastException e) {
         throw new NumberFormatException();
      }
   }

   static String tokenizeString(StreamTokenizer st)
         throws ClassCastException, NumberFormatException, IOException {
      try {
         return (String) tokenize(st);
      } catch (ClassCastException e) {
         throw new NumberFormatException();
      }
   }

   public static void main(String[] args) {
      int n = 0, numFriends = 0;  double money = 0;
      String name = null;
      String[] names = new String[MAX];
      String[][] friends = new String[MAX][MAX];
      StreamTokenizer st = null;
      if (args.length == 1) {
         File f = new File(args[0]);
         try {
            st = new StreamTokenizer(new BufferedReader(new FileReader(f))); 
         } catch (FileNotFoundException e) {
            System.err.println("GiftGivers: file " + args[0] + " not found");
            System.exit(1);
         }
      } else
         st = new StreamTokenizer(
            new BufferedReader(new InputStreamReader(System.in)));
      try {
         while (true) {
            n = tokenizeInt(st);
            System.out.println("n=" + n);
            System.out.print("names=");
            for (int i = 0; i < n; i++) {
               names[i] = tokenizeString(st);
               System.out.print(" " + names[i]);
            }
            System.out.println();
            for (int i = 0; i < n; i++) {
               name = tokenizeString(st);
               money = tokenizeDouble(st);
               numFriends = tokenizeInt(st);
               System.out.println("name=" + name + " money=" + money
                  + " numFriends=" + numFriends);
               System.out.print("friends=");
               for (int j = 0; j < numFriends; j++) {
                  friends[i][j] = tokenizeString(st);
                  System.out.print(" " + friends[i][j]);
               }
               System.out.println();
            }
         }
      } catch (EOFException e) {
         System.out.println("GiftGivers: no more input data");
         System.exit(0);
      } catch (IOException e) {
         System.err.println("GiftGivers: IOException");
         System.exit(1);
      } catch (NumberFormatException e) {
         System.err.println("GiftGivers: NumberFormatException");
         System.exit(1);
      }
   }
}

/* ............... Example compile and run(s)

% javac name.java

% java GiftGivers
abc
GiftGivers: NumberFormatException

% java GiftGivers abc
GiftGivers: file abc not found

% cat input.txt
5
dave laura owen vick amy
dave 200.00 3
laura owen vick
owen 500.00 1
dave
amy 150.00 2
vick owen
laura 0.00 2
amy vick
vick 0.00 0
3
liz steve dave
liz 30.00 1
steve
steve 55.00 2
liz dave
dave 0.00 2
steve liz

% java GiftGivers input.txt
n=5
names= dave laura owen vick amy
name=dave money=200.0 numFriends=3
friends= laura owen vick
name=owen money=500.0 numFriends=1
friends= dave
name=amy money=150.0 numFriends=2
friends= vick owen
name=laura money=0.0 numFriends=2
friends= amy vick
name=vick money=0.0 numFriends=0
friends=
n=3
names= liz steve dave
name=liz money=30.0 numFriends=1
friends= steve
name=steve money=55.0 numFriends=2
friends= liz dave
name=dave money=0.0 numFriends=2
friends= steve liz
GiftGivers: no more input data
                                            ... end of example run(s)  */
