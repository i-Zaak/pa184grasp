/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static GapProblem myProblem;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {


        int position = 1;
        String file_name = "";
        boolean random_alg = false;
        boolean greedy_alg = false;
        boolean peckish_alg = false;
        boolean local_search = false;
        boolean GRASP = false;
        boolean paralel = false;
        int numThreads = 1;

        // USAGE: java -jar GAP-grasp.jar -f ./data/gap1.txt -n 1 --greedy --local
        // get problem 1 from file in ./data/gap1.txt, show greedy solution and do the local search

        /**
         * Command-line arguments processing.
         */
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-f") || args[i].equals("--file")) { // -f file_name.txt

                file_name = args[i + 1];
                i++;
                System.out.println("Taking file " + file_name);
                continue;
            }

            if (args[i].equals("-n") || args[i].equals("--number")) { // position in file

                position = Integer.parseInt(args[i + 1]);
                i++;
                System.out.println("Taking problem " + position);
                continue;
            }
            if (args[i].equals("-g") || args[i].equals("--greedy")) { // display greedy solution

                greedy_alg = true;
                System.out.println("Greedy solution required");
                continue;
            }
            if (args[i].equals("-r") || args[i].equals("--random")) { // display random solution

                random_alg = true;
                System.out.println("Random solution required");
                continue;
            }
            if (args[i].equals("-p") || args[i].equals("--peckish")) { // display peckish solution

                peckish_alg = true;
                System.out.println("Peckish solution required");
                continue;
            }
            if (args[i].equals("-l") || args[i].equals("--local")) { // do the local search

                local_search = true;
                System.out.println("Local search required");
                continue;
            }
            if (args[i].equals("-G") || args[i].equals("--GRASP")) { // do the GRASP search

                GRASP = true;
                System.out.println("GRASP required");
                continue;
            }
            if (args[i].equals("-a") || args[i].equals("--paralel")) { // do the GRASP search
                if(GRASP){
                    paralel = true;
                    System.out.println("Going paralel");
                }else{
                    System.out.println("Working sequential, paralel only with --GRASP");
                }
                continue;
            }
            if (args[i].equals("-t") || args[i].equals("--threads")) { // -t numthreads

                numThreads = Integer.parseInt(args[i + 1]);
                i++;
                System.out.println("Number of threads " + numThreads);
                continue;
            }
        }

        if (file_name.equals("")) {
            System.out.println("File not specified. Ending.");
            return;
        }

        File file = new File(file_name);
        if (!file.exists()) {
            System.out.println("File " + file + " does not exist");
            return;
        }

        GapParser parser = new GapParser(file);

        System.out.println("Reading input for problem " + position);
        myProblem = parser.parseProblem(position);
        System.out.println("Done");

        if (myProblem == null) {
            System.out.println("Problem " + position + " from " + file_name + " cannot be found");
            return;
        }


        if (random_alg) {
            System.out.println("Generating random solution");
            generateRandomSolution();
        }

        if (greedy_alg) {
            System.out.println("Generating greedy solution");
            generateGreedySolution();
        }

        if (peckish_alg) {
            System.out.println("Generating peckish solution");
            generatePeckishSolution();
        }

        if (local_search) {
            System.out.println("Performing local search");
            localSearch();
        }

        if (GRASP) {
            if(!paralel){
                System.out.println("Performing sequential GRASP search");
                generateGRASPSolution();
            }else{
                System.out.println("Performing parallel GRASP search");
                generateParalelGRASPSolution(numThreads);
            }
            
        }

    }

    public static void generateParalelGRASPSolution(int numThreads) {
        myProblem.clear();
        long runtime = new Date().getTime();
        boolean solved = myProblem.generateParalelGRASPSolution(numThreads);
        runtime = new Date().getTime() - runtime;
        if (solved) {
            System.out.println(myProblem.toString());
            System.out.println("GRASP solution found in " + runtime + " ms with " + myProblem.getBacktracksCount() + " backtracks");
        } else {
            System.out.println(myProblem.toString());
            System.out.println("No solution:(");
        }
    }
    public static void generateGRASPSolution() {
        myProblem.clear();
        long runtime = new Date().getTime();
        boolean solved = myProblem.generateGRASPSolution();
        runtime = new Date().getTime() - runtime;
        if (solved) {
            System.out.println(myProblem.toString());
            System.out.println("GRASP solution found in " + runtime + " ms with " + myProblem.getBacktracksCount() + " backtracks");
        } else {
            System.out.println(myProblem.toString());
            System.out.println("No solution:(");
        }
    }

    public static void generatePeckishSolution() {
        myProblem.clear();
        long runtime = new Date().getTime();
        boolean solved = myProblem.generatePeckishSolution();
        runtime = new Date().getTime() - runtime;
        if (solved) {
            System.out.println(myProblem.toString());
            System.out.println("Peckish solution found in " + runtime + " ms with " + myProblem.getBacktracksCount() + " backtracks");
        } else {
            System.out.println(myProblem.toString());
            System.out.println("No solution:(");
        }
    }

    public static void generateRandomSolution() {

        myProblem.clear();
        long runtime = new Date().getTime();
        boolean solved = myProblem.generateRandomSolution();
        runtime = new Date().getTime() - runtime;
        if (solved) {
            System.out.println(myProblem.toString());
            System.out.println("Random solution found in " + runtime + " ms with " + myProblem.getBacktracksCount() + " backtracks");
        } else {
            System.out.println(myProblem.toString());
            System.out.println("No solution:(");
        }
    }

    public static void generateGreedySolution() {

        myProblem.clear();
        long runtime = new Date().getTime();
        boolean solved = myProblem.generateGreedySolution();
        runtime = new Date().getTime() - runtime;
        if (solved) {
            System.out.println(myProblem.toString());
            System.out.println("Greedy solution found in " + runtime + " ms with " + myProblem.getBacktracksCount() + " backtracks");
            try {
                FileWriter fstream = new FileWriter("solution.svg");
                BufferedWriter out = new BufferedWriter(fstream);
                out.write(myProblem.getSolution().toSVG());
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println(myProblem.toString());
            System.out.println("No solution:(");
        }
    }
    
        public static void generateTimeGreedySolution() {

        myProblem.clear();
        long runtime = new Date().getTime();
        boolean solved = myProblem.generateTimeGreedySolution();
        runtime = new Date().getTime() - runtime;
        if (solved) {
            System.out.println(myProblem.toString());
            System.out.println("Time greedy solution found in " + runtime + " ms with " + myProblem.getBacktracksCount() + " backtracks");
              System.out.println(myProblem.toString());
            System.out.println("No solution:(");
        }
    }    

    public static void localSearch() {
        long runtime = new Date().getTime();
        myProblem.clear();
        generateTimeGreedySolution(); //initial solution

        GapSolution bestSolution = myProblem.getSolution();
        GapSettings settings = bestSolution.getSettings();
        int bestCost = bestSolution.getGlobalCost();
        int min_cost = myProblem.getCostLowerBound();
        System.out.println("Lower bound of GlobalCost is " + min_cost);
        int idle_iter = 0;
        while (idle_iter < 1000) { //until we did 1000 perturbations

            GapSolution newSolution = myProblem.getBestNeighbour();
            if (newSolution.isFeasible() && newSolution.getGlobalCost() < bestCost) {
                bestSolution = new GapSolution(newSolution, settings); //best feasible solution this far

                bestCost = bestSolution.getGlobalCost();
                if (bestCost == min_cost) // when we have found the best cost
                {
                    break;
                }
            }
            if (newSolution.equals(myProblem.getSolution())) {
                Random generator = new Random(idle_iter); // we can choose between two perturbations

                int random = generator.nextInt(100);
                if (random < 50) {
                    myProblem.perturbate();  // we are stucked

                } else {
                    myProblem.perturbate2();
                }
                idle_iter++;
            } else {
                myProblem.setSolution(newSolution);
            }
        }
        myProblem.setSolution(bestSolution);
        runtime = new Date().getTime() - runtime;
        System.out.println(myProblem.toString());
        System.out.println("Solution found with local search in " + runtime + " ms");
    }
}
    
