package au.edu.rmit.csit.skindex.RCA;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;

import kotlin.Pair;

public class Query {
	public static Map<Pair<Double, Double>, ArrayList<Integer>> generate_query(String query_file, double[] array) {
		Map<Pair<Double, Double>, ArrayList<Integer>> query = new HashMap<>();
		try {
			Scanner in = new Scanner(new BufferedReader(new FileReader(query_file)));
			while (in.hasNextLine()) {				
				String line = in.nextLine();
				String[] parts = line.trim().split(",");
				double lat = Double.valueOf(parts[1]);
				double lng = Double.valueOf(parts[2]);
				if(lat<array[1] || lat>array[3] || lng<array[0] || lng >array[2]){
					lat = array[1] + lat%(array[3]-array[1]);
					lng = array[0] + lng%(array[2]-array[0]);
				}
				ArrayList<Integer> terms = new ArrayList<>();
				for(int i=3; i<parts.length;i++){
					terms.add(Integer.valueOf(parts[i]));
				}
				query.put(new Pair<Double, Double>(lat,lng), terms);
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return query;
	}
	public static void main(String[] args){
		String file = "C:/Users/e29944/workspace-java/Trajectory/Data/ETQ/LA/10_IR.txt";
		double[] array = {-121,32,-118,35};
		generate_query(file, array);
	}
}