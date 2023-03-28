import Utilities.*;
import Synchronization.*;

class Passenger extends JurassicPark implements Runnable {
   private int id = 0;
   public Passenger(int id) { this.id = id; }

   public void run() {
      while (true) {
         nap(1+(int)random(1000*wanderTime));
         System.out.println("age="+age()+" passenger"+id+" wants to ride");
         P(carAvail);  V(carTaken);  P(carFilled);
         System.out.println("age="+age()+" passenger"+id+" taking a ride");
         P(passengerReleased);
         System.out.println("age="+age()+" passenger"+id+" finished riding");
      }
   }
}

class Car extends JurassicPark implements Runnable {
   private int id = 0;
   public Car(int id) { this.id = id; }

   public void run() {
      while (true) {
         System.out.println("age="+age()+" car"+id+" ready to load");
         V(carAvail);  P(carTaken);  V(carFilled);
         System.out.println("age="+age()+" car"+id+" going on safari");
         nap(1+(int)random(1000*rideTime));
         System.out.println("age="+age()+" car"+id+" has returned");
         V(passengerReleased);
      }
   }
}

class JurassicPark extends MyObject {

   static final int numPassengers = 10, numCars = 3;
   static final int wanderTime = 5, rideTime = 4, runTime = 60;
   static final CountingSemaphore carAvail = new CountingSemaphore(0);
   static final CountingSemaphore carTaken = new CountingSemaphore(0);
   static final CountingSemaphore carFilled = new CountingSemaphore(0);
   static final CountingSemaphore passengerReleased = new CountingSemaphore(0);

   public static void main(String[] args) {
      for (int i = 0; i < numPassengers; i++)
         new Thread(new Passenger(i)).start();
      for (int i = 0; i < numCars; i++)
         new Thread(new Car(i)).start();
      nap(1000*runTime);
      System.exit(0);
   }
}
