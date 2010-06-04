package gap;

import java.util.Random;

/**
 * Class representing a single solution of the GAP problem.
 */
public class GapSolution {

    private int[] assignment; // jobs to workers
    private int jobsCount;
    private int workersCount;
    private int globalCost;
    private int[] workerTotalTime;
    private GapSettings settings;
    
    public GapSolution(int _jobsCount, int _workersCount, GapSettings _settings){
        settings = _settings;
        jobsCount = _jobsCount;
        assignment = new int[jobsCount];
        for (int i=0; i < jobsCount; i++) {            
            assignment[i] = -1;
        }
        workersCount = _workersCount;
        workerTotalTime = new int[workersCount];
        globalCost = 0;
    }  
    
    public GapSolution(GapSolution solution, GapSettings _settings){
        assignment = solution.getAssignment().clone();
        jobsCount = solution.getJobsCount();
        workersCount = solution.getWorkersCount();
        globalCost = solution.getGlobalCost();
        workerTotalTime = solution.getWorkerTotalTime().clone();
        settings = _settings;
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

    /**
     * Return id of worker assigned to the job.
     * @param job Id of the job.
     */
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
    
    /**
     * Assigns job to a worker, don't accept infeasible solution.
     * @param job
     * @param worker
     * @return True if job was actually assigned, false otherwise.
     */
    public boolean assign(int job, int worker) {
        return assign(job, worker, false);
    }
    
    /**
     * Assiign job to a worker.
     * @param job
     * @param worker
     * @param infeasibility Determines whether infeasible assignment should be
     *  accepted or not.
     * @return True if job was actually assigned, false otherwise.
     */
    public boolean assign(int job, int worker, boolean infeasibility){
        if (isAssigned(job)) // already assigned
            return false;
        
        if (!infeasibility && !canFeasiblyAssign(job, worker))
            return false; // we don't want infeasible solutions
        
        assignment[job] = worker;
        workerTotalTime[worker] += settings.getTime(worker, job);
        globalCost += settings.getCost(worker, job);

        return true;
    }
    
    /**
     * Determines whether the job can be feasibly assigned to the specified worker or not.
     * @param job
     * @param worker
     * @return True if the job can be feasibly assigne, false otherwise.
     */
    public boolean canFeasiblyAssign(int job, int worker) {
        if ((getWorkerTime(worker) + settings.getTime(worker, job) ) > settings.getLimitTime(worker)) {
            return false;
        }
        return true;
    }

    /**
     * Unassign the job from a worker and recompute the worker's total time and
     * total cost of the solution.
     * @param job
     * @return Id of the unassigned worker.
     */
    public int unassign(int job){
        return unassign(job, true);
    }
    
    /**
     * Unassign the job from a worker.
     * @param job
     * @param update True if the worker's total time and total cost of the solution
     *   should be recomputed, false otherwise.
     * @return Id of the unassigned worker.
     */
    public int unassign(int job, boolean update){
        int prev_worker = getWorker(job);
        removeWorker(job);
  
        if (update){
            workerTotalTime[prev_worker] -= settings.getTime(prev_worker, job); 
            globalCost -= settings.getCost(prev_worker, job);
        }       
        return prev_worker;
    }
    
    /**
     * Determines whether the solution is feasible or not.
     * @return True if the solution is feasible, false otherwise.
     */
    public boolean isFeasible() {
        for (int i=0; i < workersCount; i++) {
            if (workerTotalTime[i] > settings.getLimitTime(i))
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

    /**
     * Calculate the ratio between time overdues of an infeasible solution and
     * sum of the time limits of all workers.
     * @return The ratio
     */
    public double overTime(){
        double over = 0;
        int global_time = 0;
        for (int i = 0; i < workersCount; i++) {
             if (workerTotalTime[i] > settings.getLimitTime(i))
                over += workerTotalTime[i] - settings.getLimitTime(i);
             global_time += settings.getLimitTime(i);
        }
        if (global_time != 0)
            return over / global_time;
        else return 0;
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
                globalCost += settings.getCost(worker, i);
                workerTotalTime[worker] += settings.getTime(worker, i); 
            }            
        }
        return globalCost;
    }

    /**
     * Clear the solution, i.e., unassign all workers and reset their times;
     */
    public void clear(){
      for (int i=0; i < jobsCount; i++) 
            removeWorker(i);
      for (int i=0; i < workersCount; i++) 
           workerTotalTime[i] = 0;
      
      globalCost = 0;    
    }
    
    /**
     * Calculate penalty of the infeasible solution based on global cost and time
     * overdues.
     * @return Value of the penalty.
     */
    public double getPenalty(){
       return globalCost*(1 + overTime());
    }

    /**
     * Compares if two solutions are equal in terms of worker assignment.
     * @param solution
     * @return
     */
    public boolean equals(GapSolution solution){
        for (int i = 0; i < jobsCount; i++) {
           if (getWorker(i) !=  solution.getWorker(i))
               return false;
        }
            return true;
    }

    /**
     * Swap jobs of two workers.
     * @param id1
     * @param id2
     * @return True if resulting assignment is feasible, false otherwise.
     */
    public boolean swapWorkers(int id1, int id2) {
        for (int i = 0; i < jobsCount; i++) {
            if (assignment[i] == id1) {
                moveJob(id2, i);
                continue;
            }
            if (assignment[i] == id2) {
                moveJob(id1, i);
                continue;
            }
        }
        return isFeasible();
    }
    
    /**
     * Move the given job to the given worker and update the times and costs.
     * @param worker
     * @param job
     * @return True if the resulting assignment is feasible, false otherwise.
     */
    public boolean moveJob(int worker, int job) {
        return moveJob(worker, job, true);
    }
    
    /**
     * Move the given job to the worker.
     * @param worker
     * @param job
     * @param update True if times and costs shall be updated, false otherwise.
     * @return True if the resulting assignment is feasible, false otherwise.
     */
    public boolean moveJob(int worker, int job, boolean update) {
        unassign(job, update);
        assignment[job] = worker;
        if (update) {
            workerTotalTime[worker] += settings.getTime(worker, job);
            globalCost += settings.getCost(worker, job);
        }
        return workerTotalTime[worker] <= settings.getLimitTime(worker);
    }
    /**
     * Output the solution as simple table displaying worker/job assignments,
     * time required for each worker and global cost of the solution.
     */
    @Override
    public String toString(){
        String output = "Solution:\n";
        for (int i=0; i < workersCount; i++) {
            output += "worker " + (i+1) + ": items: ";
            for (int j=0; j < jobsCount; j++) {
                if (getWorker(j) == i) {
                    output +=  (j+1) + ", ";
                }                   
            }
            output += " total time used: " + getWorkerTime(i) + "/" + settings.getLimitTime(i)+ "\n";
        }
        output += "Total Cost: " + getGlobalCost();
        return output;
    }
    
    /**
     * Output a graphical representation of the solution in the SVG format.
     * @return Text that should be written to .svg file.
     */
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
        
        int maxLimit = settings.getLimitTime(0);
        for (int i=1; i<workersCount; i++) {
            if (settings.getLimitTime(i) > maxLimit) {
                maxLimit = settings.getLimitTime(i);            
            }
        }
        
        int lengthCoeff = (maxLimit < maxTime)? maxTime:maxLimit;
        lengthCoeff = 1000/lengthCoeff;
        
        for(int i=0; i< workersCount; i++){
            int x = 3;
            int y = i*110 + 50;
            int length = getWorkerTime(i)* lengthCoeff;            
            result += "\n<rect x=\""+x+"\" y=\""+y+"\" width =\""+ length +"\" height=\""+100+"\" style=\"fill:rgb(74,129,247);\" /> ";
            
            
            int limit = settings.getLimitTime(i)*lengthCoeff;
            if(getWorkerTime(i) < settings.getLimitTime(i)){                
                result += "\n<rect x=\""+(x+length)+"\" y=\""+y+"\" width =\""+ (limit-length) +"\" height=\""+100+"\" style=\"fill:rgb(141,233,355);\" /> ";
            }else if(getWorkerTime(i) != settings.getLimitTime(i)) {                
                result += "\n<rect x=\""+limit+"\" y=\""+y+"\" width =\""+ length +"\" height=\""+100+"\" style=\"fill:rgb(191,0,1);\" /> ";
            }
        }
        result += "\n</g>";
     	result += "\n</svg>";
        return result;
    }
    
    public GapSettings getSettings(){
        return settings;
    }
    
    public boolean perturb() {
        int perturbOptions = 2;
        Random generator = new Random();
        int perturbId = generator.nextInt(perturbOptions);
        switch (perturbOptions) {
            case 0: perturbNextJobWorkers();
                    break;
            case 1: perturbNextWorker();
                    break;
            default: break;
        }
        return isFeasible();
    }
    
    //shift in solution
    public boolean perturbNextJobWorkers() {
        int first_worker = getWorker(0);
        for (int i = 0; i < jobsCount - 1; i++) {
            unassign(i);
            assign(i, getWorker(i + 1), true);
        }
        unassign(jobsCount - 1);
        assign(jobsCount - 1, first_worker, true);
        return isFeasible();
    }

    //change workers posession
    public boolean perturbNextWorker() {

        for (int i = 0; i < jobsCount; i++) {
            int prev_worker = unassign(i);
            assign(i, (prev_worker + 1) % workersCount, true);
        }
        return isFeasible();
    }
}
