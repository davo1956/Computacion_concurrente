/*
 * sGA.java       version 1.00      8 August 1996
 *                version 1.01     22 August 1996 -- split into packages
 *                version 1.02      9    May 1997 -- JDK 1.1 mods
 *                version 1.03     20   June 1997 -- removed last deprecation
 *
 *   A simple genetic algorithm where the fitness function takes
 * non-negative values only: generate initial population, then for each
 * generation select a new population, apply crossover and mutation, until
 * the maximum number of generations is exceeded or, if known, the best
 * fitness is attained.  Make sure the most fit member survives to the
 * next generation (elitism).
 *   This Java code was derived from the C code in the Appendix of "Genetic
 * Algorithms + Data Structures = Evolution Programs," by Zbigniew
 * Michalewicz, Second Extended Edition, New York: Springer-Verlag (1994).
 * Other ideas and code were drawn from AGAC by Bill Spears (12 June 1991),
 * available by anonymous ftp from ftp.aic.nrl.navy.mil in file GAC.shar.Z
 * in directory /pub/galist/src.
 *
 * (C) 1996 Stephen J. Hartley.  All rights reserved.
 * Permission to use, copy, modify, and distribute this software for
 * non-commercial uses is hereby granted provided this notice is kept
 * intact within the source file.
 *
 * mailto:shartley@mcs.drexel.edu http://www.mcs.drexel.edu/~shartley
 * Drexel University, Math and Computer Science Department
 * Philadelphia, PA 19104 USA  telephone: +1-215-895-2678
 */

import java.io.*;
import Utilities.*;
import GAutilities.*;
import Chromosomes.*;
import Selections.*;
import Crossovers.*;

class sGA {

   // give fundamental parameters controlling the GA default values
   private static int         populationSize =  100;
   private static int         numXoverPoints =    1;
   private static boolean     doElitism      = false;
   private static double      crossoverRate  =     .7;
   private static double      mutationRate   =     .001;

   // these parameters control the running of the program
   private static int         printPerGens   =  100; // print every this often
   private static int         maxGenerations = 1000; // quit when reached
   private static String      logFileName    = null;

   // record the time the program started
   private static final long  startTime      = System.currentTimeMillis();
   private static long age() {
      return System.currentTimeMillis() - startTime;
   }

   public static void main(String[] args) {

      // process command line arguments, overriding GA parameter defaults
      GetOpt go = new GetOpt(args, "Udp:x:c:m:P:G:EF:");
      go.optErr = true;
      String usage = "Usage: -d -p populationSize -x numXoverPoints\n"
         + "       -E -c crossoverRate -m mutationRate\n"
         + "       -P printPerGens -G maxGenerations -F logFileName";
      int ch = -1;
      while ((ch = go.getopt()) != go.optEOF) {
         if      ((char)ch == 'U') {
            System.err.println(usage);  System.exit(0);
         }
         else if ((char)ch == 'd') Debug.flag = true;
         else if ((char)ch == 'p')
            populationSize = go.processArg(go.optArgGet(), populationSize);
         else if ((char)ch == 'x')
            numXoverPoints = go.processArg(go.optArgGet(), numXoverPoints);
         else if ((char)ch == 'c')
            crossoverRate = go.processArg(go.optArgGet(), crossoverRate);
         else if ((char)ch == 'm')
            mutationRate = go.processArg(go.optArgGet(), mutationRate);
         else if ((char)ch == 'E') doElitism = true;
         else if ((char)ch == 'P')
            printPerGens = go.processArg(go.optArgGet(), printPerGens);
         else if ((char)ch == 'G')
            maxGenerations = go.processArg(go.optArgGet(), maxGenerations);
         else if ((char)ch == 'F')
            logFileName = go.optArgGet();
         else { // undefined option
            System.err.println(usage);  System.exit(1);
         }
      }

      Defaults.logFileName = logFileName;
      Globals.stdout.println
         ("The GA parameters are:\n" +
          "  populationSize      = " + populationSize       + "\n" +
          "  numXoverPoints      = " + numXoverPoints       + "\n" +
          "  crossoverRate       = " + crossoverRate        + "\n" +
          "  mutationRate        = " + mutationRate         + "\n" +
          "  doElitism           = " + doElitism            + "\n" +
          "  printPerGens        = " + printPerGens         + "\n" +
          "  maxGenerations      = " + maxGenerations       + "\n" +
          "  Debug.flag          = " + Debug.flag           + "\n" +
          "  logFileName         = " + logFileName);

      // force loading class MyChromosome and setting static variables
      new MyChromosome();
      Globals.stdout.println("GA: chromosome length = " +
         Chromosome.getChromosomeLength());

      sGA theGA = new sGA();
      theGA.mainLoop();
   }

   private sGA() { super(); } // only GA.main() can create GA

   private void printChromosome(String name, Chromosome c) {
      Globals.stdout.println(name +
        ":\n   " + c.toGenotype() +
         "\n   " + c.toPhenotype() +
         "\n    fitness= " + c.getFitness());
   }

   private final Chromosome theBest = new MyChromosome();
   private int theBestGeneration; // generation theBest first showed up
   // 0..popSize-1 holds population and theBest holds the most fit
   private Chromosome[] population    = new Chromosome[populationSize];
   private Chromosome[] newPopulation = new Chromosome[populationSize];

   private int generationNum;     // current generation number
   private int numCrossovers, numMutations;  // tabulate for report()

   private Selection theSelection = null;
   private Crossover theCrossover = null;

   private void mainLoop() {
      Globals.stdout.println("GA: mainLoop");
      if (Chromosome.isSolutionFitnessKnown()) {
         Globals.stdout.println("Known solution fitness is " +
            Chromosome.getSolutionFitness());
      }
      initialize();
      findTheBest();
      report("Initial population");
      while (!terminated()) {
         generationNum++;
         try {
            theSelection.select(population, newPopulation, populationSize);
            swapPopulationArrays();
         } catch (FitnessSumZeroException e) {
            // replace current population with a new random one;
            // other possibilities are to do nothing and hope mutation
            // fixes the problem eventually
            if (Debug.flag) Globals.stdout.println(e + " (randomizing)");
            for (int i = 0; i < populationSize; i++) {
               population[i].initializeChromosomeRandom();
            }
         } catch (SelectionException e) {
            if (Debug.flag) Globals.stdout.println(e);
         }
         if (Debug.flag) report("Selection");
         crossover();
         if (Debug.flag) report("Crossover");
         mutate();
         if (Debug.flag) report("Mutation");
         if (doElitism) elitism();
         else justUpdateTheBest();
         if (Debug.flag || (generationNum % printPerGens) == 0) {
            report("Report");
         }
      }
      Globals.stdout.println("Simulation completed in " + generationNum +
         " generations and " + age()/1000.0 + " seconds");
      printChromosome("Best member (generation="+theBestGeneration+")",
         theBest);
      System.exit(0);
   }

   private void initialize() {

      generationNum = 0; numCrossovers = 0; numMutations = 0;

      theSelection = new ProportionalSelection();

      if (numXoverPoints == 0) theCrossover = new UniformCrossover();
      else if (numXoverPoints == 1) theCrossover = new OnePointCrossover();
      else theCrossover = new NPointCrossover(numXoverPoints);

      for (int j = 0; j < populationSize; j++) {
         population[j] = new MyChromosome();
         population[j].initializeChromosomeRandom();
         newPopulation[j] = new MyChromosome();
      }

      if (Debug.flag) {
         for (int j=0; j<populationSize; j++) {
            printChromosome("p" + j, population[j]);
         }
      }
   }

   private void findTheBest() {  // called on the initial population only
      double currentBestFitness = population[0].getFitness();
      double next = -1;
      int currentBest = 0; // index of the current best individual

      for (int j = 1; j < populationSize; j++) {
         if ((next = population[j].getFitness()) > currentBestFitness ) {
            currentBest = j;
            currentBestFitness = next;
         }
      }
      // once the best member in the population is found, copy the genes
      population[currentBest].copyChromosome(theBest);

      if (Debug.flag) {
          printChromosome("currentBest (generation="+theBestGeneration+")",
             theBest);
      }
   }

   private boolean terminated() {
      return (theBest.isSolutionFitnessKnown() &&
              theBest.getFitness() == theBest.getSolutionFitness()) ||
             generationNum >= maxGenerations;
   }

   private void swapPopulationArrays() {
      Chromosome[] temp = population;
      population = newPopulation;
      newPopulation = temp;
   }

   /***************************************************************/
   /* Crossover selection: selects two parents that take part in  */
   /* the crossover.  For each population member, flip a weighted */
   /* coin.  Every two times it comes up < crossoverRate, then    */
   /* crossover those two chromosomes.                            */
   /***************************************************************/
   private void crossover() {
      int one = -1;    // compiler complains not being initialized
      int first  =  0; // count of the number of members chosen

      for (int mem = 0; mem < populationSize; ++mem) {
         if (MyRandom.dblRandom() < crossoverRate) {
            ++first;
            if (first % 2 == 0) {
               numCrossovers++;
               if (Debug.flag) {
                  Globals.stdout.println("crossing " + one + " and " + mem);
               }
               theCrossover.xOver(population[one], population[mem]);
            } else one = mem;
         }
      }
   }

   /**************************************************************/
   /* Mutation: Random uniform mutation. A variable selected for */
   /* mutation is replaced by a random value between lower and   */
   /* upper bounds of this variable                              */
   /**************************************************************/
   private void mutate() {
      int chromosomeLength = Chromosome.getChromosomeLength();
      for (int i = 0; i < populationSize; i++) {
         for (int j = 0; j < chromosomeLength; j++) {
            if (MyRandom.dblRandom() < mutationRate) {
               numMutations++;
               population[i].mutateGene(j);
               if (Debug.flag) {
                  printChromosome("mutation, i=" + i + ", gene=" + j,
                     population[i]);
               }
            }
         }
      }
   }

   /***************************************************************/
   /* Report function: Reports progress of the simulation.
   /***************************************************************/
   private void report(String title) {
      double best_val;            // best fitness in this population
      double most_fit;            // most fit seen in previous generations
      double avg;                 // avg population fitness
      double stddev;              // std. deviation of population fitness
      double sum_square;          // sum of square for std. calc
      double square_sum;          // square of sum for std. calc
      double sum;                 // total population fitness
      double fitness;

      sum = 0.0;
      sum_square = 0.0;
      best_val = -1.0;

      for (int i = 0; i < populationSize; i++) {
         fitness = population[i].getFitness();
         sum += fitness;
         sum_square += fitness * fitness;
         if (fitness > best_val) best_val = fitness;
      }

      avg = sum/(double)populationSize;
      square_sum = sum * sum/(double)populationSize;
      stddev = Math.sqrt((1.0/(double)(populationSize - 1))
         *(sum_square - square_sum));
      most_fit = theBest.getFitness();

      Globals.stdout.println(title + ": generation=" + generationNum
         + " best value=" + best_val + " avg=" + avg + " stddev=" + stddev);
      printChromosome("most fit (previous generation=" + theBestGeneration
         + ")", theBest);
      Globals.stdout.println("number of crossovers and mutations: "
         + numCrossovers + " and " + numMutations);

      if (Debug.flag) {
         for (int j=0; j<populationSize; j++) {
            printChromosome("p" + j, population[j]);
         }
      }
      Globals.stdout.flush();
   }

   /****************************************************************/
   /* Elitist function: The best member of the previous generation */
   /* is stored in theBest Chromosome.  If the best member of      */
   /* the current generation is worse then the best member of the  */
   /* previous generation, the latter one would replace the worst  */
   /* member of the current population                             */
   /****************************************************************/
   private void elitism() {

      double best, worst;          // best and worst fitness values
      int bestMember, worstMember; // indexes of the best and worst member
      int start; // index used to start the loop

      if (populationSize % 2 == 0) {
         best = -1; bestMember = -1;
         worst = Double.MAX_VALUE; worstMember = -1;
         start = 0;
      } else {
         best  = population[0].getFitness(); bestMember = 0;
         worst = population[0].getFitness(); worstMember = 0;
         start = 1;
      }

      for (int i = start; i < populationSize - 1; i+=2) {
         if (population[i].getFitness() > population[i+1].getFitness()) {
            if (population[i].getFitness() > best) {
               best = population[i].getFitness(); bestMember = i;
            }
            if (population[i+1].getFitness() < worst) {
               worst = population[i+1].getFitness(); worstMember = i + 1;
            }
         } else {
            if (population[i].getFitness() < worst) {
               worst = population[i].getFitness(); worstMember = i;
            }
            if (population[i+1].getFitness() > best) {
               best = population[i+1].getFitness(); bestMember = i + 1;
            }
         }
      }
      // if best individual from the new population is better than
      // the best individual from the previous population, then
      // copy the best from the new population; else replace the
      // worst individual from the current population with the
      // best one from the previous generation
      if (best > theBest.getFitness()) {
         population[bestMember].copyChromosome(theBest);
         theBestGeneration = generationNum;
      } else {
         theBest.copyChromosome(population[worstMember]);
      }

      if (Debug.flag) {
         printChromosome("elitism, best (index="+bestMember+")",
            population[bestMember]);
         printChromosome("elitism, worst (index="+worstMember+")",
            population[worstMember]);
      }
   }

   private void justUpdateTheBest() {
      double currentBestFitness = population[0].getFitness();
      double next = -1;
      int currentBest = 0; // index of the current best individual

      for (int j = 1; j < populationSize; j++) {
         if ((next = population[j].getFitness()) > currentBestFitness ) {
            currentBest = j;
            currentBestFitness = next;
         }
      }
      if (currentBestFitness > theBest.getFitness()) {
         population[currentBest].copyChromosome(theBest);
         theBestGeneration = generationNum;
      }
      if (Debug.flag) {
          printChromosome("theBest (generation="+theBestGeneration+")",
             theBest);
      }
   }
}

/* ............... Example compile and run(s)

D:\>javac sGA.java

D:\>java sGA -p100 -m0.01 -P10 -G100 -F bitCount.output

D:\>type bitCount.output

The GA parameters are:
  populationSize      = 100
  numXoverPoints      = 1
  crossoverRate       = 0.7
  mutationRate        = 0.01
  doElitism           = false
  printPerGens        = 10
  maxGenerations      = 100
  Debug.flag          = false
  logFileName         = bitCount.output
BitCountChromosome: chromosome length is 32
GA: chromosome length = 32
GA: mainLoop
Known solution fitness is 32
Initial population: generation=0 most fit=23 avg=16.72 stddev=2.77099
most fit (generation=0):
   01111110111111011110101000111011
   this chromosome has 23 bits set
    fitness= 23
number of crossovers and mutations: 0 and 0
Report: generation=10 most fit=26 avg=19.17 stddev=2.39129
most fit (generation=10):
   10111111110111011111100111111101
   this chromosome has 26 bits set
    fitness= 26
number of crossovers and mutations: 341 and 304
Report: generation=20 most fit=27 avg=21.41 stddev=2.31855
most fit (generation=18):
   01111111111111011111001111111101
   this chromosome has 27 bits set
    fitness= 27
number of crossovers and mutations: 690 and 633
Report: generation=30 most fit=28 avg=21 stddev=2.3741
most fit (generation=26):
   11111111111111011111101111110110
   this chromosome has 28 bits set
    fitness= 28
number of crossovers and mutations: 1033 and 954
Report: generation=40 most fit=29 avg=23.01 stddev=2.32464
most fit (generation=34):
   11111111111111111101011111110111
   this chromosome has 29 bits set
    fitness= 29
number of crossovers and mutations: 1384 and 1269
Report: generation=50 most fit=30 avg=23.67 stddev=2.01537
most fit (generation=45):
   11111111111111111111011011111111
   this chromosome has 30 bits set
    fitness= 30
number of crossovers and mutations: 1736 and 1620
Report: generation=60 most fit=30 avg=23.53 stddev=2.10077
most fit (generation=45):
   11111111111111111111011011111111
   this chromosome has 30 bits set
    fitness= 30
number of crossovers and mutations: 2081 and 1919
Report: generation=70 most fit=30 avg=23.57 stddev=1.83267
most fit (generation=45):
   11111111111111111111011011111111
   this chromosome has 30 bits set
    fitness= 30
number of crossovers and mutations: 2437 and 2231
Report: generation=80 most fit=30 avg=24.24 stddev=1.99555
most fit (generation=45):
   11111111111111111111011011111111
   this chromosome has 30 bits set
    fitness= 30
number of crossovers and mutations: 2789 and 2520
Report: generation=90 most fit=30 avg=24.59 stddev=2.59796
most fit (generation=45):
   11111111111111111111011011111111
   this chromosome has 30 bits set
    fitness= 30
number of crossovers and mutations: 3142 and 2873
Report: generation=100 most fit=30 avg=24.94 stddev=2.0192
most fit (generation=45):
   11111111111111111111011011111111
   this chromosome has 30 bits set
    fitness= 30
number of crossovers and mutations: 3481 and 3169
Simulation completed in 100 generations and 26.58 seconds
Best member (generation=45):
   11111111111111111111011011111111
   this chromosome has 30 bits set
    fitness= 30

D:\>edit sGA.java

D:\>javac sGA.java

D:\>java sGA -E -p100 -m0.01 -P10 -G100

The GA parameters are:
  populationSize      = 100
  numXoverPoints      = 1
  crossoverRate       = 0.7
  mutationRate        = 0.01
  doElitism           = true
  printPerGens        = 10
  maxGenerations      = 100
  Debug.flag          = false
  logFileName         = null
XtoTenthChromosome: chromosome length is 32
GA: chromosome length = 32
GA: mainLoop
Known solution fitness is 1
Initial population: generation=0 most fit=0.919291 avg=0.122947 stddev=0.216728
most fit (generation=0):
   01100010000010110101101110111111
   x=0.99162
    fitness= 0.919291
number of crossovers and mutations: 0 and 0
Report: generation=10 most fit=0.999597 avg=0.827711 stddev=0.206224
most fit (generation=9):
   01111001110110101011111111111111
   x=0.99996
    fitness= 0.999597
number of crossovers and mutations: 353 and 320
Report: generation=20 most fit=0.999599 avg=0.870255 stddev=0.196344
most fit (generation=16):
   01001111111110101011111111111111
   x=0.99996
    fitness= 0.999599
number of crossovers and mutations: 694 and 632
Report: generation=30 most fit=0.999997 avg=0.891283 stddev=0.159036
most fit (generation=29):
   10110111010111111111111111111111
   x=1
    fitness= 0.999997
number of crossovers and mutations: 1039 and 990
Report: generation=40 most fit=1 avg=0.894416 stddev=0.183714
most fit (generation=40):
   00000111111111111111111111111111
   x=1
    fitness= 1
number of crossovers and mutations: 1380 and 1368
Report: generation=50 most fit=1 avg=0.907259 stddev=0.169297
most fit (generation=48):
   00110111111111111111111111111111
   x=1
    fitness= 1
number of crossovers and mutations: 1728 and 1691
Report: generation=60 most fit=1 avg=0.941364 stddev=0.142952
most fit (generation=48):
   00110111111111111111111111111111
   x=1
    fitness= 1
number of crossovers and mutations: 2083 and 2017
Report: generation=70 most fit=1 avg=0.92843 stddev=0.140908
most fit (generation=67):
   10111111111111111111111111111111
   x=1
    fitness= 1
number of crossovers and mutations: 2432 and 2328
Report: generation=80 most fit=1 avg=0.918706 stddev=0.162607
most fit (generation=67):
   10111111111111111111111111111111
   x=1
    fitness= 1
number of crossovers and mutations: 2776 and 2666
Report: generation=90 most fit=1 avg=0.927445 stddev=0.169067
most fit (generation=67):
   10111111111111111111111111111111
   x=1
    fitness= 1
number of crossovers and mutations: 3137 and 2978
Report: generation=100 most fit=1 avg=0.926531 stddev=0.184471
most fit (generation=67):
   10111111111111111111111111111111
   x=1
    fitness= 1
number of crossovers and mutations: 3481 and 3287
Simulation completed in 100 generations and 27.74 seconds
Best member (generation=67):
   10111111111111111111111111111111
   x=1
    fitness= 1

D:\>edit sGA.java

D:\>javac sGA.java

D:\>java sGA -p100 -m0.1 -P10 -G100

The GA parameters are:
  populationSize      = 100
  numXoverPoints      = 1
  crossoverRate       = 0.7
  mutationRate        = 0.1
  doElitism           = false
  printPerGens        = 10
  maxGenerations      = 100
  Debug.flag          = false
  logFileName         = null
SinesChromosome: chromosome length is 3
GA: chromosome length = 3
GA: mainLoop
Initial population: generation=0 most fit=36.8932 avg=21.3019 stddev=5.27425
most fit (generation=0):
    gene[0]=9.62878 gene[1]=5.21988 gene[2]=0.823022
   21.5+9.62878*sin(4*PI*9.62878)+5.21988*sin(20*PI*5.21988)+0.823022
    fitness= 36.8932
number of crossovers and mutations: 0 and 0
Report: generation=10 most fit=37.4763 avg=28.3356 stddev=5.45076
most fit (generation=8):
    gene[0]=11.6685 gene[1]=5.12788 gene[2]=0.963809
   21.5+11.6685*sin(4*PI*11.6685)+5.12788*sin(20*PI*5.12788)+0.963809
    fitness= 37.4763
number of crossovers and mutations: 346 and 317
Report: generation=20 most fit=37.4763 avg=29.5948 stddev=4.84377
most fit (generation=8):
    gene[0]=11.6685 gene[1]=5.12788 gene[2]=0.963809
   21.5+11.6685*sin(4*PI*11.6685)+5.12788*sin(20*PI*5.12788)+0.963809
    fitness= 37.4763
number of crossovers and mutations: 684 and 603
Report: generation=30 most fit=37.4763 avg=28.6652 stddev=4.5108
most fit (generation=8):
    gene[0]=11.6685 gene[1]=5.12788 gene[2]=0.963809
   21.5+11.6685*sin(4*PI*11.6685)+5.12788*sin(20*PI*5.12788)+0.963809
    fitness= 37.4763
number of crossovers and mutations: 1015 and 934
Report: generation=40 most fit=38.1848 avg=29.4046 stddev=6.39036
most fit (generation=39):
    gene[0]=11.6621 gene[1]=5.32681 gene[2]=0.973592
   21.5+11.6621*sin(4*PI*11.6621)+5.32681*sin(20*PI*5.32681)+0.973592
    fitness= 38.1848
number of crossovers and mutations: 1384 and 1251
Report: generation=50 most fit=38.2355 avg=31.7354 stddev=6.47591
most fit (generation=44):
    gene[0]=11.1241 gene[1]=5.12562 gene[2]=0.49033
   21.5+11.1241*sin(4*PI*11.1241)+5.12562*sin(20*PI*5.12562)+0.49033
    fitness= 38.2355
number of crossovers and mutations: 1740 and 1555
Report: generation=60 most fit=38.2355 avg=30.5727 stddev=5.91608
most fit (generation=44):
    gene[0]=11.1241 gene[1]=5.12562 gene[2]=0.49033
   21.5+11.1241*sin(4*PI*11.1241)+5.12562*sin(20*PI*5.12562)+0.49033
    fitness= 38.2355
number of crossovers and mutations: 2098 and 1851
Report: generation=70 most fit=38.4794 avg=32.0127 stddev=5.7576
most fit (generation=63):
    gene[0]=11.6621 gene[1]=5.62342 gene[2]=0.964925
   21.5+11.6621*sin(4*PI*11.6621)+5.62342*sin(20*PI*5.62342)+0.964925
    fitness= 38.4794
number of crossovers and mutations: 2437 and 2124
Report: generation=80 most fit=38.621 avg=33.0924 stddev=5.99235
most fit (generation=75):
    gene[0]=11.6621 gene[1]=5.72433 gene[2]=0.982909
   21.5+11.6621*sin(4*PI*11.6621)+5.72433*sin(20*PI*5.72433)+0.982909
    fitness= 38.621
number of crossovers and mutations: 2776 and 2406
Report: generation=90 most fit=39.0127 avg=32.0723 stddev=6.0964
most fit (generation=82):
    gene[0]=12.0983 gene[1]=5.32681 gene[2]=0.794961
   21.5+12.0983*sin(4*PI*12.0983)+5.32681*sin(20*PI*5.32681)+0.794961
    fitness= 39.0127
number of crossovers and mutations: 3120 and 2740
Report: generation=100 most fit=39.0127 avg=32.7011 stddev=6.00134
most fit (generation=82):
    gene[0]=12.0983 gene[1]=5.32681 gene[2]=0.794961
   21.5+12.0983*sin(4*PI*12.0983)+5.32681*sin(20*PI*5.32681)+0.794961
    fitness= 39.0127
number of crossovers and mutations: 3478 and 3029
Simulation completed in 100 generations and 9.77 seconds
Best member (generation=82):
    gene[0]=12.0983 gene[1]=5.32681 gene[2]=0.794961
   21.5+12.0983*sin(4*PI*12.0983)+5.32681*sin(20*PI*5.32681)+0.794961
    fitness= 39.0127

D:\>edit sGA.java

D:\>javac sGA.java

D:\>java sGA -E -p100 -m0.01 -P10 -G100

The GA parameters are:
  populationSize      = 100
  numXoverPoints      = 1
  crossoverRate       = 0.7
  mutationRate        = 0.01
  doElitism           = true
  printPerGens        = 10
  maxGenerations      = 100
  Debug.flag          = false
  logFileName         = null
ByteCountChromosome: chromosome length is 64
GA: chromosome length = 64
GA: mainLoop
Known solution fitness is 64
Initial population: generation=0 most fit=8 avg=0.48 stddev=1.90947
most fit (generation=0):
   1101001111111111011101001100100100110011101001010001010000000001
   this chromosome has 1 bytes set
    fitness= 8
number of crossovers and mutations: 0 and 0
Report: generation=10 most fit=24 avg=14.48 stddev=4.35885
most fit (generation=7):
   1111111111111111000001101100100111110000011101100011110011111111
   this chromosome has 3 bytes set
    fitness= 24
number of crossovers and mutations: 343 and 593
Report: generation=20 most fit=24 avg=17.6 stddev=5.57048
most fit (generation=7):
   1111111111111111000001101100100111110000011101100011110011111111
   this chromosome has 3 bytes set
    fitness= 24
number of crossovers and mutations: 696 and 1270
Report: generation=30 most fit=24 avg=20.64 stddev=4.9798
most fit (generation=7):
   1111111111111111000001101100100111110000011101100011110011111111
   this chromosome has 3 bytes set
    fitness= 24
number of crossovers and mutations: 1056 and 1926
Report: generation=40 most fit=24 avg=17.52 stddev=5.53498
most fit (generation=7):
   1111111111111111000001101100100111110000011101100011110011111111
   this chromosome has 3 bytes set
    fitness= 24
number of crossovers and mutations: 1414 and 2590
Report: generation=50 most fit=24 avg=18.56 stddev=5.55654
most fit (generation=7):
   1111111111111111000001101100100111110000011101100011110011111111
   this chromosome has 3 bytes set
    fitness= 24
number of crossovers and mutations: 1753 and 3229
Report: generation=60 most fit=32 avg=21.52 stddev=6.09103
most fit (generation=51):
   1111111111111111010011111111111111110011011101011000110111111111
   this chromosome has 4 bytes set
    fitness= 32
number of crossovers and mutations: 2092 and 3844
Report: generation=70 most fit=32 avg=23.6 stddev=5.72783
most fit (generation=51):
   1111111111111111010011111111111111110011011101011000110111111111
   this chromosome has 4 bytes set
    fitness= 32
number of crossovers and mutations: 2449 and 4511
Report: generation=80 most fit=32 avg=23.44 stddev=6.84654
most fit (generation=51):
   1111111111111111010011111111111111110011011101011000110111111111
   this chromosome has 4 bytes set
    fitness= 32
number of crossovers and mutations: 2789 and 5156
Report: generation=90 most fit=32 avg=23.52 stddev=5.66485
most fit (generation=51):
   1111111111111111010011111111111111110011011101011000110111111111
   this chromosome has 4 bytes set
    fitness= 32
number of crossovers and mutations: 3147 and 5815
Report: generation=100 most fit=40 avg=26.72 stddev=7.02963
most fit (generation=92):
   1111111111111111001011101111111111110011111101011111111111111111
   this chromosome has 5 bytes set
    fitness= 40
number of crossovers and mutations: 3496 and 6469
Simulation completed in 100 generations and 46.13 seconds
Best member (generation=92):
   1111111111111111001011101111111111110011111101011111111111111111
   this chromosome has 5 bytes set
    fitness= 40
                                            ... end of example run(s)  */
