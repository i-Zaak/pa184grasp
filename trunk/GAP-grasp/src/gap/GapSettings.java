/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gap;

/**
 *
 * @author Salla
 */
public class GapSettings {

    private int[] workerLimitTime;
    private int[][] workerJobCost;
    private int[][] workerJobTime;
    
    public GapSettings(int[][] _workerJobCost, int[][] _workerJobTime, int [] _workerLimitTime) {
        workerJobCost = _workerJobCost.clone();
        workerJobTime = _workerJobTime.clone();
        workerLimitTime = _workerLimitTime.clone();
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
