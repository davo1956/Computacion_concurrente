import java.awt.*;
import XtangoAnimation.*;

class AnimatedHelloWorld {

   public static void main(String[] args) {
      XtangoAnimator xa = new XtangoAnimator();
      xa.begin();
// Change coordinates so the window's lower left and upper right corners
// have coordinates (1.0,1.0) and (3.0,3.0), respectively.
      xa.coords(1.0f, 1.0f, 3.0f, 3.0f);
// Change the window background color from the default white to cyan.
      xa.bg(Color.cyan);
// Display the string "Hello, world!" in the middle of the window.  The
// text will be red and centered at coordinates (2.0,2.0).  The id of
// the text object is "hw".
      xa.bigText("hw", 2.0f, 2.0f, true, Color.red, "Hello, world!");
// Display the same animation frame multiple times.
      xa.delay(100);
// Delete the text string, identified by id.
      xa.delete("hw");
      xa.end();
      System.out.println("AnimatedHellowWorld done");
      System.exit(0);
   }
}

/* ............... Example compile and run(s)

D:\Animations>javac ahlw.java

D:\Animations>java AnimatedHelloWorld
XtangoAnimator: Push the Start button
XtangoAnimator: Push the Close or Quit button
                                            ... end of example run(s)  */
