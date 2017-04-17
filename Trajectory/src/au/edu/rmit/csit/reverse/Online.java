package au.edu.rmit.csit.reverse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import uq.entities.Point;
import uq.distance.EDRDistanceCalculator;
import uq.distance.LCSSDistanceCalculator;
import au.edu.rmit.csit.index.*;
import au.edu.rmit.csit.FTSE.*;
import au.edu.rmit.csit.Quadtree.*;
import au.edu.rmit.csit.Quadtree.QuadTree;
import au.edu.rmit.csit.dataset.Brightkite;

public class Online {
	//epsilon
	private static double threshold_epsilon = 0.01;
	
	private static Map<Integer, ArrayList<Point>> trajectory_db = new HashMap<Integer, ArrayList<Point>>();
	private static Map<Integer, ArrayList<Integer>> inverted_index = new HashMap<Integer, ArrayList<Integer>>();
	private static Map<Integer, ArrayList<String>> cell_tra = new HashMap<Integer, ArrayList<String>>();
	private static Map<Integer, Map<Integer, Integer>> rank_list = new HashMap<Integer, Map<Integer, Integer>>();
	private static int resolution;
	private static int array[]=null;
	private static QuadTree qt;
	
	public static void set_epsilon(double epsilon){
		threshold_epsilon = epsilon;
	}
	public static double get_epsilon() {
		return threshold_epsilon;
	}
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
	public static Map sortByValue_rise(Map map) {
		List list = new LinkedList(map.entrySet());
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue());
			}
		});
		Map result = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	/*
	 * load different trajectory data set
	 */
	public static void load_db_standard(int option) {
		String file = null, rank = null;
		
		switch (option) {
		case 1:
			int array1[] = {-122,31,-117,36};
			array = array1;
			file = "/home/wangsheng/Desktop/Data/LA-standard-clean.tra";					
			rank = "/home/wangsheng/Desktop/Data/RkNNT_index/ranklist_group_LA_"+Double.toString(threshold_epsilon)+".ser";
			break;
		case 2:
			int array2[] = {-76,38,-71,41};
			array = array2;
			file = "/home/wangsheng/Desktop/Data/NYC-standard-clean.tra";
			rank = "/home/wangsheng/Desktop/Data/RkNNT_index/ranklist_group_NYC_"+Double.toString(threshold_epsilon)+".ser";
			break;
		case 3:
			int array3[] = {-180,-90,180,90};
			array = array3;
			file = "/home/wangsheng/Desktop/Data/Brightkite-standard-clean-day.txt";
			rank = "/home/wangsheng/Desktop/Data/RkNNT_index/ranklist_group_Brightkite_"+Double.toString(threshold_epsilon)+".ser";
			break;
		case 4:
			int array4[] = {-180,-90,180,90};
			array = array4;
			file = "/home/wangsheng/Desktop/Data/Gowalla-standard-clean-day.txt";
			rank = "/home/wangsheng/Desktop/Data/RkNNT_index/ranklist_group_Gowalla_"+Double.toString(threshold_epsilon)+".ser";
			break;
		case 5:// divide into many trajectories.
			int array5[] = {-180,-90,180,90};
			array = array5;
			file = "/home/wangsheng/Desktop/Data/Brightkite_filte_standard_divide.txt";
			rank = "/home/wangsheng/Desktop/Data/RkNNT_index/ranklist_group_Brightkite_divide"+Double.toString(threshold_epsilon)+".ser";
			break;
		case 6:// divide into many trajectories.
			int array6[] = {-180,-90,180,90};
			array = array6;
			file = "/home/wangsheng/Desktop/Data/Gowalla_filte_standard_divide.txt";
			rank = "/home/wangsheng/Desktop/Data/RkNNT_index/ranklist_group_Gowalla_divide"+Double.toString(threshold_epsilon)+".ser";
			break;
		default:
			break;
		}
		load_standard(file);	
		load_index(rank);
	}
	/*
	 * load the standard format
	 */
	public static void load_standard(String file) {
		ArrayList<Point> trajectory = new ArrayList<>();
		String line;
		double x = 0, y = 0;
		qt = new QuadTree(array[0], array[1], array[2], array[3]);
		resolution = (int)((array[2]-array[0])/threshold_epsilon);
		
		resolution = (int) Math.pow(2,(int)(Math.log(resolution)/Math.log(2)+1));
		System.out.println(resolution);
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
						if (y > array[0] && y < array[2] && x > array[1] && x < array[3]) {
							Point a = new Point(new double[] { x, y });
							String temp = parts[0] + "_" + Integer.toString(n2);
							qt.set(y, x, temp);
							if (trajectory_db.containsKey(tra_id)) {
								trajectory = trajectory_db.get(tra_id);
							} else {
								trajectory = new ArrayList<>();
							}
							trajectory.add(a);
							trajectory_db.put(tra_id, trajectory);
							int atemp = Gridindex.ComputeCellid_nine(x, y, resolution, array[0],array[1],array[2],array[3]);
							ArrayList<String> tras = new ArrayList<>();
							if(cell_tra.containsKey(atemp)){
								tras = cell_tra.get(atemp);
							}
							tras.add(temp);
							cell_tra.put(atemp,tras);
						}
					}
					++n2;
				}
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println("aaaaaa"+cell_tra.size());
	}
	/*
	 * choose trajectory
	 */
	public static Map<Integer, ArrayList<Point>> choose_trajectory(Map<Integer, Integer> tra_count, int n, int m) {
		Map<Integer, ArrayList<Point>> query = new HashMap<>();
		Iterator<Integer> iterator = tra_count.keySet().iterator();
		int i=0;
		System.out.println(trajectory_db.size());
		while(iterator.hasNext()){
			int a = iterator.next();
			ArrayList<Point> temp = trajectory_db.get(a);
			int count = tra_count.get(a);
			if(temp.size()>m){
				query.put(a, temp);
				if(i++>n)
					break;
			}			
		}
		return query;
	}
	/*
	 * load the Serialization file into memory
	 */
	public static void load_index(String rankingfile) {
		File f = new File(rankingfile);
		long startTime = System.nanoTime();
		if (f.exists() && !f.isDirectory()) {
			Online.load_list(rankingfile);
		} else {
			Online.rank_list_group(rankingfile);
		}
		long endTime = System.nanoTime();
		Brightkite.write("/home/wangsheng/Desktop/Data/result.txt", "The ranking list cost:" + (double) (endTime - startTime) / 1000000000.0 + "s\n");
	}
	/*
	 * load the ranking list
	 */
	public static void load_list(String file) {
		try {
			FileInputStream fis = new FileInputStream(file);
			ObjectInputStream ois = new ObjectInputStream(fis);
			rank_list = (HashMap) ois.readObject();
			System.out.println(rank_list.size());
			ois.close();
			fis.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return;
		} catch (ClassNotFoundException c) {
			System.out.println("Class not found");
			c.printStackTrace();
			return;
		}
	}
	/*
	 * only compute with the trajectory cross the range of query point.
	 */
	public static void rank_list_grid(String file) {
		Iterator<Integer> itr1 = trajectory_db.keySet().iterator();
		int count = 0;
		while (itr1.hasNext()) {
			int y1 = itr1.next(); // trajectory id;
			ArrayList<Point> trajectory = trajectory_db.get(y1);
			
			TreeMap<Integer, Integer> score = new TreeMap<>();
			for (int i = 0; i < trajectory.size(); i++) {
				Point p = trajectory.get(i);
				ArrayList<Integer> cells = Gridindex.cover_cells_threshold(p.getCoordinate()[1], p.getCoordinate()[0],
						threshold_epsilon);
				Iterator<Integer> itr2 = cells.iterator();
				while (itr2.hasNext()) {
					int abc = itr2.next();
					if (inverted_index.containsKey(abc)) {
						ArrayList<Integer> candidates = inverted_index.get(abc);
						// System.out.println("the lenght of points is
						// "+candidates.size());
						if (candidates.size() > 1) {
							Iterator<Integer> itr3 = candidates.iterator();
							while (itr3.hasNext()) {
								int y2 = itr3.next();
								// int sim =
								// (int)LCSSDistanceCalculator.getLCSS(trajectory,
								// trajectory_db.get(y2));
								int sim = -1 * LCSS.getLCSS(trajectory, trajectory_db.get(y2), threshold_epsilon);
								if (sim == 0)
									continue;
								int score_count;
								if (score.containsKey(sim)) {
									score_count = score.get(sim);
									score_count += 1;
								} else
									score_count = 1;
								score.put(sim, score_count);
							}
						}
					}
				}
			}
			// System.out.println(score);
			TreeMap<Integer, Integer> sc = new TreeMap<>();
			Iterator<Integer> itr4 = score.keySet().iterator();
			int c = 0;
			while (itr4.hasNext()) {
				int rank_sim = itr4.next();
				int count_sim = score.get(rank_sim);
				c += count_sim;
				sc.put(c, rank_sim * -1);
			}
			// System.out.println(sc);
			rank_list.put(y1, sc);
		}
		//
		try {
			FileOutputStream fos = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(rank_list);
			oos.close();
			fos.close();
			System.out.println("Serialized ranklist data is saved in ranklist.ser");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	/*
	 * only compute with the trajectory cross the range of query point.
	 */
	public static void rank_list_quad(String file) {
		Iterator<Integer> itr1 = trajectory_db.keySet().iterator();
		int count = 0;		
		while (itr1.hasNext()) {
			int y1 = itr1.next(); // trajectory id;
			ArrayList<Point> trajectory = trajectory_db.get(y1);
			// System.out.println(count++);
			TreeMap<Integer, Integer> score = new TreeMap<>();
			for (int i = 0; i < trajectory.size(); i++) {
				Point p = trajectory.get(i);
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
					int sim = -1 * LCSS.getLCSS(trajectory, trajectory_db.get(y2), threshold_epsilon);
				//	sim = -1 * EDR.getEDR(trajectory, trajectory_db.get(y2));// employ the EDR metric
					if (sim == 0)
						continue;
					int score_count;
					if (score.containsKey(sim)) {
						score_count = score.get(sim);
						score_count += 1;
					} else
						score_count = 1;
					score.put(sim, score_count);
				}
			}
			// System.out.println(score);
			TreeMap<Integer, Integer> sc = new TreeMap<>();
			Iterator<Integer> itr4 = score.keySet().iterator();
			int c = 0;
			while (itr4.hasNext()) {
				int rank_sim = itr4.next();
				int count_sim = score.get(rank_sim);
				c += count_sim;
				sc.put(c, rank_sim * -1);
			}
			rank_list.put(y1, sc);
		}
		//
		try {
			FileOutputStream fos = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(rank_list);
			oos.close();
			fos.close();
			System.out.println("Serialized ranklist quad data is saved in ranklist_quad.ser");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/*
	 * only compute with the trajectory cross the range of query point.
	 */
	public static void rank_list_group(String file) {
		Iterator<Integer> itr1 = trajectory_db.keySet().iterator();
		int count = 0;
		int covered_count = 0;
		System.out.println("the length if qt is "+qt.getCount());
		Map<Integer, Integer> count_tra = new TreeMap<>();
		while (itr1.hasNext()) {
			int tra_count = 0;
			int y1 = itr1.next(); // trajectory id;
			ArrayList<Point> trajectory = trajectory_db.get(y1);
		//	System.out.println(trajectory.size());
			System.out.println(count++);
			TreeMap<Integer, Integer> score = new TreeMap<>();// sorted by similarity
			Map<Integer, Map<Integer, String>> cell_sta = new HashMap<Integer, Map<Integer, String>>();
			Map<String, ArrayList<Integer>> group = new HashMap<String, ArrayList<Integer>>();// store
			for (int i = 0; i < trajectory.size(); i++) {
				Point p = trajectory.get(i);
				au.edu.rmit.csit.Quadtree.Point[] points1 = qt.searchWithin(
						p.getCoordinate()[1] - threshold_epsilon,
						p.getCoordinate()[0] - threshold_epsilon,
						p.getCoordinate()[1] + threshold_epsilon,
						p.getCoordinate()[0] + threshold_epsilon);
				String querycell = Integer.toString(i);
				get_trajectory(points1, cell_sta, querycell);
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
				ArrayList<Integer> tra_temp = new ArrayList<Integer>();
				if (group.containsKey(tra_code1)) {
					tra_temp = group.get(tra_code1);
				}
				tra_temp.add(tra_id);
				group.put(tra_code1, tra_temp);
			}
			Iterator<String> itr2 = group.keySet().iterator();
			while (itr2.hasNext()) {
				String id = itr2.next();
				ArrayList<Integer> tra = group.get(id);
				int sim = get_similarity_rank(id);//
				// sim = get_similarity_rank_EDR(id,trajectory.size());//
				sim = sim * -1;
				int score_count;
				if (score.containsKey(sim)) {
					score_count = score.get(sim);
					score_count += tra.size();
				} else
					score_count = tra.size();
				score.put(sim, score_count);
				covered_count += tra.size();
				tra_count += tra.size();// record the number of candidates for all 
			}
			count_tra.put(y1, (int)(tra_count/trajectory.size()));
			TreeMap<Integer, Integer> sc = new TreeMap<>();
			Iterator<Integer> itr4 = score.keySet().iterator();
			int c = 0;
			while (itr4.hasNext()) {
				int rank_sim = itr4.next();
				int count_sim = score.get(rank_sim);
				c += count_sim;
				sc.put(c, rank_sim * -1);
			}
			rank_list.put(y1, sc);
		}
		double covered_rate = covered_count / (trajectory_db.size());
		covered_rate /= trajectory_db.size();
		Brightkite.write("/home/wangsheng/Desktop/Data/result.txt", "the cover rate is " + 100 * covered_rate + "%\n");
		try {
			FileOutputStream fos = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(rank_list);
			oos.close();
			fos.close();
			System.out.println("Serialized ranklist quad data is saved in ranklist_quad.ser");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		try {
			String str = file.substring(0, file.length()-4)+"query.txt";
			FileOutputStream fos = new FileOutputStream(str);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(count_tra);
			oos.close();
			fos.close();
			System.out.println("Serialized ranklist quad data is saved in ranklist_quad.ser");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	/*
	 * get similarity of the code
	 * 
	 */
	public static int get_similarity_rank(String id) {
		String[] abc = id.split("-");
		int tra_order[] = new int[abc.length];
		for (int i = 0; i < abc.length; i++) {
			tra_order[i] = Integer.valueOf(abc[i]);
		}
		// find the longest common keywords
		return lis(tra_order, abc.length);
	}
	/*
	 * get similarity of the code
	 * 
	 */
	public static int get_similarity_rank_EDR(String id, int size) {
		String[] abc = id.split("-");
		int tra_order[] = new int[abc.length];
		for (int i = 0; i < abc.length; i++) {
			tra_order[i] = Integer.valueOf(abc[i]);
		}
		// find the longest common keywords
		return EDR(tra_order, size, abc.length);
	}

	/*
	 * query by the rectangles and compare it with the ranking list
	 */
	public static void query_grid(ArrayList<Point> query, int k) {
		Iterator<Point> query_ite = query.iterator();
		ArrayList<Integer> result = new ArrayList<>();
		while (query_ite.hasNext()) {
			Point p = query_ite.next();
			ArrayList<Integer> cells = Gridindex.cover_cells_threshold(p.getCoordinate()[1], p.getCoordinate()[0],
					threshold_epsilon);
			Iterator<Integer> itr2 = cells.iterator();
			while (itr2.hasNext()) {
				int abc = itr2.next();
				if (inverted_index.containsKey(abc)) {
					ArrayList<Integer> candidates = inverted_index.get(abc);
					if (candidates.size() > 1) {
						Iterator<Integer> itr3 = candidates.iterator();
						while (itr3.hasNext()) {
							int y2 = itr3.next();
							double distance = LCSS.getLCSS(query, trajectory_db.get(y2), threshold_epsilon);
							if (rank_list.containsKey(y2) && distance != 0) {
								Map<Integer, Integer> dis_map = rank_list.get(y2);
								int k_thdis = 0;
								Iterator<Integer> it2 = dis_map.keySet().iterator();
								while (it2.hasNext()) {
									k_thdis = it2.next();
									if (k_thdis > k) {
										if (distance > dis_map.get(k_thdis)) {
											result.add(y2);
										}
										break;
									}
								}
							}
						}
					}
				}
			}
		}
	}
	/*
	 * query by the rectangles and compare with the ranking list
	 */
	public static void query_quad(ArrayList<Point> query, int k) {
		Iterator<Point> query_ite = query.iterator();
		ArrayList<Integer> result = new ArrayList<>();
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
				int distance = LCSS.getLCSS(query, trajectory_db.get(y2), threshold_epsilon);				
				if(knn_group(y2,k,distance))
					result.add(y2);
			}
		}
		// System.out.println("The number of trajectories is "+ tra.size());
	//	System.out.println("The number of results isaa " + result.size());
	}
	/*
	 * In this algorithm, we will group the trajectory by different set of
	 * bounded rectangles of query.
	 * we should not divide it into four groups.
	 */
	public static void query_group_pure(ArrayList<Point> query, int k) {
		Iterator<Point> query_ite = query.iterator();
		ArrayList<Integer> result = new ArrayList<>();
		double threshold = threshold_epsilon;
		Map<String, ArrayList<Integer>> group = new HashMap<String, ArrayList<Integer>>();// store
		Map<Integer, Map<Integer, String>> cell_sta = new HashMap<Integer, Map<Integer, String>>();
		int i = 1;
		while (query_ite.hasNext()) {
			Point p = query_ite.next();
			double x = p.getCoordinate()[1];
			double y = p.getCoordinate()[0];
			// divide the BR into four quad BR and give them a code.
			au.edu.rmit.csit.Quadtree.Point[] points_en = qt.searchWithin(x - threshold, y - threshold, x + threshold, y + threshold);
			String querycell = Integer.toString(i);
			get_trajectory(points_en, cell_sta, querycell);
			i++;
		}
		// System.out.println("The number of trajectories is "+
		// cell_sta.size());
		Iterator<Integer> itr_tra = cell_sta.keySet().iterator();
		while (itr_tra.hasNext()) {
			int tra_id = itr_tra.next();
			Map<Integer, String> order_code = cell_sta.get(tra_id);
			Iterator<Integer> itr2 = order_code.keySet().iterator();
			String tra_code1 = "";
			while (itr2.hasNext()) {
				tra_code1 += order_code.get(itr2.next()) + "-";
			}
			ArrayList<Integer> tra_temp = new ArrayList<Integer>();
			if (group.containsKey(tra_code1)) {
				tra_temp = group.get(tra_code1);
			}
			if (!tra_temp.contains(tra_id))
				tra_temp.add(tra_id);
			group.put(tra_code1, tra_temp);
			// System.out.println(tra_code1+"-");
		}
		// System.out.println("There are "+ group.size()+" quad groups!");
		Iterator<String> itr1 = group.keySet().iterator();
		int count1 = 0;
		while (itr1.hasNext()) {
			String id = itr1.next();
			String[] pointarray = id.split("-");
			int a[] =new int[pointarray.length];
			for(int re = 0; re<a.length; re++){
				a[re] = Integer.valueOf(pointarray[re]);
			}
			ArrayList<Integer> tra = group.get(id);
			count1 += tra.size();
			int sim = lis(a, a.length);
			for (int in = 0; in < tra.size(); in++) {
				int tra_id = tra.get(in);
				if(knn_group(tra_id,k,sim))
					result.add(tra_id);
			}
		}
		//System.out.println("The number of candidates is "+count1);
		// System.out.println("The number of results is "+result.size());
	}
	/*
	 * In this algorithm, we will group the trajectory by different set of
	 * bounded rectangles of query.
	 */
	public static void query_group_pruning(ArrayList<Point> query, int k) {
		Iterator<Point> query_ite = query.iterator();
		ArrayList<Integer> result = new ArrayList<>();
		double threshold = threshold_epsilon;
		Map<String, ArrayList<Integer>> group = new HashMap<String, ArrayList<Integer>>();// store
		Map<Integer, Map<Integer, String>> cell_sta = new HashMap<Integer, Map<Integer, String>>();
		int i = 1;
		while (query_ite.hasNext()) {
			Point p = query_ite.next();
			double x = p.getCoordinate()[1];
			double y = p.getCoordinate()[0];
			au.edu.rmit.csit.Quadtree.Point[] points_en = qt.searchWithin(x, y, x + threshold, y + threshold);
			au.edu.rmit.csit.Quadtree.Point[] points_wn = qt.searchWithin(x - threshold, y, x, y + threshold);
			au.edu.rmit.csit.Quadtree.Point[] points_es = qt.searchWithin(x, y - threshold, x + threshold, y);
			au.edu.rmit.csit.Quadtree.Point[] points_ws = qt.searchWithin(x - threshold, y - threshold, x, y);
			String querycell = "00_" + Integer.toString(i);
			get_trajectory(points_en, cell_sta, querycell);
			querycell = "01_" + Integer.toString(i);
			get_trajectory(points_wn, cell_sta, querycell);
			querycell = "10_" + Integer.toString(i);
			get_trajectory(points_ws, cell_sta, querycell);
			querycell = "11_" + Integer.toString(i);
			get_trajectory(points_es, cell_sta, querycell);
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
		// prune those groups that has more than k results.
		// System.out.println("There are "+ group.size()+" quad groups!");
		Iterator<String> itr1 = group.keySet().iterator();
		while (itr1.hasNext()) {
			String id = itr1.next();
			ArrayList<Integer> tra = group.get(id);
			if (tra.size() <= k) {
				int sim = get_longest_group(id).split("-").length;
				for (int in = 0; in < tra.size(); in++) {
					int tra_id = tra.get(in);
					if(knn_group(tra_id,k,sim))
						result.add(tra_id);
				}
			}
		}
	//	System.out.println("The number of results is " + result.size());
	}
	/*
	 * In this algorithm, we will group the trajectory by different set of
	 * bounded rectangles of query.
	 */
	public static void query_group_merge(ArrayList<Point> query, int k) {
		Iterator<Point> query_ite = query.iterator();
		ArrayList<Integer> result = new ArrayList<>();
		double threshold = threshold_epsilon;
		Map<String, ArrayList<Integer>> group = new HashMap<String, ArrayList<Integer>>();// store
		Map<Integer, Map<Integer, String>> cell_sta = new HashMap<Integer, Map<Integer, String>>();
		int i = 0;
		while (query_ite.hasNext()) {
			Point p = query_ite.next();
			double x = p.getCoordinate()[1];
			double y = p.getCoordinate()[0];
			au.edu.rmit.csit.Quadtree.Point[] points_en = qt.searchWithin(x, y, x + threshold, y + threshold);
			au.edu.rmit.csit.Quadtree.Point[] points_wn = qt.searchWithin(x - threshold, y, x, y + threshold);
			au.edu.rmit.csit.Quadtree.Point[] points_es = qt.searchWithin(x, y - threshold, x + threshold, y);
			au.edu.rmit.csit.Quadtree.Point[] points_ws = qt.searchWithin(x - threshold, y - threshold, x, y);
			String querycell = "00_" + Integer.toString(i);
			get_trajectory(points_en, cell_sta, querycell);
			querycell = "01_" + Integer.toString(i);
			get_trajectory(points_wn, cell_sta, querycell);
			querycell = "10_" + Integer.toString(i);
			get_trajectory(points_ws, cell_sta, querycell);
			querycell = "11_" + Integer.toString(i);
			get_trajectory(points_es, cell_sta, querycell);
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
			ArrayList<Integer> tra_temp = new ArrayList<Integer>();
			if (group.containsKey(tra_code1)) {
				tra_temp = group.get(tra_code1);
			}
			tra_temp.add(tra_id);
			group.put(tra_code1, tra_temp);
		}
		Iterator<String> itr1 = group.keySet().iterator();
		Map<String, ArrayList<Integer>> merged_group = new HashMap<String, ArrayList<Integer>>();// store
		while (itr1.hasNext()) {
			String id = itr1.next();
			ArrayList<Integer> tra = group.get(id);
			String temp = get_longest_group(id);
			ArrayList<Integer> tra_temp = new ArrayList<Integer>();
			if (merged_group.containsKey(temp)) {
				tra_temp = merged_group.get(temp);
			}
			tra_temp.addAll(tra);
			merged_group.put(temp, tra_temp);
		}
		Iterator<String> itr2 = merged_group.keySet().iterator();
		while (itr2.hasNext()) {
			String id = itr2.next();
			ArrayList<Integer> tra = merged_group.get(id);
			if (tra.size() <= k) {
				int sim = id.split("-").length;// get the size
				for (int in = 0; in < tra.size(); in++) {
					int tra_id = tra.get(in);
					if (knn_group(tra_id, k, sim))
						result.add(tra_id);
				}
			}
		}
	}
	/*
	 * we will use the grid-index instead of quad-tree.
	 * bounded rectangles of query.
	 */
	public static void query_group_merge_nine(ArrayList<Point> query, int k) {
		Iterator<Point> query_ite = query.iterator();
		ArrayList<Integer> result = new ArrayList<>();
		Map<String, ArrayList<Integer>> group = new HashMap<String, ArrayList<Integer>>();// store
		//traid, order, code
		Map<Integer, Map<Integer, String>> cell_sta = new HashMap<Integer, Map<Integer, String>>();
		
		int i = 0;
		while (query_ite.hasNext()) {
			Point p = query_ite.next();
			double x = p.getCoordinate()[0];
			double y = p.getCoordinate()[1];
		//	System.out.println(threshold_epsilon+"\t"+(double)(array[2]-array[0])/resolution);
			ArrayList<Integer> cells = Gridindex.cover_cells_threshold_nine(x, y, threshold_epsilon, resolution, array[0],array[1],array[2],array[3]);		
	//		System.out.println(cells.size());
			for(int ij=0; ij<cells.size();ij++){
				ArrayList<String> points = cell_tra.get(cells.get(ij));
				if (points != null) {
					String querycell = Integer.toString(ij) + "_" + Integer.toString(i);
					get_trajectory_nine(points, cell_sta, querycell);
				}
			}
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
			ArrayList<Integer> tra_temp = new ArrayList<Integer>();
			if (group.containsKey(tra_code1)) {
				tra_temp = group.get(tra_code1);
			}
			tra_temp.add(tra_id);
			group.put(tra_code1, tra_temp);
		}
		Iterator<String> itr1 = group.keySet().iterator();
	//	System.out.println(group);
		Map<String, ArrayList<Integer>> merged_group = new HashMap<String, ArrayList<Integer>>();// store
		while (itr1.hasNext()) {
			String id = itr1.next();
			ArrayList<Integer> tra = group.get(id);
			String temp = get_longest_group(id);
			ArrayList<Integer> tra_temp = new ArrayList<Integer>();
			if (merged_group.containsKey(temp)) {
				tra_temp = merged_group.get(temp);
			}
			tra_temp.addAll(tra);
			merged_group.put(temp, tra_temp);
		}
		Iterator<String> itr2 = merged_group.keySet().iterator();
		while (itr2.hasNext()) {
			String id = itr2.next();
			ArrayList<Integer> tra = merged_group.get(id);
			if (tra.size() <= k) {
				int sim = id.split("-").length;// get the size
				for (int in = 0; in < tra.size(); in++) {
					int tra_id = tra.get(in);
				/*	if (knn_group(tra_id, k, sim))
						result.add(tra_id);*/
				}
			}
		}
		//next we will employ above algorithm to prune all results.
	}
	/*
	 * we will prune based on more than 4 groups to do pruning
	 * for each group, find new members outside the query rectangle to prune.
	 * find the four cells with dense distribution after the initial pruning.
	 * still choose four cells which will cover most points to prune.
	 */
	public void large_pruning() {
		
	}
	/*
	 * the tra_id is the candidates trajectory waited to be check,
	 * intersection: store the id of cells that intersect with query as tra_id
	 */
	public boolean grid_check_pruning(int tra_id, int[] intersection, int k) {
		ArrayList<Integer> whole = null;
		for(int i=0; i<intersection.length;i++){
			ArrayList<Integer> tra_index = inverted_index.get(intersection[i]);
			if(i==0){
				whole = tra_index;
			}else{
				whole.retainAll(tra_index);//intersection
			}
		}
		return k>whole.size()?true:false;
	}
	/*
	 * the tra_id is the candidates trajectory waited to be check,
	 * intersection: store the id of cells that intersect with query as tra_id.
	 * we use quad tree to cover as many as candidates, since we think it will increase the chance to do pruning.
	 * How to maximize is an important problem, it should not spend too much time, since we can not guarantee it can be pruned.
	 * 
	 */
	public boolean quad_check_pruning(int tra_id, int[] intersection, int k) {
		ArrayList<Integer> whole = null;
		for(int i=0; i<intersection.length;i++){
			ArrayList<Integer> tra_index = inverted_index.get(intersection[i]);
			if(i==0){
				whole = tra_index;
			}else{
				whole.retainAll(tra_index);//intersection
			}
		}
		return k>whole.size()?true:false;
	}
	/*
	 * we need to build index to improve the efficiency of this part.
	 * we can group for each trajectory and do not compute the knn, when the query is coming, we will compare and prune,
	 * which will save much time.
	 */
	public static boolean knn_group(int y1, int k, int sim_query){
		ArrayList<Point> trajectory = trajectory_db.get(y1);
		TreeMap<Integer, Integer> score = new TreeMap<>();// sorted by similarity
		Map<Integer, Map<Integer, String>> cell_sta = new HashMap<Integer, Map<Integer, String>>();
		Map<String, ArrayList<Integer>> group = new HashMap<String, ArrayList<Integer>>();// store
		for (int i = 0; i < trajectory.size(); i++) {
			Point p = trajectory.get(i);
			au.edu.rmit.csit.Quadtree.Point[] points1 = qt.searchWithin(
					p.getCoordinate()[1] - threshold_epsilon,
					p.getCoordinate()[0] - threshold_epsilon,
					p.getCoordinate()[1] + threshold_epsilon,
					p.getCoordinate()[0] + threshold_epsilon);
			String querycell = Integer.toString(i);
			get_trajectory(points1, cell_sta, querycell);
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
			ArrayList<Integer> tra_temp = new ArrayList<Integer>();
			if (group.containsKey(tra_code1)) {
				tra_temp = group.get(tra_code1);
			}
			tra_temp.add(tra_id);
			group.put(tra_code1, tra_temp);
		}
		Iterator<String> itr2 = group.keySet().iterator();
		while (itr2.hasNext()) {
			String id = itr2.next();
			ArrayList<Integer> tra = group.get(id);
			int sim = get_similarity_rank(id);//
			// sim = get_similarity_rank_EDR(id,trajectory.size());//
			sim = sim * -1;
			int score_count;
			if (score.containsKey(sim)) {
				score_count = score.get(sim);
				score_count += tra.size();
			} else
				score_count = tra.size();
			score.put(sim, score_count);
		}
		Iterator<Integer> itr4 = score.keySet().iterator();
		int c = 0;
		boolean result = false;
		while (itr4.hasNext()) {
			int rank_sim = itr4.next();
			int count_sim = score.get(rank_sim);
			c += count_sim;
			if(c>=k){
				if(sim_query>=rank_sim){
					result = true;
				}
			}
		}
		return result;
	}
	public boolean knn_basic(int y1, int k, int sim_query) {
		ArrayList<Point> trajectory = trajectory_db.get(y1);
		TreeMap<Integer, Integer> score = new TreeMap<>();
		for (int i = 0; i < trajectory.size(); i++) {
			Point p = trajectory.get(i);
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
				int sim = -1 * LCSS.getLCSS(trajectory, trajectory_db.get(y2), threshold_epsilon);
			//	sim = -1 * EDR.getEDR(trajectory, trajectory_db.get(y2));// employ the EDR metric
				if (sim == 0)
					continue;
				int score_count;
				if (score.containsKey(sim)) {
					score_count = score.get(sim);
					score_count += 1;
				} else
					score_count = 1;
				score.put(sim, score_count);
			}
		}
		Iterator<Integer> itr4 = score.keySet().iterator();
		int c = 0;
		boolean result = false;
		while (itr4.hasNext()) {
			int rank_sim = itr4.next();
			int count_sim = score.get(rank_sim);
			c += count_sim;
			if(c>=k){
				if(sim_query>=rank_sim){
					result = true;
				}
			}
		}
		return result;
	}
	
	public static String get_longest_group_big(String id) {
		String[] abc = id.split("-");
		int tra_order[] = new int[abc.length];
		String[] quad = new String[abc.length];
		for (int i = 0; i < abc.length; i++) {
			String[] quad_cell = abc[i].split("_");
			quad[i] = quad_cell[0];
			tra_order[i] = Integer.valueOf(quad_cell[1]);
		}
		// find the longest common keywords
		// for(int i=0;i<tra_order.length;i++)
		// System.out.print(tra_order[i]+"-");
		// System.out.println();
		return LongestIncreasingSubsequenceLength(tra_order, quad, abc.length);// 15s
	}
	
	/*
	 * get similarity of the code
	 * 
	 */
	public static String get_longest_group(String id) {
		String[] abc = id.split("-");
		int tra_order[] = new int[abc.length];
		String[] quad = new String[abc.length];
		for (int i = 0; i < abc.length; i++) {
			String[] quad_cell = abc[i].split("_");
			quad[i] = quad_cell[0];
			tra_order[i] = Integer.valueOf(quad_cell[1]);
		}
		// find the longest common keywords
		// for(int i=0;i<tra_order.length;i++)
		// System.out.print(tra_order[i]+"-");
		// System.out.println();
		return LongestIncreasingSubsequenceLength(tra_order, quad, abc.length);// 15s
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
	 * trajectory belong to a point array
	 */
	public static void get_trajectory_nine(ArrayList<String> points,
			Map<Integer, Map<Integer, String>> cell_sta, String querycell) {
		for (int i = 0; i < points.size(); i++) {
			String temp = points.get(i);
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
	 * binary search
	 */
	public static int CeilIndex(int A[], int l, int r, int key) {
		while (r - l > 1) {
			int m = l + (r - l) / 2;
			if (A[m] >= key)
				r = m;
			else
				l = m;
		}
		return r;
	}

	/*
	 * longest increasing subsequence
	 */
	public static String LongestIncreasingSubsequenceLength(int A[], String quad[], int size) {
		// Add boundary case, when array size is one
		int[] tailTable = new int[size];
		String[] tempstring = new String[size];
		int len; // always points empty slot
		tailTable[0] = A[0];
		tempstring[0] = quad[0];
		len = 1;
		for (int i = 1; i < size; i++) {
			if (A[i] < tailTable[0]) {
				// new smallest value
				tailTable[0] = A[i];
				tempstring[0] = quad[i];
			} else if (A[i] > tailTable[len - 1]) {
				// A[i] wants to extend largest subsequence
				tailTable[len] = A[i];
				tempstring[len] = quad[i];
				len++;
			} else {
				// A[i] wants to be current end candidate of an existing
				// subsequence. It will replace ceil value in tailTable
				int tem = CeilIndex(tailTable, -1, len - 1, A[i]);
				tailTable[tem] = A[i];
				tempstring[tem] = quad[i];
			}
		}
		String temp = "";
		for (int i = 0; i < len; i++) {
			temp += tempstring[i] + "_" + Integer.toString(tailTable[i]) + "-";
		}
		return temp;
	}

	/*
	 * Longest increasing subsequence
	 */
	static int lis_EDR(int arr[], int n) {
		int lis[] = new int[n];
		int i, j, max = 0;
		/* Initialize LIS values for all indexes */
		for (i = 0; i < n; i++)
			lis[i] = 1;
		/* Compute optimized LIS values in bottom up manner */
		for (i = 1; i < n; i++)
			for (j = 0; j < i; j++)
				if (arr[i] > arr[j] && lis[i] < lis[j] + 1)
					lis[i] = lis[j] + 1;
		/* Pick maximum of all LIS values */
		for (i = 0; i < n; i++)
			if (max < lis[i])
				max = lis[i];
		return max;
	}

	/*
	 * Longest increasing subsequence
	 */
	static int lis(int arr[], int n) {
		int lis[] = new int[n];
		int i, j, max = 0;
		/* Initialize LIS values for all indexes */
		for (i = 0; i < n; i++)
			lis[i] = 1;
		/* Compute optimized LIS values in bottom up manner */
		for (i = 1; i < n; i++)
			for (j = 0; j < i; j++)
				if (arr[i] > arr[j] && lis[i] < lis[j] + 1)
					lis[i] = lis[j] + 1;
		/* Pick maximum of all LIS values */
		for (i = 0; i < n; i++)
			if (max < lis[i])
				max = lis[i];
		return max;
	}

	/*
	 * get the intersection for EDR
	 */
	public static Map<Integer, ArrayList<Integer>> getIntersection(int arr[]) {
		Map<Integer, ArrayList<Integer>> L = new HashMap<>();
		for (int i = 0; i < arr.length; i++) {
			ArrayList<Integer> temp = new ArrayList<>();
			if (L.containsKey(arr[i])) {
				temp = L.get(arr[i]);
			}
			temp.add(i);
			L.put(arr[i], temp);
		}
		return L;
	}

	/*
	 * EDR based on FTSE
	 */
	static int EDR(int arr[], int n, int m) {// n is the length of code, m is
												// the length of query
		ArrayList<Integer> match = new ArrayList<>();
		int length = 2 * m + 1;
		int max = 0;
		match.add(0);
		for (int i = 1; i < n * 2 + 1; i++)
			match.add(length);
		int c = 0;
		int temp = 0, temp2 = 0;
		//index of cells
		Map<Integer, ArrayList<Integer>> L = getIntersection(arr);
		Iterator<Integer> itr1 = L.keySet().iterator();
		while (itr1.hasNext()) {
			temp = match.get(0);
			temp2 = match.get(0);
			int tem = itr1.next();
			ArrayList<Integer> tem1 = L.get(tem);
			System.out.println(tem1.size());
			for (int j = 0; j < tem1.size(); j++) {
				int k = tem1.get(j) + 1;
				if (temp < k) {
					while (match.get(c) < k) {
						if (temp < match.get(c) - 1 && temp < n - 1) {
							temp2 = match.get(c);
							match.set(c, temp + 1);
							temp = temp2;
						} else {
							temp = match.get(c);
						}
						c = c + 1;
					}
					temp2 = match.get(c);
					match.set(c, temp + 1);
					temp = match.get(c + 1);
					if (match.get(c + 1) > k)
						match.set(c + 1, k);
					if (max < c + 1)
						max = c + 1;
					c = c + 2;
				} else if (temp2 < k && k < match.get(c)) {
					temp2 = temp;
					temp = match.get(c);
					match.set(c, k);
					if (max < c)
						max = c;
					c = c + 1;
				}
			}
			for (int j = c; j < max + 1; j++) {
				if (temp < match.get(j) - 1 && temp < n - 1) {
					temp2 = match.get(j);
					match.set(j, temp + 1);
					temp = temp2;
					if (max < j)
						max = j;
				} else {
					temp = match.get(j);
				}
			}
			for (int pc = 0; pc < match.size(); pc++) {
				System.out.print(match.get(pc) + " ");
			}
		}
		return max;
	}
}
