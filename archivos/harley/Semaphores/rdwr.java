import Utilities.*;
import Synchronization.*;

class Database extends MyObject {

   private int numReaders = 0;
   private BinarySemaphore mutex = new BinarySemaphore(1);
   private BinarySemaphore ok = new BinarySemaphore(1);

   public Database() { super("Database"); }

   public void startRead(int i) {
      P(mutex);
      numReaders++;
      if (numReaders == 1) {
         System.out.println("age=" + age() + " reader " + i
            + " waiting to read, numReaders=" + numReaders);
         P(ok);
      }
      System.out.println("   age=" + age() + " reader " + i
         + " has begun reading, numReaders=" + numReaders);
      V(mutex);
   }

   public void endRead(int i) {
      P(mutex);
      numReaders--;
      System.out.println("   age=" + age() + " reader " + i
         + " finished reading, numReaders=" + numReaders);
      if (numReaders == 0) V(ok);
      V(mutex);
   }

   public void startWrite(int i) {
      P(ok);
      System.out.println("      age=" + age() + " WRITER " + i
         + " has begun Writing");
   }

   public void endWrite(int i) {
      System.out.println("   age=" + age() + " WRITER " + i
         + " has finished Writing");
      V(ok);
   }
}

/* ............... Example compile and run(s)

D:\>javac rdwr.java rwdr.java

D:\>java ReadersWriters -E4 -W2 -e2 -w2 -R5
ReadersWriters: numReaders=4, numWriters=2, rNap=2, wNap=2, runTime=5
All threads started
age=220, Reader2 napping for 1086 ms
age=160, Reader1 napping for 1252 ms
age=220, Reader3 napping for 1617 ms
age=270, WRITER0 napping for 1402 ms
age=270, WRITER1 napping for 77 ms
age=270, Reader0 napping for 1116 ms
age=380, WRITER1 wants to write
      age=380 WRITER 1 has begun Writing
age=380, WRITER1 writing for 715 ms
   age=1090 WRITER 1 has finished Writing
age=1090, WRITER1 finished writing
age=1090, WRITER1 napping for 1549 ms
age=1310, Reader2 wants to read
age=1310 reader 2 waiting to read, numReaders=1
   age=1310 reader 2 has begun reading, numReaders=1
age=1310, Reader2 reading for 145 ms
age=1420, Reader0 wants to read
   age=1420 reader 0 has begun reading, numReaders=2
age=1420, Reader0 reading for 752 ms
   age=1480 reader 2 finished reading, numReaders=1
age=1480, Reader2 finished reading
age=1480, Reader2 napping for 693 ms
age=1480, Reader1 wants to read
   age=1480 reader 1 has begun reading, numReaders=2
age=1480, Reader1 reading for 963 ms
age=1700, WRITER0 wants to write
age=1860, Reader3 wants to read
   age=1860 reader 3 has begun reading, numReaders=3
age=1860, Reader3 reading for 1631 ms
age=2190, Reader2 wants to read
   age=2190 reader 2 has begun reading, numReaders=4
age=2190, Reader2 reading for 1247 ms
   age=2190 reader 0 finished reading, numReaders=3
age=2190, Reader0 finished reading
age=2190, Reader0 napping for 1213 ms
   age=2470 reader 1 finished reading, numReaders=2
age=2470, Reader1 finished reading
age=2470, Reader1 napping for 1097 ms
age=2630, WRITER1 wants to write
age=3400, Reader0 wants to read
   age=3400 reader 0 has begun reading, numReaders=3
age=3400, Reader0 reading for 1920 ms
   age=3400 reader 2 finished reading, numReaders=2
age=3400, Reader2 finished reading
age=3400, Reader2 napping for 1311 ms
   age=3510 reader 3 finished reading, numReaders=1
age=3510, Reader3 finished reading
age=3510, Reader3 napping for 1302 ms
age=3570, Reader1 wants to read
   age=3570 reader 1 has begun reading, numReaders=2
age=3570, Reader1 reading for 348 ms
   age=3950 reader 1 finished reading, numReaders=1
age=3950, Reader1 finished reading
age=3950, Reader1 napping for 921 ms
age=4720, Reader2 wants to read
   age=4720 reader 2 has begun reading, numReaders=2
age=4720, Reader2 reading for 1589 ms
age=4830, Reader3 wants to read
   age=4830 reader 3 has begun reading, numReaders=3
age=4830, Reader3 reading for 1018 ms
age=4880, Reader1 wants to read
   age=4880 reader 1 has begun reading, numReaders=4
age=4880, Reader1 reading for 1401 ms
age()=5050, time to stop the threads and exit
                                            ... end of example run(s)  */
