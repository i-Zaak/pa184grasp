package gap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;
import java.util.Vector;

public class GapProblem {
    
    private int workersCount;
    private int jobsCount;  
    private GapSolution solution;
    private int[] workerLimitTime;
    private int[][] workerJobCost;
    private int[][] workerJobTime;
    private ArrayList<LinkedList<Integer>> jobDomains = new ArrayList<LinkedList<Integer>>(); 
    int backtracksCount;
    
    public GapProblem(int _workersCount, int _jobsCount, int[][] _workerJobCost, int[][] _workerJobTime, int [] _workerLimitTime){
        workersCount = _workersCount;
        jobsCount = _jobsCount;
        workerJobCost = _workerJobCost.clone();
        workerJobTime = _workerJobTime.clone();
        workerLimitTime = _workerLimitTime.clone();
        solution = new GapSolution(jobsCount, workersCount,this);
        for (int i=0; i < jobsCount; i++) {            
            
            jobDomains.add(new LinkedList<Integer>());
            for (int j=0; j < workersCount; j++) {
                // domains
                jobDomains.get(i).add(new Integer(j));
            }
        }
        backtracksCount = 0;
    }
    
    @Override // lame, but it will do for now
    public String toString(){
        return solution.toString();        
    }
     
    
    
    public int getTime(int worker, int job){
        return workerJobTime[worker][job];
    }
    
    public int getCost(int worker, int job){
        return workerJobCost[worker][job];
    }
    
    public int getLimitTime(int worker){
        return workerLimitTime[worker];
    }
       
    public boolean generateRandomSolution(){
        arcConsistency(-1); 
        Random generator = new Random();
        
        for (int i = 0; i < jobsCount; i++){
            if(!jobDomains.get(i).isEmpty()){
                int pos = generator.nextInt(jobDomains.get(i).size());
                int worker = jobDomains.get(i).get(pos).intValue();
                solution.assign(i,worker,true);
                jobDomains.get(i).remove(pos);
                arcConsistency(-1); // arc consistency on all not assigned variables
            } else{
                i = i - 1; // unassign previous
                if (i < 0){
                    return false; // no solution
                }
                backtracksCount++;
                solution.unassign(i);
                arcConsistency(i);   
                i = i - 1; // just step back in for cycle to get to the unassign variable       
            }
       }
       return true; 
    }  
    //the main idea is to clear infeasible values from not assigned variables except of the ommited one = node we are backtracking to
    private void arcConsistency(int ommit){
        for (int i=0; i < jobsCount; i++) {
            if (solution.isAssigned(i) || i == ommit)
                continue;
            jobDomains.get(i).clear();
            for (int j=0; j < workersCount; j++) {
               if (workerLimitTime[j] >= (solution.getWorkerTime(j) + workerJobTime[j][i]))
                    jobDomains.get(i).add(new Integer(j));  // this value can be used!         
            }
        }        
    }
    
    public int getBacktracksCount(){
        return backtracksCount;
    }
       
    public boolean generateGreedySolution(){
        Vector<Job> sortedJobs = new Vector<Job>(jobsCount);
        int minTime, maxTime, bestWorker, time;
        for(int job=0; job < jobsCount; job++){
            minTime = -1;
            maxTime = -1;
            bestWorker = -1;
            
            for(int worker=0; worker < workersCount; worker++){
                time = getTime(worker,job);
                if(maxTime < time){
                    maxTime = time;                    
                }
                if(time < minTime || minTime == -1){
                    minTime = time;
                    bestWorker = worker;
                }
            }
            sortedJobs.add(job, new Job(job, maxTime - minTime, bestWorker));
        }
        Collections.sort(sortedJobs);
        System.out.println(sortedJobs);
        for(int i=0; i < jobsCount; i++){
            Job job = sortedJobs.get(i);
            if(!solution.assign(job.getId(),job.getBestWorkerId())){
                boolean assigned = false;
                for(int j=0; j < workersCount; j++){
                    if(solution.assign(job.getId(),j)){
                        assigned = true;
                        break;
                    }
                }
                if(!assigned){
                    System.out.println("Failed to assign job " + job.getId());
                    System.out.println("FIXME: implement backtracking in Greedy solution generation!");
                    return false; 
                }
            }
        }
        
        return true;
    }

    public void clear(){
      solution.clear();  
      backtracksCount = 0;   
    }
    
    //shift in solution
    public boolean perturbate(){
        int first_worker = solution.getWorker(0);
        for (int i = 0; i < jobsCount -1; i++){
            solution.unassign(i);
            solution.assign(i, solution.getWorker(i+1), true);            
        }
        solution.unassign(jobsCount -1);
        solution.assign(jobsCount -1, first_worker, true);
        return solution.isFeasible();           
    }
    
        //change workers posession
    public boolean perturbate2(){
        
        for (int i = 0; i < jobsCount -1; i++){
            int prev_worker = solution.unassign(i);
            solution.assign(i, (prev_worker+1) % workersCount, true);            
        }
        return solution.isFeasible();           
    }
    
    
    
    public GapSolution getSolution(){
        return solution;
    }
    
    public void setSolution(GapSolution _solution){
        solution = _solution;
    }
    
    public int getCostLowerBound(){
        int minimal_global_cost = 0;
        for (int i = 0; i < jobsCount; i++) {
            int min = Integer.MAX_VALUE;
            for (int j = 0; j < workersCount; j++){
                if (min > workerJobCost[j][i]){
                    min = workerJobCost[j][i];
                }
            }
            minimal_global_cost += min;
        }      
        return minimal_global_cost;
    }
    
    //change one worker to get neighbour
    public GapSolution getBestNeighbour(){
        int bestCost = solution.getPenalty();
        GapSolution bestSolution = new GapSolution(solution,this);
        for (int i = 0; i < jobsCount; i++){
            GapSolution neighSolution = new GapSolution(solution,this);
            int old_worker = neighSolution.getWorker(i);
            for (int j = 0; j < workersCount; j++){
                if (j != old_worker){
                    neighSolution.unassign(i);
                    neighSolution.assign(i, j, true);
                    int cost = neighSolution.getPenalty();
                    if (cost < bestCost) {
                        bestSolution = new GapSolution(neighSolution,this);
                        bestCost = cost;
                    }
                }
            }
            
        }  
        return bestSolution;
    }
    
    // My take on greedy algrithm with backtracing
        public boolean generateGreediestSolution(){
        arcConsistency(-1); 
        double[] min_cost = new double[jobsCount];
        int[] jobs = new int[jobsCount];
        
        for (int i = 0; i < jobsCount; i++){
            jobs[i] = i;
            int min = Integer.MAX_VALUE;
            for (int j = 0; j < workersCount; j++){
                if (min > workerJobCost[j][i]){
                    min = workerJobCost[j][i];
                    min_cost[i] = min;                  
                }
            }    
        }

        for (int i = 0; i < jobsCount - 1; i++){
            for (int j = i + 1; j < jobsCount; j++){
                 if (min_cost[i] > min_cost[j]){
                    double tmp = min_cost[i];
                    min_cost[i] = min_cost[j];
                    min_cost[j] = tmp;
                    tmp = jobs[i];
                    jobs[i] = jobs[j];
                    jobs[j] = (int) tmp;
                }
            }
        }
        
           for (int i = 0; i < jobsCount; i++){
            int job = jobs[i];
            if(!jobDomains.get(job).isEmpty()){
               int min = Integer.MAX_VALUE; 
               int best_pos = -1;
               for (int j = 0; j < jobDomains.get(job).size(); j++){
                   int worker = jobDomains.get(job).get(j).intValue();
                   if (min > workerJobCost[worker][job]){
                        min = workerJobCost[worker][job];
                        best_pos = j; 
                    }
               } 
               int worker = jobDomains.get(job).get(best_pos).intValue();
                solution.assign(job,worker,true);
                jobDomains.get(job).remove(best_pos);
                arcConsistency(-1); // arc consistency on all not assigned variables
            } else{
                i = i - 1; // unassign previous
                if (i < 0){
                    return false; // no solution
                }
                backtracksCount++;
                solution.unassign(jobs[i]);
                arcConsistency(jobs[i]);   
                i = i - 1; // just step back in for cycle to get to the unassign variable       
            }
       }
       return true; 
    } 
    
    
    
    
    
    
    
    
    
    
    
    }
