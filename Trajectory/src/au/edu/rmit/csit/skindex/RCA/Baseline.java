package au.edu.rmit.csit.skindex.RCA;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import kotlin.Pair;

public class Baseline {
	
	private static double alpha = 0.3;//test the effect 
	static int iteration_time = 150;
	
	//random access
	private static Map<Integer,Map<Integer,Double>> pointTermWeight = new HashMap<Integer, Map<Integer,Double>>();
	
	private static Map<Integer, Pair<Double, Double>> point_coordinate = new HashMap<>();
	private static Map<Integer, Pair<Double, Double>> point_range = new HashMap<>();

	
	//we will divide the whole interval into iteration_time intervals based on the range of weight.
	//we can store it using mapdb
	private static Map<Integer,Map<Integer,ArrayList<Integer>>> term_interval_points = new HashMap<Integer,Map<Integer,ArrayList<Integer>>>();
		
	//each cell will contain points belonging to this cell.
	
	private static Map<Integer, Map<Integer, ArrayList<Integer>>> cell_term_points = new HashMap<>();
	
	//keep it in main memory
	private static Map<Integer, ArrayList<Integer>> x_cell = new HashMap<>();

	//the resolution of the cell.
	private static int resolution = 256;
	//the range of whole space.
	private static double array[] = {-121,32,-118,35};//{-75,39,-73,42};
	private static double max_distance;
	/*
	 * initialize the x_cell and y_cell
	 */
	public static void divide_grid(){
		for(int i=0; i<resolution; i++){
			ArrayList<Integer> x_arrayList = new ArrayList<>();
			for(int j=0; j<resolution; j++){
				x_arrayList.add(combine(i, j, (int)Math.sqrt(resolution)));
			}
			x_cell.put(i,x_arrayList);
		}		
	}

	/*
	 * we will use mapdb to store the index.
	 */
	public static void storeToDisk(){
		
	}
	/*
	 * the maximum distance of whole space
	 */
	public static double get_max_dis(){
		return Math.sqrt(Math.pow((array[0]-array[2]),2)+Math.pow((array[1]-array[3]),2));
	}
	/*
	 * comoute the weight range of each term
	 */
	public static void term_range(String termWeight){
		try {
			Scanner in = new Scanner(new BufferedReader(new FileReader(termWeight)));
			while (in.hasNextLine()) {
				String line = in.nextLine();
				String[] parts = line.trim().split(",");
				for(int i =1 ; i<parts.length; i++){
					String[] point = parts[i].split(" ");
					int termID = Integer.valueOf(point[0]);
					double weight = Double.valueOf(point[1]);
					double max, min;
					if(point_range.containsKey(termID)){
						if(weight>point_range.get(termID).getFirst()){
							max = weight;
						}
						else
							max = point_range.get(termID).getFirst();
						if(weight<point_range.get(termID).getSecond()){
							min = weight;
						}else{
							min = point_range.get(termID).getSecond();
						}
					}else{
						max = weight;
						min = weight;
					}
					Pair<Double, Double> aPair = new Pair<Double, Double>(max, min);
					point_range.put(termID, aPair);
				}
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}


	/*
	 * create the index
	 */
	public static void creat_index(String termWeight, String coordinate, String pointTrajectory, String file){
		String line;
		/*
		 * textual lists
		 */
		divide_grid();
		term_range(termWeight);
		//put each term into different iteration based on the range of weight
		//term_interval_points(termID,aMap<list of iterations,list of pointIds>)
		try {
			Scanner in = new Scanner(new BufferedReader(new FileReader(termWeight)));
			while (in.hasNextLine()) {
				line = in.nextLine();
				String[] parts = line.trim().split(",");
				int pointid = Integer.valueOf(parts[0]);
				Map<Integer, Double> termweight = new HashMap<>();
				for(int i =1 ; i<parts.length; i++){
					String[] point = parts[i].split(" ");
					int termID = Integer.valueOf(point[0]);
					double weight = Double.valueOf(point[1]);
					Pair<Double, Double> aPair = point_range.get(termID);
					double tempa = aPair.getFirst();
					double tempb = aPair.getSecond();
					int iteration;
					if(tempa != tempb)
						iteration = iteration_time - (int) ((weight-aPair.getSecond())/(aPair.getFirst()-aPair.getSecond())*iteration_time);
					else {
						iteration = 0;
					}
					Map<Integer,ArrayList<Integer>> aMap = new HashMap<Integer, ArrayList<Integer>>();
					if(term_interval_points.containsKey(termID)){
						aMap = term_interval_points.get(termID);
					}
					ArrayList<Integer> arrayList = new ArrayList<>();
					if(aMap.containsKey(iteration))
						arrayList = aMap.get(iteration);
					arrayList.add(pointid);
					aMap.put(iteration, arrayList);					
					term_interval_points.put(termID, aMap);
					termweight.put(termID, weight);
				}
				pointTermWeight.put(pointid, termweight);
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		/*
		 * spatial list
		 */
		//put each point into different cell based on their coordinate
		//in a given cell, it stores a list of terms and a list of corresponding points.
		try {
			Scanner in = new Scanner(new BufferedReader(new FileReader(coordinate)));
			while (in.hasNextLine()) {
				line = in.nextLine();
				String[] parts = line.trim().split(",");
				int pointID = Integer.valueOf(parts[0]);
				double lat = Double.valueOf(parts[1]);
				double lng = Double.valueOf(parts[2]);
				if(lng < array[2] && lng > array[0] && lat > array[1] && lat < array[3]){
					int cell_id = Gridindex.ComputeCellid_nine(lat, lng, resolution, array[0],array[1],array[2],array[3]);					
					ArrayList<Integer> pointList = new ArrayList<>();					
					Pair<Double, Double> aPair = new Pair<Double, Double>(lat, lng);
					point_coordinate.put(pointID, aPair);					
					Map<Integer, ArrayList<Integer>> aMap = new HashMap<>();
					if(cell_term_points.containsKey(cell_id)){
						aMap = cell_term_points.get(cell_id);
					}
					Map<Integer, Double> term_weight = pointTermWeight.get(pointID);
					Iterator<Integer> iterator = term_weight.keySet().iterator();
					while(iterator.hasNext()){
						int term = iterator.next();
						pointList = new ArrayList<>();
						if(aMap.containsKey(term)){
							pointList = aMap.get(term);
						}
						pointList.add(pointID);
						aMap.put(term, pointList);
						cell_term_points.put(cell_id, aMap);
					}
				}
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}


	/*
	 * 
	 */
	public static int bitToint(int[] a, int length){
		int sum = 0;
		for(int i=0; i<length; i++){
			sum += a[i]*Math.pow(2, length-i-1);
		}
		return sum;
	}
	/*
	 * combine two integers to produce a new value
	 */
	public static int combine(int aid, int bid, int lengtho){
		int length = lengtho;
		int[] a =new int[length];
		int[] b =new int[length];
		while(length-- >= 1){
			a[length] = aid%2;
			aid /=2;
			b[length] = bid%2;
			bid /=2;
		}
		int com[] = new int[2*lengtho];
		for(int i = 0; i<lengtho; i++){
			com[2*i]= a[i];
			com[2*i+1] = b[i];
		}
		return bitToint(com, 2*lengtho);
	}

	/*
	 * 
	 */
	private static double max(double a, double b)
	{
		if (a >= b)
		{
			return a;
		}
		else
		{
			return b;
		}
	}
	/*
	 * compute the upper bound of textual similarity after i iterations.
	 */
	public static double upper_bound_text(int it, ArrayList<Integer> term){
		double sim = 0;
		for(int i=0; i<term.size(); i++){
			if (point_range.containsKey(term.get(i))) {
				Pair<Double, Double> xPair = point_range.get(term.get(i));
				sim += xPair.getFirst() - it * (xPair.getFirst() - xPair.getSecond()) / iteration_time;
			}
		}
		sim /= term.size();
		return sim;
	}
	/*
	 * compute the upper bound of spatial similarity after i iterations.
	 */
	public static double upper_bound_spatial(int it, double max) {
		double cell_length = (array[2]-array[0])/iteration_time;
		double bound = (max - it*cell_length)/max;
		return bound;
	}
	/*
	 * union of two lists
	 */
	public static <T> List<T> union(List<T> list1, List<T> list2) {
        Set<T> set = new HashSet<T>();
        if(list1!=null)
        	set.addAll(list1);
        if(list2!=null)
        	set.addAll(list2);
        return new ArrayList<T>(set);
    }
	/*
	 * we use the number to extend
	 */
	public static ArrayList<Integer> SpatialListByNumber(int x, int y, int radius, int minradius){
		ArrayList<Integer> cells = new ArrayList<>();		
		for(int i = x-radius; i<=x+radius; i++){
			if (x_cell.containsKey(i)) {
				ArrayList<Integer> x_cells = x_cell.get(i);
				for (int j = y - radius; j <= y + radius; j++) {
					if (Math.abs(i - x) <= minradius && Math.abs(j - y) <= minradius) {
						continue;
					}
					if (j>=0 && j<resolution && !cells.contains(x_cells.get(j)))
						cells.add(x_cells.get(j));
				}
			}
		}
		return cells;
	}
	/*
	 *compute the point similarity based on random access
	 */
	public static double randomAccessPoint1(int point, ArrayList<Integer> terms, double x, double y) {
		double sim = 0;
		if (pointTermWeight.containsKey(point)) {
			Map<Integer, Double> term_weight = pointTermWeight.get(point);
		//	System.out.println(term_weight);
			for (int i = 0; i < terms.size(); i++) {
				if (term_weight.containsKey(terms.get(i))) {
					sim += term_weight.get(terms.get(i));
				}
			}
			sim /= terms.size();
			double spatial_sim =0;
			if(point_coordinate.containsKey(point)){
				Pair<Double, Double> pair = point_coordinate.get(point);
				double distance = Math.sqrt(Math.pow(x - pair.getFirst(), 2) + Math.pow((y - pair.getSecond()), 2));
				spatial_sim = (max_distance - distance) / max_distance;
			}
			if (spatial_sim > 0)
				return alpha * sim + spatial_sim * (1 - alpha);
			else
				return 0;
		} else {
			return 0;
		}
	//	return sim;
	}
	/*
	 * trajectory search using two level TA, we will employ partial random access of trajectory 
	 * to make the lower bound more tighter to achieve early termination.
	 */
	public static void finding_bound(String query_file, int k){
		System.out.println("two-levels TA search");
		long time = 0;
		int candidate_number = 0;
		long random_access_time = 0;
		Map<Integer, Map<Integer, Double>> tramap = new HashMap<Integer, Map<Integer, Double>>();
		//store the lower bound
		Map<Integer, Double> point_sim = new HashMap<Integer, Double>();
		Map<Integer, Double> q_score;
		ArrayList<Double> lower_bound = new ArrayList<>();
		Map<Pair<Double, Double>, ArrayList<Integer>> query = new HashMap<>();	
		try {
			//form the query.
			Scanner in = new Scanner(new BufferedReader(new FileReader(query_file)));
			Map<Integer, ArrayList<Integer>> query_terms = new HashMap<>();
			Map<Integer, Pair<Double, Double>> query_coordinate = new HashMap<>();
			int queryid=0;
			//initializing the two levels query
			while (in.hasNextLine()) {				
				String line = in.nextLine();
				String[] parts = line.trim().split(",");
				double lat = Double.valueOf(parts[1]);
				double lng = Double.valueOf(parts[2]);
				ArrayList<Integer> terms = new ArrayList<>();
				for(int i=3; i<parts.length;i++){
					terms.add(Integer.valueOf(parts[i]));
				}
				query_terms.put(queryid, terms);
				query_coordinate.put(queryid, new Pair<Double, Double>(lat,lng));
				query.put(new Pair<Double, Double>(lat,lng), terms);
				queryid++;
			}
			in.close();
			//compute the upper bound of every query in each iteration
			long startTime = System.nanoTime();
			double[] bounds = new double[query_coordinate.size()];
			for (int it = 0; it < iteration_time; it++) {
				double ub = 0;
				for (int q = 0; q < query_coordinate.size(); q++) {
					ArrayList<Integer> all = new ArrayList<>();
					ArrayList<Integer> term = query_terms.get(q);				
					bounds[q] = alpha * upper_bound_text(it, term)
							+ (1 - alpha) * upper_bound_spatial(it, max_distance);
					ub += bounds[q];
					Pair<Double, Double> coordinate = query_coordinate.get(q);
					double lat = coordinate.getFirst();
					double lng = coordinate.getSecond();
					int x = (int)((lat-array[1])/(array[3]-array[1])*resolution);
					int y = (int)((lng-array[0])/(array[2]-array[0])*resolution);
					//term_interval_points(termID,aMap<list of iterations,list of pointIds>)
					for (int i = 0; i < term.size(); i++) {
						ArrayList<Integer> poIntegers = null;
						if (term_interval_points.containsKey(term.get(i))) {
							Map<Integer, ArrayList<Integer>> interval_points = term_interval_points.get(term.get(i));
							if (interval_points.containsKey(it)) {
								poIntegers = interval_points.get(it);
							}
							//get all points which contain the terms in a query in a given iteration
							all = (ArrayList<Integer>) union(all, poIntegers);
						}
					}
					//in a given cell, it stores a list of terms and a list of corresponding points.
					//get all points in the cell which the query located in.
					double radius = resolution / iteration_time * (it+1);
					long s = System.nanoTime();
					ArrayList<Integer> poIntegers = SpatialListByNumber(x, y, (int)radius, (int)(resolution/iteration_time)*it);
					time += System.nanoTime() - s;
					for (int ip = 0; ip < poIntegers.size(); ip++) {
						if (cell_term_points.containsKey(poIntegers.get(ip))) {
							Map<Integer, ArrayList<Integer>> aMap = cell_term_points.get(poIntegers.get(ip));
							Iterator<Integer> iter = aMap.keySet().iterator();
							while (iter.hasNext()) {

							    all = (ArrayList<Integer>) union(all, aMap.get(iter.next()));

							}
							/*
							for (int i = 0; i < term.size(); i++) {
								if (aMap.containsKey(term.get(i)))
									all = (ArrayList<Integer>) union(all, aMap.get(term.get(i)));
							}
							*/
						}
					}					
					if (all.size() == 0)
						continue;
					candidate_number += all.size();
					for (int j = 0; j < all.size(); j++) {
						int pointid = all.get(j);
						if(true){
							long temp = System.nanoTime();
							double sim = randomAccessPoint1(pointid, term, lat, lng);
							random_access_time += System.nanoTime()-temp;							
							//int tra_id = point_trajectory.get(pointid);
							double temp_lower = 0;
			    			if(tramap.containsKey(pointid)){
			    				q_score = tramap.get(pointid);
			    				double sim_tra = point_sim.get(pointid);
			    				if(q_score.containsKey(q)){
			    					if(q_score.get(q)<sim){
			    						q_score.put(q, sim);
			    						point_sim.put(pointid, sim_tra-q_score.get(q)+sim);			    						
			    						temp_lower = sim_tra-q_score.get(q)+sim;
			    					}
			    				}else{
			    					q_score.put(q, sim);
			    					point_sim.put(pointid, sim_tra+sim);
			    					temp_lower = sim_tra+sim;
			    				}
			    			}
			    			else{
			    				q_score = new HashMap<Integer, Double>();
			    				q_score.put(q, sim);
			    				point_sim.put(pointid, sim);
			    				temp_lower = sim;
			    			}
			    			tramap.put(pointid, q_score);			    						    						    			
			    			if(lower_bound.size()==k){
			    				Collections.sort(lower_bound, Collections.reverseOrder());
			    				if(temp_lower>lower_bound.get(k-1)){
			    					lower_bound.remove(k-1);			    				
			    					lower_bound.add(temp_lower);
			    				}			    				
			    			}else{
			    				lower_bound.add(temp_lower);
			    			}			    			
						}
					}
				}
				double lb = lower_bound.get(k-1);
				System.out.println(it + " the bound is " + lb + "\t" + ub);
				if (lb > ub) {
					break;
				}
			}
			// build the upper bound and compute the real similarity
			long time1 = System.nanoTime();
			//we choose those point in each ranking list that have similarity than the bound to compute the bound.
			//refinement(tramap, point_sim, bounds, query_coordinate.size(), query_terms, query_coordinate, k);
		
			long endTime = System.nanoTime();
			System.out.println((endTime - startTime) / 1000000000.0);
			System.out.println("Scanning candidates number is "+ candidate_number+"\nthe exploration on spatial list cost "+time / 1000000000.0);	
			System.out.println("refinmenting time is "+(System.nanoTime()-time1)/1000000000.0);
			System.out.println("the time spent on random access is "+ random_access_time / 1000000000.0);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}


	
	public static void main(String[] args){
		String query_file = "Dataset/LA/query.txt";
		String coordidate = "Dataset/LA/IR_coordinate.txt";
		String pointTra = "Dataset/LA/point-trajectory.txt";
		String newterm = "Dataset/LA/new_word_tfidf_weight.txt";
		String index = "Dataset/LA/Index";		
		String query_batch = "Dataset/LA/10_IR.txt";

		max_distance = get_max_dis();		
		creat_index(newterm, coordidate, pointTra, index);
		finding_bound(query_file, 20);

	}
}
