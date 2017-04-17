package uq.distance;

import java.util.ArrayList;

import uq.entities.Point;
import uq.services.DistanceService;

public class LCSSDistanceCalculator implements SequenceDistanceCalculator
{

	static double Threshold=0.01;
	
	public static void main(String[] args) {
        Point p1 = new Point(new double[]{0,0});//x,y
        Point p2 = new Point(new double[]{0,7});
        Point p3 = new Point(new double[]{0,10});
        Point p33 = new Point(new double[]{0,12});
        ArrayList<Point> r =  new ArrayList<>();
    //    for(int i = 0; i<1000;i++)
        r.add(p33);
        r.add(p3);
        r.add(p2);
        r.add(p1);
        
        Point p4 = new Point(new double[]{2,0});
        Point p5 = new Point(new double[]{2,7});
        Point p6 = new Point(new double[]{2,10});
        ArrayList<Point> s =  new ArrayList<>();
   //     for(int i = 0; i<1000;i++)
        s.add(p4); s.add(p5); s.add(p6);
        
        long startTime = System.nanoTime();
        double cost = getLCSS(r, s);
        long endTime = System.nanoTime();
		System.out.println((double) (endTime - startTime));
        
        System.out.println("Cost: " + cost);
    }
	@Override
	public double getDistance(ArrayList<Point> r, ArrayList<Point> s){
		 // make sure the original objects will not be changed
		ArrayList<Point> r_clone = DistanceService.clonePointsList(r);
		ArrayList<Point> s_clone = DistanceService.clonePointsList(s);
		
		return getLCSS(r_clone, s_clone); 
	}
	
	public LCSSDistanceCalculator(double threshold)
	{
		Threshold = threshold;
	}

	public static double getLCSS(ArrayList<Point> r, ArrayList<Point> s)
	{

		double[][] LCSSMetric = new double[r.size() + 1][s.size() + 1];

		for (int i = 0; i <= r.size(); i++)
		{
			LCSSMetric[i][0] = 0;
		}
		for (int i = 0; i <= s.size(); i++)
		{
			LCSSMetric[0][i] = 0;
		}

		LCSSMetric[0][0] = 0;

		for (int i = 1; i <= r.size(); i++)
		{
			for (int j = 1; j <= s.size(); j++)
			{
				if (subcost(r.get(i - 1), s.get(j - 1)) == 0)
				{
					LCSSMetric[i][j] = LCSSMetric[i - 1][j - 1] + 1;
				}
				else
				{
					LCSSMetric[i][j] = max(LCSSMetric[i][j - 1],
					        LCSSMetric[i - 1][j]);
				}

			}
		}

		double tempR = LCSSMetric[r.size()][s.size()];

		double result = 1 - (tempR / Math.min(r.size(), s.size()));

		return (max(r.size(), s.size()) - tempR) / Math.max(r.size(), s.size());// transfer to distance
	}

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

	private static int subcost(Point p1, Point p2)
	{
		boolean isSame = true;
		for (int i = 0; i < p1.dimension; i++)
		{
			if (Math.abs(p1.coordinate[i] - p2.coordinate[i]) > Threshold)
			{
				isSame = false;
			}
		}

		if (isSame)
		{
			return 0;
		}
		return 1;
	}

	public String toString()
	{
		return "LCSS";
	}
}
