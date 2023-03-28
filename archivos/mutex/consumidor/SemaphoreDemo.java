import java.util.concurrent.Semaphore;
class Counter {
  static int count = 0;
}
class SemaphoreDemoLock extends Thread {
  
  Semaphore s;
  String name;
  
  SemaphoreDemoLock(Semaphore s, String name){
    this.s = s;
    this.name = name;
  }
  
  public void run() {
    if(this.getName().equals("Thread 1")) {
      System.out.println(name + " started execution");
      
      try {
        System.out.println(name + " waiting to acquire permit");
        s.acquire();
        System.out.println(name + " acquired permit");
        
        for(int i=0;i<3;i++) {
          Counter.count++;
          System.out.println(name + ":" + Counter.count);
          Thread.sleep(1000);
        }
      }catch(InterruptedException e) {
        e.printStackTrace();
      }
      System.out.println(name + " releasing permit");
      s.release();
    }
    else {
      System.out.println(name + " started execution");
      
      try {
        System.out.println(name + " waiting for permit");
        s.acquire();
        System.out.println(name + " acquired permit");
        
        for(int i=0;i<3;i++) {
          Counter.count--;
          System.out.println(name + ":" + Counter.count);
          Thread.sleep(1000);
        }
      }
      catch(InterruptedException e) {
        e.printStackTrace();
      }
      
      System.out.println(name + " releasing permit");
      s.release();
    }
  }
}public class SemaphoreDemo {
    public static void main(String[] args) throws InterruptedException {
      Semaphore s = new Semaphore(1);
      
      SemaphoreDemoLock sd1 = new SemaphoreDemoLock(s, "Thread 1");
      SemaphoreDemoLock sd2 = new SemaphoreDemoLock(s, "Thread 2");
      
      sd1.start();
      sd2.start();
      
      
      sd1.join();
      sd2.join();
      
      System.out.println("Counter value: " + Counter.count);
    }
  }