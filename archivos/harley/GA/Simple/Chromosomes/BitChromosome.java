package Chromosomes;

import GAutilities.*;

abstract class BitChromosome extends Chromosome { // genes are 0/1 (boolean)

   protected boolean[] bits = null;

   protected BitChromosome() { // construction only by subclasses
      super();
      bits = new boolean[chromosomeLength];
   }

   public void copyChromosome(Chromosome c) { // copy this to c
      BitChromosome bc = (BitChromosome) c;
      bc.fitness = this.fitness;    // these three ='s
      bc.rfitness = this.rfitness;  // really belong
      bc.cfitness = this.cfitness;  // in the superclass
      for (int i = 0; i < chromosomeLength; i++) {
         bc.bits[i] = this.bits[i];
      }
   }

   public Chromosome cloneChromosome() {
      MyChromosome theClone = new MyChromosome();
      copyChromosome((Chromosome) theClone);
      return (Chromosome) theClone;
   }

   public void initializeChromosomeRandom() {
      for (int i = 0; i <chromosomeLength; i++) {
         bits[i] = MyRandom.boolRandom();
      }
      fitness = -1;                // set this again since initializeCR()
      rfitness = 0; cfitness = 0;  // may be called more than once
   }

   public void clearChromosome() {
      for (int i = 0; i <chromosomeLength; i++) bits[i] = false;
      fitness = -1;
      rfitness = 0; cfitness = 0;
   }

   public Number getGene(int i) {
      return (Number) (bits[i]?new Integer(1):new Integer(0));
   }

   public void setGene(int i, Number n) {
      bits[i] = ((Integer) n).intValue() != 0;
      fitness = -1;
      rfitness = 0; cfitness = 0;
   }

   public void mutateGene(int i) {
      bits[i] = !bits[i];
      fitness = -1;
      rfitness = 0; cfitness = 0;
   }

   public String toGenotype() {
      String genotype = "";
      for (int i = 0; i < chromosomeLength; i++) {
         genotype += (bits[i] ? "1" : "0");
      }
      return genotype;
   }
}
