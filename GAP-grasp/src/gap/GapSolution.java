package gap;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

/**
 *
 * @author Salla
 */
public class GapSolution {

    private GapProblem problem;
    private int[] assignment; // jobs to workers
    private int jobsCount;
    private int workersCount;
    private int globalCost;
    private int[] workerTotalTime;
    
    public GapSolution(int _jobsCount, int _workersCount, GapProblem _problem){
        problem = _problem;
        jobsCount = _jobsCount;
        assignment = new int[jobsCount];
        for (int i=0; i < jobsCount; i++) {            
            assignment[i] = -1;
        }
        workersCount = _workersCount;
        workerTotalTime = new int[workersCount];
        globalCost = 0;
    }  
    
    public GapSolution(GapSolution solution, GapProblem _problem){
        assignment = solution.getAssignment().clone();
        jobsCount = solution.getJobsCount();
        workersCount = solution.getWorkersCount();
        globalCost = solution.getGlobalCost();
        workerTotalTime = solution.getWorkerTotalTime().clone();
        problem = _problem;
    }
      
    // are all jobs assigned?
    public boolean allAssigned() {
        for (int i=0; i < jobsCount; i++) {
            if (getWorker(i) == -1){
                return false;
            }
        }    
        return true;        
    }
    
    // get first job without worker
    public int getFirstUnassign() {
        for (int i=0; i < jobsCount; i++) {
            if (getWorker(i) == -1){
                return i;
            }
        }    
        return -1;        
    } 
    
    public int getWorker(int job){
        return assignment[job];
    }
    
    public void removeWorker(int job){
        assignment[job] = -1;
    }
       
    public boolean isAssigned(int job){
        if (assignment[job] == -1)
            return false;
        return true;            
    }
    
    public int getWorkerTime(int worker){
        return workerTotalTime[worker];
    }
    
        // assign worker to job
    public boolean assign(int job, int worker) {
        return assign(job, worker, false);
    }
    
    // assign worker to job, are we accepting infeasible solutions?
    public boolean assign(int job, int worker, boolean infeasibility){
        if (isAssigned(job)) // already assigned
            return false;
        
        if (!infeasibility && (getWorkerTime(worker) + problem.getTime(worker, job) ) > problem.getLimitTime(worker)) 
            return false; // we don't want infeasible solutions
        
        assignment[job] = worker;
        workerTotalTime[worker] += problem.getTime(worker, job);
        globalCost += problem.getCost(worker, job);

        return true;
    }
    
    // unassign job
    public int unassign(int job){
        return unassign(job, true);
    }
    
    // unassign job, do we want to update cost and time?
    public int unassign(int job, boolean update){
        int prev_worker = getWorker(job);
        removeWorker(job);
  
        if (update){
            workerTotalTime[prev_worker] -= problem.getTime(prev_worker, job); 
            globalCost -= problem.getCost(prev_worker, job);
        }       
        return prev_worker;
    }
    
    // is the solution feasible?
    public boolean isFeasible() {
        for (int i=0; i < workersCount; i++) {
            if (workerTotalTime[i] > problem.getLimitTime(i))
                return false;
        }
        return true;
    }



    public int[] getAssignment() {
        return assignment;
    }

    public int getJobsCount() {
        return jobsCount;
    }
 

    public int[] getWorkerTotalTime() {
        return workerTotalTime;
    }

    public int getWorkersCount() {
        return workersCount;
    }

    public int overTime(){
        int over = 0;
        for (int i = 0; i < workersCount; i++) {
            if (workerTotalTime[i] > problem.getLimitTime(i))
                over += workerTotalTime[i] - problem.getLimitTime(i);
        }    
        return over;    
    }
            
    public int getGlobalCost(){
        return globalCost;
    }
       
    public int recountCostAndTime(){
        globalCost = 0;
        for (int i=0; i < workersCount; i++)
            workerTotalTime[i] = 0;
            
        for (int i=0; i < jobsCount; i++) {
            int worker = getWorker(i);
            if (isAssigned(i)){
                globalCost += problem.getCost(worker, i);
                workerTotalTime[worker] += problem.getTime(worker, i); 
            }            
        }
        return globalCost;
    }
        
    public void clear(){
      for (int i=0; i < jobsCount; i++) 
            removeWorker(i);
      for (int i=0; i < workersCount; i++) 
           workerTotalTime[i] = 0;
      
      globalCost = 0;    
    }
    
    public int getPenalty(){
        return globalCost + overTime();        
    }

    public boolean equals(GapSolution solution){
        for (int i = 0; i < jobsCount; i++) {
           if (getWorker(i) !=  solution.getWorker(i))
               return false;
        }
            return true;
    }
    
    @Override // lame, but it will do for now
    public String toString(){
        String output = "Solution:\n";
        for (int i=0; i < workersCount; i++) {
            output += "worker " + i + ": items: ";
            for (int j=0; j < jobsCount; j++) {
                if (getWorker(j) == i) {
                    output +=  j + ", ";
                }                   
            }
            output += " total time used: " + getWorkerTime(i) + "/" + problem.getLimitTime(i)+ "\n";
        }
        output += "Total Cost: " + getGlobalCost();
        return output;
    }
    
    public String toSVG(){
        int width = 1300;
        int height = workersCount*110;
        
        String result = "<?xml version=\"1.0\"?>";
        result += "\n<svg width=\"" + width + "\" height=\"" + (height + 300) + "\">";
        result += "\n<desc>GAP solution</desc>";
        result += "\n<g transform=\"translate(50,50)\">";
        
        // axes
        result += "\n<!-- Now Draw the main X and Y axis -->";
        result += "\n<g style=\"stroke-width:5; stroke:black\">";
        result += "\n<!-- X Axis -->";        
        result += "\n<path d=\"M 0 "+ (height +50)+ " L 1000 " + (height + 50) +" Z\"/>";
        result += "\n<!-- Y Axis -->";
        result += "\n<path d=\"M 0 0 L 0 "+(height + 50)+" Z\"/>";
        result += "\n</g>";
        
        int maxTime = workerTotalTime[0];
        for (int i=1; i<workersCount; i++) {
            if (workerTotalTime[i] > maxTime) {
                maxTime = workerTotalTime[i];            
            }
        }
        
        int maxLimit = problem.getLimitTime(0);
        for (int i=1; i<workersCount; i++) {
            if (problem.getLimitTime(i) > maxLimit) {
                maxLimit = problem.getLimitTime(i);            
            }
        }
        
        int lengthCoeff = (maxLimit < maxTime)? maxTime:maxLimit;
        lengthCoeff = 1000/lengthCoeff;
        
        for(int i=0; i< workersCount; i++){
            int x = 3;
            int y = i*110 + 50;
            int length = getWorkerTime(i)* lengthCoeff;            
            result += "\n<rect x=\""+x+"\" y=\""+y+"\" width =\""+ length +"\" height=\""+100+"\" style=\"fill:rgb(74,129,247);\" /> ";
            
            
            int limit = problem.getLimitTime(i)*lengthCoeff;
            if(getWorkerTime(i) < problem.getLimitTime(i)){                
                result += "\n<rect x=\""+(x+length)+"\" y=\""+y+"\" width =\""+ (limit-length) +"\" height=\""+100+"\" style=\"fill:rgb(141,233,355);\" /> ";
            }else if(getWorkerTime(i) != problem.getLimitTime(i)) {                
                result += "\n<rect x=\""+limit+"\" y=\""+y+"\" width =\""+ length +"\" height=\""+100+"\" style=\"fill:rgb(191,0,1);\" /> ";
            }
        }
        result += "\n</g>";
     	result += "\n</svg>";
        return result;
    }
}