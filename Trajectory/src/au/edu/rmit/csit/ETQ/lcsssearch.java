package au.edu.rmit.csit.ETQ;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
import java.util.TreeMap;

import au.edu.rmit.csit.FTSE.LCSS;
import au.edu.rmit.csit.Quadtree.QuadTree;
import au.edu.rmit.csit.index.Gridindex;

public class lcsssearch {
	private static final Object[][] String = null;
	// epsilon
	private static double threshold_epsilon = 0.01;
	private static Map<Integer, ArrayList<Point>> trajectory_db = new HashMap<Integer, ArrayList<Point>>();
	private static Map<Integer, ArrayList<Integer>> inverted_index = new HashMap<Integer, ArrayList<Integer>>();
	private static Map<Integer, ArrayList<String>> cell_words = new HashMap<Integer, ArrayList<String>>();//it is used to store all the words inside a cell
	private static QuadTree qt;
	
	public static Map sortByValue(Map map) {
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
	public void load_dataset(){
		
	}
	
	/*
	 * load the standard format
	 */
	public static void load_standard(String file, int array[]) {
		ArrayList<Point> trajectory = new ArrayList<>();
		String line;
		double x = 0, y = 0;
		qt = new QuadTree(array[0], array[1], array[2], array[3]);
		int resolution = (int)((array[2]-array[0])/threshold_epsilon);
		System.out.println("the resolution is "+resolution);
		resolution = (int) Math.pow(2,(int)(Math.log(resolution)/Math.log(2)+1));
		System.out.println("the resolution is "+resolution);
		try {
			Scanner in = new Scanner(new BufferedReader(new FileReader(file)));
			while (in.hasNextLine()) {
				line = in.nextLine();
				String[] parts = line.trim().split("\t");
				int tra_id = Integer.valueOf(parts[0]);
				String[] p;
				String[] arrstring = p = parts[2].trim().substring(0, parts[2].length() - 1).split(";");
				int n = arrstring.length;
				int n2 = 1;
				while (n2 <= n) {
					String pt = arrstring[n2-1];
					if (!p.equals("")) {
						p = pt.split(",");
						x = new Double(p[0]);
						y = new Double(p[1]);
						String keyword = p[2];
						String[] keywords = keyword.split(" ");// the keywords
						ArrayList<String> key = new ArrayList(Arrays.asList(keywords));
						if (y > array[0] && y < array[2] && x > array[1] && x < array[3]) {
							Point a = new Point(new double[] { x, y }, keywords);
							String temp = parts[0] + "_" + Integer.toString(n2);
							qt.set(y, x, temp);
							if (trajectory_db.containsKey(tra_id)) {
								trajectory = trajectory_db.get(tra_id);
							} else {
								trajectory = new ArrayList<>();
							}
							trajectory.add(a);
							trajectory_db.put(tra_id, trajectory);					
							int atemp = Gridindex.ComputeCellid1(x, y, resolution);
							ArrayList<Integer> tras = new ArrayList<>();
							if(inverted_index.containsKey(atemp)){
								tras = inverted_index.get(atemp);
							}
							tras.add(tra_id);
							inverted_index.put(atemp,tras);
							//cell words to store all the keywords for, which can be sericize.
							
							ArrayList<String> words = new ArrayList<>();
							if(cell_words.containsKey(atemp)){
								words = cell_words.get(atemp);
							}
							words.addAll(key);//union between key and words.
							cell_words.put(atemp, words);
						}
					}
					++n2;
				}
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println(trajectory_db.size());
	}
	/*
	 * compute one by one.
	 */
	public static void query_basic(ArrayList<Point> query, int k){
		Iterator<Point> query_ite = query.iterator();
		ArrayList<Double> result = new ArrayList<>();
		double kth = 0;
		while (query_ite.hasNext()) {
			Point p = query_ite.next();
			au.edu.rmit.csit.Quadtree.Point[] points1 = qt.searchWithin(
					p.getCoordinate()[1] - threshold_epsilon,
					p.getCoordinate()[0] - threshold_epsilon,
					p.getCoordinate()[1] + threshold_epsilon,
					p.getCoordinate()[0] + threshold_epsilon);
			for (int ij = 0; ij < points1.length; ij++) {
				au.edu.rmit.csit.Quadtree.Point point = points1[ij];
				String temp = (String) point.getValue();
				String[] abc = temp.split("_");
				int y2 = Integer.valueOf(abc[0]);
				TLCSS.set_threshold(threshold_epsilon);
				double distance = TLCSS.getLCSS(query, trajectory_db.get(y2));	
				Collections.sort(result);
				if(result.size()==k){
					if(distance>result.get(k-1)){	
						result.remove(k-1);
						result.add(distance);					
					}
				}else{
					result.add(distance);
				}
			}
		}
	}
	/*
	 * top-k search through group and pruning.
	 * we can also divide the group into sub-groups to get a tighter bound.
	 * we will use language model
	 */
	public void query_group(ArrayList<Point> query, int k) {
		Iterator<Point> query_ite = query.iterator();
		ArrayList<Double> result = new ArrayList<>();
		double threshold = threshold_epsilon;
		Map<String, ArrayList<Integer>> group = new HashMap<String, ArrayList<Integer>>();// store
		Map<Integer, Map<Integer, String>> cell_sta = new HashMap<Integer, Map<Integer, String>>();
		int i = 1;
		while (query_ite.hasNext()) {
			Point p = query_ite.next();
			double x = p.getCoordinate()[1];
			double y = p.getCoordinate()[0];
			au.edu.rmit.csit.Quadtree.Point[] points_en = qt.searchWithin(x - threshold, y - threshold, x + threshold, y + threshold);
			String querycell = Integer.toString(i);
			get_trajectory(points_en, cell_sta, querycell);
			i++;
		}
		Iterator<Integer> itr_tra = cell_sta.keySet().iterator();
		while (itr_tra.hasNext()) {
			int tra_id = itr_tra.next();
			Map<Integer, String> order_code = cell_sta.get(tra_id);
			Iterator<Integer> itr2 = order_code.keySet().iterator();
			String tra_code1 = "";
			while (itr2.hasNext()) {
				tra_code1 += order_code.get(itr2.next()) + "-";
			}
			// System.out.println(tra_code1);
			ArrayList<Integer> tra_temp = new ArrayList<Integer>();
			if (group.containsKey(tra_code1)) {
				tra_temp = group.get(tra_code1);
			}
			tra_temp.add(tra_id);
			group.put(tra_code1, tra_temp);
		}
		double[] cell_upper = get_textual_sim(query);
		Map<String, Double> group_bound = new HashMap<>();
		Iterator<String> aIterator = group.keySet().iterator();
		while(aIterator.hasNext()){
			String code = aIterator.next();
			double bound = upper_bound(code, cell_upper);//compute the similarity of each group
			group_bound.put(code, bound);
		}
		group_bound = sortByValue(group_bound);		
		Iterator<String> itr1 = group_bound.keySet().iterator();
		double k_sim=0;//record the k-th result
		ArrayList<Double> sims = new ArrayList<>();//store the score.
		while (itr1.hasNext()) {
			String id = itr1.next();
			double bound = group_bound.get(id);
			if(k_sim>bound){
				break;
			}else{
				ArrayList<Integer> tra = group.get(id);
				for (int in = 0; in < tra.size(); in++) {
					int tra_id = tra.get(in);
					ArrayList<Point> candidate = trajectory_db.get(tra_id);
					double distance = TLCSS.getLCSS(candidate, query);
					Collections.sort(result);
					if(result.size()==k){
						if(distance>result.get(k-1)){	
							result.remove(k-1);
							result.add(distance);					
						}
					}else{
						result.add(distance);
					}
				}
			}
		}
	}
	/*
	 * we need to divide one group into sub groups
	 */
	public double query_subgroup(){
		
		return 0.0;
	}
	/*
	 * get the cells that contain all the keywords.
	 */
	public double[] get_textual_sim(ArrayList<Point> query) {
		double[] arraylist=new double[query.size()];
		//all the cells 
		for(int i=0; i< query.size(); i++){
			Point aPoint = query.get(i);
			ArrayList<Integer> cells = Gridindex.cover_cells_threshold(aPoint.getCoordinate()[1], aPoint.getCoordinate()[0],
				threshold_epsilon);
			ArrayList<String> arrayList2 = new ArrayList<>();
			for(int j = 0; j<cells.size();j++){
				int cellid = cells.get(j);
				if(cell_words.containsKey(cellid)){
					ArrayList<String> arrayList3 = cell_words.get(cellid);
					arrayList2.addAll(arrayList3);
				}
			}
			String[] aStrings = (String[])arrayList2.toArray(String[arrayList2.size()]);
			arraylist[i] = TLCSS.textual_similarity_Jaccard(aPoint.keyword, aStrings);
		}
		return arraylist;
	}
	public static double textual_similarity_Jaccard(String[] r, String[] s){
		ArrayList<String> keyr = new ArrayList(Arrays.asList(r));
		ArrayList<String> keys = new ArrayList(Arrays.asList(s));
		
		System.out.println(union(keyr,keys));
		ArrayList<String> keyrtemp = keyr;
		System.out.println(keyr);
		System.out.println(keys);
		keyrtemp.retainAll(keys);
		System.out.println(keyrtemp);

		keyr.addAll(keys);
		System.out.println(keyr);
		double ratio = (double)(keyrtemp.size())/keyr.size();
		System.out.println("aaa"+ratio);
		return ratio;
	}
	public static <T> List<T> union(List<T> list1, List<T> list2) {
        Set<T> set = new HashSet<T>();
        set.addAll(list1);
        set.addAll(list2);
        return new ArrayList<T>(set);
    }

    public <T> List<T> intersection(List<T> list1, List<T> list2) {
        List<T> list = new ArrayList<T>();
        for (T t : list1) {
            if(list2.contains(t)) {
                list.add(t);
            }
        }
        return list;
    }
	/*
	 * trajectory belong to a point array
	 */
	public static void get_trajectory(au.edu.rmit.csit.Quadtree.Point[] points,
			Map<Integer, Map<Integer, String>> cell_sta, String querycell) {
		for (int i = 0; i < points.length; i++) {
			String temp = (String) points[i].getValue();
			String[] abc = temp.split("_");
			int y2 = Integer.valueOf(abc[0]);// tra_id;
			int order = Integer.valueOf(abc[1]);// order
			Map<Integer, String> abc1 = new TreeMap<Integer, String>();
			if (cell_sta.containsKey(y2)) {
				abc1 = cell_sta.get(y2);
			}
			abc1.put(order, querycell);
			cell_sta.put(y2, abc1);
		}
	}
	
	/*
	 * text_sim stores the upper bound of textual similarity for each query point
	 * code is the code of a group.
	 * this algorihtm will use weighted longest increasing subsequence.
	 */
	public static double upper_bound(String code, double[] text_sim){
		String[] sequence = code.split("-");
		int[] se = new int[sequence.length];
		for(int i = 0; i<sequence.length; i++){
			se[i] = Integer.valueOf(sequence[i]);
		}		
		return 0;
	}
}
