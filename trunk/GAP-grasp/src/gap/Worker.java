/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gap;

/**
 *
 * @author pavel
 */
public class Worker implements Comparable {
    int workerId;
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
        
    public int compareTo(Object otherWorker) {
        Worker ow = (Worker) otherWorker;
        if (ow.getSortParam() < sortParam) return 1;
        else if (ow.sortParam> sortParam) return -1;
        return 0;
    }
}
