import java.awt.*;
import java.awt.event.*;
import Utilities.*;

class FibonacciFrame extends Frame implements ActionListener {

   private TextArea textArea = null;
   private ComputeFibonacci fibonacci = null;
   private Thread thread = null;
   private static final int START = 0, SUSPEND = 1, RESUME = 2,
      FASTER = 3, SLOWER = 4, STOP = 5, CLOSE = 6, QUIT = 7;
   private static final String[] buttonLabel = {"Start", "Suspend", "Resume",
      "Faster", "Slower", "Stop", "Close", "Quit"};

   public FibonacciFrame(int id) {
      super();
      setTitle("FibonacciFrame " + id);
      setLayout(new BorderLayout());
      Panel p = new Panel();
      p.setLayout(new FlowLayout());
      for (int i = 0; i < buttonLabel.length; i++) {
         Button b = new Button(buttonLabel[i]);
         b.addActionListener(this);
         p.add(b);
      }
      add("North", p);
      textArea = new TextArea(12, 40);
      // the TextArea's memory may eventually
      // fill up and freeze the program
      textArea.setEditable(false);
      // change font, if desired
      textArea.setFont(new Font("Helvetica", Font.PLAIN, 16));
      add("Center", textArea);
      fibonacci = new ComputeFibonacci(textArea);
   }

   public void actionPerformed(ActionEvent evt) {
      Object o = evt.getSource();
      if (o instanceof Button) {
         String label = ((Button) o).getLabel();
         int i;
         for (i=0; i < buttonLabel.length; i++)
            if (buttonLabel[i].equals(label)) break;
         switch (i) {
            case START:
               if (thread != null) thread.stop();
               thread = new Thread(fibonacci);
               // without lowering the thread's priority, a
               // newNap < 32 has sometimes frozen the buttons
               thread.setPriority(Thread.MIN_PRIORITY);
               thread.start();
               break;
            case SUSPEND:
               if (thread != null) thread.suspend();
               break;
            case RESUME:
               if (thread != null) thread.resume();
               break;
            case FASTER:
               fibonacci.faster();
               break;
            case SLOWER:
               fibonacci.slower();
               break;
            case STOP:
               if (thread != null) thread.stop();
               break;
            case CLOSE:
               if (thread != null) thread.stop();
               this.setVisible(false);
               this.dispose();
               break;
            case QUIT:
               System.err.println("Quit");
               System.exit(0);
               break;
            default:
               System.err.println("unknown Button label: " + label);
               break;
         }
      } else System.err.println("ActionEvent is not a Button");
   }
}

class ComputeFibonacci extends MyObject implements Runnable {

   private TextArea textArea = null;
   private int napping = 256;

   public ComputeFibonacci(TextArea textArea) {
      super("ComputeFibonacci");
      this.textArea = textArea;
   }

   public void faster() {
      int newNap = napping;
      newNap /= 2;
      newNap = newNap < 8 ? 8 : newNap;
      napping = newNap;
      textArea.append("napping=" + napping + "\n");
   }

   public void slower() {
      int newNap = napping;
      newNap *= 2;
      napping = newNap;
      textArea.append("napping=" + napping + "\n");
   }

   public void run() {
      while (true) {
         long x = (long)random(1,100);
         long y = (long)random(1,100);
         long z;
         do {
            z = x + y;
            textArea.append("x=" + x + ", y=" + y + ", z=" + z + "\n");
            nap(napping);
            x = y;  y = z;
         } while (z > 0);
      }
   }
}

class Fibonacci extends MyObject implements WindowListener {

   private static final int NUM_FRAMES = 2;
   private Frame[] frame = null;

   private Fibonacci() {
      super("Fibonacci");
      frame = new Frame[NUM_FRAMES];
      for (int i = 0; i < NUM_FRAMES; i++) {
         frame[i] = new FibonacciFrame(i);
         frame[i].addWindowListener(this);
         frame[i].pack();
         frame[i].setVisible(true);
      }
   }

   private void checkEvent(WindowEvent evt, String what) {
      Object o = evt.getSource();
      for (int i = 0; i < NUM_FRAMES; i++) {
         if (o == frame[i]) {
            System.err.println("frame " + i + " " + what);
            break;
         }
      }
   }

   public void windowClosed(WindowEvent evt) {
      checkEvent(evt, "closed");
   }

   public void windowDeiconified(WindowEvent evt) {
      checkEvent(evt, "deiconified");
   }

   public void windowIconified(WindowEvent evt) {
      checkEvent(evt, "iconified");
   }

   public void windowActivated(WindowEvent evt) {
      checkEvent(evt, "activated");
   }

   public void windowDeactivated(WindowEvent evt) {
      checkEvent(evt, "deactivated");
   }

   public void windowOpened(WindowEvent evt) {
      checkEvent(evt, "opened");
   }

   // user used window manager, not Java, to kill the window
   public void windowClosing(WindowEvent evt) {
      checkEvent(evt, "closing");
      for (int i = 0; i < NUM_FRAMES; i++) frame[i].dispose();
      System.exit(0);
   }

   public static void main(String[] args) {
      new Fibonacci();
   }
}
