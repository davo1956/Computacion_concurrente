package Chromosomes;

import GAutilities.*;

/*
 * This chromosome class implements encoding the problem of maximizing
 * the number of bits that are set.
 */
public
class BigBitCountChromosome extends BitChromosome {// this user-defined class
                                               // implements evalChromosome,
   protected BigBitCountChromosome() {           // toPhenotype;
      super();                               // defines chromosomeLength,
   }                                        // knownSolutionFitness, and
                                           // solutionFitness
   private static double denominator = 0;

   static {
      chromosomeLength = 100;
      Globals.stdout.println("BitCountChromosome: chromosome length is "
         + chromosomeLength);
      knownSolutionFitness = true;
      solutionFitness = 1.0;
      denominator = (double) chromosomeLength*chromosomeLength;
   }

   private int bitCount() {
      int count = 0;
      for (int i = 0; i < chromosomeLength; i++) if (bits[i]) count++;
      return count;
   }

   protected double evalChromosome() {
      int value = bitCount();
      return value*value/denominator;
   }

   public String toPhenotype() {
      return "this chromosome has " + bitCount() + " bits set";
   }
}
