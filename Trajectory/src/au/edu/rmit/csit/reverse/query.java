package au.edu.rmit.csit.reverse;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import uq.entities.Point;

public class query {
	/*
	 * we will generate the most popular trajectories with 10 points.
	 */
	//popular points by using the quad-tree cell.
	public void generate_density(String file){
		
	}
	/*
	 * generate the query randomly
	 */
	public static ArrayList<Point> generate_query_LA(int num) {
		Random rand = new Random();
		ArrayList<Point> query = new ArrayList<>();
		for (int i = 0; i < num; i++) {
			int randomInt = rand.nextInt(100000);
			int random = rand.nextInt(100000);
			double ranx = (double) randomInt / 500000;
			double rany = (double) random / 500000;
		//	System.out.println(ranx + "\t" + rany);
			query.add(new Point(new double[] { 33.9890 + ranx, -118.3 + rany }));
		}
		return query;
	}

	public static ArrayList<Point> generate_query_NYC(int num) {
		Random rand = new Random();
		ArrayList<Point> query = new ArrayList<>();
		for (int i = 0; i < num; i++) {
			int randomInt = rand.nextInt(100000);
			int random = rand.nextInt(100000);
			double ranx = (double) randomInt / 500000;
			double rany = (double) random / 500000;
			System.out.println(ranx + "\t" + rany);
			query.add(new Point(new double[] { 40.73112 + ranx, -73.99738 + rany }));
		}
		return query;
	}
	//popular trajectories.
	public static Map<Integer, ArrayList<Point>> generate_by_numberCandidate(int option, int n, int m, String epsilon) {
		String file = null;
		switch (option) {
		case 1:
			file = "/home/wangsheng/Desktop/Data/RkNNT_index/ranklist_group_LA_"+epsilon+"query.txt";				
			break;			
		case 2:
			file = "/home/wangsheng/Desktop/Data/RkNNT_index/ranklist_group_NYC_"+epsilon+"query.txt";
			break;
		case 3:
			file = "/home/wangsheng/Desktop/Data/RkNNT_index/ranklist_group_Brightkite_"+epsilon+"query.txt";
			break;
		case 4:
			file = "/home/wangsheng/Desktop/Data/RkNNT_index/ranklist_group_Gowalla_"+epsilon+"query.txt";
			break;
		case 5:// divide into many trajectories.
			file = "/home/wangsheng/Desktop/Data/RkNNT_index/ranklist_group_Brightkite_divide"+epsilon+"query.txt";
			break;
		case 6:// divide into many trajectories.
			file = "/home/wangsheng/Desktop/Data/RkNNT_index/ranklist_group_Gowalla_divide"+epsilon+"query.txt";
			break;
		default:
			break;
		}
		Map<Integer, Integer> tra_count = new TreeMap<>();
		Map<Integer, ArrayList<Point>> query;
		try {
			FileInputStream fis = new FileInputStream(file);
			ObjectInputStream ois = new ObjectInputStream(fis);
			tra_count = (TreeMap)ois.readObject();
		//	tra_count = Online.sortByValue(tra_count);
		//	query = Online.choose_trajectory(tra_count, n, m);
			tra_count = Online.sortByValue(tra_count);
			query = Online.choose_trajectory(tra_count, n, m);
			ois.close();
			fis.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return null;
		} catch (ClassNotFoundException c) {
			System.out.println("Class not found");
			c.printStackTrace();
			return null;
		}
		return query;
	}
	public static void main(String[] args) {
		Map<Integer, ArrayList<Point>> query = generate_by_numberCandidate(1,100,10,"0.01");
		
	}
	//trajectory clustering
}
