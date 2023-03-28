package Selections;

import Chromosomes.*;

public interface Selection {
   public void select(Chromosome[] population,
      Chromosome[] newPopulation, int populationSize)
      throws SelectionException;
}
