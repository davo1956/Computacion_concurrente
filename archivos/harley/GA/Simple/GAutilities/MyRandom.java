package GAutilities;

public class MyRandom {

   // Return a random double in [0,1).
   public static double dblRandom() {
      return Math.random();
   }

   // Return a random double in [0,high).
   public static double dblRandom(double high) {
      return Math.random()*high;
   }

   // Return a random double in [low,high).
   public static double dblRandom(double low, double high) {
      return Math.random()*(high - low) + low;
   }

   // Return a random integer from low to high (inclusive).
   public static int intRandom(int low, int high) {
      int nb = high - low + 1;
      return (int)(Math.random()*nb + low);
   }

   // Return a random integer from 1 to n inclusive.
   public static int intRandom(int n) {
      return (int)(Math.random()*n + 1);
   }

   // Return a random bit (0 or 1).
   public static int bitRandom() {
      return (int)(Math.random()*2);
   }

   // Return a random boolean (false or true).
   public static boolean boolRandom() {
      return Math.random() >= 0.5;
   }
}
