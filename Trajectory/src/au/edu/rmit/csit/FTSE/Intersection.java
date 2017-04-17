package au.edu.rmit.csit.FTSE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import uq.entities.Point;

public class Intersection {
	public static Map<Integer, ArrayList<Integer>> getIntersection(ArrayList<Point> R, ArrayList<Point> S, double threshold){
		int i=0, j=0;
		Map<Integer, ArrayList<Integer>> point_index = new HashMap<Integer, ArrayList<Integer>>();
		for(;i<R.size();i++){
			Point pr = R.get(i);
			ArrayList<Integer> index = new ArrayList<>();
			for(j=0;j<S.size();j++){
				Point ps = S.get(j);
				if(Math.abs(pr.coordinate[0]-ps.coordinate[0])<=threshold && Math.abs(pr.coordinate[1]-ps.coordinate[1])<=threshold)
					index.add(j);					
			}
			point_index.put(i, index);
		}
		return point_index;
	}
	/*
	 * use to prune;
	 */
	public static boolean getIntesectNum(ArrayList<Point> R, ArrayList<Point> S, double threshold){
		int i=0, j=0;
		int count;
		for(;i<R.size();i++){
			Point pr = R.get(i);
			for(j=0;j<S.size();j++){
				Point ps = S.get(j);
				if(Math.abs(pr.coordinate[0]-ps.coordinate[0])<=threshold && Math.abs(pr.coordinate[1]-ps.coordinate[1])<=threshold)
					return true;					
			}
		}
		return false;		
	}
	
	public static Map<Integer, ArrayList<Integer>> getIntersectionGrid(ArrayList<Point> R, ArrayList<Point> S, double threshold){
		int i=0, j=0;
		Map<Integer, ArrayList<Integer>> point_index = new HashMap<Integer, ArrayList<Integer>>();
		for(;i<R.size();i++){
			Point pr = R.get(i);
			ArrayList<Integer> index = new ArrayList<>();
			for(;j<R.size();j++){
				Point ps = S.get(j);
				if(Math.abs(pr.coordinate[0]-ps.coordinate[0])<=threshold && Math.abs(pr.coordinate[1]-ps.coordinate[1])<=threshold)
					index.add(j);
			}
			point_index.put(i, index);
		}
		return point_index;
	}
}
