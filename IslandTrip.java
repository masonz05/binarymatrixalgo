package comp2402a5;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.*;
public class IslandTrip {
	
	
	/**
	 * Read lines one at a time from r.  Discover "islands" of 1s in the input grid.
	 * Read the specification to see where "bridges" (i.e., edges) exist between islands.
	 * Find the length of the shortest path from the smallest island to the biggest 
	 * island.
	 * 
	 * Ouput should be the size of each island, in sorted order, separated by newline characters.
	 * The last line should be the length of the shortest path from the smallest island to the largest.
	 * 
	 * @param r the reader to read from
	 * @param w the writer to write to
	 * @throws IOException
	 */

	public static void doIt(BufferedReader r, PrintWriter w) throws IOException {
		List<String> islandLines = new ArrayList<>();
		//for loop to read all of the lines
		for (String currLine = r.readLine(); currLine != null; currLine = r.readLine()) {
			islandLines.add(currLine);
		}

		int rows = islandLines.size();
		int columns = islandLines.get(0).length();
		char[][] mapGrid = new char[rows][columns];
		//i make a grid so it's easier later on to find islands from each tile
		for (int i = 0; i < rows; i++) {
			mapGrid[i] = islandLines.get(i).toCharArray();
		}

		List<Integer> islands = islandFinder(mapGrid);

		if (islands.size() > 0) { //find all the islands and their sizes
			Map<Integer, List<Integer>> graph = buildGraph(islands);
			int smallestIsland = islands.get(0); //build the graph
			int largestIsland = islands.get(islands.size() - 1);
			int pathLength = findShortestPath(graph, smallestIsland, largestIsland);
			//find the shortest path between the smallest and the largest
			for (int size : islands) {
				w.println(size); //pritn size
			}
			w.println(pathLength); //print lengths
		}
	}
	// Function to perform BFS on the grid starting from the cell (startX, startY)
	public static int bfs(char[][] grid, int startingX, int startingY, boolean[][] visited) {

		Queue<int[]> queue = new LinkedList<>();
		queue.offer(new int[]{startingX, startingY});
		//add the starting x y coord into the queue
		visited[startingX][startingY] = true;
		int islandSize = 0;
		//i set directions to be all eight possible directions, vertical horizontal, diagonal
		int[][] directions = {
				{-1, 0}, {1, 0}, {0, -1}, {0, 1},
				{-1, -1}, {-1, 1}, {1, -1}, {1, 1}
		};

		while (!queue.isEmpty()) {
			int[] cell = queue.poll();
			int x = cell[0];
			int y = cell[1];
			islandSize++;
			//i go around the all the directions
			for (int[] direction : directions) {
				int newX = x + direction[0];
				int newY = y + direction[1];
				//if the tile is a land tile and in bounds with the starting cell
				if (newX >= 0 && newX < grid.length && newY >= 0 && newY < grid[0].length
						&& !visited[newX][newY] && grid[newX][newY] == '1') {
					visited[newX][newY] = true;
					queue.offer(new int[]{newX, newY}); //quueue that cell to have its directions
					//explored to see if its also connected to other land cells
				}
			}
		}

		return islandSize; //return island size
	}

	// finds islands
	public static List<Integer> islandFinder(char[][] grid) {
		List<Integer> islandSizes = new ArrayList<>();

		if (grid == null || grid.length == 0) {
			return islandSizes;
		}

		int rows = grid.length;
		int columns = grid[0].length;
		//make another grid to mark which tiles have been visited or not
		boolean[][] visited = new boolean[rows][columns];

		for (int n = 0;n < rows; n++) {
			for (int j = 0; j < columns; j++) {
				if (grid[n][j] == '1' && !visited[n][j]) {// if the tile is
					//a land tile and it hasn't been visited
					//i use the bfs method i created to find the size of the island
					//the tile is connected to
					int islandSize = bfs(grid, n, j, visited);
					islandSizes.add(islandSize);
				}
			}
		}

		Collections.sort(islandSizes); //sort by size
		return islandSizes;
	}

	// small function to calculate sum of digits
	public static int sumOfDigits(int dig) {
		int sum = 0;
		while (dig != 0) {
			sum += dig % 10;
			dig /= 10;
		}
		return sum;
	}

	//i build a graph based on bridge connectiosn
	public static Map<Integer, List<Integer>> buildGraph(List<Integer> islandSizes) {
		Map<Integer, List<Integer>> islandBridges = new HashMap<>(); //stores island and their bridges
		for (int size : islandSizes) {
			islandBridges.put(size, new ArrayList<>());
		} //create a adjacency list for each island
		// go through each pair of islands to calculate bridges
		for (int i = 0; i < islandSizes.size(); i++) {
			int currentSize = islandSizes.get(i);
			int minDiff = Integer.MAX_VALUE;
			int maxDiff = Integer.MAX_VALUE;
			Integer minLarger = null;
			Integer maxSmaller = null;

			for (int j = 0; j < islandSizes.size(); j++) {
				if (i == j) continue;

				int size = islandSizes.get(j);
				if (size > currentSize && size - currentSize < minDiff) {
					minDiff = size - currentSize;
					minLarger = size;
				}
				if (size < currentSize && currentSize - size < maxDiff) {
					maxDiff = currentSize - size;
					maxSmaller = size;
				}
				if (sumOfDigits(currentSize) == sumOfDigits(size)) {
					islandBridges.get(currentSize).add(size);
				}// add brdige based on sum of digist
			}

			if (minLarger != null) {
				islandBridges.get(currentSize).add(minLarger);
			}//add bridge to the smallest larger islands and largest smaller island
			if (maxSmaller != null) {
				islandBridges.get(currentSize).add(maxSmaller);
			}
		}

		return islandBridges;
	}

	// this finds shortest path
	public static int findShortestPath(Map<Integer, List<Integer>> graphAdj, int startIslandSize, int endIslandSize) {
		Queue<Integer> queue = new LinkedList<>(); //queue for bfs
		Set<Integer> visited = new HashSet<>(); //track visited nodes
		Map<Integer, Integer> distanceBetweenIslands = new HashMap<>();

		queue.offer(startIslandSize);
		visited.add(startIslandSize);
		distanceBetweenIslands.put(startIslandSize, 0);

		while (!queue.isEmpty()) {
			int current = queue.poll();
			if (current == endIslandSize) { //if current is the same as end, the shortest
				//path is found
				return distanceBetweenIslands.get(current);
			}

			for (int neighbor : graphAdj.get(current)) {
				if (!visited.contains(neighbor)) { //if the neighbor isn't visited
					visited.add(neighbor);
					distanceBetweenIslands.put(neighbor, distanceBetweenIslands.get(current) + 1);
					//set distance as plus one distance to the current island
					queue.offer(neighbor);//queue the neighbour
				}
			}
		}

		return -1;
	}


	// Driver function
	public static void main(String[] args) {
		try {
			BufferedReader r;
			PrintWriter w;
			if (args.length == 0) {
				r = new BufferedReader(new InputStreamReader(System.in));
				w = new PrintWriter(System.out);
			} else if (args.length == 1) {
				r = new BufferedReader(new FileReader(args[0]));
				w = new PrintWriter(System.out);
			} else {
				r = new BufferedReader(new FileReader(args[0]));
				w = new PrintWriter(new FileWriter(args[1]));
			}
			long start = System.nanoTime();
			doIt(r, w);
			w.flush();
			long stop = System.nanoTime();
			System.out.println("Execution time: " + 1e-9 * (stop - start));
		} catch (IOException e) {
			System.err.println(e);
			System.exit(-1);
		}
	}
}
