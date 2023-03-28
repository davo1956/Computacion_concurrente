package Chromosomes;

import GAutilities.*;

/*
 * This chromosome class implements encoding the problem of maximizing
 * the number of bits that are set.
 */
public
class BitCountChromosome extends BitChromosome {// this user-defined class
                                               // implements evalChromosome,
   protected BitCountChromosome() {           // toPhenotype;
      super();                               // defines chromosomeLength,
   }                                        // knownSolutionFitness, and
                                           // solutionFitness
   static {
      chromosomeLength = 32;
      Globals.stdout.println("BitCountChromosome: chromosome length is "
         + chromosomeLength);
      knownSolutionFitness = true;
      solutionFitness = chromosomeLength;
   }

   private int bitCount() {
      int count = 0;
      for (int i = 0; i < chromosomeLength; i++) if (bits[i]) count++;
      return count;
   }

   protected double evalChromosome() {
      return (double) bitCount();
   }

   public String toPhenotype() {
      return "this chromosome has " + bitCount() + " bits set";
   }
}
