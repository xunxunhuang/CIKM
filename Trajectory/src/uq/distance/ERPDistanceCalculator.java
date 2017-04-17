package uq.distance;

import java.util.ArrayList;

import uq.entities.Point;
import uq.services.DistanceService;

public class ERPDistanceCalculator implements SequenceDistanceCalculator
{
	public static void main(String[] args) {
        Point p1 = new Point(new double[]{0,0});
        Point p2 = new Point(new double[]{0,7});
        Point p3 = new Point(new double[]{0,10});
        Point p33 = new Point(new double[]{0,12});
        ArrayList<Point> r =  new ArrayList<>();
        r.add(p1); r.add(p2); r.add(p3);r.add(p33);
        
        Point p4 = new Point(new double[]{2,0});
        Point p5 = new Point(new double[]{2,7});
        Point p6 = new Point(new double[]{2,10});
        Point p7 = new Point(new double[]{2,12});
        ArrayList<Point> s =  new ArrayList<>();
        s.add(p4); s.add(p5); s.add(p6);s.add(p7);
        
        ArrayList<Point> r_clone = DistanceService.clonePointsList(r);
		ArrayList<Point> s_clone = DistanceService.clonePointsList(s);
        double cost = getERP(r_clone, s_clone);
        
        System.out.println("Cost: " + cost);
    }

	public double getDistance(ArrayList<Point> r, ArrayList<Point> s){
		 // make sure the original objects will not be changed
		ArrayList<Point> r_clone = DistanceService.clonePointsList(r);
		ArrayList<Point> s_clone = DistanceService.clonePointsList(s);
		return getERP(r_clone, s_clone); 
	}

	private static ArrayList<Point> normalization(ArrayList<Point> t)
	{
		ArrayList<Point> result = new ArrayList<Point>();

		if (t.size() == 0)
		{
			return result;
		}

		assert (t.get(0).dimension == 1);

		double mean = 0;
		for (int i = 0; i < t.size(); i++)
		{
			mean += t.get(i).coordinate[0];
			System.out.println(t.get(i).coordinate[0]);
		}
		mean /= t.size();

		double standardDeviation = 0;
		for (int i = 0; i < t.size(); i++)
		{
			standardDeviation += Math.pow(t.get(i).coordinate[0] - mean, 2);
		}
		standardDeviation = Math.sqrt(standardDeviation);

		for (int i = 0; i < t.size(); i++)
		{

			double[] tempCoordinate = t.get(i).coordinate;
			tempCoordinate[0] = (tempCoordinate[0] - mean) / standardDeviation;
			Point temp = new Point(tempCoordinate);
			result.add(temp);
		}

		return result;
	}

	public static double getERP(ArrayList<Point> r0, ArrayList<Point> s0)
	{

		ArrayList<Point> r = EDRDistanceCalculator.normalization(r0);
		ArrayList<Point> s = EDRDistanceCalculator.normalization(s0);
		System.out.println(r);
		System.out.println(s);
		
		double[][] erpMetric = new double[r.size() + 1][s.size() + 1];

		double rAB = 0;
		for (int i = 0; i < r.size(); i++)
		{
			rAB += Math.abs(r.get(i).coordinate[1]);
			Point aPoint = r.get(i);
			System.out.println(Math.abs(r.get(i).coordinate[1]));
		}

		double sAB = 0;
		for (int i = 0; i < s.size(); i++)
		{
			sAB += Math.abs(s.get(i).coordinate[1]);
		}

		for (int i = 0; i <= r.size(); i++)
		{
			erpMetric[i][0] = sAB;
	//		System.out.println(sAB);
		}
		for (int i = 0; i <= s.size(); i++)
		{
			erpMetric[0][i] = rAB;
		}

		erpMetric[0][0] = 0;

		for (int i = 1; i <= r.size(); i++)
		{
			for (int j = 1; j <= s.size(); j++)
			{
				erpMetric[i][j] = min(
				        erpMetric[i - 1][j - 1]
				                + subcost(r.get(i - 1), s.get(j - 1)),
				        erpMetric[i - 1][j]
				                + Math.abs(r.get(i - 1).coordinate[1]),
				        erpMetric[i][j - 1]
				                + Math.abs(s.get(j - 1).coordinate[1]));
			}
		}

		return erpMetric[r.size()][s.size()];
	}

	private static double min(double a, double b, double c)
	{
		if (a <= b && a <= c)
		{
			return a;
		}
		if (b <= c)
		{
			return b;
		}
		return c;
	}

	private static double subcost(Point p1, Point p2)
	{
		return Math.abs(p1.coordinate[0] - p2.coordinate[0]);
	}

	public String toString()
	{
		return "ERP";
	}
}
