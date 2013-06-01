
Implementation of Dijkstra Scholten Termination Detection Algorithm.

sample.txt file contains the events to execute.

config.txt file contains the configuration of the nodes.

First line of configuration file contains the information of the current node on which it is running.
0,1301   nodeid, port number
Rest of the lines contains the configuration of other systems in the network
0,net02.utdallas.edu,2222 nodeid, host name, port number

command to compile source files and place it in bin folder.
javac -d bin src/ds/*.java

command to run the compiled files by providing the main class
java -cp ./bin ds.DijkstraScholtenAlgorithm

Else run the provided jar directly
java -jar ds.jar

