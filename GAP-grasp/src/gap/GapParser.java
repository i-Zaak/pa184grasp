/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class GapParser {

    File file;
    
    public GapParser(String _file){
        file = new File("./data/"+_file);
    }
    
    public GapProblem parseProblem(int pos) throws FileNotFoundException, IOException{
        BufferedReader in = new BufferedReader(new FileReader(file));
        
        String line; 
        line = in.readLine();
        
        if (pos > Integer.parseInt(line))
            return null; // we want something that is not there
        in.readLine(); // first is always empty 
        while (pos > 1){            
            if (in.readLine().isEmpty()) {
                pos--;
                if (pos == 1) // time to read problem
                    break;
                while(in.readLine().isEmpty()) { // skip empty lines                    
                }                
            }    
        }        
        while((line = in.readLine()).isEmpty()){ // skip empty lines    
        }            
        // our problem is here
        String tmp[] = line.split(" ");
        int workersCount = Integer.valueOf(tmp[0]);
        int jobCount = Integer.valueOf(tmp[1]);
        GapProblem myProblem = new GapProblem(workersCount,jobCount);
        
        for (int i = 0; i < workersCount; i++) { // reading costs
            line = in.readLine();
            tmp = line.split(" ");
            for (int j=0; j < tmp.length; j++){
                myProblem.setWorkerJobCost(i, j, Integer.parseInt(tmp[j]));
            }
        }
        
        for (int i = 0; i < workersCount; i++) { // reading time
            line = in.readLine();
            tmp = line.split(" ");
            for (int j=0; j < tmp.length; j++){
                myProblem.setWorkerJobTime(i, j, Integer.parseInt(tmp[j]));
            }
        }
        
        line = in.readLine();
        tmp = line.split(" ");    
        for (int i=0; i < tmp.length; i++){
            myProblem.setWorkerLimitTime(i, Integer.parseInt(tmp[i]));
        }
        
        return myProblem;
    }
}
