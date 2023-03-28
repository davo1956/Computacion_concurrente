package Crossovers;

import GAutilities.*;
import Chromosomes.*;

/*
 * Pick a point at random between 1 and chromosomeLength-1 (inclusive),
 * then exchange all genes at positions 0 to point-1 (inclusive) between
 * the two chromosomes being crossed over.
 */
public class OnePointCrossover implements Crossover {

   public void xOver(Chromosome one, Chromosome two) {

      int point; // crossover point
      int chromLen = Chromosome.getChromosomeLength();
      Number temp;

      if (chromLen > 1) { // select crossover point
         if (chromLen == 2) point = 1;
         else point = MyRandom.intRandom(chromLen-1);
         for (int i = 0; i < point; i++) {
            temp = one.getGene(i);
            one.setGene(i, two.getGene(i));
            two.setGene(i, temp);
         }
         if (Debug.flag) {
            Globals.stdout.println("OnePointCrossover: just crossed at " + point);
         }
      }
   }
}
