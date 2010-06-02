package gap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;
import java.util.Vector;
import java.util.Comparator;
import java.util.Date;

public class GapProblem {

    private int workersCount;
    private int jobsCount;
    private GapSolution solution;
    private ArrayList<LinkedList<Integer>> jobDomains = new ArrayList<LinkedList<Integer>>();
    int backtracksCount;

    public GapProblem(int _workersCount, int _jobsCount, GapSettings _settings) {
        workersCount = _workersCount;
        jobsCount = _jobsCount;
        solution = new GapSolution(jobsCount, workersCount, _settings);
        fillJobDomains();
        backtracksCount = 0;
    }

    // Comparator for Job class, compares by minTime in descending order
    public static Comparator<Job> JOB_MINTIME_ORDER_DESC = new Comparator<Job>() {

        public int compare(Job j1, Job j2) {
            int minTime1 = j1.getMinTime();
            int minTime2 = j2.getMinTime();
            if (minTime1 < minTime2) {
                return -1;
            } else if (minTime2 < minTime1) {
                return 1;
            } else {
                return 0;
            }
        }
    };

    private void fillJobDomains() {
        jobDomains.clear();
        for (int i = 0; i < jobsCount; i++) {
            jobDomains.add(new LinkedList<Integer>());
            for (int j = 0; j < workersCount; j++) {
                jobDomains.get(i).add(new Integer(j));
            }
        }
    }

    @Override // lame, but it will do for now

    public String toString() {
        return solution.toString();
    }

    public boolean generateRandomSolution() {
        arcConsistency(-1);
        Random generator = new Random();

        for (int i = 0; i < jobsCount; i++) {
            if (!jobDomains.get(i).isEmpty()) {
                int pos = generator.nextInt(jobDomains.get(i).size());
                int worker = jobDomains.get(i).get(pos).intValue();
                solution.assign(i, worker, true);
                jobDomains.get(i).remove(pos);
                arcConsistency(-1); // arc consistency on all not assigned variables

            } else {
                i = i - 1; // unassign previous

                if (i < 0) {
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
    private void arcConsistency(int ommit) {
        arcConsistency(solution, ommit);
    }
    
    
    private void arcConsistency(GapSolution gs, int ommit) {
        GapSettings set = gs.getSettings();
        for (int i = 0; i < jobsCount; i++) {
            if (gs.isAssigned(i) || i == ommit) {
                continue;
            }
            jobDomains.get(i).clear();
            for (int j = 0; j < workersCount; j++) {
                if (set.getLimitTime(j) >= (gs.getWorkerTime(j) + set.getCost(j, i))) {
                    jobDomains.get(i).add(new Integer(j));  // this value can be used!         

                }
            }
        }
    }

    public int getBacktracksCount() {
        return backtracksCount;
    }

    public boolean generateGreedySolution() {
        GapSettings set = solution.getSettings();
        Vector<Job> sortedJobs = new Vector<Job>(jobsCount);
        int minTime, maxTime, bestWorker, time;
        for (int job = 0; job < jobsCount; job++) {
            minTime = -1;
            maxTime = -1;
            bestWorker = -1;

            for (int worker = 0; worker < workersCount; worker++) {
                time = set.getTime(worker, job);
                if (maxTime < time) {
                    maxTime = time;
                }
                if (time < minTime || minTime == -1) {
                    minTime = time;
                    bestWorker = worker;
                }
            }
            sortedJobs.add(job, new Job(job, maxTime - minTime, bestWorker));
        }
        Collections.sort(sortedJobs);
        System.out.println(sortedJobs);
        for (int i = 0; i < jobsCount; i++) {
            Job job = sortedJobs.get(i);
            if (!solution.assign(job.getId(), job.getBestWorkerId())) {
                boolean assigned = false;
                for (int j = 0; j < workersCount; j++) {
                    if (solution.assign(job.getId(), j)) {
                        assigned = true;
                        break;
                    }
                }
                if (!assigned) {
                    System.out.println("Failed to assign job " + job.getId());
                    System.out.println("FIXME: implement backtracking in Greedy solution generation!");
                    return false;
                }
            }
        }

        return true;
    }

    public boolean generatePeckishSolution() {
        return generatePeckishSolution(0.1);
    }

    public boolean generateGRASPSolution() {
        return generateGRASPSolution(10000, .2);
    }
    
    public boolean generateGRASPSolution(int iterations, double rclRatio) {
        Vector<Vector<Worker>> sortedWorkers = sortWorkers();
        GapSolution bestSolution = new GapSolution(jobsCount, workersCount, solution.getSettings());
        for (int i = 0; i < iterations; i++) {
            GapSolution gs = generateInitialSolutionForGrasp(sortedWorkers, rclRatio);
            if (!gs.allAssigned()) {
                System.out.println("No initial solution found.");
                return false;
            } else {
                System.out.println("Initial solution found.");
            }
            gs = localSearch(gs);
            if (gs.getGlobalCost() < bestSolution.getGlobalCost())
                bestSolution = gs;
        }
        solution = bestSolution;
        return true;
    }
    
    private Vector<Vector<Worker>> sortWorkers() {
        GapSettings set = solution.getSettings();
        Vector<Vector<Worker>> w = new Vector<Vector<Worker>>(jobsCount);
        for (int i = 0; i < jobsCount; i++) {
            Vector<Worker> tempWorkers = new Vector<Worker>(workersCount);
            for (int j = 0; j < workersCount; j++) {
                tempWorkers.add(new Worker(j, set.getCost(j,i)));
            }
            if (!tempWorkers.isEmpty()) Collections.sort(tempWorkers);
            w.add(tempWorkers);
        }
        return w;
    }
    
    public GapSolution generateInitialSolutionForGrasp(Vector<Vector<Worker>> sortedWorkers, double rclRatio) {
        GapSolution gs = new GapSolution(jobsCount, workersCount, solution.getSettings());
        Vector<Integer> jobsOrder = new Vector<Integer>(jobsCount);
        for (int i = 0; i < jobsCount; i++) {
            jobsOrder.add(i, i);
        }
        Random generator = new Random();
        for (int i = 0; i < jobsCount; i++) {
            int pos = generator.nextInt(jobsCount - i) + i;
            int tmp = jobsOrder.get(pos);
            jobsOrder.set(pos, jobsOrder.get(i));
            jobsOrder.set(i, tmp);
        }
        fillJobDomains();
        arcConsistency(gs, -1);
        for (int i = 0; i < jobsCount; i++) {
            int jobId = jobsOrder.get(i);
            Vector<Integer> rcl = makeRcl(gs, jobId, sortedWorkers.get(jobId), rclRatio);
            if (rcl.size() != 0) {
                int pos = generator.nextInt(rcl.size());
                gs.assign(jobId, rcl.get(pos));
                int index = jobDomains.get(jobId).indexOf(rcl.get(pos));
                jobDomains.get(jobId).remove(index);
                arcConsistency(gs, -1);
            } else {
                i--;
                gs.unassign(jobId);
                if (i < 0) return gs;
                arcConsistency(gs, jobId);
                backtracksCount++;
                i--;
            }
        }
        return gs;
    }
    
    private Vector<Integer> makeRcl(GapSolution gs, int jobId, Vector<Worker> workers, double ratio)  {
        int tmpRclCard = 0;
        for (int i = 0; i < workers.size(); i++) {
            if (gs.canFeasiblyAssign(jobId, workers.get(i).getWorkerId())) tmpRclCard++;
        }
        int rclCard = (int) (tmpRclCard * ratio);
        if (rclCard == 0 && tmpRclCard > 0) rclCard = 1;
        int rclSize = 0;
        Vector<Integer> rcl = new Vector<Integer>();
        for (int i = 0; i < workers.size() && rclSize < rclCard; i++) {
            if (gs.canFeasiblyAssign(jobId, workers.get(i).getWorkerId())) {
                rcl.add(workers.get(i).getWorkerId());
                rclSize++;
            }
        }
        return rcl;
    }
    
    /* The function generates feasible peckish solution of the problem. Given
     * ratio of jobs (those considered the hardest) is assigned to the best
     * possible worker (if the assignemnt is feasible), the others are assigned
     * randomly. */
    public boolean generatePeckishSolution(double ratio) {
        GapSettings set = solution.getSettings();
        if (ratio < 0.0) {
            ratio = 0.0;
        }
        if (ratio > 1.0) {
            ratio = 0.1;
        }
        int greedyJobs = (int) (ratio * jobsCount);
        // Selected ratio too low, fall back to random
        if (greedyJobs <= 0) {
            System.out.println("Peckish generator: ratio too low, fallback to radnom generation.");
            return generateRandomSolution();
        }

        Vector<Job> sortedJobs = new Vector<Job>(jobsCount);
        int minTime, bestWorker, time;
        minTime = bestWorker = -1;

        // Determine the shortest time needed for completion of each job.
        for (int job = 0; job < jobsCount; job++) {

            for (int worker = 0; worker < workersCount; worker++) {
                time = set.getTime(worker, job);
                if (time < minTime || minTime == -1) {
                    minTime = time;
                    bestWorker = worker;
                }
            }
            sortedJobs.add(job, new Job(job, bestWorker));
            sortedJobs.get(job).setMinTime(minTime);
        }
        // Sort jobs by the minimum time they take to any worker in a descending
        // order.
        Collections.sort(sortedJobs, JOB_MINTIME_ORDER_DESC);
        // Output for a visual check.
        System.out.println(sortedJobs);

        //Create new vector of jobs that shall be assigned randomly
        Vector<Job> randomJobs = new Vector<Job>(sortedJobs);
        // Assign jobs selected for greedy assignment
        for (int i = 0; i < greedyJobs; i++) {
            Job job = sortedJobs.get(i);
            // If possigle, assign job to the best worker and remove the job from
            // randomJobs vector
            if (solution.assign(job.getId(), job.getBestWorkerId())) {
                randomJobs.remove(job);
            }
        }

        // how many times have we run?
        int level = 0;
        Random generator = new Random();
        while (level < 3 && greedyJobs > 5) {
            // Reset job domains
            fillJobDomains();
            // Remove infeasible values on the basis of previous assignments
            arcConsistency(-1);
            // Iterate through randomJobs and attempt to assign them randomly
            for (int i = 0; i < randomJobs.size(); i++) {
                int jobId = randomJobs.get(i).getId();
                if (!jobDomains.get(i).isEmpty()) {
                    int workerPos = generator.nextInt(jobDomains.get(jobId).size());
                    int workderId = jobDomains.get(jobId).get(workerPos).intValue();
                    solution.assign(jobId, workderId);
                    jobDomains.get(jobId).remove(workerPos);
                    arcConsistency(-1);
                } else { //we have no option to assign the job, hence backtracking

                    i--;
                    if (i < 0) {
                        break; //no solution found

                    }
                    solution.unassign(jobId);
                    arcConsistency(jobId);
                    backtracksCount++;
                    i--;
                }
            }
            // If all variables are assigned, we are done.
            if (solution.allAssigned()) {
                return true;
            }
            // Otherwise, start another iteration
            level++;
            int newGreedyJobs = greedyJobs / 2;
            // Move some jobs from greedy assignment to random assigment
            for (int i = newGreedyJobs; i < greedyJobs; i++) {
                // Unassign the job
                solution.unassign(sortedJobs.get(i).getId());
                // Add the job to randomJobs vector
                randomJobs.add(sortedJobs.get(i));
            }
            // Sort the randomJobs vector again
            Collections.sort(randomJobs, JOB_MINTIME_ORDER_DESC);
            greedyJobs = newGreedyJobs;
        }
        // Failed to assign few times, falling back to random
        if (!solution.allAssigned()) {
            System.out.println("Peckish generator: FAILED, fallback to radnom generation.");
            return generateRandomSolution();
        }
        //This should never happen:
        System.out.println("Peckish generator: something went very wrong");
        return true;
    }

    public void clear() {
        solution.clear();
        backtracksCount = 0;
    }

    //shift in solution
    public boolean perturbate() {
        int first_worker = solution.getWorker(0);
        for (int i = 0; i < jobsCount - 1; i++) {
            solution.unassign(i);
            solution.assign(i, solution.getWorker(i + 1), true);
        }
        solution.unassign(jobsCount - 1);
        solution.assign(jobsCount - 1, first_worker, true);
        return solution.isFeasible();
    }

    //change workers posession
    public boolean perturbate2() {

        for (int i = 0; i < jobsCount - 1; i++) {
            int prev_worker = solution.unassign(i);
            solution.assign(i, (prev_worker + 1) % workersCount, true);
        }
        return solution.isFeasible();
    }

    public GapSolution getSolution() {
        return solution;
    }

    public void setSolution(GapSolution _solution) {
        solution = _solution;
    }

    public int getCostLowerBound() {
        GapSettings set = solution.getSettings();
        int minimal_global_cost = 0;
        for (int i = 0; i < jobsCount; i++) {
            int min = Integer.MAX_VALUE;
            for (int j = 0; j < workersCount; j++) {
                if (min > set.getCost(j, i)) {
                    min = set.getCost(j, i);
                }
            }
            minimal_global_cost += min;
        }
        return minimal_global_cost;
    }

    //change one worker to get neighbour
    public GapSolution getBestNeighbour() {
        double bestCost = solution.getPenalty();
        GapSolution bestSolution = new GapSolution(solution, solution.getSettings());
        for (int i = 0; i < jobsCount; i++) {
            GapSolution neighSolution = new GapSolution(solution, solution.getSettings());
            int old_worker = neighSolution.getWorker(i);
            for (int j = 0; j < workersCount; j++) {
                if (j != old_worker) {
                    neighSolution.unassign(i);
                    neighSolution.assign(i, j, true);
                    double cost = neighSolution.getPenalty();
                    if (cost < bestCost) {
                        bestSolution = new GapSolution(neighSolution, solution.getSettings());
                        bestCost = cost;
                    }
                }
            }

        }
        return bestSolution;
    }

    // My take on greedy algrithm with backtracing
    public boolean generateGreediestSolution() {
        GapSettings set = solution.getSettings();
        arcConsistency(-1);
        double[] min_cost = new double[jobsCount];
        int[] jobs = new int[jobsCount];

        for (int i = 0; i < jobsCount; i++) {
            jobs[i] = i;
            int min = Integer.MAX_VALUE;
            for (int j = 0; j < workersCount; j++) {
                if (min > set.getCost(j, i)) {
                    min = set.getCost(j, i);
                    min_cost[i] = min;
                }
            }
        }

        for (int i = 0; i < jobsCount - 1; i++) {
            for (int j = i + 1; j < jobsCount; j++) {
                if (min_cost[i] > min_cost[j]) {
                    double tmp = min_cost[i];
                    min_cost[i] = min_cost[j];
                    min_cost[j] = tmp;
                    tmp = jobs[i];
                    jobs[i] = jobs[j];
                    jobs[j] = (int) tmp;
                }
            }
        }

        for (int i = 0; i < jobsCount; i++) {
            int job = jobs[i];
            if (!jobDomains.get(job).isEmpty()) {
                int min = Integer.MAX_VALUE;
                int best_pos = -1;
                for (int j = 0; j < jobDomains.get(job).size(); j++) {
                    int worker = jobDomains.get(job).get(j).intValue();
                    if (min > set.getCost(worker, job)) {
                        min = set.getCost(worker, job);
                        best_pos = j;
                    }
                }
                int worker = jobDomains.get(job).get(best_pos).intValue();
                solution.assign(job, worker, true);
                jobDomains.get(job).remove(best_pos);
                arcConsistency(-1); // arc consistency on all not assigned variables

            } else {
                i = i - 1; // unassign previous

                if (i < 0) {
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
    
        // with infeasible solution -> hard to get a feasible one :(
    public GapSolution localSearch(GapSolution gs) {
        GapSolution bestSolution = gs;
        GapSettings settings = bestSolution.getSettings();
        int bestCost = bestSolution.getGlobalCost();
        int min_cost = getCostLowerBound();
        int idle_iter = 0;
        while (idle_iter < 1000) { //until we did 1000 perturbations

            GapSolution newSolution = getBestNeighbour();
            if (newSolution.isFeasible() && newSolution.getGlobalCost() < bestCost) {
                bestSolution = new GapSolution(newSolution, settings); //best feasible solution this far

                bestCost = bestSolution.getGlobalCost();
                if (bestCost == min_cost) // when we have found the best cost
                {
                    break;
                }
            }
            if (newSolution.equals(getSolution())) {
                Random generator = new Random(idle_iter); // we can choose between two perturbations

                int random = generator.nextInt(100);
                if (random < 50) {
                    perturbate();  // we are stuck

                } else {
                    perturbate2();
                }
                idle_iter++;
            } else {
                setSolution(newSolution);
            }
        }
        return bestSolution;
    }

}
