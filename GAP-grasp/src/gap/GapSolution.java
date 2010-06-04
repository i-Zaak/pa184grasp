package gap;

/**
 *
 * @author Salla
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
        
        if (!infeasibility && !canFeasiblyAssign(job, worker))
            return false; // we don't want infeasible solutions
        
        assignment[job] = worker;
        workerTotalTime[worker] += settings.getTime(worker, job);
        globalCost += settings.getCost(worker, job);

        return true;
    }
    
    public boolean canFeasiblyAssign(int job, int worker) {
        if ((getWorkerTime(worker) + settings.getTime(worker, job) ) > settings.getLimitTime(worker)) {
            return false;
        }
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
            workerTotalTime[prev_worker] -= settings.getTime(prev_worker, job); 
            globalCost -= settings.getCost(prev_worker, job);
        }       
        return prev_worker;
    }
    
    // is the solution feasible?
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

    public double overTime(){
        double over = 0;
        int global_time = 0;
        for (int i = 0; i < workersCount; i++) {
             if (workerTotalTime[i] > settings.getLimitTime(i))
                over += workerTotalTime[i] - settings.getLimitTime(i);
             global_time += settings.getLimitTime(i);
        }    
        over = over/global_time;
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
                globalCost += settings.getCost(worker, i);
                workerTotalTime[worker] += settings.getTime(worker, i); 
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
    
    public double getPenalty(){
       return globalCost*(1 + overTime());
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
}
