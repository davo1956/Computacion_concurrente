import java.awt.*;
import java.util.*;

public class Animation {

   public static void animate(XtangoApplet xa, TextField commandTF) {
      System.out.println("Animation: Push the Command button after setting");
      synchronized (xa) {
         try { xa.wait(); } catch (InterruptedException e) {}
      }
      StringTokenizer tokens = new StringTokenizer(commandTF.getText());
      String which = tokens.nextToken();
      int numArgs = tokens.countTokens();
      String[] args = new String[numArgs];
      int j = 0;
      while (tokens.hasMoreTokens()) args[j++] = tokens.nextToken();
      System.out.println("Animation: command set, which = "
         + which + ", numArgs = " + numArgs);

      if ("bubbleSort".equals(which)) {
         (new AnimatedBubbleSort(xa)).main(args);
      } else if ("diningPhilosophers".equals(which)) {
         (new AnimatedDiningPhilosophers(xa)).main(args);
      } else if ("distributedPhilosophers".equals(which)) {
         (new AnimatedDistributedPhilosophers(xa)).main(args);
      } else if ("quickSort".equals(which)) {
         (new AnimatedQuickSort(xa)).main(args);
      } else if ("testing".equals(which)) {
         System.out.println("XtangoApplet: testing");
         xa.begin();
         xa.delay(10);
         xa.pointLine("Lthin", 0.3f, 0.2f, 0.8f, 0.7f, Color.black,
            XtangoApplet.THIN);
         xa.delay(10);
         xa.pointLine("Lmedium", 0.4f, 0.2f, 0.9f, 0.7f, Color.black,
            XtangoApplet.MEDTHICK);
         xa.delay(10);
         xa.pointLine("Lthick", 0.5f, 0.2f, (float)1, 0.7f, Color.black,
            XtangoApplet.THICK);
         xa.delay(10);
         xa.triangle("Tri1", 0.1f, 0.1f, 0.5f, 0.9f, 0.9f, 0.2f, Color.magenta,
            XtangoApplet.OUTLINE);
         xa.delay(10);
         xa.triangle("Tri2", 0.1f, 0.3f, 0.7f, 0.9f, 0.8f, 0.3f, Color.green,
            XtangoApplet.SOLID);
         xa.delay(10);
         xa.bg(Color.yellow);
         xa.delay(10);
         xa.text("t00", 0.0f, 0.0f, false, Color.gray, "text 0");
         xa.delay(10);
         xa.text("t01", 0.1f, 0.1f, false, Color.blue, "text 1");
         xa.delay(10);
         xa.circle("c0", 0.8f, 0.2f, 0.1f, Color.red, XtangoApplet.SOLID);
         xa.delay(10);
         xa.bg(Color.cyan);
         xa.delay(10);
         xa.text("t02", 0.2f, 0.2f, false, Color.gray, "text 2");
         xa.delay(10);
         xa.text("t03", 0.3f, 0.3f, false, Color.gray, "text 3");
         xa.delay(10);
         xa.text("t04", 0.4f, 0.4f, false, Color.gray, "text 4");
         xa.delay(10);
         xa.text("t05", 0.5f, 0.5f, false, Color.gray, "text 5");
         xa.delay(10);
         xa.text("t06", 0.6f, 0.6f, false, Color.gray, "text 6");
         xa.delay(10);
         xa.text("t06", 0.6f, 0.6f, false, Color.gray, "TEXT 6");
         xa.delay(10);
         xa.text("t07", 0.7f, 0.7f, false, Color.gray, "text 7");
         xa.delay(10);
         xa.text("t08", 0.8f, 0.8f, false, Color.gray, "text 8");
         xa.delay(10);
         xa.text("t09", 0.9f, 0.9f, false, Color.gray, "text 9");
         xa.delay(10);
         xa.text("t10", (float)1, (float)1, false, Color.gray, "text10");
         xa.delay(10);
         xa.circle("c1", 0.7f, 0.7f, 0.05f, Color.black, XtangoApplet.OUTLINE);
         xa.delay(10);
         xa.fill("c1", XtangoApplet.SOLID);
         xa.delay(10);
         xa.fill("c1", XtangoApplet.HALF);
         xa.delay(10);
         xa.color("t07", Color.white);
         xa.delay(10);
         xa.raise("t07");
         xa.delay(10);
         xa.swapIds("t04", "t05");
         xa.jump("t04", 0.9f, 0.1f);
         xa.delay(10);
         xa.delete("t04"); xa.delete("t05"); xa.delete("t06");
         xa.delete("t04");
         xa.delay(10);
         xa.jumpTo("c0", "t03");
         xa.delay(10);
         xa.vis("t03");    // should lower
         xa.delay(10);
         xa.vis("t03");    // should raise
         xa.delay(10);
         xa.rectangle("R", 0.5f, 0.5f, 0.1f, 0.2f, Color.black,
            XtangoApplet.SOLID);
         xa.delay(10);
         xa.rectangle("Rhalf", 0.6f, 0.6f, 0.2f, 0.1f, Color.black,
            XtangoApplet.HALF);
         xa.delay(10);
         xa.line("L", 0.1f, 0.1f, 0.8f, 0.8f, Color.black, XtangoApplet.THIN);
         xa.delay(10);
         xa.circle("moveTest1", 0.1f, 0.9f, 0.05f, Color.magenta,
            XtangoApplet.SOLID);
         xa.move("moveTest1", 0.9f, 0.1f);
         xa.circle("moveTest2", 0.9f, 0.9f, 0.05f, Color.blue,
            XtangoApplet.OUTLINE);
         xa.move("moveTest2", 0.1f, 0.1f);
         for (int i = 0; i < 5; i++) {
            xa.rectangle("R"+i, (float)Math.random(), (float)Math.random(),
               (float)Math.random()/4,
               (float)Math.random()/4, Color.magenta, XtangoApplet.SOLID);
            xa.delay(2);
            xa.color("R"+i, Color.orange);
            xa.delay(2);
            xa.delete("R"+i);
         }
         for (int i = 0; i < 5; i++) {
            xa.delay(2);
            float x = (float)Math.random();
            float y = (float)Math.random();
            System.out.println("jump x=" + x + ", y=" + y);
            xa.jump("c1", x, y);
         }
         xa.exchangePosAsync("moveTest1", "moveTest2");
         xa.delay(1);
         xa.bigText("ha!", 0.5f, 0.1f, true, Color.black, "ha!");
         xa.exchangePos("moveTest1", "moveTest2");
         xa.delay(1);
         xa.bigText("Ha!", 0.6f, 0.1f, true, Color.black, "Ha!");
         xa.exchangePosAsync("moveTest1", "moveTest2");
         xa.delay(5);
         xa.bigText("HA!", 0.7f, 0.1f, true, Color.black, "HA!");
         xa.delay(10);
         xa.jumpRelative("t00", 0.05f, 0.05f);
         xa.delay(10);
         xa.jumpRelative("t01", -0.05f, 0.07f);
         xa.delay(10);
         xa.jumpRelative("t02", 0.02f, -0.15f);
         xa.delay(10);
         xa.jumpRelative("t03", -0.3f, -0.3f);
         xa.delay(10);
         xa.moveRelative("moveTest1", 0.3f, 0.3f);
         xa.moveTo("moveTest2", "t00");
         xa.delay(10);
         xa.smallText("Tsmall", 0.2f, 0.9f, true, Color.magenta, "Going...");
         xa.line("L1", 0.2f, 0.85f, 0.0f, 0.1f, Color.black, XtangoApplet.THIN);
         xa.line("L2", 0.15f, 0.9f, 0.1f, 0.0f, Color.black, XtangoApplet.THIN);
         xa.delay(10);
         xa.text("Tnormal", 0.15f, 0.8f, true, Color.blue, "Yup, Going...");
         xa.line("L3", 0.15f, 0.75f, 0.0f, 0.1f, Color.black, XtangoApplet.THIN);
         xa.line("L4", 0.1f, 0.8f, 0.1f, 0.0f, Color.black, XtangoApplet.THIN);
         xa.delay(10);
         xa.bigText("Tbig", 0.1f, 0.7f, true, Color.darkGray, "Surely, GONE!");
         xa.line("L5", 0.1f, 0.65f, 0.0f, 0.1f, Color.black, XtangoApplet.THIN);
         xa.line("L6", 0.05f, 0.7f, 0.1f, 0.0f, Color.black, XtangoApplet.THIN);
         xa.delay(10);
         xa.coords(0.0f, 0.0f, 0.5f, 0.5f);
         xa.delay(10);
         xa.coords(0.0f, 0.5f, 0.5f, (float)1);
         xa.delay(10);
         xa.coords(0.0f, 0.0f, (float)1, (float)1);
         xa.delay(10);
         xa.coords(0.0f, 0.0f, (float)1, 0.5f);
         xa.delay(10);
         xa.coords(0.0f, 0.0f, 0.5f, (float)1);
         xa.delay(10);
         xa.switchPos("Tbig", "Tsmall");
         xa.delay(10);
         xa.moveToAsync("Tnormal", "Tsmall");
         xa.delay(1);
         xa.moveRelativeAsync("Tbig", 0.3f, 0.0f);
         xa.moveAsync("Tsmall", 0.2f, 0.9f);
         System.out.println("DONE!");
         xa.end();
      } else
         System.out.println("Animation: unknown which=" + which);
   }
}
