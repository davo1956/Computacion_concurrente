package Chromosomes;

import GAutilities.*;

abstract class DoubleChromosome extends Chromosome { // genes are doubles

   protected        double[] gene           = null;
   protected static double[] geneLowerBound = null;
   protected static double[] geneUpperBound = null;

   protected DoubleChromosome() { // construction only by subclasses
      super();
      gene = new double[chromosomeLength];
   }

   public void copyChromosome(Chromosome c) { // copy this to c
      DoubleChromosome dc = (DoubleChromosome) c;
      dc.fitness = this.fitness;    // these three
      dc.rfitness = this.rfitness;  // really belong
      dc.cfitness = this.cfitness;  // in superclass
      for (int i = 0; i < chromosomeLength; i++) {
         dc.gene[i] = this.gene[i];
      }
   }

   public Chromosome cloneChromosome() {
      MyChromosome theClone = new MyChromosome();
      copyChromosome((Chromosome) theClone);
      return (Chromosome) theClone;
   }

   public void initializeChromosomeRandom() {
      for (int i = 0; i <chromosomeLength; i++) {
         gene[i] = MyRandom.dblRandom(geneLowerBound[i], geneUpperBound[i]);
      }
      fitness = -1;                // set this again since initializeCR()
      rfitness = 0; cfitness = 0;  // may be called more than once
   }

   public void clearChromosome() {
      for (int i = 0; i <chromosomeLength; i++) gene[i] = 0;
      fitness = -1;
      rfitness = 0; cfitness = 0;
   }

   public Number getGene(int i) {
      return (Number) (new Double(gene[i]));
   }

   public void setGene(int i, Number n) {
      gene[i] = ((Double) n).doubleValue();
      fitness = -1;
      rfitness = 0; cfitness = 0;
   }

   public void mutateGene(int i) {
      gene[i] = MyRandom.dblRandom(geneLowerBound[i], geneUpperBound[i]);
      fitness = -1;
      rfitness = 0; cfitness = 0;
   }

   public String toGenotype() {
      String genotype = "";
      for (int i = 0; i < chromosomeLength; i++) {
         genotype += " gene["+i+"]="+gene[i];
      }
      return genotype;
   }
}
