/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gap;


public class GapProblem {
    
    private int workersCount;
    private int jobsCount;
    private int cost;
    private int[] assignment;
    private int[] workerTotalTime;
    private int[] workerLimitTime;
    private int[][] workerJobCost;
    private int[][] workerJobTime;
    
    public GapProblem(int _workersCount, int _jobsCount){
        workersCount = _workersCount;
        jobsCount = _jobsCount;
        assignment = new int[jobsCount];
        for (int i=0; i < jobsCount; i++)
            assignment[i] = -1;
        workerTotalTime = new int[workersCount];
        workerLimitTime = new int[workersCount];
        workerJobCost = new int[workersCount][jobsCount];
        workerJobTime = new int[workersCount][jobsCount];
        cost = 0;
    }
    // assign worker to job
    public boolean assign(int job, int worker) {
        return assign(job, worker, false);
    }
    
    // assign worker to job, are we accepting infeasible solutions?
    public boolean assign(int job, int worker, boolean infeasibility){
        if (assignment[job] != -1) // already assigned
            return false;
        
        if (!infeasibility && (workerTotalTime[worker] + workerJobTime[worker][job]) > workerLimitTime[worker]) 
            return false; // we don't want infeasible solutions
        
        assignment[job] = worker;
        workerTotalTime[worker] += workerJobTime[worker][job];
        cost += workerJobCost[worker][job];
                    
        return true;
    }
    
    // unassign job
    public int unassign(int job){
        return unassign(job, true);
    }
    
    // unassign job, do we want to update cost and time?
    public int unassign(int job, boolean update){
        int prev_worker = assignment[job];
        assignment[job] = -1;
        
        if (update){
            workerTotalTime[prev_worker] -= workerJobTime[prev_worker][job]; 
            cost -= workerJobCost[prev_worker][job];
        }
        return prev_worker;
    }
    
    // is the solution feasible?
    public boolean feasible() {
        for (int i=0; i < workersCount; i++) {
            if (workerTotalTime[i] > workerLimitTime[i])
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
                if (assignment[j] == i) {
                    output +=  j + ", ";
                }                   
            }
            output += " total time used: " + workerTotalTime[i] + "/" + workerLimitTime[i] + "\n";
        }
        output += "Total Cost: " + cost;
        return output;
    }
            
    public int getCost(){
        return cost;
    }
    
    public int recountCostAndTime(){
        cost = 0;
        for (int i=0; i < workersCount; i++)
            workerTotalTime[i] = 0;
            
        for (int i=0; i < jobsCount; i++) {
            int worker = assignment[i];
            if (worker != -1){
                cost += workerJobCost[worker][i];
                workerTotalTime[worker] += workerJobTime[worker][i]; 
            }            
        }
        return cost;
    }
    
    // are all jobs assigned?
    public boolean allAssigned() {
        for (int i=0; i < jobsCount; i++) {
            if (assignment[i] == -1){
                return false;
            }
        }    
        return true;        
    }
    
    // get first job without worker
    public int getFirstUnassign() {
        for (int i=0; i < jobsCount; i++) {
            if (assignment[i] == -1){
                return i;
            }
        }    
        return -1;        
    }   
    
    public void setWorkerJobCost(int worker, int job, int _cost){
        workerJobCost[worker][job] = _cost;
    }
    
    public void setWorkerJobTime(int worker, int job, int _time){
        workerJobTime[worker][job] = _time;
    }
    
    public void setWorkerLimitTime(int worker, int _limit){
        workerLimitTime[worker] = _limit;
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
}  


