sGA.java       version 1.00      8 August 1996
               version 1.01     22 August 1996 -- split into packages
               version 1.02      9    May 1997 -- JDK 1.1 mods
               version 1.03     20   June 1997 -- removed last deprecation

  This Java program implements a simple genetic algorithm where the
fitness function takes non-negative values only.
          generate random initial population
          for each generation select a new population
          apply crossover and mutation
          until the maximum number of generations is exceeded
          or if known the best fitness is attained
Make sure the most fit member survives to the next generation (elitism).
  This Java code was derived from the C code in the Appendix of "Genetic
Algorithms + Data Structures = Evolution Programs," by Zbigniew
Michalewicz, Second Extended Edition, New York: Springer-Verlag (1994).
Other ideas and code were drawn from GAC by Bill Spears (12 June 1991).
Available by anonymous ftp from ftp.aic.nrl.navy.mil in file GAC.shar.Z
in directory /pub/galist/src.

  Four sample problems are contained in the code: three with bit
genes and one with double genes.

  BitCountChromosome:  maximize the set bits in a bit string
  XtoTenthChromosome:  maximize x**10 over the interval 0.0 to 1.0
                       where the value of x is encoded in some number
                       of bits representing an unsigned integer
                       normalized to be between 0 and 1.
  SinesChromosome:     maximize 21.5+x1*sin(4*PI*x1)+x2*sin(20*PI*x2)+x3
                       for x1 in [-3.0,12.1], x2 in [4.1,5.8], and
                       x3 in [0.0, 1.0]
  ByteCountChromosome: maximize the number of bytes that are all one
                       bits in an eight byte word

  To use this program, modify the class MyChromosome to include your
problem, which you have coded in some class, say YourChromosome.
All changes to the sGA.java file to run your problem are confined to
your class YourChromosome.  This is what object-oriented programming
is all about!

final class MyChromosome extends
// Put your Chromosome class name here (comment out the others):
   // BitCountChromosome
   // XtoTenthChromosome
   // SinesChromosome
   // ByteCountChromosome
      YourChromosome
}

Then compile the source code.

% javac sGA.java

% java sGA -U

Usage: -d -p populationSize -x numXoverPoints
       -E -c crossoverRate -m mutationRate
       -P printPerGens -G maxGenerations -F logFileName

where

       -d          if present, turns on some debugging output
       -p          population size (integer)
       -x          number of crossover points (0 for uniform)
       -E          if present, turns on elitism
       -c          Pc, crossover probability
       -m          Pm, mutation probability
       -P          print a summary every this many generations
       -G          maximum number of generations to run
       -F          name of output file ti use instead of stdout

The sGA.java source code file has a big comment at the end containing
some sample runs.

(C) 1996 Stephen J. Hartley.  All rights reserved.
Permission to use, copy, modify, and distribute this software for
non-commercial uses is hereby granted provided this notice is kept
intact within the source file.

mailto:shartley@mcs.drexel.edu http://www.mcs.drexel.edu/~shartley
Drexel University, Math and Computer Science Department
Philadelphia, PA 19104 USA  telephone: +1-215-895-2678
