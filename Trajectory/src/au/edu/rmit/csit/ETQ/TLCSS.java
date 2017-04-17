package au.edu.rmit.csit.ETQ;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

public class TLCSS {
	/*
	 * extend the LCSS to be a textual LCSS, instead of giving 1 as reward, we
	 * will give textual similarity as reward
	 */
	static double Threshold = 2;

	public static void main(String[] args) {
		String[] aStrings = { "hello", "world" };
		Point p1 = new Point(new double[] { 0, 0 }, aStrings);// x,y
		Point p2 = new Point(new double[] { 0, 7 }, aStrings);
		Point p3 = new Point(new double[] { 0, 10 }, aStrings);
		Point p33 = new Point(new double[] { 0, 12 }, aStrings);
		ArrayList<Point> r = new ArrayList<>();//ordered.
		// for(int i = 0; i<1000;i++)
		r.add(p1);
		r.add(p2);
		r.add(p3);
		r.add(p33);

		String[] aStrings1 = { "hello", "fuck" };
		String[] aStrings2 = { "what", "fuck" };
		Point p4 = new Point(new double[] { 2, 0 }, aStrings1);
		Point p5 = new Point(new double[] { 2, 7 }, aStrings1);
		Point p6 = new Point(new double[] { 2, 10 }, aStrings2);
		ArrayList<Point> s = new ArrayList<>();
		// for(int i = 0; i<1000;i++)
		
		
		s.add(p4);
		s.add(p5);
		s.add(p6);

		long startTime = System.nanoTime();
		double cost = getLCSS(r, s);
		long endTime = System.nanoTime();
		// System.out.println((double) (endTime - startTime));

		System.out.println("Cost: " + cost);
	}

	public void LCSSDistanceCalculator(double threshold) {
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
					LCSSMetric[i][j] = LCSSMetric[i - 1][j - 1] + //1;
							textual_similarity_Jaccard_2(r.get(i-1).keyword, s.get(j-1).keyword);
				}
				else
				{
					LCSSMetric[i][j] = max(LCSSMetric[i][j - 1],
					        LCSSMetric[i - 1][j]);
				}
			}
		}
		double tempR = LCSSMetric[r.size()][s.size()];
		printGrid(LCSSMetric,r.size()+1,s.size()+1);
	//	double result = 1 - (tempR / Math.min(r.size(), s.size()));

	//	return (max(r.size(), s.size()) - tempR) / Math.max(r.size(), s.size());// transfer to distance
		return tempR;
	}
	/*
	 * print the matrix
	 */
	public static void printGrid(double a[][], int m, int n)
	{
	   for(int i = 0; i < m; i++)
	   {
	      for(int j = 0; j < n; j++)
	      {
	         System.out.printf("%5f ", a[i][j]);
	      }
	      System.out.println();
	   }
	}
	/*
	 * 
	 */
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
	/*
	 * we use Jaccard to compute the similarity;
	 */
	public static double textual_similarity_Jaccard(String[] r, String[] s){
		double count = 0;
		for(int i=0; i<r.length; i++){
			for(int j=0; j<s.length; j++){				
				if(r[i] == s[j]){
					count++;
					break;
				}
			}
		}
		return count/(r.length+s.length-count);
	}
	public static double textual_similarity_Jaccard_2(String[] r, String[] s){
		ArrayList<String> keyr = new ArrayList(Arrays.asList(r));
		ArrayList<String> keys = new ArrayList(Arrays.asList(s));
		ArrayList<String> keyrtemp = keyr;
		System.out.println(keyr);
		System.out.println(keys);
		
		keyrtemp.retainAll(keys);
		keyr.addAll(keys);
		
		System.out.println(keyrtemp);
		System.out.println(keyr);
		
		double ratio = (double)(keyrtemp.size())/keyr.size();
		System.out.println("aaa"+ratio);
		return ratio;
	}
	/*
	 * intersection similarity, which can be transfered to heaviest increasing subsequence.
	 */
	public static double getLCSS_inter(ArrayList<Point> R, ArrayList<Point> S){
		//store all the ranking of points
		Map<Integer, ArrayList<Integer>> point_index = new HashMap<Integer, ArrayList<Integer>>();
		for(int i=0; i<R.size(); i++){
			Point aPoint = R.get(i);
			ArrayList<Integer> index = new ArrayList<>();
			for(int j=0; j<S.size(); j++){
				Point bPoint = S.get(j);
				double tsim = textual_similarity_Jaccard(aPoint.keyword, bPoint.keyword);
				if(tsim>0 && subcost(aPoint,bPoint)==0){
					index.add(j);
				}
			}
			if(index.size()>0)
				point_index.put(i,index);
		}
		// how to further compute the similarity based on intersecting list?
		
		return 0.0;
	}
	public static void set_threshold(double thresold){
		Threshold = thresold;
	}
}
