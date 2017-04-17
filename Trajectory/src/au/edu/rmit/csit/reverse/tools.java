package au.edu.rmit.csit.reverse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.omg.CosNaming.BindingIterator;

import uq.distance.EuclideanDistanceCalculator;
import uq.entities.Point;

public class tools {
	public static void main(String[] args) {
		Point p1 = new Point(new double[] { 0, 0 });
		Point p2 = new Point(new double[] { 2, 2 });
		Point p3 = new Point(new double[] { 4, 0 });
		Point p4 = new Point(new double[] { 6, 2 });
		ArrayList<Point> r = new ArrayList<>();
	//	for(int i=0; i<1000; i++)
		r.add(p1);
		r.add(p2);
		r.add(p3);
		r.add(p4);
		
		Point p5 = new Point(new double[] { 0, 2 });
		Point p6 = new Point(new double[] { 2, 0 });
		Point p7 = new Point(new double[] { 4, 2 });
		Point p71 = new Point(new double[] { 4, 3 });
		Point p8 = new Point(new double[] { 6, 0 });
		ArrayList<Point> s = new ArrayList<>();
	//	for(int i=0; i<1000; i++)
		
		s.add(p5);
		s.add(p6);
		s.add(p7);
		s.add(p71);
		s.add(p8);
		
//		System.out.println(s.size());
		Point aPoint = lineIntersect(1.5,3,3,2,0,2,2,2);
	//	System.out.println(aPoint);
		double distance = area_trajectories(r,s);
		System.out.println("the area of all polygons is "+distance);
	}
	/*
	 * the LIP similarity computation based on the area of intersecting polygons.
	 */
	public static double area_trajectories(ArrayList<Point> tra1, ArrayList<Point> tra2) {
		double area = 0;
		double length1 = getLength(tra1);
		double length2 = getLength(tra2);
		ArrayList<Point> tra1_new = new ArrayList<>();
		ArrayList<Point> tra2_new = new ArrayList<>();
		//it must be LinkedHashMap, maintain the inserting order.
		Map<Point, twoInt> inter_position = new LinkedHashMap<Point, twoInt>();//we maintain the intersection information first, twoint store the id of segment in each trajectory
		for(int i=0; i<tra1.size()-1;i++){
			Point astart = tra1.get(i);
		//	System.out.println(astart);
			Point aend = tra1.get(i+1);
			for(int j=0; j<tra2.size()-1;j++){
				Point bstart = tra2.get(j);
				Point bend = tra2.get(j+1);
				Point inter = lineIntersect(astart.coordinate[0], astart.coordinate[1],aend.coordinate[0], aend.coordinate[1],
						bstart.coordinate[0], bstart.coordinate[1],bend.coordinate[0], bend.coordinate[1]);
				if(inter!=null){	
					inter_position.put(inter, new twoInt(i, j));		
				}
			}			
		}
	//	System.out.println(inter_position);
		Iterator<Point> aIterator = inter_position.keySet().iterator();
		Point a=null;
		int counter=0;
		if (inter_position.size() > 1) {
			while (aIterator.hasNext()) {
				if(counter++>inter_position.size()-1)
					break;
				else{
					ArrayList<Point> polygon = new ArrayList<>();
					ArrayList<Point> temp = new ArrayList<>();
					if (a == null) {
						a = aIterator.next();
					}
					twoInt position = inter_position.get(a);
					int x1 = position.getx();
					int y1 = position.gety();
					Point b = aIterator.next();
					twoInt position1 = inter_position.get(b);
					int x2 = position1.getx();
					int y2 = position1.gety();
					polygon.add(a);
					for (int i = x1 + 1; i < x2 + 1; i++) {
						polygon.add(tra1.get(i));
					}
					polygon.add(b);
					for (int i = y2; i > y1; i--) {
						polygon.add(tra2.get(i));
					}
					polygon.add(a);
					System.out.println("ther polygon is "+polygon);
					double weight = getLength(polygon)/(length1 + length2);
					area += area_polygon(polygon, polygon.size()) * weight;
				//	System.out.println(area);
					a = b;
				}
			}
		}
		return area;
	}
	/*
	 * compute the area of polygon.
	 */
	public static double area_polygon(ArrayList<Point> points, int number){
		double area = 0;
		int j = number -1;
		for (int i=0; i<number; i++)
	    {
			Point pointi = points.get(i); 
			Point pointj = points.get(j);
			area = area +  (pointj.getCoordinate()[0]+pointi.getCoordinate()[0]) * (pointj.getCoordinate()[1]-pointi.getCoordinate()[1]); 
			j = i;
	    }
	//	System.out.println(area);
		return Math.abs(area/2);
	}
	/*
	 * compute the intersecting points inside the lines.
	 */
	public static Point lineIntersect(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
		double denom = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
		if (denom == 0.0) { // Lines are parallel.
			return null;
		}
		double ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / denom;
		double ub = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / denom;
		if (ua >= 0.0f && ua <= 1.0f && ub >= 0.0f && ub <= 1.0f) {
			// Get the intersecting points.
			double[] x = {(x1 + ua * (x2 - x1)), (y1 + ua * (y2 - y1))};
			return new Point(x);
		}
		return null;
	}
	/*
	 * compute the perimeter of a polygon.
	 * 
	 */
	static double getLength(ArrayList<Point> p) {
		double result = 0;
		EuclideanDistanceCalculator e = new EuclideanDistanceCalculator();
		for (int i = 0; i < p.size() - 1; i++) {
			result += e.getDistance(p.get(i), p.get(i + 1));
		}
		return result;
	}

}
/*
 * store the according intersecting segments in each trajectory
 */
class twoInt {
	int x;
	int y;

	public twoInt(int i, int j) {
		x = i;
		y = j;
	}
	public int getx() {
		// TODO Auto-generated constructor stub
		return this.x;
	}
	public int gety() {
		// TODO Auto-generated constructor stub
		return this.y;
	}
}
