package gap;

/**
 *
 * @author izaak
 */
public class Job implements Comparable {
    private int id;
    private int deltaMinMaxCost;
    private int bestWorkerId;
    
    
    public Job( int id, int deltaMinMaxCost, int bestWorkerId){
        this.id = id;
        this.deltaMinMaxCost = deltaMinMaxCost;
        this.bestWorkerId = bestWorkerId;
    }
    
    public int getId(){
        return id;        
    }
    
    public int getDeltaMinMaxCost(){
        return deltaMinMaxCost;
    }
    
    public int getBestWorkerId(){
        return bestWorkerId;
    }
    
    public int compareTo(Object otherJob) {
        int otherDelta = ((Job) otherJob).getDeltaMinMaxCost();
        if(deltaMinMaxCost < otherDelta){
            return -1;
        }else if(deltaMinMaxCost == otherDelta){
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
