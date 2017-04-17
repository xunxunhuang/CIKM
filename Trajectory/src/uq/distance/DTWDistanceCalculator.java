package uq.distance;

import java.util.ArrayList;
import java.util.Arrays;

import uq.entities.Point;
import uq.services.DistanceService;

/**
 * Dynamic Time Warping distance measure.
 * 
 * @author uqhsu1, h.wang16, uqdalves
 *
 */
public class DTWDistanceCalculator implements SequenceDistanceCalculator
{
	public static void main(String[] args) {
        Point p1 = new Point(new double[]{0,0});
        Point p2 = new Point(new double[]{0,0});
        Point p3 = new Point(new double[]{0,1});
        Point p33 = new Point(new double[]{0,12});
        ArrayList<Point> r =  new ArrayList<>();
        r.add(p1); r.add(p2);
        r.add(p3);
  //      r.add(p33);
        
        Point p4 = new Point(new double[]{2,0});
        Point p5 = new Point(new double[]{2,7});
   //     Point p6 = new Point(new double[]{2,10});
   //     Point p7 = new Point(new double[]{2,12});
        ArrayList<Point> s =  new ArrayList<>();
        s.add(p4); s.add(p5); 
   //     s.add(p6);s.add(p7);
        
        ArrayList<Point> r_clone = DistanceService.clonePointsList(r);
		ArrayList<Point> s_clone = DistanceService.clonePointsList(s);
        double cost = getDTW(r_clone, s_clone);
        
        System.out.println("Cost: " + cost);
    }
	@Override
	public double getDistance(ArrayList<Point> r, ArrayList<Point> s){
		 // make sure the original objects will not be changed
		ArrayList<Point> r_clone = DistanceService.clonePointsList(r);
		ArrayList<Point> s_clone = DistanceService.clonePointsList(s);
		
		return getDTW(r_clone, s_clone); 
	}

	private static double getDTW(ArrayList<Point> r, ArrayList<Point> s)
	{
		double[][] dist = new double[r.size() + 1][s.size() + 1];

		// initialize the dynamic programming seeds
		for (int i = 0; i <= r.size(); ++i)
		{
			dist[i][0] = Double.MAX_VALUE;
		}
		for (int j = 0; j <= s.size(); ++j)
		{
			dist[0][j] = Double.MAX_VALUE;
		}
		dist[r.size()][s.size()] = 0;

		// state transition
		EuclideanDistanceCalculator pdc = new EuclideanDistanceCalculator();
		for (int i = r.size() - 1; i >= 0; --i)
		{
			for (int j = s.size() - 1; j >= 0; --j)
			{
				Point rp = r.get(i);
				Point sp = s.get(j);
				double edd = pdc.getDistance(rp, sp);
				double temp = edd+ Math.min(dist[i + 1][j + 1],Math.min(dist[i + 1][j], dist[i][j + 1]));
				dist[i][j] = temp;
			}
		}
		for (double[] arr : dist) {
            System.out.println(Arrays.toString(arr));
        }
		return dist[0][0];
	}
	public void getDTW1(ArrayList<Point> r, ArrayList<Point> s){
		
	}
	public String toString()
	{
		return "DTW";
	}
}
