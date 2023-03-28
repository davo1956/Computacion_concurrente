cd lib\Synchronization
javac *.java
cd ..\Utilities
javac GetOpt.java
cd ..\XtangoAnimation
javac XtangoAnimator.java
cd ..\..
dir
java Utilities.MyObject
java Utilities.GetOpt
java XtangoAnimation.XtangoAnimator
