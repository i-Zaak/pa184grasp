/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
//gap12.txt - problems 1,3
//gap9.txt - problem 3
//wrong count of numbers -> fixed?
package gap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class GapParser {

    File file;
    
    public GapParser(File _file){
        file = _file;
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
        
        int[][] workerJobCost = new int[workersCount][jobCount];
        int [][] workerJobTime = new int[workersCount][jobCount];
        int [] workerLimitTime = new int[workersCount];
        
        for (int i = 0; i < workersCount; i++) { // reading costs
            line = in.readLine();
            tmp = line.split(" ");
            if (tmp.length != jobCount) {
                System.out.println("Parser: Format of file is wrong");
                System.out.println("Parser: Count of workerJobCost elements is " + tmp.length + "(should be "+ jobCount + ")");
                return null;
            }
            
            for (int j=0; j < tmp.length; j++){
                workerJobCost[i][j] = Integer.parseInt(tmp[j]);
            }
        }
        
        for (int i = 0; i < workersCount; i++) { // reading time
            line = in.readLine();
            tmp = line.split(" ");
            if (tmp.length != jobCount) {
                System.out.println("Parser: Format of file is wrong");
                System.out.println("Parser: Count of workerJobTime elements is " + tmp.length + "(should be "+ jobCount + ")");
                return null;
            }
            for (int j=0; j < tmp.length; j++){
                workerJobTime[i][j] = Integer.parseInt(tmp[j]);
            }
        }
        
        line = in.readLine();
        tmp = line.split(" ");    
        if (tmp.length != workersCount) {
                System.out.println("Parser: Format of file is wrong");
                System.out.println("Parser: Count of workerLimitTime elements is " + tmp.length + "(should be "+ workersCount + ")");
                return null;
        }
        
        for (int i=0; i < tmp.length; i++){
            workerLimitTime[i] =Integer.parseInt(tmp[i]);
        }
        GapSettings settings = new GapSettings(workerJobCost, workerJobTime, workerLimitTime);
        GapProblem myProblem = new GapProblem(workersCount, jobCount, settings);
        
        return myProblem;
    }
}
