package Chromosomes;

public abstract class Chromosome { // designed for genes that are class Number

   protected static int     chromosomeLength = -1;        // will be set by
   protected static boolean knownSolutionFitness = false; // MyChromosome
   protected static double  solutionFitness  = -1;        // if known
   protected        double  fitness          = -1;
   protected        double  rfitness         = -1; // relative fitness
   protected        double  cfitness         = -1; // cumulative fitness

   protected Chromosome() {} // construction only by subclasses

   protected abstract double evalChromosome(); // supplied by MyChromosome

   public static final int getChromosomeLength() {
      if (chromosomeLength <= 0) {
         System.err.println("Chromosome: length has not yet been set.");
         System.exit(1);
      }
      return chromosomeLength;
   }

   public final double getFitness() {
      if (fitness < 0) fitness = evalChromosome();
      if (fitness < 0) {
         System.err.println("getFitness: negative fitness so quit");
         System.exit(1);
      }
      return fitness;
   }

   public final double rfitnessGet() { return rfitness; }

   public final double cfitnessGet() { return cfitness; }

   public final void rfitnessSet(double value) { rfitness = value; return; }

   public final void cfitnessSet(double value) { cfitness = value; return; }

   public static final boolean isSolutionFitnessKnown() {
      return knownSolutionFitness;
   }

   public static final double getSolutionFitness() {
      return solutionFitness;
   }

   public              String toString() {return this.toGenotype();}

   // copy this to c (CAREFUL!)
   public abstract void       copyChromosome(Chromosome c);
   public abstract Chromosome cloneChromosome();
   public abstract void       initializeChromosomeRandom();
   public abstract void       clearChromosome();
   public abstract Number     getGene(int i);
   public abstract void       setGene(int i, Number n);
   public abstract void       mutateGene(int i);

   public abstract String     toPhenotype(); // domain value(s) encoded
                                             // in the chromosome genes
   public abstract String     toGenotype();  // gene values that encode
                                             // the phenotype (domain values)
}
