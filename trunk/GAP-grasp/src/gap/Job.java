package gap;

/**
 *
 * @author izaak
 */
public class Job implements Comparable {
    private int id;
    private int deltaMinMaxTime;
    private int bestWorkerId;
    
    
    public Job( int id, int deltaMinMaxTime, int bestWorkerId){
        this.id = id;
        this.deltaMinMaxTime = deltaMinMaxTime;
        this.bestWorkerId = bestWorkerId;
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
