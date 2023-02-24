package unam.ciencias.computoconcurrente;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PrimeNumberCalculatorTest {

    PrimeNumberCalculator primeNumberCalculator;

    @Test
    void zeroIsNotPrime() throws InterruptedException{
        primeNumberCalculator = new PrimeNumberCalculator();

        assertFalse(primeNumberCalculator.isPrime(0));
    }

    @Test
    void oneIsNotPrime() throws InterruptedException{
        primeNumberCalculator = new PrimeNumberCalculator();

        assertFalse(primeNumberCalculator.isPrime(1));
    }

    @Test
    void negativeIsPrime() throws InterruptedException{
        primeNumberCalculator = new PrimeNumberCalculator();

        assertTrue(primeNumberCalculator.isPrime(-131));
    }


    @Test
    void isPrimeSequential() throws InterruptedException{
        primeNumberCalculator = new PrimeNumberCalculator();

        assertTrue(primeNumberCalculator.isPrime(191));
    }

    @Test
    void isNotPrimeSequential() throws InterruptedException{
        primeNumberCalculator = new PrimeNumberCalculator();

        assertFalse(primeNumberCalculator.isPrime(192));
    }

    @Test
    void isPrimeConcurrent() throws InterruptedException{
        primeNumberCalculator = new PrimeNumberCalculator(2);

        assertTrue(primeNumberCalculator.isPrime(191));
    }
    
    @Test
    void isNotPrimeConcurrent() throws InterruptedException{
        primeNumberCalculator = new PrimeNumberCalculator(2);

        assertFalse(primeNumberCalculator.isPrime(192));
    }   
    
    @Test
    void isPrimeSequentialBigNumber() throws InterruptedException{
        primeNumberCalculator = new PrimeNumberCalculator();

        assertTrue(primeNumberCalculator.isPrime(1297633));
    }

    @Test
    void isNotPrimeSequentialBigNumber() throws InterruptedException{
        primeNumberCalculator = new PrimeNumberCalculator();

        assertFalse(primeNumberCalculator.isPrime(1298777));
    }

    @Test
    void isPrimeConcurrentBigNumber() throws InterruptedException{
        primeNumberCalculator = new PrimeNumberCalculator(4);

        assertTrue(primeNumberCalculator.isPrime(1297633));
    }
    
    @Test
    void isNotPrimeConcurrentBigNumber() throws InterruptedException{
        primeNumberCalculator = new PrimeNumberCalculator(4);

        assertFalse(primeNumberCalculator.isPrime(1298777));
    }

}