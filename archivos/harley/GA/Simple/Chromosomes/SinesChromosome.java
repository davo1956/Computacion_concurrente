package Chromosomes;

import GAutilities.*;

/*
 * This chromosome class implements encoding the problem of maximizing
 * 21.5+x1*sin(4*PIx1)+x2*sin(20*PI*x2)+x3 for x1 in [-3.0,12.1],
 * x2 in [4.1,5.8], and x3 in [0.0, 1.0].
 */
public
class SinesChromosome extends DoubleChromosome {// this user-defined class
                                               // implements evalChromosome,
   protected SinesChromosome() {              // toPhenotype;
      super();                               // defines chromosomeLength,
   }                                        // knownSolutionFitness, and
                                           // solutionFitness
   static {
      chromosomeLength = 3;
      Globals.stdout.println("SinesChromosome: chromosome length is "
         + chromosomeLength);
      knownSolutionFitness = false;
      solutionFitness = -1;
      // compiler forbids this :-( geneLowerBound = {-3.0, 4.1, 0.0};
      double[] lower = {-3.0, 4.1, 0.0}; geneLowerBound = lower;
      double[] upper = {12.1, 5.8, 1.0}; geneUpperBound = upper;
   }

   protected double evalChromosome() {
      double fitness = 21.5 + gene[0] * Math.sin( 4*Math.PI*gene[0])
                            + gene[1] * Math.sin(20*Math.PI*gene[1])
                            + gene[2];
      if (fitness <= 0) {
         System.err.println("fitness of " + fitness + " at x1=" + gene[0] +
         " x2=" + gene[1] + " x3=" + gene[2] + " is <= 0");
         System.exit(1);
      }
      return fitness;
   }

   public String toPhenotype() {
      String phenotype = "21.5+" + gene[0] + "*sin(4*PI*" + gene[0] + ")+"
         + gene[1] + "*sin(20*PI*" + gene[1] + ")+" + gene[2];
      return phenotype;
   }
}
