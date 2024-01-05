# Searching Algorithms (AI project 1)
 
The file Search.java contains all of the necessary code.


## What does the project do? 

Using an initial location and a goal location, the program solves for the path (based on the search algorithm chosen) based on locations of cities


## Command Line Arguements

-f(File Location)*, -i(Initial City)*, -g(Goal City)*, -v(Verbosity Level), -s(Search Algorithm), -h(Heuristic Function) and, --no-reached(Disables reached table)

**NOTE: All command line arguments marked with star are required**

EXAMPLE OF CMD ARGUEMENT: Java Search.java -i "Initial City" -f InputFile.csv -s "Search Algorithm" -h "Heuristic Function" --no-reached -v 4 -g "Goal City"

-f*:String of reference file  
-i*:The string of the starting location(Must be contained in reference file)  
-g*:The string of the goal location(Must be contained in reference file)  
-s :Search function {uniform, greedy, a-star(Default)}  
-h	:The formula that will be used to come up with the heuristic{euclidean, haversine(Default)}  
--no-reached: The search will not hold a reference of all cities that have been accessed and instead only search for cycles{defaults to reached table unless this command is added}  
-v :The amount of information about the search that you want to display(Higher the number the more information that will display){0(Default), 1, 2, 3, 4, 5}

*Verbosity level 3 prints outputs as something is happening (i.e. if a node is being added to the frontier, the output for the node being added will be printed and then the node would be added to the frotier).{This is best to use when there is an issue with a solution to debug, or if you need to know what is currently happening within the code}

*Verbosity level 4 prints outputs evertime there is about to be an expansion of a new node(i.e. the output for a node being added to the frontier will be stored in the string builder to be printed when the next node is about to be expanded).{This is best to use when you want to have an idea of what the code is doing, but want the output to print much faster or if there is a stack issue in verbosity 5}

*Verbosity level 5 prints outputs when after the solution (or an error) has been found(i.e. the output for a node being added to the frontier will be stored in the string builder to be printed when the solution is found).{This is the most efficient solution, and I would advise using it unless one of the criteria in verbosity levels 3 or 4 apply}

**It is important to understand that verbosity levels 3-5 will have the same output

The input file must be a csv file includes cities (along with coordinates) and paths between cities. Both of these sections of the file are denoted by a comment that looks exactly like the comment shown in the example below.

**NOTE: The file must look Exactly like this. Pay attention to spaces!

Example file:

\# Cities: name, latitude, longitude  
La Crosse, 43.8, -91.24  
La Crescent, 43.83, -91.3  
Winona, 44.06, -91.67  
Minneapolis, 44.98, -93.27  
\# Distances: name1, name2, distance  
La Crosse, La Crescent, 5.0  
La Crosse, Winona, 31.6  
La Crescent, Winona, 27.5  
La Crescent, Minneapolis, 142.0  
Winona, Minneapolis, 116.0


###### Author: Jackson Mishuk
 
