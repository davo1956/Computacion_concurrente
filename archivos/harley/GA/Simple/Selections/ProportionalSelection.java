package Selections;

import Chromosomes.*;
import GAutilities.*;

/**************************************************************/
/* Selection function: Standard proportional selection for    */
/* maximization problems, also called the roulette wheel      */
/* method.                                                    */
/**************************************************************/
public
class ProportionalSelection implements Selection {

   public void select(Chromosome[] population,
      Chromosome[] newPopulation, int populationSize)
      throws FitnessSumZeroException {

      double p, sum = 0;

      // find total fitness of the population
      for (int mem = 0; mem < populationSize; mem++) {
         sum += population[mem].getFitness();
      }

      if (sum == 0) throw new FitnessSumZeroException();

      // calculate relative fitness
      for (int mem = 0; mem < populationSize; mem++) {
         population[mem].rfitnessSet(population[mem].getFitness()/sum);
      }

      population[0].cfitnessSet(population[0].rfitnessGet());
      if (Debug.flag) {
         Globals.stdout.println("mem=0, cfitness="+population[0].cfitnessGet());
      }

      // calculate cumulative fitness
      for (int mem = 1; mem < populationSize; mem++) {
         population[mem].cfitnessSet(population[mem-1].cfitnessGet() +
            population[mem].rfitnessGet());
         if (Debug.flag) {
            Globals.stdout.println("mem=" + mem + ", cfitness=" +
               population[mem].cfitnessGet());
         }
      }

      // finally select survivors using cumulative fitness.
      for (int i = 0; i < populationSize; i++) {
         p = MyRandom.dblRandom();
         if (p < population[0].cfitnessGet()) {
            population[0].copyChromosome(newPopulation[i]);
            if (Debug.flag) {
               Globals.stdout.println("p="+p+", selected 0");
            }
         } else {
            for (int j = 0; j < populationSize; j++) {
               if (p >= population[j].cfitnessGet() &&
                   p < population[j+1].cfitnessGet()) {
                  // note that population[populationSize-1].cfitnessGet()
                  // is 1.0, so j+1 never gets as big as populationSize
                  population[j+1].copyChromosome(newPopulation[i]);
                  if (Debug.flag) {
                     Globals.stdout.println("p="+p+", selected "+(j+1));
                  }
               }
            }
         }
      }
   }
}
