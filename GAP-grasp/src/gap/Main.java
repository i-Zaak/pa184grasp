/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gap;

import java.io.BufferedWriter;
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
        GapParser parser= new GapParser("gap12.txt"); //files in ./data/ dir
        System.out.println("Reading information...");
        myProblem = parser.parseProblem(5); // 5th example in file
        System.out.println("Done");
 
        generateRandomSolution();
        generateGreedySolution();
        
      //  localSearch();
  
    }
    
    public static void generateRandomSolution(){
        
       myProblem.clear(); 
       long runtime = new Date().getTime();
       boolean solved = myProblem.generateRandomSolution();
       runtime = new Date().getTime() - runtime;
       if (solved){
            System.out.println(myProblem.toString()); 
            System.out.println("Random solution found in " + runtime + " ms with " + myProblem.getBacktracksCount() + " backtracks"); 
        }else{System.out.println(myProblem.toString()); 
             System.out.println("No solution:(");  
        }
    }
    
    public static void generateGreedySolution(){
        
       myProblem.clear(); 
       long runtime = new Date().getTime();
       boolean solved = myProblem.generateGreedySolution();
       runtime = new Date().getTime() - runtime;
       if (solved){
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
       }else{System.out.println(myProblem.toString());         
             System.out.println("No solution:(");  
        }
    }
    // with infeasible solution -> hard to get a feasible one :(
    public static void localSearch(){
        myProblem.clear(); 
        generateGreedySolution(); //initial solution
        GapSolution bestSolution = myProblem.getSolution();
        int bestCost = bestSolution.getGlobalCost();
        int min_cost = myProblem.getCostLowerBound();
        System.out.println("Uplne minimum je " + min_cost);
        int idle_iter = 0;
        while(idle_iter < 1000) { //until we did 1000 perturbations
            GapSolution newSolution = myProblem.getBestNeighbour();
            if (newSolution.isFeasible() && newSolution.getGlobalCost() < bestCost){
                bestSolution = new GapSolution(newSolution,myProblem); //best feasible solution this far
                bestCost = bestSolution.getGlobalCost();
               if (bestCost == min_cost) // when we have found the best cost
                    break;
            }
            if (newSolution.equals(myProblem.getSolution())) {
                Random generator = new Random(idle_iter); // we can choose between two perturbations
                int random = generator.nextInt(100);
                if (random < 50){
                    myProblem.perturbate();  // we are stucked
                }else{
                    myProblem.perturbate2();    
                }    
                idle_iter++;
            } else{
                myProblem.setSolution(newSolution);
            }            
        } 
        myProblem.setSolution(bestSolution);
        System.out.println(myProblem.toString()); 
         
        }    
           
    }
    