package gap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;
import java.util.Vector;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GapProblem {

    /** Number of workers in the problem */
    private int workersCount;
    /** Number of jobs in the problem */
    private int jobsCount;
    /** Solution of the problem, used by Main class */
    private GapSolution solution;
    /** Domains of feasible assignments of workers to jobs.*/
    private ArrayList<LinkedList<Integer>> jobDomains = new ArrayList<LinkedList<Integer>>();
    /** Statistic of backtrack steps needed to solve the problem */
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
                return 1;
            } else if (minTime2 < minTime1) {
                return -1;
            } else {
                int maxTime1 = j1.getMaxTime();
                int maxTime2 = j2.getMaxTime();
                if (maxTime1 < maxTime2) {
                    return 1;
                } else if (maxTime2 < maxTime1) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }
    };

    private GapProblem(GapProblem problem) {
        this(problem.workersCount, problem.jobsCount, problem.getSolution().getSettings());
    }

    /**
     * Reset the jom domains structure to contain all workers for all jobs.
     */
    private void fillJobDomains() {
        jobDomains.clear();
        for (int i = 0; i < jobsCount; i++) {
            jobDomains.add(new LinkedList<Integer>());
            for (int j = 0; j < workersCount; j++) {
                jobDomains.get(i).add(new Integer(j));
            }
        }
    }

    @Override
    public String toString() {
        return solution.toString();
    }

    /**
     * Random generator of feasible problem solutions. For each job, worker is assigned
     * randomly from the job domain (i.e., list of workers that can be assigned feasibly.
     * If any job domain is empty, it backtracks until any assignemnt is available. If there
     * is not any, failure is reported.
     * @return
     */
    public boolean generateRandomSolution() {
        /** Update job domains (i.e., fill them completely in the beginning). */
        updateJobDomains(-1);
        Random generator = new Random();
        for (int i = 0; i < jobsCount; i++) {
            if (!jobDomains.get(i).isEmpty()) { // There is something in the job domain
                /** Select value form the job domain */
                int pos = generator.nextInt(jobDomains.get(i).size());
                /** Determine id of the worker from the domain */
                int worker = jobDomains.get(i).get(pos).intValue();
                solution.assign(i, worker, true);
                /** Remove the worker from the domain. In this subtree, this worker
                 * will not be assigned to the job again. */
                jobDomains.get(i).remove(pos);
                updateJobDomains(-1); // arc consistency on all not assigned variables                
            } else { // No workers found in the job domain
                i = i - 1; // Unassign the previously assigned job.
                if (i < 0) { // We are on the top of the tree - no solution found, return failure.
                    return false;
                }
                backtracksCount++;
                /** Unassigne the previously assigned job and update the domains. The
                 * previously assigned job is omitted from the update, as the domain would
                 * otherwise get filled with previously eliminated values.*/
                solution.unassign(i);
                updateJobDomains(i);
                i = i - 1; // Step back in the cycle
            }
        }
        return true;
    }

    /**
     * Clear infeasible values from the domains of unassigned jobs. One job might need to be omitted.
     * @param ommit Id of the ommited job.
     */
    private void updateJobDomains(int ommit) {
        updateJobDomains(solution, ommit);
    }

    private void updateJobDomains(GapSolution gs, int ommit) {
        GapSettings set = gs.getSettings();
        for (int i = 0; i < jobsCount; i++) {
            /** We do not want to mangle with assigned variables, as it would break
             * the backtracking. */
            if (gs.isAssigned(i) || i == ommit) {
                continue;
            }
            /** Clear the domain from all values. */
            jobDomains.get(i).clear();
            for (int j = 0; j < workersCount; j++) {
                /** Add only the feasible values */
                if (gs.canFeasiblyAssign(i, j)) {
                    jobDomains.get(i).add(new Integer(j));  // this value can be used!         

                }
            }
        }
    }

    public int getBacktracksCount() {
        return backtracksCount;
    }

    /**
     * Generate solution greedy for time - for each job, select the worker, which
     * can complete the job in the shortest time. This method is capable of finding
     * a feasible solution very quickly.
     * @return True if a feasible solution was found, false otherwise.
     */
    public boolean generateTimeGreedySolution() {
        GapSettings set = solution.getSettings();
        Vector<Job> sortedJobs = new Vector<Job>(jobsCount);
        int minTime, maxTime, bestWorker, time;
        /** Find minimum and maximum times needed for completion of each job.*/
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
        /** Sort the jobs by the difference between minimum and maximum time in
         * descending order.*/
        Collections.sort(sortedJobs);
        /** Reset the job domains.*/
        fillJobDomains();
        updateJobDomains(-1);
        for (int i = 0; i < jobsCount; i++) {
            Job job = sortedJobs.get(i);
            if (!jobDomains.get(job.getId()).isEmpty()) {
                // There is pretty good chance to get a deterministic asignment; try
                // to get the best worker available.
                if (jobDomains.get(job.getId()).contains(job.getBestWorkerId())) {
                    solution.assign(job.getId(), job.getBestWorkerId());
                    int pos = jobDomains.get(job.getId()).indexOf(job.getBestWorkerId());
                    jobDomains.get(job.getId()).remove(pos);
                } else {
                    int min = Integer.MAX_VALUE;
                    int position = -1;
                    for (int j = 0; j < jobDomains.get(job.getId()).size(); j++) {
                        if (min > set.getTime(jobDomains.get(job.getId()).get(j), job.getId())) {
                            min = set.getTime(jobDomains.get(job.getId()).get(j), job.getId());
                            position = j;
                        }
                    }
                    solution.assign(job.getId(), jobDomains.get(job.getId()).get(position));
                    jobDomains.get(job.getId()).remove(position);
                }
                updateJobDomains(-1);
            } else { //Something went wrong, we have to backtrack.
                i--;
                if (i < 0) {
                    return false;
                }
                int jobId = sortedJobs.get(i).getId();
                solution.unassign(jobId);
                updateJobDomains(jobId);
                backtracksCount++;
                i--;
            }
        }

        return true;
    }

    /**
     *  Call GRASP metaheuristics with the default parameters.
     * @return True if there was feasible solution found, false otherwise.
     */
    public boolean generateGRASPSolution() {
        int numIterations = Math.max(jobsCount, 50);
        return generateGRASPSolution(numIterations, .5);
    }

    /**
     * GRASP metaheuristics method. Initial solutions are generated by a special
     * method and local search is further applied. There is a dynamic adaptive limit
     * for number of backtracks in each iteration.
     * @param iterations Maximum number of iterations, i.e., initial solution generations
     *  and local searches.
     * @param rclRatio Size of RCL compared to number of the available values.
     * @return True if there was feasible solution found, false otherwise.
     */
    public boolean generateGRASPSolution(int iterations, double rclRatio) {
        Vector<Vector<Worker>> sortedWorkers = sortWorkers();
        GapSolution bestSolution = new GapSolution(jobsCount, workersCount, solution.getSettings());
        int bestCost = Integer.MAX_VALUE;
        /** If there are too many failed solution generations, we force the backtrack to search
         * whole tree. This ensures that infeasible assignments are detected correctly. The
         * variable is set to false in the beginning and optionally switched to true later.*/
        boolean forceBacktrack = false;
        /** Maximum number of backtracks allowed in the solution generator.*/
        int maxBacktracks = 5000;
        /** How many runs of solution generator are allowed to fail before we force
         * the backtracking to finish.*/
        double maxFailedIterRatio = 0.8;
        int failedIterations = 0;

        for (int i = 0; i < iterations; i++) {
            /** Generate the initial solution. */
            GapSolution gs = generateInitialSolutionForGrasp(sortedWorkers, rclRatio, forceBacktrack, maxBacktracks);
            if (!gs.allAssigned()) { // Solution generator failed to find a feasible solution.
                if (forceBacktrack) {
                    return false;
                }
                failedIterations++;
                maxBacktracks *= 2;
                if (failedIterations > maxFailedIterRatio * iterations) { //Too many iterations have failed
                    forceBacktrack = true;
                    System.out.println("GRASP: Too many failed iterations, forcing full backtracking.");
                }
                continue; // Do not perform local search
            } else {
                maxBacktracks *= 0.8;
            }
            /** Perform the local search on the generated solution */
            gs = new GapSolution(localSearch(gs), gs.getSettings());
            if (gs.getGlobalCost() < bestCost) { // We found the best solution so far
                bestSolution = new GapSolution(gs, gs.getSettings());
                bestCost = gs.getGlobalCost();
            }
        }
        solution = new GapSolution(bestSolution, bestSolution.getSettings());
        return true;
    }

    /**
     * For each job, sort the workers by time needed for them to complete the job.
     * @return Two dimensional array, indexed by job id first and worker id second.
     */
    private Vector<Vector<Worker>> sortWorkers() {
        GapSettings set = solution.getSettings();
        Vector<Vector<Worker>> w = new Vector<Vector<Worker>>(jobsCount);
        for (int i = 0; i < jobsCount; i++) {
            Vector<Worker> tempWorkers = new Vector<Worker>(workersCount);
            for (int j = 0; j < workersCount; j++) {
                tempWorkers.add(new Worker(j, set.getTime(j, i)));
            }
            if (!tempWorkers.isEmpty()) {
                Collections.sort(tempWorkers);
            }
            w.add(tempWorkers);
        }
        return w;
    }

    /**
     * Generate initial solution for each GRASP iteration. Local search is then performed
     * on the generated solution. The method iterates over jobs, which are sorted by their
     * delta (difference between minimum and maximum time over all workers). For each job,
     * workers are sorted in ascending order by their time needed to complete the job. RCLs
     * are generated with respect to this sorting.
     * @param sortedWorkers
     * @param rclRatio
     * @param forceBacktrack
     * @param maxBacktracks
     * @return Generated solution.
     */
    public GapSolution generateInitialSolutionForGrasp(Vector<Vector<Worker>> sortedWorkers, double rclRatio, boolean forceBacktrack, int maxBacktracks) {
        GapSolution gs = new GapSolution(jobsCount, workersCount, solution.getSettings());
        Vector<Job> jobsOrder = new Vector<Job>(jobsCount);
        for (int job = 0; job < jobsCount; job++) {
            int minTime = Integer.MAX_VALUE;
            int maxTime = Integer.MIN_VALUE;
            int time;
            int bestWorker = -1;
            for (int worker = 0; worker < workersCount; worker++) {
                time = gs.getSettings().getTime(worker, job);
                if (time < minTime || minTime == -1) {
                    minTime = time;
                    bestWorker = worker;
                }
                if (time > maxTime) {
                    maxTime = time;
                }
            }
            jobsOrder.add(job, new Job(job, maxTime - minTime, bestWorker));
        }
        // Sort jobs by the delta.
        Collections.sort(jobsOrder);
        Random generator = new Random();

        /** Mangle the job list slightly - randomly move the jobs a little.
         * This might help variability a little.*/
        for (int i = 0; i < jobsCount; i++) {
            int pos = generator.nextInt(Math.min(jobsCount - i, 2)) + i;
            Job tmp = jobsOrder.get(pos);
            jobsOrder.set(pos, jobsOrder.get(i));
            jobsOrder.set(i, tmp);
        }

        /** Traditional search wich backtracking similar to other solution generators.*/
        fillJobDomains();
        updateJobDomains(gs, -1);
        int backtracks = 0;
        for (int i = 0; i < jobsCount; i++) {
            int jobId = jobsOrder.get(i).getId();
            Vector<Integer> rcl = makeRcl(gs, jobId, sortedWorkers.get(jobId), rclRatio);
            if (rcl.size() != 0) {
                int pos = generator.nextInt(rcl.size());
                gs.assign(jobId, rcl.get(pos).intValue());
                int index = jobDomains.get(jobId).indexOf(rcl.get(pos));
                jobDomains.get(jobId).remove(index);
                updateJobDomains(gs, -1);
            } else {
                if (!forceBacktrack && backtracks > maxBacktracks) {
                    return gs;
                }
                i--;
                if (i < 0) {
                    return gs;
                }
                jobId = jobsOrder.get(i).getId();
                gs.unassign(jobId);
                updateJobDomains(gs, jobId);
                backtracks++;
                i--;
            }
        }
        backtracksCount += backtracks;
        return gs;
    }

    /**
     * Build RCL list for GRASP solution generator. The RCL is built from the list
     * of workers available for the job, where the workers with shortest time needed
     * are placed first. Only feasible values are added to the RCL.
     * @param jobId Id of the job
     * @param workers Sorted list of the workers
     * @param ratio Ratio between size of the RCL and size of workers vector
     * @return Generated RCL.
     */
    private Vector<Integer> makeRcl(GapSolution gs, int jobId, Vector<Worker> workers, double ratio) {
        int tmpRclCard = 0;
        int rclCard = (int) (workers.size() * ratio);
        if (rclCard == 0) {
            rclCard = 1;
        }
        int rclSize = 0;
        Vector<Integer> rcl = new Vector<Integer>();
        for (int i = 0; i < workers.size() && rclSize < rclCard; i++) {
            if (gs.canFeasiblyAssign(jobId, workers.get(i).getWorkerId())
                    && jobDomains.get(jobId).contains(workers.get(i).getWorkerId())) {
                rcl.add(workers.get(i).getWorkerId());
                rclSize++;
            }
        }
        return rcl;
    }

    /**
     * Call the peckish solution generator with default parameters.
     * @return
     */
    public boolean generatePeckishSolution() {
        return generatePeckishSolution(0.1);
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
        int greedyJobs = Math.max((int) (ratio * jobsCount), 5);
        greedyJobs = Math.min(greedyJobs, jobsCount);
        // Selected ratio too low, fall back to random
        if (greedyJobs <= 0) {
            System.out.println("Peckish generator: ratio too low, fallback to radnom generation.");
            return generateRandomSolution();
        }

        Vector<Job> sortedJobs = new Vector<Job>(jobsCount);
        int minTime, bestWorker, time;
        minTime = bestWorker = -1;
        int maxTime;

        // Determine the shortest time needed for completion of each job.
        for (int job = 0; job < jobsCount; job++) {
            minTime = Integer.MAX_VALUE;
            maxTime = Integer.MIN_VALUE;
            for (int worker = 0; worker < workersCount; worker++) {
                time = set.getTime(worker, job);
                if (time < minTime || minTime == -1) {
                    minTime = time;
                    bestWorker = worker;
                }
                if (time > maxTime) {
                    maxTime = time;
                }
            }
            sortedJobs.add(job, new Job(job, bestWorker));
            sortedJobs.get(job).setMinTime(minTime);
            sortedJobs.get(job).setMaxTime(maxTime);
        }
        // Sort jobs by the minimum time they take to any worker in a descending
        // order.
        Collections.sort(sortedJobs, JOB_MINTIME_ORDER_DESC);

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

        // How many times have we run?
        int level = 0;
        Random generator = new Random();
        while (level < 3 && greedyJobs >= 5) {
            // Reset job domains
            fillJobDomains();
            // Remove infeasible values on the basis of previous assignments
            updateJobDomains(-1);
            // Iterate through randomJobs and attempt to assign them randomly
            for (int i = 0; i < randomJobs.size(); i++) {
                int jobId = randomJobs.get(i).getId();
                if (!jobDomains.get(jobId).isEmpty()) {
                    int workerPos = generator.nextInt(jobDomains.get(jobId).size());
                    int workderId = jobDomains.get(jobId).get(workerPos).intValue();
                    solution.assign(jobId, workderId);
                    jobDomains.get(jobId).remove(workerPos);
                    updateJobDomains(-1);
                } else { //we have no option to assign the job, hence backtracking
                    i--;
                    if (i < 0) {
                        break; //no solution found
                    }
                    jobId = randomJobs.get(i).getId();
                    solution.unassign(jobId);
                    updateJobDomains(jobId);
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
            // Decrease the number of deterministic assignments
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
        return true;
    }

    public void clear() {
        solution.clear();
        backtracksCount = 0;
    }

    public GapSolution getSolution() {
        return solution;
    }

    public void setSolution(GapSolution _solution) {
        solution = _solution;
    }

    public int getCostLowerBound() {
        return getCostLowerBound(solution.getSettings());
    }

    /**
     * Compute the lower bound of the problem as a sum of the
     * cheapes assignments of all jobs.
     * @return The lower bound.
     */
    public int getCostLowerBound(GapSettings set) {
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

    // greedy algrithm with backtracing hungry for costs (with respect to time)
    /**
     * Generate solution in a greedy manner. The main criterion is cost of the job,
     * respect is given to the time needed. considering the time significantly decreases
     * number of backtracks needed and consequently makes the generator faster.
     * @return True if a feasible solution has benn found, false otherwise.
     */
    public boolean generateGreedySolution() {
        GapSettings set = solution.getSettings();
        updateJobDomains(-1);
        double[] min_cost = new double[jobsCount];
        int[] jobs = new int[jobsCount];

        for (int i = 0; i < jobsCount; i++) {
            jobs[i] = i;
            int min = Integer.MAX_VALUE;
            for (int j = 0; j < workersCount; j++) {
                if (min > set.getCost(j, i) * set.getTime(j, i)) {
                    min = set.getCost(j, i) * set.getTime(j, i);
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
                    if (min > set.getCost(worker, job) * set.getTime(worker, job)) {
                        min = set.getCost(worker, job) * set.getTime(worker, job);
                        best_pos = j;
                    }
                }
                int worker = jobDomains.get(job).get(best_pos).intValue();
                solution.assign(job, worker, true);
                jobDomains.get(job).remove(best_pos);
                updateJobDomains(-1); // arc consistency on all not assigned variables

            } else {
                i = i - 1; // unassign previous

                if (i < 0) {
                    return false; // no solution

                }
                backtracksCount++;
                solution.unassign(jobs[i]);
                updateJobDomains(jobs[i]);
                i = i - 1; // just step back in for cycle to get to the unassign variable       

            }
        }
        return true;
    }

    public boolean generateParalelGRASPSolution(int numThreads) {
        Vector<SolverThread> threads = new Vector<SolverThread>(numThreads);
        for (int i = 0; i < numThreads; i++) {
            threads.add(new SolverThread(new GapProblem(this)));
            threads.get(i).start();
        }
        for (int i = 0; i < numThreads; i++) {
            try {
                threads.get(i).join();
            } catch (InterruptedException ex) {
                Logger.getLogger(GapProblem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        GapSolution bestSolution = threads.get(0).getSolution();

        boolean foundSolution = false;
        for (int i = 0; i < numThreads; i++) {
            System.out.println("Thread num:" + threads.get(i).getId());
            if (!threads.get(i).foundSolution()) {
                System.out.println("Thread " + threads.get(i).getId() + ": Didn't find solution.");
            } else {
                foundSolution = true;
                System.out.println("Thread " + threads.get(i).getId() + ": Found solution: \n" + threads.get(i).getSolution());
                if (threads.get(i).getSolution().getGlobalCost() < bestSolution.getGlobalCost()) {
                    bestSolution = threads.get(i).getSolution();
                }
            }
        }
        solution = bestSolution;
        return foundSolution;
    }

    /**
     * Perform the local search starting from a given solution.
     * @return Local minima found.
     */
    public GapSolution localSearch(GapSolution gs) {
        GapSolution bestSolution = new GapSolution(gs, gs.getSettings());
        GapSettings settings = bestSolution.getSettings();
        GapSolution bestFeasible = new GapSolution(gs, gs.getSettings());
        int bestCost = bestSolution.getGlobalCost();
        int lowerBound = getCostLowerBound(bestSolution.getSettings());
        int idle_iter = 0;
        while (idle_iter < 100) { //We do 100 perturbation at most
            /** Find the best neighbour */
            GapSolution newSolution = bestSolution.getBestNeighbour(false);

            if (newSolution.isFeasible() && newSolution.getGlobalCost() < bestCost) {
                bestFeasible = new GapSolution(newSolution, settings);
                bestCost = bestFeasible.getGlobalCost();
                if (bestCost == lowerBound) {
                    break;
                }
            }
            /** No better neighbour found, perturb the solution.*/
            if (newSolution.equals(bestSolution)) {
                bestSolution.perturb();
                idle_iter++;
            }
            bestSolution = new GapSolution(newSolution, settings); //best solution this far
        }
        return bestFeasible;
    }
}
