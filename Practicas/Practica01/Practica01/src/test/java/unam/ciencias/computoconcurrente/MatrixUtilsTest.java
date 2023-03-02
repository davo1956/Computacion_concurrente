package unam.ciencias.computoconcurrente;

import java.util.Random;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MatrixUtilsTest {
    MatrixUtils matrixUtils;

    @Test
    void findAverage() throws InterruptedException{
        matrixUtils = new MatrixUtils();
        int[][] matrix = {
                {4, 29, -6, 0},
                {15, 6, 0, 4},
                {25, 41, -10, 4},
                {0, 0, -1, 39},
        };

        assertEquals(9.375, matrixUtils.findAverage(matrix));
    }

    @Test
    void findAverageConcurrent() throws InterruptedException{
        matrixUtils = new MatrixUtils(2);
        int[][] matrix = {
                {4, 29, -6, 0},
                {15, 6, 0, 4},
                {25, 41, -10, 4},
                {0, 0, -1, 39},
        };

        assertEquals(9.375, matrixUtils.findAverage(matrix));
    }

    @Test
    void findAverage2() throws InterruptedException{
        matrixUtils = new MatrixUtils();
        int[][] matrix = {
                {1, 1, 1},
                {1, 1, 1},
                {1, 1, 1}
        };

        assertEquals(1, matrixUtils.findAverage(matrix));
    }

    @Test
    void findaverageConcurrent2() throws InterruptedException{
        matrixUtils = new MatrixUtils(2);
        int[][] matrix = {
            {1, 1, 1},
            {1, 1, 1},
            {1, 1, 1}
    };

        assertEquals(1, matrixUtils.findAverage(matrix));
    }
}

    
