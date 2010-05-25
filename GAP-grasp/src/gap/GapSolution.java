package gap;

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
}
