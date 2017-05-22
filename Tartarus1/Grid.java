/*
 * Authors: Michael Kipp, Jordan Aron
 * Class: Evolutionary Computation & Artificial Life
 *
 */

import java.util.*;
import java.io.*;
import java.util.concurrent.ThreadLocalRandom;

public class Grid {

    //functions and terminals
    public final static int ZERO = 0;
    public final static int ONE = 1;
    public final static int TWO = 2;
    public final static int UR = 3;
    public final static int MR = 4;
    public final static int LR = 5;
    public final static int UM = 6;
    public final static int LM = 7;
    public final static int UL = 8;
    public final static int ML = 9;
    public final static int LL = 10;
    public final static int INC = 11;
    public final static int DEC = 12;
    public final static int ADD = 13;
    public final static int SUB = 14;
    public final static int MAX = 15;
    public final static int MIN = 16;
    public final static int ITE = 17;
    // new Nodes
    public final static int RND = 18;
    public final static int TWOAHEAD = 19;
    public final static int THREEAHEAD = 20;


    // grid private vars
    private char[][] grid;
    private int xdim, ydim;
    private Random rgen;
    private int numBoxes;
    private int dozerX, dozerY;
    public int dozerFacing;
    char[] dirs = new char[] {'e','n','w','s'};


    private int xend, yend;

    // if no seed given, use -1 for cur time
    public Grid(int xdim, int ydim, int numBoxes) {
	     this(xdim, ydim, numBoxes, -1);
    }

    // create a Grid of characters with the given dimensions and number of boxes
    public Grid(int xdim, int ydim, int numBoxes, int seed) {
        this.xdim = xdim;
        this.ydim = ydim;
        this.numBoxes = numBoxes;

      	// grid is just 2d array of chars, initially fill with spaces
      	grid = new char[xdim][ydim];
              for (int i=0; i<xdim; i++)
                  for (int j=0; j<ydim; j++)
                  // ADD INDICATOR FOR THE EXIT SPACE
                  // New code to put 'X's in corners <---------------------------------------------
                  if ((i != 0 && i != 1 && i != xdim - 1 && i != xdim - 2)
                        || (j != 0 && j != 1 && j != ydim - 1 && j != ydim - 2))
                      grid[i][j] = ' ';
                  else
                    grid[i][j] = 'X';

      	// create rand generator (if seed is -1, use time instead)
      	if (seed == -1) rgen = new Random();
      	else rgen = new Random(seed);

        initGrid();
    }

    // place boxes and bulldozer on the grid, may not create 2x2 square of boxes
    // as bulldozer could not move any of them no matter how good the strategy
    private void initGrid() {

        int toPlace = numBoxes;
        int remLocs = (xdim-2)*(ydim-2);
        int x=1, y=1;
        int exitX=1, exitY=1;
        int gridEndSpace;

        // Shouldn't need this since we won't have boxes <-----------------------------------------
        while (toPlace > 0) {

            // the probability that this square should get a block is
      	    // (blocks still to place) / (squares not yet considered)
      	    // Note that this probability will grow to 1 when there are only as
      	    // many squares left as there are blocks to place.
            double p = (double)toPlace/remLocs;
            if (rgen.nextDouble() < p) {
        		// only place if won't create a 2x2 square of blocks
        		// if p is 1, place block even if creates square, so as to avoid infinitie loop
                if (grid[x-1][y]!='b' || grid[x][y-1]!='b' || grid[x-1][y-1]!='b' || p>=0.99) {
                    grid[x][y] = 'b';
        		        toPlace--;
        		    }
            }
            remLocs--;

            // at end of each row, move to beginning of next row
            if (++x == xdim-1) {
                x=1;
                y++;
            }
        }

        // This needs to place the dozer in a gate <-----------------------------------------------
        // place dozer in random start location
        x = rgen.nextInt(xdim-xdim/2) + xdim/2 - 1;
        y = ydim - 1;
        gridEndSpace = rgen.nextInt(3);

        // This shouldn't need to be here since there won't be boxes <-----------------------------
        // if chosen dozer location already has a box, just search 2x2 space around
        // it because know can't have 2x2 squares all with boxes
        if (grid[x][y] == 'X') {
            if (grid[x+1][y] != 'X') x++;
            else if (grid[x-1][y] != 'X') x--;
            else {
                x--;
            }
        }

        //This will be where the exit is on the left
        if (gridEndSpace == 0){
          exitX = 0;
          exitY = rgen.nextInt(ydim-ydim/2) + ydim/2 - 1;
          if (grid[exitX][exitY] == 'X') {
              if (grid[exitX][exitY+1] != 'X') exitY++;
              else if (grid[exitX][exitY-1] != 'X') exitY--;
              else {
                  exitY--;
              }
          }
        }




        //This will be where the exit is in front
        if (gridEndSpace == 1) {
          exitX = rgen.nextInt(xdim-xdim/2) + xdim/2 - 1;
          exitY = 0;
          if (grid[exitX][exitY] == 'X') {
              if (grid[exitX+1][exitY] != 'X') exitX++;
              else if (grid[exitX-1][exitY] != 'X') exitX--;
              else {
                  exitX--;
              }
          }
        }


        //This will be where the exit is on the right
        if (gridEndSpace == 2){
          exitX = xdim - 1;
          exitY = rgen.nextInt(ydim-ydim/2) + ydim/2 - 1;
          if (grid[exitX][exitY] == 'X') {
              if (grid[exitX][exitY+1] != 'X') exitY++;
              else if (grid[exitX][exitY-1] != 'X') exitY--;
              else {
                  exitY--;
              }
          }
        }

        // This shouldn't need to be here since there won't be boxes <-----------------------------
      	// if 2x2 square is all blocks because of one weird case, oh well, put dozer
      	// somewhere at least, even if shares location with block
        grid[x][y] = 'D';
        dozerX = x;
        dozerY = y;

        grid[exitX][exitY] = '*';

        // Change so the robot faces inwards <-----------------------------------------------------
        // set dozer to face random direction
        dozerFacing = 1;
    }


    // turn dozer left
    public void left() {
	     left(null);
    }

    // turn dozer left and update output file
    public void left(BufferedWriter out) {
        dozerFacing = (dozerFacing + 1) % 4;
        if (out != null) updateFile(out, dozerX, dozerY, dozerFacing);
    }

    // turn dozer right
    public void right() {
	     right(null);
    }

    // turn dozer right and update output file
    public void right(BufferedWriter out) {
        if (--dozerFacing < 0) dozerFacing = 3;
        if (out != null) updateFile(out, dozerX, dozerY, dozerFacing);
    }

   // if no out file specified, use null
    public void forward() {
	     forward(null);
    }

    // Should be changed to discount block instances and account for other robot <-----------------
    // if dozer is facing wall or 2 consecutive squares with blocks, nothing happens
    // otherwise move dozer forward 1 and if block in front move it too
    public void forward(BufferedWriter out) {
        // get coordinates of space in front of dozer and space 2 in front
        int frontX = dozerX, frontY = dozerY;
        int forw2X = dozerX, forw2Y = dozerY;
        if (dozerFacing==0) {
            frontX++;
            forw2X += 2;
        }
        else if (dozerFacing==1) {
            frontY--;
            forw2Y -= 2;
        }
        else if (dozerFacing==2) {
            frontX--;
            forw2X -= 2;
        }
        else {
            frontY++;
            forw2Y += 2;
        }

        // if facing wall, do nothing
        if (frontX<0 || frontX >= xdim || frontY<0 || frontY>=ydim) {
            //record that step spent not moving
    	      if (out != null) updateFile(out, dozerX, dozerY, dozerFacing);
                return;
        }

        // if facing block
        if (grid[frontX][frontY] == 'X') {
            // // if has wall or another block behind it, do nothing
            // if (forw2X<0 || forw2X>=xdim || forw2Y<0 || forw2Y>=ydim || grid[forw2X][forw2Y]=='X') {
            //     //record that step spent not moving
		            if (out != null) updateFile(out, dozerX, dozerY, dozerFacing);
                return;
            //}

            // Remove to disallow robots from moving other robots <--------------------------------
            // if clear behind block, move block
	          // grid[forw2X][forw2Y] = 'b';

      	    // record move
      	    // if (out != null) {
            // 		try{
            // 		    out.write(forw2X + " " + forw2Y + " " + "b\n");
            // 		} catch (IOException e) {
            // 		    System.out.println("Error while writing to file in forward method");
            // 		}
      	    // }

        }
        // if here were either facing block with nothing behind it or empty space so will move dozer
      	// record move
      	if (out != null) {
      	    try{
      		out.write(dozerX + " " + dozerY + " " + " \n");
      	    } catch (IOException e) {
      		System.out.println("Error while writing to file in forward method");
      	    }
      	}

      	// dozer moves
        grid[frontX][frontY] = 'D';
        grid[dozerX][dozerY] = ' ';
        dozerX = frontX;
        dozerY = frontY;

      	if (out!= null) updateFile(out, dozerX, dozerY, dozerFacing);


    }

    // Adapt from boxes to other robots <----------------------------------------------------------
    // frontOffset is 1 for square in front of dozer, 0 for inline with dozer, and -1 for behind
    // sideOffset is 1 for left of dozer, 0 inline, -1 right
    // y goes from 0 at the top to max val at the bottom of grid/screen
    // returns 0,1, or 2 for empty, box, wall respectively
    public int sensor(int frontOffset, int sideOffset) {
        boolean randSensor = false;
        if (randSensor) {
            return rgen.nextInt(3);
        }

        // determine the appropriate square to check if is empty, wall, or box
        int checkX=-1, checkY=-1;
        if (dirs[dozerFacing] == 'w') {
            checkX = dozerX - frontOffset;
            checkY = dozerY + sideOffset;
        }
        else if (dirs[dozerFacing] == 'e') {
            checkX = dozerX + frontOffset;
            checkY = dozerY - sideOffset;
        }
        else if (dirs[dozerFacing] == 'n') {
            checkX = dozerX - sideOffset;
            checkY = dozerY - frontOffset;
        }
        else if (dirs[dozerFacing] == 's') {
            checkX = dozerX + sideOffset;
            checkY = dozerY + frontOffset;
        }

        // if box to check is out of bounds, return 2 for wall
        if (checkX < 0 || checkY < 0 || checkX >= xdim || checkY >= ydim) return 2;

        // otherwise 0 for empty and 1 for box
        if (grid[checkX][checkY] == 'b') {
          return 1;
        } else {
          return 0;
        }
    }

    // Should be changed to use our new fitness calculation <--------------------------------------
    // determine the fitness of the current state of the grid. fitness is (maxScore+1) - score
    // where score is the number of sides of blocks that are touching a wall
    public int calcFitness() {
        int fit = 0;
        int maxFit = 0;
        if (numBoxes>=4) maxFit = numBoxes+4;
        else maxFit = numBoxes*2;

        // increase fitness if find boxes in first or last col
        int i=0;
        for (int j=0; j<ydim; j++)
            if (grid[i][j] == 'b') fit++;
        i=xdim-1;
        for (int j=0; j<ydim; j++)
            if (grid[i][j] == 'b') fit++;

        // increase fitness for boxes in first or last row, note that
        // boxes in the corner will have fitness increased twice which
        // correctly gives them their bonus of 2 instead of 1.
        i=0;
        for (int j=0; j<xdim; j++)
            if (grid[j][i] == 'b') fit++;
        i=ydim-1;
        for (int j=0; j<xdim; j++)
            if (grid[j][i] == 'b') fit++;

        return maxFit + 1 - fit;
    }

    public void print() {
        print(System.out);
    }

    // Needs to be changed to accomodate new corners and other robots <----------------------------
    // print the current state of the grid, showing blocks and the dozer
    // pointing in the correct direction
    public void print(PrintStream os) {
        for (int y=0; y<ydim; y++) {
            os.print("|");
            for (int x=0; x<xdim; x++) {
                char out = grid[x][y];
                if (out=='D') {
                    if (dozerFacing==0) out = '>';
                    else if (dozerFacing==1) out = '^';
                    else if (dozerFacing==2) out = '<';
                    else if (dozerFacing==3) out = 'v';
                }
                os.print(out+"|");
            }
            os.println();
        }
        os.println();
    }

    private void updateFile(BufferedWriter out, int x, int y, int dir) {
        try{
            out.write(x + " " + y + " ");
            if (dir==0) out.write('>');
            else if (dir==1) out.write('^');
            else if (dir==2) out.write('<');
            else if (dir==3) out.write('v');
            out.write("\n*******\n");
        } catch (IOException e) {
            System.out.println("Error while writing to file in update method");
        }
    }

    public void initSimulationFile(BufferedWriter out){
        try{
            out.write("New Grid\n");
        } catch (IOException e) {
            System.out.println("Error while writing to file in init method");
        }
        for (int y=0; y<ydim; y++) {
            for (int x=0; x<xdim; x++) {
                char c = grid[x][y];
                if (c=='b') {
                    try{
                        out.write(x + " " + y + " b");
                        out.newLine();
                    } catch (IOException e) {
                        System.out.println("Error while writing to file in init method");
                    }
                }
            }
        }
        updateFile(out, dozerX, dozerY, dozerFacing);
    }

    public void outputFitness(BufferedWriter out, int gridFitness) {
        try{
            out.write("Fitness = " + gridFitness + "\n");
        } catch (IOException e) {
            System.out.println("Error while writing to file in fitness method");
        }
    }

    //Returns the average fitness of a given number of board generations
    //usig different manuvering methods
    public static void main (String[] args) {

      BufferedWriter log = new BufferedWriter(new OutputStreamWriter(System.out));
      double averageFitness = 0.0;
      int numMoves = 1;
      int numBoards = 10;
      int previousMove = 3;
      int nextMove;
      int gridSize = 6;
      int boxNumber = 0;
      int distance = 0;
      int[] weighting2 = {0, 0, 0, 0, 0, 0, 1, 1, 2, 2};
      int[] weighting4 = {0, 0, 0, 0, 0, 0, 0, 0, 1, 2};

      //Run test on 1000 random initializations of 6x6 boards with 5 blocks
      //return sum of fitnesses
      for (int i = 0; i < numBoards; i++){
          //generate random board
          Grid grid = new Grid(gridSize, gridSize, boxNumber);
          System.out.println("Grid Number: " + i);
          grid.initSimulationFile(log);

          //loop through number of mooves
          //print the board
          //move the robot and update the board
          for (int j = 0; j < numMoves; j++) {
              grid.print(System.out);

              // method 1
              // randomly choose a direction
              nextMove = ThreadLocalRandom.current().nextInt(0, 2 + 1);

              // method 2
              // weighted to move forward 60% and left or right 20% each
              // nextMove = weighting2[ThreadLocalRandom.current().nextInt(0, 9 + 1)];

              // method 3
              // Do random, except can't turn left then right or right then left
              // nextMove = ThreadLocalRandom.current().nextInt(0, 2 + 1);
              // if (previousMove == 3) {
              //     previousMove = nextMove;
              // } else {
              //     while(nextMove == previousMove) {
              //       nextMove = ThreadLocalRandom.current().nextInt(0, 2 + 1);
              //     }
              //     previousMove = nextMove;
              // }

              // method 4
              // Different weighting of version 2
              // nextMove = weighting4[ThreadLocalRandom.current().nextInt(0, 9 + 1)];

              // method 5
              // Go random distance less than length of grid, then turn randomly, repeat
              // if (distance == 0) {
              //   distance = ThreadLocalRandom.current().nextInt(0, gridSize + 1);
              //   nextMove = ThreadLocalRandom.current().nextInt(1, 2 + 1);
              // } else {
              //   nextMove = 0;
              //   distance--;
              // }

              // method 6
              // blending of 2 and 3
              // nextMove = weighting2[ThreadLocalRandom.current().nextInt(0, 2 + 1)];
              // if (previousMove == 3) {
              //     previousMove = nextMove;
              // } else {
              //     while(nextMove == previousMove) {
              //       nextMove = ThreadLocalRandom.current().nextInt(0, 2 + 1);
              //     }
              //     previousMove = nextMove;
              // }

              if (nextMove == 0) {
                grid.forward();
              } else if (nextMove == 1) {
                grid.left();
              } else {
                grid.right();
              }
          }

          //calculate fitness of board and add to averageFitness
          averageFitness += grid.calcFitness();

      }
      averageFitness = averageFitness/numBoards;
      System.out.println("The average fitness over " + numMoves + " moves and "
                        + numBoards + " was: " + averageFitness);

    }
}
