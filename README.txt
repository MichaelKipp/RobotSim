Project: Robot Sim
Authors: Jordan Aron, Michael Kipp
Class: Evolutionary Computation & Artificial Life
Professor: Sherri Goings
——————————————————————————————————————————————————
*** ALL CODE ADAPTED FROM TARTARUS PROJECT PROVIDED BY SHERRI GOINGS***
——————————————————————————————————————————————————

Files
—————

Reportable:

	SprialDodgeSolution 	- Print out of the solution which developed to avoid the other bots by spiraling around them.
	t01.gif		     	- Image of a solution’s tree demonstrating bloat.

TartarusCode:

	Acme			- Don’t know, did not use. Presumably part of the Tartarus project.
	docs			- gpjpp documentation
	gpjpp			- Java code adding genetic programming capabilities.

	Tartarus 2:
		
		Same as for the Tartarus project but with a bit of additional code.
		New code includes adapted grid, removed block functionality, optional interfering robots to add complexity to problem, and new fitness calculation.

		Most of the edited code is in Grid.java.
			Code for new node functions.
			New Grid layout including ‘X’s in corners.
			Addition of interfering robots and their random placement in the grid.
			At the bottom is the code that runs the interfering robots and makes their strategies.
		Added code to Tartarus.java for new functions.
		Added code to TartGP.java to executer other bots and print out board.

To Run
——————
The program can be run the same way Tartarus is run.

1. Navigate to Tartarus2 folder 	- cd ../RobotSim/TartarusCode/Tartarus2
2. Compile the project 		- javac -cp “../:.” *.java
3. Create test variables		- cp tartarus.ini t0.ini
4. Run with new variable		- java -cp “../:.” Tartarus t0
5. Celebrate.

Output fitnesses will be stored in a new file “<given variable name>.stc” stored inside of the data folder.








