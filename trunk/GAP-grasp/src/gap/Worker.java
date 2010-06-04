/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gap;

/**
 * Simple class representing worker, intended mainly for general sorting purposes 
 */
public class Worker implements Comparable {
    /** Id of the worker. */
    int workerId;
    /** Sorting parameter. */
    int sortParam;

    public Worker(int workerId, int sortParam) {
        this.workerId = workerId;
        this.sortParam = sortParam;
    }
    
    public int getSortParam() {
        return sortParam;
    }
     
    public int getWorkerId() {
        return workerId;
    }

    /**
     * General comparison function. Sorting is based on a general sortParam parameter,
     * hence class can be used for various range of purposes.
     */
    public int compareTo(Object otherWorker) {
        Worker ow = (Worker) otherWorker;
        if (ow.getSortParam() < sortParam) return 1;
        else if (ow.sortParam> sortParam) return -1;
        return 0;
    }
}
