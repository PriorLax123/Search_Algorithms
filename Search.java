//Creator: Jackson Mishuk

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Stack;
import java.lang.Math;

/*
 * This program allows you to give an input file with locations and distances between some pairs of cities.
 * 
 * From that point, using the initial location and the goal location parameters, the program solves for the path that a search algorithm(chosen by the user) thinks is the best
 * 
 * Command line arguments include: -f(File Location)*, -i(Initial City)*, -g(Goal City)*, -v(Verbosity Level), -s(Search Algorithm), -h(Heuristic Function) and, --no-reached(Disables reached table)
 * **NOTE: All command line arguments marked with star are required**
 * 
 * EXAMPLE OF CMD ARGUEMENT: Java Search.java -i "Initial City" -f "InputFile.csv" -s "Search Algorithm" -h "Heuristic Function" --no-reached -v 4 -g "Goal City"
 * 
 * -f*:String of reference file
 * -i*:The string of the starting location(Must be contained in reference file)
 * -g*:The string of the goal location(Must be contained in reference file)
 * -v :The amount of information about the search that you want to display(Higher the number the more information that will display){0(Default), 1, 2, 3}
 * -s :Search function {uniform, greedy, a-star(Default)}
 * -h	:The formula that will be used to come up with the heuristic{euclidean, haversine(Default)}
 * --no-reached: The search will not hold a reference of all cities that have been accessed and instead only search for cycles{defaults to reached table unless this command is added}
 */

public class Search {
	public static void main(String[] args) {
		
		Problem P = new Problem(args);
		
		if(P.inputFile==null || P.initLocation==null || P.goalLocation==null) {
			System.out.println("Not enough information\n");
			return;
		}
		
		if(P.verbosityLvl>=1) 
			System.out.printf("* Reading data from [%s]\n", P.inputFile);
		
		if(!load(P)) {
			return;
		}
		
		if(P.verbosityLvl>=1) {
			String searchString = "";
			String[] searchArr = P.searchAgthm.split("-");
			for(String str:searchArr) {
				if(!searchString.equals(""))
					searchString += "-";
				searchString += str.substring(0, 1).toUpperCase() + str.substring(1);
			}
				
			System.out.printf("* Searching for path from %s to %s using %s Search\n", P.initLocation, P.goalLocation, searchString);
		}
		
		long startTimeSearch = System.currentTimeMillis();
		if(!executeSearch(P)){
			System.out.println("There was an issue in the attempt to search for the goal!");
			return;
		}
		
		if(P.verbosityLvl>=1) {
			String parent = P.currNode.Parent != null ?P.currNode.Parent.City.name + ")":"null)";
			System.out.printf("* Goal found  : %-30s(p-> %-30s[f= %.1f; g= %.1f; h= %.1f]\n* Search took %dms\n\n", P.currNode.City.name, parent, P.currNode.fN, P.currNode.gN, P.currNode.hN,
					System.currentTimeMillis() - startTimeSearch);
			
		}
		Problem.Node Solution = P.currNode;
		Stack<City> cityPath = new Stack<City>();
		while(true) {
			cityPath.add(P.currNode.City);
			if(P.currNode.Parent == null)
				break;
			P.currNode = P.currNode.Parent;
		}

		System.out.printf("Route found: %s", cityPath.pop().name);
		while(!cityPath.isEmpty()) {
			System.out.printf(" -> %s", cityPath.pop().name);
		}
		
		System.out.printf("\nDistance : %.1f \n\n", Solution.gN);
		
		System.out.printf("Total nodes generated \t: %d", P.nodeCounter);
		System.out.printf("\nNodes remaining on frontier: %d\n", P.frontier.size());

		return;
	}
	
	/*
	 * Used to make sure that the initial and goal cities exist in the reference file
	 * 
	 * RETURN: boolean(true if the file, initial, and goal cities are valid, false if not)
	 */
	private static boolean load(Problem P){
		
		if(!loadCities(P)) {
			return false;
		}
		
		City InitCity = P.citiesMap.get(P.initLocation);
		P.goalCity = P.citiesMap.get(P.goalLocation);
		
		
		if(InitCity == null || P.goalCity == null) {
			System.out.print("The initial and/or goal states weren't stated!");
			return false;
		}
		
		return true;
	}
	
	/*
	 * Used to load city information from the reference file
	 * Will return information of the error if there is one
	 * 
	 * RETURN: boolean(true if the file is valid, false if not)
	 */
	private static boolean loadCities(Problem P) {
		try (BufferedReader br = new BufferedReader(new FileReader(P.inputFile))){
			
			HashMap<String, City> cities = new HashMap<String, City>();
			String line = br.readLine();
			
			String[] cityInfo = line.split(" ");
			
			while(cityInfo[0].equals("#")){
				if(cityInfo[1].equals("Cities:")) {
					
					line = br.readLine();
					cityInfo = line.split(", ");
					int cityTotal = 0;
					while(!line.substring(0, 13).equals("# Distances: ")) {
						cityTotal++;
						City currCity = new City(cityInfo[0], Double.parseDouble(cityInfo[1]), Double.parseDouble(cityInfo[2]), P);
						cities.put(cityInfo[0], currCity);
						
						line = br.readLine();
						if (line == null) {
							System.out.println("There is no Distances section, and therefore a path can not be defined!");
							return false;
						}
						cityInfo = line.split(", ");
					}
					if(P.verbosityLvl>=1) 
						System.out.printf("* Number of cities: %d\n", cityTotal);
				}
				line = br.readLine();
				cityInfo = line.split(", ");
				
				while(true) {
					
					if(!createCityPath(cities, cityInfo[0], cityInfo[1], Double.parseDouble(cityInfo[2]))){
						System.out.println(cityInfo[0] + " or " + cityInfo[1] + " are not defined cities, but are given in the distances section!");
						return false;
					}
					line = br.readLine();
					if (line == null) {
						break;
					}
					cityInfo = line.split(", ");	
				}
			}
			P.citiesMap = cities;
			return true;
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return true;
	}

	/*
	 * Used to create a city path while loading a file
	 * 
	 * RETURNS: boolean(true if the path was created successfully, false if not)
	 */
	private static boolean createCityPath(HashMap<String, City> cities, String c1, String c2, double dist) {
		
		City city1 = cities.get(c1);
		City city2 = cities.get(c2);
		
		if(!(city1.pathCreate(city2, dist) && city2.pathCreate(city1, dist)))		
			return false;
		
		return true;
	}

	
	/*
	 * Used in all searches
	 * 
	 * RETURNS: boolean(true if a path was found, false if not)
	 */
	private static boolean executeSearch(Problem P) {
		
		P.root = P.createNode(P.citiesMap.get(P.initLocation), null);//Sets root Node to initial city
		P.nodeCounter++;
	  P.frontier.add(P.root);
	  if(P.reached)
	  	P.reachedTable.put(P.root.City, 0.0);

		while(!P.frontier.isEmpty()) {
			P.currNode = P.frontier.remove();
			if(P.verbosityLvl >=2) {
				if(P.verbosityLvl >= 4) {
					String parent = P.currNode.Parent != null ?P.currNode.Parent.City.name + ")":"null)";
					P.stringPrint.append(String.format("  Expanding   : %-30s(p-> %-30s[f= %.1f; g= %.1f; h= %.1f]\n", P.currNode.City.name, parent, P.currNode.fN, P.currNode.gN, P.currNode.hN));
				}else{
					String parent = P.currNode.Parent != null ?P.currNode.Parent.City.name + ")":"null)";
					System.out.printf("  Expanding   : %-30s(p-> %-30s[f= %.1f; g= %.1f; h= %.1f]\n", P.currNode.City.name, parent, P.currNode.fN, P.currNode.gN, P.currNode.hN);
				}
			}
			if (P.currNode.City.name.equals(P.goalLocation)) {
				if(P.verbosityLvl == 5) {
					System.out.print(P.stringPrint.toString());
				}
				return true;
			}
			City[] currNodePaths = new City[P.currNode.City.pathCity.size()];
			currNodePaths = P.currNode.City.pathCity.keySet().toArray(currNodePaths);
			for(int i = 0; i < P.currNode.City.pathCity.size(); i++){
				Problem.Node createdNode = P.createNode(currNodePaths[i], P.currNode);
				if((!P.reached)||((P.reachedTable.get(currNodePaths[i]) == null || createdNode.gN < P.reachedTable.get(currNodePaths[i])))) {
					P.nodeCounter++;
					if(P.reached)
						P.reachedTable.put(currNodePaths[i], createdNode.gN);
					
					if(P.verbosityLvl >= 3) {
						if(P.verbosityLvl >= 4) {
							String parent = P.currNode.City.name + ")";
							P.stringPrint.append(String.format("    Adding    : %-30s(p-> %-30s[f= %.1f; g= %.1f; h= %.1f]\n", currNodePaths[i].name, parent, createdNode.fN, createdNode.gN, createdNode.hN));
						}else {
							String parent = P.currNode.City.name + ")";
							System.out.printf("    Adding    : %-30s(p-> %-30s[f= %.1f; g= %.1f; h= %.1f]\n", currNodePaths[i].name, parent, createdNode.fN, createdNode.gN, createdNode.hN);
						}
					}
					P.frontier.add(createdNode);
				}else {
					if(P.verbosityLvl >= 3) {
						if(P.verbosityLvl >= 4) {
							String parent = P.currNode.City.name + ")";
							P.stringPrint.append(String.format("    NOT adding: %-30s(p-> %-30s[f= %.1f; g= %.1f; h= %.1f]\n", currNodePaths[i].name, parent, createdNode.fN, createdNode.gN, createdNode.hN));
						}else {
							String parent = P.currNode.City.name + ")";
							System.out.printf("    NOT adding: %-30s(p-> %-30s[f= %.1f; g= %.1f; h= %.1f]\n", currNodePaths[i].name, parent, createdNode.fN, createdNode.gN, createdNode.hN);
						}
					}
				}
				if(P.verbosityLvl == 4) {
					System.out.print(P.stringPrint.toString());
					P.stringPrint = new StringBuilder("");
				}
			}
		}
		return false;
	}
	
	/*
	 * Holds all of the information necessary for defining the problem that the user wants to solve
	 */
	static private class Problem{
		
		//All defined by user command line argument
		private String inputFile = null;
		private String initLocation = null;
		private String goalLocation = null;
		private String searchAgthm = "a-star";
		private String heuristicFunction = "haversine";
		private boolean reached = true;
		private int verbosityLvl = 0;
		
		private City goalCity;
		
		private HashMap<String, City> citiesMap = null; //Holds the references to all cities in the input file (key is string of city)
		
		private Node root = null; //Holds reference to the root of the tree during search
		private Node currNode = null; //Holds reference to whatever node is currently being expanded during search
		private int nodeCounter = 0; //# of nodes that have been added to frontier
		
		private PriorityQueue<Node> frontier; //Holds all nodes on frontier
		private HashMap<City, Double> reachedTable = new HashMap<City, Double>(); //Holds City that has accessed and the fN value of the node that was created with the city
		
		private StringBuilder stringPrint = new StringBuilder();
		
		Problem(String[] args){
			for(int j = 0; j<args.length; j++){
				switch (args[j]) {
					case "-f":
						j++;
						inputFile = args[j];
						break;
					case "-i":
						j++;
						initLocation = args[j];
						break;
					case "-g":
						j++;
						goalLocation = args[j];
						break;
					case "-s":
						j++;
						searchAgthm = args[j];
						break;
					case "-h":
						j++;
						heuristicFunction = args[j];
						break;
					case "--no-reached":
						reached = false;
						break;
					case "-v":
						j++;
						verbosityLvl = Integer.parseInt(args[j]);
						break;
					}
			}
			frontier = new PriorityQueue<Node>(new ComparitorPriority());
					
		}
		
		private Node createNode(City C, Node Par) {
			return new Node(C, Par, this);
		}
		
		/*
		 * Used to hold information and references to other nodes during the search process
		 */
		static private class Node{
			private City City = null;
			private Node Parent = null;
			
			//Used to hold data used to create the frontier and determine distances
			private double gN;
			private double hN;
			private double fN;
			
			Node(City C, Node Par, Problem P){
				City = C;
				Parent = Par;
				gN = Parent != null ? this.setG(): 0.0;
				hN = this.setH(P.heuristicFunction, P.goalCity);
				fN = this.setF(P.searchAgthm);
			}
			
			// RETURN: double (The calculated gN value)
			double setG() {
				if(Parent==null)
					return 0.0;
				double d = Parent.gN + City.getPathCost(Parent.City);
				return d;
			}
			
			// RETURN: double (The calculated hN value)
			double setH(String heuristic, City goalCity) {
				hN = City.huristic != -1.0 ? City.huristic : City.getHuristic(heuristic, goalCity);
				return hN;
			}
			
			// RETURN: double (The calculated fN value)
			double setF(String search) {
				switch (search){
					case("uniform"):
						return gN;
					case("greedy"):
						return hN;
				}
				return gN+hN;
			}
		}
	}
	
	
	// Used for creating the frontier priority queue and order the nodes and the order that should be searched
	static class ComparitorPriority implements Comparator<Problem.Node>{
		public int compare(Problem.Node N1, Problem.Node N2) {
       
			double d1 = N1.fN; double d2 = N2.fN;

       if (d1-d2<0) {
           return -1;
       } else if (d1-d2>0) {
           return 1;
       } else {
           return 0;
       }
    }
	}
	
	//Used to hold the information for a city that was found in the file
	static private class City {
		private String name;
		private double latitude;
		private double longitude;
		private double huristic = -1.0;
		private HashMap<City, Double> pathCity = new HashMap<City, Double>();

		City(String n, double lat, double lon, Problem P){
			name 			= n;
			latitude  = lat;
			longitude = lon;
		}
		
		boolean pathCreate(City c, double d){
			pathCity.put(c, d);
			return true;
		}
		//PRE: Path exists
		protected double getPathCost(City C) {
			return pathCity.get(C);
		}
		
		double getHuristic(String huristic, City goalCity) {

			if(huristic.equals("euclidean"))
				return Math.sqrt(Math.pow(this.longitude - goalCity.longitude, 2) + Math.pow(this.latitude - goalCity.latitude, 2));
			
			//Calculating Haversine formula
			double chLon = Math.toRadians(longitude) - Math.toRadians(goalCity.longitude);
			double chLat = Math.toRadians(latitude) - Math.toRadians(goalCity.latitude);
			
			double a = (Math.pow((Math.sin(chLat/2)), 2)+Math.cos(Math.toRadians(goalCity.latitude))*Math.cos(Math.toRadians(this.latitude))*Math.pow((Math.sin(chLon/2)), 2));
			double c = 2*(Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
			
			this.huristic = 3958.8*c;
			return this.huristic;
		}
	}
}
