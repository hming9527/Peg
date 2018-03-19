# Peg
## How to run
To run the peg solver, first compile the programs: `javac *.java`

Then run the peg solver program: `java PegSolver path_to_input_puzzle`

The name of output files are generated automatically, if the input file containing the puzzle has the name `X`, then output from the front end will be named `X_Clauses`, output from the DPLL program will be named `X_Clauses_DPLL_output`, and the output from the back end will be named `X_Clauses_DPLL_output_Final_Path`.
