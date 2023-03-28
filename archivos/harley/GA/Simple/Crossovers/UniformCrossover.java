package Crossovers;

import GAutilities.*;
import Chromosomes.*;

/*
 * For each gene position in the two chromosomes being crossed over, flip a
 * coin and exchange the genes if the coin comes up heads.
 */
public class UniformCrossover implements Crossover {
   private int chromLength = -1;

   public UniformCrossover() {
      super();
      chromLength = Chromosome.getChromosomeLength();
   }

   public void xOver(Chromosome x, Chromosome y) {
      Number temp;
      for (int i = 0; i < chromLength; i++) {
         if (MyRandom.boolRandom()) {
            temp = x.getGene(i);
            x.setGene(i, y.getGene(i));
            y.setGene(i, temp);
         }
      }
   }
}
