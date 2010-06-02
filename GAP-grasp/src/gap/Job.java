package gap;

/**
 *
 * @author izaak
 */
public class Job implements Comparable {
    private int id;
    private int minTime;
    private int deltaMinMaxTime;
    private int bestWorkerId;
    
    
    public Job( int id, int deltaMinMaxTime, int bestWorkerId){
        this.id = id;
        this.minTime = -1;
        this.deltaMinMaxTime = deltaMinMaxTime;
        this.bestWorkerId = bestWorkerId;
    }
    
    public Job (int id, int bestWorkerId) {
        this.id = id;
        this.minTime = -1;
        this.deltaMinMaxTime = -1;
        this.bestWorkerId = bestWorkerId;
    }
    
    public void setMinTime(int minTime) {
        this.minTime = minTime;
    }
    
    public int getMinTime() {
        return this.minTime;
    }
    
    public int getId(){
        return id;        
    }
    
    public int getDeltaMinMaxTime(){
        return deltaMinMaxTime;
    }
    
    public int getBestWorkerId(){
        return bestWorkerId;
    }
    
    public int compareTo(Object otherJob) {
        int otherDelta = ((Job) otherJob).getDeltaMinMaxTime();
        if(deltaMinMaxTime < otherDelta){
            return -1;
        }else if(deltaMinMaxTime == otherDelta){
            return 0;
        }else{
            return 1;
        }
            
    }

    @Override
    public String toString() {
        return Integer.toString(this.id);
    }
    
    
    
}