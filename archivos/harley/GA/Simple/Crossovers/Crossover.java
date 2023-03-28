package Crossovers;

import Chromosomes.*;

/**************************************************************/
/* Crossover: performs crossover of the two selected parents. */
/**************************************************************/
public interface Crossover {
   public void xOver(Chromosome x, Chromosome y);
}
