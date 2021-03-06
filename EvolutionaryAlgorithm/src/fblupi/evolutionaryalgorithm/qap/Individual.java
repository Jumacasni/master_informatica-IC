package fblupi.evolutionaryalgorithm.qap;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Scanner;

/**
 * Single individual in the evolutionary algorithm
 * @author fblupi
 */
public class Individual {
    /**
     * Current solution
     */
    private int[] solution;

    /**
     * Improved solution
     */
    private int[] improved;

    /**
     * Fitness of current solution
     */
    private int fitness;

    /**
     * Constructor
     * @param size number of genes
     */
    public Individual(int size) {
        solution = new int[size];
        improved = null;
        fitness = 0;
    }

    /**
     * Copy constructor
     * @param other individual that will be copied
     */
    public Individual(Individual other) {
        solution = new int[other.getSolution().length];
        System.arraycopy(other.getSolution(), 0, solution, 0, other.getSolution().length);
        fitness = other.getFitness();
    }

    /**
     * Create an individual from a file with the solution
     * @param file file with a serie of integers with the solution
     * @throws FileNotFoundException
     */
    public Individual(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);
        solution = new int[Matrices.getSize()];
        for (int i = 0; i < Matrices.getSize(); i++) {
            solution[i] = scanner.nextInt();
        }
        calculateFitness();
    }

    /**
     * Generate random solution
     */
    public void generate() {
        generateRandomSolution();
    }

    /**
     * Calculate fitness of current solution
     */
    public void calculateFitness() {
        fitness = 0;
        for (int i = 0; i < Matrices.getSize(); i++) {
            for (int j = 0; j < Matrices.getSize(); j++) {
                fitness += Matrices.getFlowMatrix()[i][j] * Matrices.getDistanceMatrix()[solution[i]][solution[j]];
            }
        }
    }

    /**
     * Calculate fitness of improved solution
     * @param type type of evolutionary algorithm
     */
    public void calculateImprovedFitness(EAType type) {
        if (type == EAType.BALDWINIAN || type == EAType.LAMARCKIAN) {
            if (improved == null) {
                improved = localSearch();
                if (type == EAType.LAMARCKIAN) {
                    solution = improved;
                }
            }
            fitness = 0;
            for (int i = 0; i < Matrices.getSize(); i++) {
                for (int j = 0; j < Matrices.getSize(); j++) {
                    fitness += Matrices.getFlowMatrix()[i][j] * Matrices.getDistanceMatrix()[improved[i]][improved[j]];
                }
            }
        } else {
            calculateFitness();
        }
    }

    /**
     * Get fitness of current solution
     * @return fitness of current solution
     */
    public int getFitness() {
        return fitness;
    }

    /**
     * Get current solution
     * @return current solution
     */
    public int[] getSolution() {
        return solution;
    }

    /**
     * Mutate a solution
     * @param pos1 a position to mutate
     * @param pos2 another position to mutate
     */
    public void mutate(int pos1, int pos2) {
        int swap = solution[pos1];
        solution[pos1] = solution[pos2];
        solution[pos2] = swap;
    }

    /**
     * Generate random solution
     */
    private void generateRandomSolution() {
        // Initialize ordered array
        for (int i = 0; i < solution.length; i++) {
            solution[i] = i;
        }
        // Shuffle it
        shuffleSolution();

        // Calculate fitness
        calculateFitness();
    }

    /**
     * Shuffle solution
     */
    private void shuffleSolution() {
        Random r = new Random();
        for (int i = 0; i < solution.length; i++) {
            int swap = solution[i]; // value of gene i
            int pos = r.nextInt(solution.length); // position of gene to swap
            solution[i] = solution[pos];
            solution[pos] = swap;
        }
    }

    /**
     * Greedy 2-opt algorithm to optimize an individual
     */
    private int[] localSearch() {
        Individual best;
        Individual S = new Individual(this);
        S.calculateFitness();
        do {
            best = new Individual(S); // save best solution by now
            for (int i = 0; i < solution.length; i++) {
                for (int j = i + 1; j < solution.length; j++) {
                    // Create T exchanging i and j gene
                    Individual T = new Individual(S);
                    T.getSolution()[i] = S.getSolution()[j];
                    T.getSolution()[j] = S.getSolution()[i];
                    T.adjustFitness(i, j, S.getSolution()); // calculate fitness of new solution
                    if (T.getFitness() < S.getFitness()) { // if new solution is better than older updates
                        S = new Individual(T);
                    }
                }
            }
        } while (S.getFitness() < best.getFitness());
        return S.getSolution();
    }

    /**
     * Adjust fitness after changing two genes
     * @param i first gene
     * @param j second gene
     * @param old old solution before changing genes
     */
    private void adjustFitness(int i, int j, int[] old) {
        for (int k = 0; k < Matrices.getSize(); k++) {
            // Fil i
            fitness -= Matrices.getFlowMatrix()[i][k] * Matrices.getDistanceMatrix()[old[i]][old[k]];
            fitness += Matrices.getFlowMatrix()[i][k] * Matrices.getDistanceMatrix()[solution[i]][solution[k]];

            // Fil j
            fitness -= Matrices.getFlowMatrix()[j][k] * Matrices.getDistanceMatrix()[old[j]][old[k]];
            fitness += Matrices.getFlowMatrix()[j][k] * Matrices.getDistanceMatrix()[solution[j]][solution[k]];

            if (k != i && k != j) {
                // Col i
                fitness -= Matrices.getFlowMatrix()[k][i] * Matrices.getDistanceMatrix()[old[k]][old[i]];
                fitness += Matrices.getFlowMatrix()[k][i] * Matrices.getDistanceMatrix()[solution[k]][solution[i]];

                // Col j
                fitness -= Matrices.getFlowMatrix()[k][j] * Matrices.getDistanceMatrix()[old[k]][old[j]];
                fitness += Matrices.getFlowMatrix()[k][j] * Matrices.getDistanceMatrix()[solution[k]][solution[j]];
            }
        }
    }

}
