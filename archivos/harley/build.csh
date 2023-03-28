setenv CLASSPATH .:/usr/local/JDK/lib/classes.zip:/home/shartley/lib
cd lib/Synchronization
javac *.java
cd ../Utilities
javac GetOpt.java
cd ../XtangoAnimation
javac XtangoAnimator.java
cd ../..
ls -al
java Utilities.MyObject
java Utilities.GetOpt
java XtangoAnimation.XtangoAnimator
cd ..
tar cvf - ConcProgJava | gzip -9 >/var/tmp/bookJavaExamples.tar.gz
zip -r - ConcProgJava >/var/tmp/bookJavaExamples.zip
cd ConcProgJava
mv /var/tmp/bookJavaExamples.tar.gz .
mv /var/tmp/bookJavaExamples.zip .
rcp bookJavaExamples.tar.gz king:"~ftp/pub/shartley/bookJavaExamples.tar.gz"
rcp bookJavaExamples.zip king:"~ftp/pub/shartley/bookJavaExamples.zip"
cd Applets
tar cvf - . | gzip -9 >/var/tmp/source.tar.gz
zip -r - . >/var/tmp/source.zip
mv /var/tmp/source.tar.gz .
mv /var/tmp/source.zip .
echo "Do not forget to put the PostScript of the program listings (appendix)"
echo "in the ConcProgsJava directory as file name bookJavaExamples.ps.gz"
echo "and also in the pub/shartley ftp directory on king."
