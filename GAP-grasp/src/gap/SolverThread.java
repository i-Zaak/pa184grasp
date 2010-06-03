package gap;

/**
 *
 * @author izaak
 */
public class SolverThread extends Thread{
    private GapProblem problem;
    private boolean foundSolution;

    public SolverThread(GapProblem problem){
        this.problem = problem;
        this.foundSolution = false;
    }

    public GapSolution getSolution(){
        return this.problem.getSolution();
    }

    public boolean foundSolution(){
        return this.foundSolution;
    }

    @Override
    public void run(){
        this.foundSolution = problem.generateGRASPSolution();
        if(foundSolution){
            System.out.println("Found solution.");
        }else{
            System.out.println("Didn't find anything.");
        }
    }

}
