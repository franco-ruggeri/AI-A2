# Client Java for Tic-Tac-Toe dd2380

# Compile
javac *.java

# Run
# The players use standard input and output to communicate
# The Moves made are shown as unicode-art on std err if the parameter verbose is given

# Play against self in same terminal
mkfifo pipe
java Main init verbose < pipe | java Main > pipe

# Play against self in two different terminals
# Terminal 1:
mkfifo pipe1 pipe2
java Main init verbose < pipe1 > pipe2

# Terminal 2:
java Main verbose > pipe1 < pipe2

# Play against your friend (in the same terminal)
If you want two algorithms to play against each other, create two folders, folder1 and folder2. Copy the skeleton to both of these folders and compile the code. Then run:

mkfifo pipe

java -cp folder1 Main init verbose < pipe | java -cp folder2 Main > pipe


