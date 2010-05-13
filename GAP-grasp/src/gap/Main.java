/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gap;

import java.io.FileNotFoundException;
import java.io.IOException;


public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        GapParser parser= new GapParser("gap4.txt"); //files in ./data/ dir
        System.out.println("Reading information...");
        GapProblem myProblem = parser.parseProblem(5); // 5th example in file
        System.out.println("Done");
        myProblem.assign(1, 1);
        
        System.out.println(myProblem.toString());        
    }
}
