# Java skeleton for checkers dd2380

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

# To play two different agents against each other, you can use the classpath argument
java -classpath <path> Main init verbose < pipe | java -classpath <path> Main > pipe

