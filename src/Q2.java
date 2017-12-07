/*********************************************************************
 Name:		Saad Mushtaq
 S Num:		7785430
 Course:	COMP 3370
 Question:	Q:2
 Professor:	Peter Graham
 Ass #:		Assignment # 4
 *******************************************************************/

import java.io.*;

public class Q2 {
    public static void main(String[] args) {

        // Constants for the different types of caches
        int TWO_WAY = 2;
        int FOUR_WAY = 4;
        int ONE = 1;

        // input reader classes
        FileInputStream fIn = null;
        BufferedReader input = null;


        // check if length of args array is
        // less than 0
        if (args.length < 0) {
            System.out.println("Please enter the arguments");

        } else {

            /************ Cache organization ****************************************/
            int[][] cacheOrg = {{8, 8192}, {32, 1024}, {8, 16384}, {32, 2048}};

            /******** Three instances of saCache class *****************************/
            saCache twoWaySet = new saCache();
            saCache fourWaySet = new saCache();
            saCache directMapped = new saCache();

            /************ setting the associativity to the required number for each instance *******/
            twoWaySet.setCacheDegree(TWO_WAY);
            fourWaySet.setCacheDegree(FOUR_WAY);
            directMapped.setCacheDegree(ONE);

            /*************************** iterating through the different cache organization for each cache type *******/
            for (int i = 0; i < cacheOrg.length; i++) {
                twoWaySet.setOrg(cacheOrg[i][0], cacheOrg[i][1]);
                fourWaySet.setOrg(cacheOrg[i][0], cacheOrg[i][1]);
                directMapped.setOrg(cacheOrg[i][0], cacheOrg[i][1]);

                // resets the simulator for the current iteration
                initSimulator(twoWaySet, fourWaySet, directMapped);

                try {
                    fIn = new FileInputStream(args[0]);
                    input = new BufferedReader(new InputStreamReader(fIn));

                    // initialize the cache directories
                    twoWaySet.initDirectory();
                    fourWaySet.initDirectory();
                    directMapped.initDirectory();

                    // Echo out run parameters
                    System.out.print("\nThis is a " + TWO_WAY + " way set associative cache");
                    System.out.print(", cache line size is: " + cacheOrg[i][0]);
                    System.out.println(", total number of cache lines: " + cacheOrg[i][1]);

                    // run through the file and find the hits and misses for 2-way cache
                    processSimulation(twoWaySet, input);

                    // "reset" to beginning of file (discard old buffered reader)
                    fIn.getChannel().position(0);
                    input = new BufferedReader(new InputStreamReader(fIn));

                    System.out.println("**************************************************************************************************");

                    System.out.print("\nThis is a "+ FOUR_WAY + " way set associative cache");
                    System.out.print(", cache line size is: "+cacheOrg[i][0]);
                    System.out.println(", total number of cache lines:  "+cacheOrg[i][1]);

                    // run through the file and find the hits and misses for 4-way cache
                    processSimulation(fourWaySet, input);

                    // "reset" to beginning of file (discard old buffered reader)
                    fIn.getChannel().position(0);
                    input = new BufferedReader(new InputStreamReader(fIn));

                    System.out.println("**************************************************************************************************");

                    System.out.print("\nThis is a direct mapped cache");
                    System.out.print(", cache line size is: "+cacheOrg[i][0]);
                    System.out.println(", total number of cache lines:  "+cacheOrg[i][1]);

                    // run the file and find ten hits and misses for a direct mapped cache
                    processSimulation(directMapped, input);

                    System.out.println("**************************************************************************************************");


                } catch (IOException io) {
                    System.err.println("Error opening the file");
                    io.printStackTrace();
                    System.exit(1);
                } // close try-catch
            }
        } // end if-else block

        System.out.println("\n\nProgrammed by Saad.");
    } //close main method

    /*****************************************************************************
    * Runs the file for the cache type specified to find the hits and misses
     ******************************************************************************/
    public static void processSimulation(saCache whichSet, BufferedReader input) {
        whichSet.simulate(input);
    } // close processSimulation method


    /***************************************************************************************************
     * Resets the simulator for the different cache organizations before actually going into simulation
     ***************************************************************************************************/
    public static void initSimulator(saCache twoWay, saCache fourWay, saCache dm) {

        /***** reset the ref to 0 ****/
        twoWay.setRef();
        fourWay.setRef();
        dm.setRef();

        /******* reset the hits/misses ********/
        twoWay.setHitsMiss();
        fourWay.setHitsMiss();
        dm.setHitsMiss();
    } // close initSimulator method

} // close SAcache


/********************************************************************
 *  Class: saCache
 ********************************************************************/
class saCache {

    /***************** Instance Variables ****************************/
    cacheDirectory[] directory;
    int lineSize; // num of bytes
    int address; // address from the file
    int N; // num of lines
    int cacheLineNum; // cache line number
    int cacheDegree; // associativity
    int hits = 0;
    int misses = 0;
    int memNumber; // memory line number
    String line; // line from the file
    int setNum; // cache set number
    int ref = 0; // number of address reference
    int isHit; // flag
    int tag;


    /***************  Constructor ************************************/
    public saCache() { } // saCache Constructor

    /***************** Setters **************************************/
    public void setCacheDegree(int degree) {
        this.cacheDegree = degree;
    } // close setCacheDegree method

    public void setOrg(int lineSize, int N ) {
        directory = new cacheDirectory[N];
        this.lineSize = lineSize;
        this.N = N;
    } // setOrg method

    public void setRef(){
        this.ref = 0;
    } // close setRef method

    public void setHitsMiss() {
        this.hits = 0;
        this.misses = 0;
    } //setHitsMiss method

    /************** Initializing directory ****************************/
    public void initDirectory() {
        int i = 0;
        while(i < N) {
            directory[i] = new cacheDirectory();
            i++;
        } // end while

        int j = 0;
        while(j < N) {
            directory[j].is_Valid = false;
            j++;
        } // end while
    } // close initDirectory method


    /**************************** Simulate Method *************************/
    public void simulate(BufferedReader input) {


        try {
            while ((line = input.readLine()) != null ) {
                String[] token = line.split(" "); // split them into two tokens for each line with regex as whitespace
                ref++; // increment the count for references read from the file


                /*********************** compute the tag and the index number for the cache ******************/
                address = Integer.valueOf(Integer.parseInt(token[1],16)); // java 8 hex to dec built-in method
                memNumber = address/lineSize;
                tag = memNumber/cacheDegree;
                setNum = memNumber % (N/cacheDegree);


                isHit = 0;
                int setFirst = setNum * cacheDegree;
                int setLast = (setNum + 1) * cacheDegree - 1;

                int count = setFirst;
                while (count <= setLast) {
                    if( (directory[count].is_Valid) && (directory[count].tag == tag)) {
                        hits++;
                        cacheLineNum = count;
                        isHit = 1;

                        if(cacheLineNum != setFirst) {

                            int ii = cacheLineNum;
                            while (ii > setFirst ) {
                                directory[ii].is_Valid = directory[ii - 1].is_Valid;
                                directory[ii].tag = directory[ii - 1].tag;

                                ii--;
                            } // end while

                            directory[setFirst].is_Valid = true;
                            directory[setFirst].tag = tag;
                        } // end inner if

                        break;
                    } // end outer if
                    count++;
                } // end inner while loop

                if( isHit == 0 ) {
                    misses++;
                    directory[setLast].is_Valid = true;
                    directory[setLast].tag = tag;
                } // end if
            } // end outer while loop

        } catch (IOException ex) {
            System.err.println("file read unsuccessful.");
            ex.printStackTrace();
            System.exit(1);
        } // end try-catch

        toString(hits,ref, misses); // call the toString to display the result
    } // close simulate method


    /************* display the result of the cache ***************************************/
    public void toString(int hits, int ref, int misses) {

        // calculate the hits/miss percentages
        double hitPercent = hits/(double)ref;
        double missPercent = misses/(double)ref;
        hitPercent *= 100;
        missPercent *= 100;

        // echo out the report
        System.out.println("The number of hits: " + Integer.toString(hits));
        System.out.println("The number of references: " + Integer.toString(ref));
        System.out.println("The number of misses: " + Integer.toString(misses));
        System.out.println("The hit percentage is: " + String.format("%.2f", hitPercent) + "%");
        System.out.println("The miss percentage is: " + String.format("%.2f", missPercent)+ "%");
    }

} // close saCache class


/********************************************************************
 *  Class: Cache Directory
 ********************************************************************/
class cacheDirectory {

    // Instance Variables
    public int tag;
    public boolean is_Valid;

} /* close cacheDirectory */