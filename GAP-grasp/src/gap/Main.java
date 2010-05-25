/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;


public class Main {

    private static GapProblem myProblem;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        GapParser parser= new GapParser("gap1.txt"); //files in ./data/ dir
        System.out.println("Reading information...");
        myProblem = parser.parseProblem(4); // 5th example in file
        System.out.println("Done");
 
        generateRandomSolution();
        generateGreedySolution();
        
     //   localSearch();
  
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
        }else{System.out.println(myProblem.toString()); 
             System.out.println("No solution:(");  
        }
    }
    // with infeasible solution -> hard to get a feasible one :(
    public static void localSearch(){
        myProblem.clear(); 
        generateRandomSolution(); //initial solution
        GapSolution bestSolution = myProblem.getSolution();
        int bestCost = bestSolution.getGlobalCost();
        for (int i = 0; i < 10000; i++){  
            GapSolution newSolution = myProblem.getBestNeighbour();
            if (newSolution.isFeasible()){
                bestSolution = new GapSolution(newSolution,myProblem); //best feasible solution this far
            }
            if (newSolution.equals(myProblem.getSolution())) {
                myProblem.perturbate();  // we are stucked
            } else{
                myProblem.setSolution(newSolution);
            }            
        }    
        myProblem.setSolution(bestSolution);
        System.out.println(myProblem.toString()); 
         
        }    
           
    }
    
