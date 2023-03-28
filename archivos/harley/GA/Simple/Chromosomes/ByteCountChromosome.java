package Chromosomes;

import GAutilities.*;

/*
 * This chromosome class implements encoding the problem of maximizing
 * the number of bytes that are all one bits in an eight byte word.  This
 * is a hard problem for genetic algorithms to solve because of "hitchhiking":
 * see the 64-bit hitchhiking example on pages 127-134 of Mitchell's "An
 * Introduction to Genetic Algorithms" book.
 */
public
class ByteCountChromosome extends BitChromosome {// this user-defined class
                                                // implements evalChromosome,
   protected ByteCountChromosome() {           // toPhenotype;
      super();                                // defines chromosomeLength,
   }                                         // knownSolutionFitness, and
                                            // solutionFitness
   static {
      chromosomeLength = 64;
      Globals.stdout.println("ByteCountChromosome: chromosome length is "
         + chromosomeLength);
      knownSolutionFitness = true;
      solutionFitness = chromosomeLength;
   }

   private int bitCount(int byteNum) {
      int count = 0;
      int start = byteNum*8;
      for (int i = 0; i < 8; i++) if (bits[start+i]) count++;
      return count;
   }

   private int byteCount() {
      int count = 0;
      for (int i = 0; i < 8; i++) if (bitCount(i) == 8) count++;
      return count;
   }

   protected double evalChromosome() {
      return (double) 8*byteCount();
   }

   public String toPhenotype() {
      return "this chromosome has " + byteCount() + " bytes set";
   }
}
