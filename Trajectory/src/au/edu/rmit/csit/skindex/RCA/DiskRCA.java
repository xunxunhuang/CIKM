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
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
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

import javax.sound.midi.Instrument;

import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.impl.tuple.Tuples;
import org.mapdb.*;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.SystemException;

import com.google.common.primitives.Ints;

import au.edu.rmit.csit.index.Gridindex;
import au.edu.rmit.csit.reverse.main;
import kotlin.Pair;

public class DiskRCA{
	
	private static double alpha = 0.3;
	private static int iteration_time = 150;
	
//	private static DB db = DBMaker.fileDB("C:/Users/e29944/workspace-java/Trajectory/Data/ETQ/LA/Index/LA_MapDB.db").make();
	
	private static Map<Integer,Map<Integer,Double>> pointTermWeight = new HashMap<>();//(Map<Integer, Map<Integer, Double>>) db.hashMap("pointTermWeight").make();
	
	private static Map<Integer, Pair<Double, Double>> point_range = new HashMap<>();//memory
	//we will divide the whole interval into iteration_time intervals based on the range of weight.
	private static Map<Integer,Map<Integer,ArrayList<Integer>>> term_interval_points = new HashMap<Integer,Map<Integer,ArrayList<Integer>>>();
		
	//each cell will contain points belonging to this cell.	
	private static Map<Integer, Pair<Double, Double>> point_coordinate = new HashMap<>();
	private static Map<Integer, Map<Integer, ArrayList<Integer>>> cell_term_points = new HashMap<>();
	private static Map<Integer, ArrayList<Integer>> x_cell = new HashMap<>();
	
	// building the map between point and trajectory
	private static Map<Integer, Integer> point_trajectory = new HashMap<>();
	private static Map<Integer, ArrayList<Integer>> trajectory_points = new HashMap<>();

	//the resolution of the cell.
	private static int resolution = 256;
	//the range of whole space.
	private static double array[] = {-121,32,-118,35};
	private static double max_distance;
	private static double internal_bound_time=0;
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
	public static Map sortByValue_des(Map map) {
		List list = new LinkedList(map.entrySet());
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o2)).getValue()).compareTo(((Map.Entry) (o1)).getValue());
			}
		});
		Map result = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	public static void write(String fileName, String content) {   
        RandomAccessFile randomFile = null;  
        try {       
            randomFile = new RandomAccessFile(fileName, "rw");     
            long fileLength = randomFile.length();     
            randomFile.seek(fileLength);     
            randomFile.writeBytes(content);      
        } catch (IOException e) {     
            e.printStackTrace();     
        } finally{  
            if(randomFile != null){  
                try {  
                    randomFile.close();  
                } catch (IOException e) {  
                    e.printStackTrace();  
                }  
            }
        }
    }
	/*
	 * the maximum distance of whole space
	 */
	public static double get_max_dis(){
		return Math.sqrt(Math.pow((array[0]-array[2]),2)+Math.pow((array[1]-array[3]),2));
	}
	/*
	 * the range of term
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
	 * 
	 */
	public static void tf_idf(String termWeight, String term_tfidf){
		Map<Integer, Integer> term_count = new HashMap<>();
		Map<Integer, Double> term_weight = new HashMap<>();
		int all=0;
		try {
			Scanner in = new Scanner(new BufferedReader(new FileReader(termWeight)));
			while (in.hasNextLine()) {
				all++;
				String line = in.nextLine();
				String[] parts = line.trim().split(",");
				for(int i =1 ; i<parts.length; i++){
					String[] point = parts[i].split(" ");
					int termID = Integer.valueOf(point[0]);
					int count =1;
					if(term_count.containsKey(term_count)){
						count = term_count.get(termID)+1;											
					}
					term_count.put(termID, count);					
				}
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Iterator<Integer> tiIterator = term_count.keySet().iterator();
		while(tiIterator.hasNext()){
			int termid = tiIterator.next();
			double count = term_count.get(termid);
			double weight = Math.log10(1+all/count);
			term_weight.put(termid, weight);
		}
		try {
			Scanner in = new Scanner(new BufferedReader(new FileReader(termWeight)));
			while (in.hasNextLine()) {
				String line = in.nextLine();
				String[] parts = line.trim().split(",");
				String content = parts[0]+",";
				for(int i =1 ; i<parts.length; i++){
					String[] point = parts[i].split(" ");
					int termID = Integer.valueOf(point[0]);
					double weight = Double.valueOf(point[1])*term_weight.get(termID);
					content += Integer.toString(termID)+" "+Double.toString(weight)+",";					
				}
				content +="\n";
				System.out.println(content);
				write(term_tfidf, content);
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	/*
	 * filter points out side the grid index.
	 */
	public static void filerbyrange(String termWeight, String coordinate, double[] array, String newterm){
		ArrayList<Integer> arrayList = new ArrayList<>();
		try {
			Scanner in = new Scanner(new BufferedReader(new FileReader(coordinate)));
			while (in.hasNextLine()) {
				String line = in.nextLine();
				String[] parts = line.trim().split(",");
				int pointID = Integer.valueOf(parts[0]);
				double lat = Double.valueOf(parts[1]);
				double lng = Double.valueOf(parts[2]);
				if(lng < array[2] && lng > array[0] && lat > array[1] && lat < array[3]){
					arrayList.add(pointID);
				}
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			Scanner in = new Scanner(new BufferedReader(new FileReader(termWeight)));
			while (in.hasNextLine()) {
				String line = in.nextLine();
				String[] parts = line.trim().split(",");
				int pointid = Integer.valueOf(parts[0]);
				if(arrayList.contains(pointid)){
					write(newterm, line+"\n");
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
		System.out.println(term_interval_points.size());
		/*
		 * spatial list
		 */
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
		/*
		 * relationship between point and trajectory
		 */
		try {
			Scanner in = new Scanner(new BufferedReader(new FileReader(pointTrajectory)));
			while (in.hasNextLine()) {
				line = in.nextLine();
				String[] parts = line.trim().split(" ");
				int pointID = Integer.valueOf(parts[0]);
				int traID = Integer.valueOf(parts[1]);
			    point_trajectory.put(pointID, traID);
			    ArrayList<Integer> poIntegers = new ArrayList<>();
			    if(trajectory_points.containsKey(traID)){
			    	poIntegers= trajectory_points.get(traID);
			    }
			    poIntegers.add(pointID);
			    trajectory_points.put(traID, poIntegers);
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}		
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
	 * get the cells based on combination of code
	 * query: the cell where query point locates.
	 * get all the cells near query within radius;
	 */
	public static ArrayList<Integer> exploreForwardSpatialList(int query, int radius, int length, int minradius){
		ArrayList<Integer> cells = new ArrayList<>();
		ArrayList<Integer> bit_id = new ArrayList<Integer>();
		int temp = query;
		int lengthtemp = 2*length;
		while(lengthtemp-->0){
			bit_id.add(temp%2);
			temp = temp/2;
		}
		int a[] = new int[bit_id.size()/2];
		int b[] = new int[bit_id.size()/2];
		for(int i=bit_id.size();i>0; i--){
			if(i%2>0){
				a[bit_id.size()/2-(i/2+1)] = bit_id.get(i-1);
			}else{
				b[bit_id.size()/2-(i/2)] = bit_id.get(i-1);
			}
		}		
		int xid = bitToint(a, a.length);
		int yid = bitToint(b, b.length);
		for(int i = xid-radius; i<=xid+radius; i++){
			for(int j = yid -radius; j<=yid+radius; j++){
				if(Math.abs(i-xid)<=minradius && Math.abs(j-yid)<=minradius){
					continue;
				}
				int cell = combine(i,j,length);
				if(cell>=0 && !cells.contains(cell))
					cells.add(cell);
			}
		}
		return cells;
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
	 * compute the upper bound of textual similarity after i iterations.
	 */
	public static double upper_bound_text(int it, ArrayList<Integer> term){
		double sim = 0;
		for(int i=0; i<term.size(); i++){
			Pair<Double, Double> xPair = point_range.get(term.get(i));			
			sim += xPair.getFirst()-it*(xPair.getFirst()-xPair.getSecond())/iteration_time;
		}
		sim /= term.size();
		return sim;
	}
	/*
	 * compute the upper bound of textual similarity after i iterations.
	 */
	public static double upper_bound_text1(int it, int[] term){
		double sim = 0;
		for(int i=0; i<term.length; i++){
			Pair<Double, Double> xPair = point_range.get(term[i]);			
			sim += xPair.getFirst()-it*(xPair.getFirst()-xPair.getSecond())/iteration_time;
		}
		sim /= term.length;
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
	 *compute the point similarity based on random access
	 */
	public static double randomAccessPoint(int point, int[] terms, double x, double y) {
		double sim = 0;
		Map<Integer, Double> term_weight = pointTermWeight.get(point);
		for(int i=0; i<terms.length; i++){
			if(term_weight.containsKey(terms[i])){
				sim +=term_weight.get(terms[i]);
			}
		}
		sim /= terms.length;
		Pair<Double, Double> pair = point_coordinate.get(point);
		double distance = Math.sqrt(Math.pow(x-pair.getFirst(),2)+Math.pow((y-pair.getSecond()), 2));		
		double spatial_sim = (max_distance-distance)/max_distance;
		if(spatial_sim>0)
			return alpha*sim+spatial_sim*(1-alpha);
		else
			return 0;
	//	return sim;
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
			Pair<Double, Double> pair = point_coordinate.get(point);
			double distance = Math.sqrt(Math.pow(x - pair.getFirst(), 2) + Math.pow((y - pair.getSecond()), 2));
			double spatial_sim = (max_distance - distance) / max_distance;
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
	 *compute the trajectory similarity based on random access
	 */
	public static double randomAccessTrajectory(Map<Pair<Double, Double>, ArrayList<Integer>> query, int tra_id) {
		Iterator<Pair<Double, Double>> aIterator= query.keySet().iterator();
		double whole_sim = 0;
		while(aIterator.hasNext()){
			Pair<Double, Double> coordinate = aIterator.next();
			double x = coordinate.getFirst();
			double y = coordinate.getSecond();
			ArrayList<Integer> query_terms = query.get(coordinate);
			ArrayList<Integer> points = trajectory_points.get(tra_id);
			double max_sim =0;
			for(int p_i = 0; p_i<points.size(); p_i++){				
				int point = points.get(p_i);
				if (pointTermWeight.containsKey(point)) {
					double sim = 0;
					Map<Integer, Double> terms = pointTermWeight.get(point);
					for (int term_i = 0; term_i < query_terms.size(); term_i++) {
						int term_id = query_terms.get(term_i);
						if (terms.containsKey(term_id))
							sim += terms.get(term_id);
					}
					sim = sim/query_terms.size();
					Pair<Double, Double> pair = point_coordinate.get(point);
					double distance = Math.sqrt(Math.pow(x - pair.getFirst(), 2) + Math.pow((y - pair.getSecond()), 2));
					double spatial_sim = (max_distance - distance) / max_distance;
					if (spatial_sim > 0)
						sim = alpha * sim + spatial_sim * (1 - alpha);
					else
						sim = alpha * sim;
					if (sim > max_sim)
						max_sim = sim;
				}
			}
			whole_sim += max_sim;
		}
		return whole_sim;
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
	 * pre-process the ranking list and eliminate those points.
	 */
	public static double preprocess_ranking(Map<Integer, Map<Integer,Double>> query_result) {
		
		return 0;
	}	
	/*
	 * the lower bound of checked result.
	 * we will only keep those points which has more than half .
	 * for those point which has more points, we will conduct random access based on additional index.
	 * how to choose the best points from a set of points in trajectory is a problem.
	 */
	public static double old_lower_bound(Map<Integer, Map<Integer,Double>> query_result, int topk){
		Iterator<Integer> itr1 = query_result.keySet().iterator();
		Map<Integer, Map<Integer, Double>> tramap = new HashMap<Integer, Map<Integer, Double>>();
		Map<Integer, Double> q_score;
    	while(itr1.hasNext()){
    		int query_id = itr1.next();
    		Map<Integer,Double> al = query_result.get(query_id);
    		Iterator<Integer> itr2 = al.keySet().iterator();
    		int point=0;
    		while(itr2.hasNext()){    			
    			point = itr2.next();//last one
    			int tra_id = point_trajectory.get(point);    			
    			double sim = al.get(point);
    			if(tramap.containsKey(tra_id)){
    				q_score = tramap.get(tra_id);
    				if(q_score.containsKey(query_id)){
    					if(q_score.get(query_id)<sim){
    						q_score.put(query_id, sim);
    					}
    				}else{
    					q_score.put(query_id, sim);
    				}   				
    			}
    			else{
    				q_score = new HashMap<Integer, Double>();
    				q_score.put(query_id, sim);
    			}
    			tramap.put(tra_id, q_score);
    		}
    	}    	
    	Iterator<Integer> itr = tramap.keySet().iterator();
    	ArrayList<Double> similarity = new ArrayList<>();
    	while(itr.hasNext()){
    		int tra_id = itr.next();
    		Map<Integer, Double> ab = tramap.get(tra_id);
    		Iterator<Integer> itr2 = ab.keySet().iterator();
    		double sim = 0;
    		while(itr2.hasNext()){
    			int q_id = itr2.next();
    			sim += ab.get(q_id);
    		}
    		similarity.add(sim);
    	}
    	Collections.sort(similarity,Collections.reverseOrder());
		if(similarity.size()>topk)
			return similarity.get(topk-1);
		else
			return 0;
	}
	/*
	 * upper bound based on ranking list
	 */
	private static double upper_bound(Map<Integer, Map<Integer, Double>> rank_list) {
		// TODO Auto-generated method stub
		double upper_bound = 0;
		Iterator<Integer> itr1 = rank_list.keySet().iterator();
    	while(itr1.hasNext()){
    		int y1 = itr1.next();	        		
    		Map<Integer,Double> al = rank_list.get(y1);
    		upper_bound += al.get(al.keySet().toArray()[al.size()-1]);
    	}
		return upper_bound;
	}
	/*
	 * test for spatial keyword query.
	 * we can predict the iteration time for next step as it cost much time to do 
	 */
	public static void point_search(String query_file, int k){		
		ArrayList<Double> result = new ArrayList<>();
		double kth=0;		
		int candidate_number = 0;
		try {
			Scanner in = new Scanner(new BufferedReader(new FileReader(query_file)));
			while (in.hasNextLine()) {
				String line = in.nextLine();
				String[] parts = line.trim().split(",");
				double lat = Double.valueOf(parts[1]);
				double lng = Double.valueOf(parts[2]);
				int x = (int)((lat-array[1])/(array[3]-array[1])*resolution);
				int y = (int)((lng-array[0])/(array[2]-array[0])*resolution);
				int term[] = new int[parts.length-3];
				for(int i=3; i<parts.length;i++){
					term[i-3] = Integer.valueOf(parts[i]);
				}
				long startTime = System.nanoTime();
				long time = 0;
				for (int it = 0; it < iteration_time; it++) {
					ArrayList<Integer> all = new ArrayList<>();					
					for (int i = 0; i < term.length; i++) {
						ArrayList<Integer> poIntegers = null;
						if (term_interval_points.containsKey(term[i])) {
							Map<Integer, ArrayList<Integer>> interval_points = term_interval_points.get(term[i]);
							if (interval_points.containsKey(it)) {
								poIntegers = interval_points.get(it);
							}
							all = (ArrayList<Integer>) union(all, poIntegers);
						}
					}
					double radius = resolution/iteration_time*(it+1);
					long s = System.nanoTime();
					ArrayList<Integer> poIntegers = SpatialListByNumber(x, y, (int)radius, (int)(resolution/iteration_time)*(it));
					time+=System.nanoTime()-s;
					for (int ip = 0; ip < poIntegers.size(); ip++) {
						if(cell_term_points.containsKey(poIntegers.get(ip))){
							Map<Integer, ArrayList<Integer>> aMap = cell_term_points.get(poIntegers.get(ip));
							for(int i=0; i<term.length; i++){
								if(aMap.containsKey(term[i]))
									all = (ArrayList<Integer>) union(all, aMap.get(term[i]));
							}
						}
					}
					if(all.size()==0)
						continue;
					candidate_number += all.size();
					for (int j = 0; j < all.size(); j++) {
						int pointid = all.get(j);		
						double sim = randomAccessPoint(pointid, term, lat, lng);
						Collections.sort(result,Collections.reverseOrder());//descendly sort
						
						if (result.size() == k) {
							if (sim > result.get(k - 1)) {
								if(sim>result.get(k-2))
									kth = result.get(k-2);
								else{
									kth = sim;
								}
								result.remove(k - 1);
								result.add(sim);
							}
						} else {
							result.add(sim);
						}
					}
					double ub = alpha * upper_bound_text1(it, term) + (1 - alpha) * upper_bound_spatial(it, max_distance);
					System.out.println(it+" the bound is "+kth + "\t"+ub);
					if (kth > ub) {
						break;
					}
				}
				long endTime = System.nanoTime();
				System.out.println((endTime - startTime) / 1000000000.0);
				System.out.println("the exploration on spatial list cost "+time /1000000000.0+"\ncandidates number is "+ candidate_number);
				System.out.println(result);
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	/*
	 * test for spatial keyword query.
	 * we can predict the iteration time for next step as it cost much time to do spatial exploration
	 */
	public static void point_search_extend(String query_file, int k){		
		ArrayList<Double> result = new ArrayList<>();
		double kth=0;		
		int candidate_number = 0;
		try {
			Scanner in = new Scanner(new BufferedReader(new FileReader(query_file)));
			while (in.hasNextLine()) {
				String line = in.nextLine();
				String[] parts = line.trim().split(",");
				double lat = Double.valueOf(parts[1]);
				double lng = Double.valueOf(parts[2]);
				int x = (int)((lat-array[1])/(array[3]-array[1])*resolution);
				int y = (int)((lng-array[0])/(array[2]-array[0])*resolution);
				int term[] = new int[parts.length-3];
				for(int i=3; i<parts.length;i++){
					term[i-3] = Integer.valueOf(parts[i]);
				}
				long startTime = System.nanoTime();
				long time = 0;
				int increment = 1;
				for (int it = 0; it < iteration_time; ) {
					ArrayList<Integer> all = new ArrayList<>();					
					for (int i = 0; i < term.length; i++) {
						ArrayList<Integer> poIntegers = null;
						if(term_interval_points.containsKey(term[i])){
							Map<Integer, ArrayList<Integer>> interval_points = term_interval_points.get(term[i]);
							for (int in_i = 0; in_i < increment; in_i++) {
								if (interval_points.containsKey(it+in_i)) {
									poIntegers = interval_points.get(it+in_i);
									all = (ArrayList<Integer>) union(all, poIntegers);
								}								
							}
						}
					}
					double radius = resolution/iteration_time*(it+increment);
					long s = System.nanoTime();
					ArrayList<Integer> poIntegers = SpatialListByNumber(x, y, (int)radius, (int)(resolution/iteration_time)*(it));					
					time+=System.nanoTime()-s;
					for (int ip = 0; ip < poIntegers.size(); ip++) {
						if(cell_term_points.containsKey(poIntegers.get(ip))){
							Map<Integer, ArrayList<Integer>> aMap = cell_term_points.get(poIntegers.get(ip));
							for(int i=0; i<term.length; i++){
								if(aMap.containsKey(term[i]))
									all = (ArrayList<Integer>) union(all, aMap.get(term[i]));
							}
						}
					}
					it += increment;
					
					if(all.size()==0)
						continue;
					candidate_number += all.size();
					for (int j = 0; j < all.size(); j++) {
						int pointid = all.get(j);		
						double sim = randomAccessPoint(pointid, term, lat, lng);
						Collections.sort(result,Collections.reverseOrder());//descendly sort						
						if (result.size() == k) {
							if (sim > result.get(k - 1)) {
								if(sim>result.get(k-2))
									kth = result.get(k-2);
								else{
									kth = sim;
								}
								result.remove(k - 1);
								result.add(sim);
							}
						} else {
							result.add(sim);
						}
					}
					double ub = alpha * upper_bound_text1(it-increment, term) + (1 - alpha) * upper_bound_spatial(it-increment, max_distance);					
					System.out.println(it+" the bound is "+kth + "\t"+ub);					
					if (kth > ub) {
						break;
					}
					increment++;
				}
				long endTime = System.nanoTime();
				System.out.println((endTime - startTime) / 1000000000.0);
				System.out.println("the exploration on spatial list cost "+time /1000000000.0+"\ncandidates number is "+ candidate_number);
				System.out.println(result);
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	/*
	 * we will compute the upper bound for each candidates.
	 */
	public static Map<Integer, Double> refinement(Map<Integer, Map<Integer, Double>> tramap, Map<Integer, Double> tra_low, 
			double[] bound, int query_size, Map<Integer, ArrayList<Integer>> query_term, Map<Integer, Pair<Double, Double>> query_coordinate, int k) {
		//upper bound for each trajectory.
		Map<Integer, Double> tra_upper = new HashMap<>();
		Iterator<Integer> iterator = tramap.keySet().iterator();
		Map<Integer, Double> tra_sim = new HashMap<>();
		int temp = 0;
	//	while(temp++<k-1)
		//	System.out.println(tra_low.get(tra_low.keySet().toArray()[temp]));
		while(iterator.hasNext()){
			int tra_id = iterator.next();
			Set<Integer> arrayList = tramap.get(tra_id).keySet();
			double sim = tra_low.get(tra_id);
			if(arrayList.size()<query_size){				
				for(int i=0; i<bound.length; i++){
					if(!arrayList.contains(i)){
						sim += bound[i];
					}
				}				
			}
			tra_upper.put(tra_id, sim);
		}
		tra_upper = sortByValue_des(tra_upper);
	//	temp = 0;
	//	while(temp++<k-1)
		//	System.out.println(tra_upper.get(tra_upper.keySet().toArray()[temp]));
		Iterator<Integer> aIterator = tra_upper.keySet().iterator();
		while(aIterator.hasNext()){
			int tra_id = aIterator.next();			
			Set<Integer> arrayList = tramap.get(tra_id).keySet();
			double sim = tra_low.get(tra_id);
			if(arrayList.size()<query_size){				
				for(int i=0; i<bound.length; i++){
					if(!arrayList.contains(i)){
						//compute the similarity
						double sim_point_tra = SimilarityPointTra(query_term.get(i), query_coordinate.get(i), tra_id);
						sim += sim_point_tra;
					}
				}
			}			
			if(tra_sim.size()==k){
				tra_sim = sortByValue_des(tra_sim);
				if(sim>tra_sim.get(tra_sim.keySet().toArray()[k-1])){
					tra_sim.remove(tra_sim.keySet().toArray()[k-1]);
					tra_sim.put(tra_id, sim);
					if(sim>tra_sim.get(tra_sim.keySet().toArray()[k-2]))
						sim = tra_sim.get(tra_sim.keySet().toArray()[k-2]);
					if(sim>tra_upper.get(aIterator.next()))
						break;
				}				
			}else{
				tra_sim.put(tra_id, sim);
			}
		}
		tra_sim = sortByValue_des(tra_sim);
		return tra_sim;
	}
	/*
	 * compute the similarity between a point and a trajectory through random access
	 */
	public static double SimilarityPointTra(ArrayList<Integer> terms, Pair<Double, Double> coordinate, int tra_id){	
		ArrayList<Integer> poIntegers = trajectory_points.get(tra_id);
		double sim = 0;
		double lat = coordinate.getFirst();
		double lng = coordinate.getSecond();
		for(int p=0; p<poIntegers.size(); p++){
			int point_id = poIntegers.get(p);
			double new_sim = randomAccessPoint1(point_id, terms, lat, lng);
			sim = new_sim>sim?new_sim:sim;
		}
		return sim;
	}
	/*
	 * trajectory search using two level TA, we will employ partial random access of trajectory 
	 * to make the lower bound more tighter to achieve early termination.
	 */
	public static void trajectory_search(String query_file, int k){
		System.out.println("two-levels TA search");
		long time = 0;
		int candidate_number = 0;
		long random_access_time = 0;
		Map<Integer, Map<Integer, Double>> tramap = new HashMap<Integer, Map<Integer, Double>>();
		//store the lower bound
		Map<Integer, Double> tra_sim = new HashMap<Integer, Double>();
		Map<Integer, Double> q_score;
		ArrayList<Double> lower_bound = new ArrayList<>(); 
		try {
			Scanner in = new Scanner(new BufferedReader(new FileReader(query_file)));
			Map<Integer, ArrayList<Integer>> query_terms = new HashMap<>();
			Map<Integer, Pair<Double, Double>> query_coordinate = new HashMap<>();
			int queryid=0;
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
				queryid++;
			}
			in.close();
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
					for (int i = 0; i < term.size(); i++) {
						ArrayList<Integer> poIntegers = null;
						if (term_interval_points.containsKey(term.get(i))) {
							Map<Integer, ArrayList<Integer>> interval_points = term_interval_points.get(term.get(i));
							if (interval_points.containsKey(it)) {
								poIntegers = interval_points.get(it);
							}
							all = (ArrayList<Integer>) union(all, poIntegers);
						}
					}
					double radius = resolution / iteration_time * (it+1);
					long s = System.nanoTime();
					ArrayList<Integer> poIntegers = SpatialListByNumber(x, y, (int)radius, (int)(resolution/iteration_time)*it);
					time += System.nanoTime() - s;
					for (int ip = 0; ip < poIntegers.size(); ip++) {
						if (cell_term_points.containsKey(poIntegers.get(ip))) {
							Map<Integer, ArrayList<Integer>> aMap = cell_term_points.get(poIntegers.get(ip));
							for (int i = 0; i < term.size(); i++) {
								if (aMap.containsKey(term.get(i)))
									all = (ArrayList<Integer>) union(all, aMap.get(term.get(i)));
							}
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
							int tra_id = point_trajectory.get(pointid);
							double temp_lower = 0;
			    			if(tramap.containsKey(tra_id)){
			    				q_score = tramap.get(tra_id);
			    				double sim_tra = tra_sim.get(tra_id);
			    				if(q_score.containsKey(q)){
			    					if(q_score.get(q)<sim){
			    						q_score.put(q, sim);
			    						tra_sim.put(tra_id, sim_tra-q_score.get(q)+sim);			    						
			    						temp_lower = sim_tra-q_score.get(q)+sim;
			    					}
			    				}else{
			    					q_score.put(q, sim);
			    					tra_sim.put(tra_id, sim_tra+sim);
			    					temp_lower = sim_tra+sim;
			    				}
			    				//?????
			    				if(lower_bound.contains(sim_tra)){
		    					//	lower_bound.remove(new Double(sim_tra));
		    					//	lower_bound.add(temp_lower);
		    						continue;
			    				}
			    			}
			    			else{
			    				q_score = new HashMap<Integer, Double>();
			    				q_score.put(q, sim);
			    				tra_sim.put(tra_id, sim);
			    				temp_lower = sim;
			    			}
			    			tramap.put(tra_id, q_score);			    						    			
			    			
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
			//	tra_sim = sortByValue_des(tra_sim);
				double lb = tra_sim.get(tra_sim.keySet().toArray()[k-1]);
				lb = lower_bound.get(k-1);
				System.out.println(it + " the bound is " + lb + "\t" + ub);			
				if (lb > ub) {
					break;
				}
			}
			// build the upper bound and compute the real similarity
			long time1 = System.nanoTime();
			tra_sim = sortByValue_des(tra_sim);
			Map<Integer, Double> result = refinement(tramap, tra_sim, bounds, query_coordinate.size(), query_terms, query_coordinate, k);
			System.out.println("the result is \n"+result);			
			long endTime = System.nanoTime();
			System.out.println((endTime - startTime) / 1000000000.0);
			System.out.println("Scanning candidates number is "+ candidate_number+"\nthe exploration on spatial list cost "+time / 1000000000.0);	
			System.out.println("refinmenting time is "+(System.nanoTime()-time1)/1000000000.0);
			System.out.println("the time spent on random access is "+ random_access_time / 1000000000.0);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	public static Map<Integer, Double> refinement_incremental(Map<Integer, Map<Integer,Double>> query_result, 
			Map<Integer, ArrayList<Integer>> query_term, Map<Integer, Pair<Double, Double>> query_coordinate, int k){
		Iterator<Integer> itr1 = query_result.keySet().iterator();
		Map<Integer, Map<Integer, Double>> tramap = new HashMap<Integer, Map<Integer, Double>>();
		Map<Integer, Double> q_score;
    	while(itr1.hasNext()){
    		int query_id = itr1.next();
    		Map<Integer,Double> al = query_result.get(query_id);
    		Iterator<Integer> itr2 = al.keySet().iterator();
    		int point=0;
    		while(itr2.hasNext()){    			
    			point = itr2.next();//last one
    			int tra_id = point_trajectory.get(point);    			
    			double sim = al.get(point);
    			if(tramap.containsKey(tra_id)){
    				q_score = tramap.get(tra_id);
    				if(q_score.containsKey(query_id)){
    					if(q_score.get(query_id)<sim){
    						q_score.put(query_id, sim);
    					}
    				}else{
    					q_score.put(query_id, sim);
    				}   				
    			}
    			else{
    				q_score = new HashMap<Integer, Double>();
    				q_score.put(query_id, sim);
    			}
    			tramap.put(tra_id, q_score);
    		}
    	}
    	Iterator<Integer> itr = tramap.keySet().iterator();
    	Map<Integer, Double> tra_low = new HashMap<>();
    	while(itr.hasNext()){
    		int tra_id = itr.next();
    		Map<Integer, Double> ab = tramap.get(tra_id);
    		Iterator<Integer> itr2 = ab.keySet().iterator();
    		double sim = 0;
    		while(itr2.hasNext()){
    			int q_id = itr2.next();
    			sim += ab.get(q_id);
    		}
    		tra_low.put(tra_id, sim);
    	}
    	Map<Integer, Double> tra_upper = new HashMap<>();
		Iterator<Integer> iterator = tramap.keySet().iterator();
		Map<Integer, Double> tra_sim = new HashMap<>();
		while(iterator.hasNext()){
			int tra_id = iterator.next();
			Set<Integer> arrayList = tramap.get(tra_id).keySet();
			double sim = tra_low.get(tra_id);
			if(arrayList.size()<query_result.size()){				
				for(int i=0; i<query_result.size(); i++){
					if(!arrayList.contains(i)){
						Map<Integer, Double> tra_result = query_result.get(i);
						sim += tra_result.get(tra_result.keySet().toArray()[tra_result.size()-1]);
					}
				}				
			}
			tra_upper.put(tra_id, sim);
		}
		tra_upper = sortByValue_des(tra_upper);
		Iterator<Integer> aIterator = tra_upper.keySet().iterator();
		while(aIterator.hasNext()){
			int tra_id = aIterator.next();			
			Set<Integer> arrayList = tramap.get(tra_id).keySet();
			double sim = tra_low.get(tra_id);
			if(arrayList.size()<query_result.size()){				
				for(int i=0; i<query_result.size(); i++){
					if(!arrayList.contains(i)){
						double sim_point_tra = SimilarityPointTra(query_term.get(i), query_coordinate.get(i), tra_id);
						sim += sim_point_tra;
					}
				}
			}			
			if(tra_sim.size()==k){
				tra_sim = sortByValue_des(tra_sim);
				if(sim>tra_sim.get(tra_sim.keySet().toArray()[k-1])){
					tra_sim.remove(tra_sim.keySet().toArray()[k-1]);
					tra_sim.put(tra_id, sim);
					if(sim>tra_sim.get(tra_sim.keySet().toArray()[k-2]))
						sim = tra_sim.get(tra_sim.keySet().toArray()[k-2]);
					if(sim>tra_upper.get(aIterator.next()))
						break;
				}				
			}else{
				tra_sim.put(tra_id, sim);
			}
		}
		return tra_sim;
	}
	/*
	 * this algorithm employs the IKNN idea, which employs repetitive incremental search
	 */
	public static void trajectory_search_incremental(String query_file, int k){		
		System.out.println("Incremental search");
		int candidate_number = 0;		
		long random_access_time = 0;
		Map<Integer, Map<Integer,Double>> rank_list = new HashMap<>();
		Map<Integer, Double> tra_sim = new HashMap<Integer, Double>();
		try {
			Scanner in = new Scanner(new BufferedReader(new FileReader(query_file)));
			Map<Integer, ArrayList<Integer>> query_terms = new HashMap<>();
			Map<Integer, Pair<Double, Double>> query_coordinate = new HashMap<>();
			int queryid=0;
			while (in.hasNextLine()) {				
				String line = in.nextLine();
				String[] parts = line.trim().split(",");
				double lat = Double.valueOf(parts[1]);
				double lng = Double.valueOf(parts[2]);
				int term[] = new int[parts.length-3];
				ArrayList<Integer> terms = new ArrayList<>();
				for(int i=3; i<parts.length;i++){
					term[i-3] = Integer.valueOf(parts[i]);
					terms.add(Integer.valueOf(parts[i]));
				}
				query_terms.put(queryid, terms);
				query_coordinate.put(queryid, new Pair<Double, Double>(lat,lng));
				queryid++;
			}
			in.close();			
			int lamda = k;
			double whole_ub = 10000;
			double whole_lb = 0;
			long startTime = System.nanoTime();
			long bound_computation_time = 0;
			long time = 0;
			while (whole_ub > whole_lb) {
				for (int q = 0; q < query_coordinate.size(); q++) {
					Map<Integer, Double> tra_result = new HashMap<>();
					for (int it = 0; it < iteration_time; it++) {
						double ub = 0;
						ArrayList<Integer> all = new ArrayList<>();//candidates set
						ArrayList<Integer> term = query_terms.get(q);						
						Pair<Double, Double> coordinate = query_coordinate.get(q);
						ub = alpha * upper_bound_text(it, term)
								+ (1 - alpha) * upper_bound_spatial(it, max_distance);
						double lat = coordinate.getFirst();
						double lng = coordinate.getSecond();
						int x = (int) ((lat - array[1]) / (array[3] - array[1]) * resolution);
						int y = (int) ((lng - array[0]) / (array[2] - array[0]) * resolution);
						for (int i = 0; i < term.size(); i++) {
							ArrayList<Integer> poIntegers = null;
							if (term_interval_points.containsKey(term.get(i))) {
								Map<Integer, ArrayList<Integer>> interval_points = term_interval_points
										.get(term.get(i));
								if (interval_points.containsKey(it)) {
									poIntegers = interval_points.get(it);
								}
								all = (ArrayList<Integer>) union(all, poIntegers);
							}
						}
						double radius = resolution / iteration_time * (it+1);
						long s = System.nanoTime();
						ArrayList<Integer> poIntegers = SpatialListByNumber(x, y, (int) radius,
								(int) (resolution / iteration_time) * it);
						time += System.nanoTime() - s;
						for (int ip = 0; ip < poIntegers.size(); ip++) {
							if (cell_term_points.containsKey(poIntegers.get(ip))) {
								Map<Integer, ArrayList<Integer>> aMap = cell_term_points.get(poIntegers.get(ip));
								for (int i = 0; i < term.size(); i++) {
									if (aMap.containsKey(term.get(i)))
										all = (ArrayList<Integer>) union(all, aMap.get(term.get(i)));
								}
							}
						}
						if (all.size() == 0)
							continue;
						candidate_number += all.size();
						if (rank_list.containsKey(q))
							tra_result = rank_list.get(q);
						for (int j = 0; j < all.size(); j++) {
							int pointid = all.get(j);
							if (!tra_result.containsKey(pointid)) {
								long temp = System.nanoTime();
								double sim = randomAccessPoint1(pointid, term, lat, lng);
								random_access_time += System.nanoTime() - temp;
								tra_result.put(pointid, sim);
							}
						}
						tra_result = sortByValue_des(tra_result);
						if(tra_result.size()>=lamda)
							if (tra_result.get(tra_result.keySet().toArray()[lamda - 1]) > ub) {
								break;
							}
					}
					tra_result.keySet().removeAll(Arrays.asList(tra_result.keySet().toArray()).subList(lamda, tra_result.size()-1));
					rank_list.put(q, tra_result);
				}
				long temp = System.nanoTime();
				whole_lb = old_lower_bound(rank_list, k);
				whole_ub = upper_bound(rank_list);
				bound_computation_time += System.nanoTime() - temp;
				System.out.println("the whole bound is " + whole_lb + "\t" + whole_ub);
				lamda += 800;// the lamda
			}
			tra_sim = refinement_incremental(rank_list, query_terms, query_coordinate, k);
			long endTime = System.nanoTime();
			System.out.println((endTime - startTime) / 1000000000.0);
			System.out.println("the result is \n"+tra_sim);	
			System.out.println("Scanning candidates number is "+ candidate_number+"\nthe exploration on spatial list cost "+time / 1000000000.0);	
			System.out.println("the time spent on bound is "+ bound_computation_time / 1000000000.0);
			System.out.println("the time spent on random access is "+ random_access_time / 1000000000.0);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	/*
	 * brute force search, find all the candidates in the posting list of each term, compute one by one.
	 */
	public static void trajectory_search_bruteforce(String query_file, int k){
		System.out.println("Brute force search");
		int candidate_number = 0;		
		long random_access_time = 0;
		Map<Integer, Double> tra_result = new HashMap<>();
		ArrayList<Integer> whole_terms = new ArrayList<>();
		Map<Pair<Double, Double>, ArrayList<Integer>> query = new HashMap<>();
		try {
			Scanner in = new Scanner(new BufferedReader(new FileReader(query_file)));
			Map<Integer, Pair<Double, Double>> query_coordinate = new HashMap<>();
			int queryid=0;			
			while (in.hasNextLine()) {				
				String line = in.nextLine();
				String[] parts = line.trim().split(",");
				double lat = Double.valueOf(parts[1]);
				double lng = Double.valueOf(parts[2]);
				int term[] = new int[parts.length-3];
				ArrayList<Integer> terms = new ArrayList<>();
				for(int i=3; i<parts.length;i++){
					term[i-3] = Integer.valueOf(parts[i]);
					terms.add(Integer.valueOf(parts[i]));
					whole_terms.add(Integer.valueOf(parts[i]));
				}
				query_coordinate.put(queryid, new Pair<Double, Double>(lat,lng));
				query.put(new Pair<Double, Double>(lat,lng), terms);
				queryid++;
			}
			in.close();	
			long startTime = System.nanoTime();
			long bound_computation_time = 0;
			long time = 0;
			for(int term_i=0; term_i< whole_terms.size(); term_i++){
				int term_id = whole_terms.get(term_i);
				Map<Integer,ArrayList<Integer>> point_double = term_interval_points.get(term_id);
				Iterator<Integer> iterator = point_double.keySet().iterator();
				while(iterator.hasNext()){
					int inteval = iterator.next();
					ArrayList<Integer> points = point_double.get(inteval);
					for(int point_i = 0; point_i<points.size(); point_i++){
						int point_id = points.get(point_i);
						int tra_id = point_trajectory.get(point_id);
					//	if(!tra_result.containsKey(tra_id))
						tra_result.put(tra_id, randomAccessTrajectory(query, tra_id));
					}
				}
			}
			tra_result = sortByValue_des(tra_result);
			tra_result.keySet().removeAll(Arrays.asList(tra_result.keySet().toArray()).subList(k, tra_result.size()));
			
			long endTime = System.nanoTime();
			System.out.println("the result is \n"+tra_result+"\t"+tra_result.size());			
			System.out.println((endTime - startTime) / 1000000000.0);
			System.out.println("Scanning candidates number is "+ candidate_number+"\nthe exploration on spatial list cost "+time / 1000000000.0);	
			System.out.println("the time spent on bound is "+ bound_computation_time / 1000000000.0);
			System.out.println("the time spent on random access is "+ random_access_time / 1000000000.0);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) throws IOException{
		String query_file = "C:/Users/e29944/workspace-java/Trajectory/Data/ETQ/LA/query.txt";
		String coordidate = "C:/Users/e29944/workspace-java/Trajectory/Data/ETQ/LA/IR_coordinate.txt";
		String pointTra = "C:/Users/e29944/workspace-java/Trajectory/Data/ETQ/LA/point-trajectory.txt";
		String newterm = "C:/Users/e29944/workspace-java/Trajectory/Data/ETQ/LA/new_word_tfidf_weight.txt";
		String index = "C:/Users/e29944/workspace-java/Trajectory/Data/ETQ/LA/Index";				
	//	creat_index(newterm,coordidate,pointTra,index);
	//	max_distance = get_max_dis();
		
	//	trajectory_search(query_file,20);
	//	trajectory_search_incremental(query_file,20);
	//	trajectory_search_bruteforce(query_file, 20);
		int count = 10;
		RandomAccessFile memoryMappedFile = new RandomAccessFile("C:/1.txt", "rw");
		MappedByteBuffer out = memoryMappedFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, count);
		for (int i = 0; i < count; i++) {
			out.put((byte) 'A');
		}
		for (int i = 0; i < count; i++) {
			System.out.print((char) out.get(i));
		}
		memoryMappedFile.close();
	//	point_search(query_file, 10);
	//	point_search_extend(query_file,10);
	}
}