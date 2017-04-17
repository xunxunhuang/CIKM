/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uq.distance;

import java.util.ArrayList;

import uq.entities.Line;
import uq.entities.Point;
import uq.entities.Polygon;
import uq.entities.PolygonNew;
import uq.services.DistanceService;

/**
 *
 * @author uqhsu1
 */
public class LIPDistanceCalculator implements SequenceDistanceCalculator {

	static ArrayList<Polygon> polygon;
	static ArrayList<PolygonNew> polygonNew;
	static ArrayList<Double> weight;

	public static void main(String[] args) {
		Point p1 = new Point(new double[] { 0, 2 });
		Point p2 = new Point(new double[] { 0, 0 });
		Point p3 = new Point(new double[] { 2, 0 });
		ArrayList<Point> r = new ArrayList<>();
	//	for(int i=0; i<1000; i++)
		r.add(p1);
		r.add(p2);
		r.add(p3);

		Point p4 = new Point(new double[] { 0, 2 });
		Point p5 = new Point(new double[] { 2, 20 });
		Point p6 = new Point(new double[] { 2, 100 });
		ArrayList<Point> s = new ArrayList<>();
	//	for(int i=0; i<1000; i++)
		s.add(p4);
		s.add(p5);
		s.add(p6);
		Point aPoint = lineIntersect(1.5,3,3,2,0,2,2,2);
		System.out.println(aPoint);
		double cost = getLIP(r, s);

		System.out.println("Cost: " + cost);
	}

	public double getDistance(ArrayList<Point> r, ArrayList<Point> s) {
		// make sure the original objects will not be changed
		ArrayList<Point> r_clone = DistanceService.clonePointsList(r);
		ArrayList<Point> s_clone = DistanceService.clonePointsList(s);

		return getLIP(r_clone, s_clone);
	}

	private static double getLIP(ArrayList<Point> r, ArrayList<Point> s) {
		
	/*	polygon = getPolygon(r, s);
		double result = 0;
		for (int i = 0; i < polygon.size(); i++) {
			result += polygon.get(i).getArea() * weight.get(i).doubleValue();
		}
		if (result > 100) {
			int kk = 0;
		}
		System.out.println(result + '\n');
		return result;*/
		

		polygonNew = getPolygonNew(r, s);
		double result = 0;

		for (int i = 0; i < polygonNew.size(); i++) {
			result += polygonNew.get(i).getArea() * weight.get(i).doubleValue();
			System.out.println(result+'\n');
		}
		if (result > 100) {
			int kk = 0;
		}
		System.out.println(result+'\n');
		return result;

	}

	static double getLength(ArrayList<Point> p) {
		double result = 0;

		EuclideanDistanceCalculator e = new EuclideanDistanceCalculator();

		for (int i = 0; i < p.size() - 1; i++) {
			result += e.getDistance(p.get(i), p.get(i + 1));
		}

		return result;
	}

	private static ArrayList<Polygon> getPolygon(ArrayList<Point> r, ArrayList<Point> s) {

		ArrayList<Polygon> result = new ArrayList<Polygon>();

		weight = new ArrayList<Double>();
		double lengthR = getLength(r);
		double lengthS = getLength(s);

		ArrayList<Line> rl = getPolyline(r);
		ArrayList<Line> sl = getPolyline(s);

		ArrayList<Point> intersections = new ArrayList<Point>();
		ArrayList<twoInt> index = new ArrayList<twoInt>();

		boolean[] used = new boolean[sl.size()];
		for (int i = 0; i < used.length; i++) {
			used[i] = false;
		}

		for (int i = 0; i < rl.size(); i++) {
			for (int j = 0; j < sl.size(); j++) {
				if (used[j]) {
					continue;
				}

				Point inter = rl.get(i).getIntersection(sl.get(j));
				if (inter == null) {
					continue;
				}
				double x = inter.coordinate[0];
				double y = inter.coordinate[1];

				double r1x = r.get(i).coordinate[0];
				double r1y = r.get(i).coordinate[1];

				double r2x = r.get(i + 1).coordinate[0];
				double r2y = r.get(i + 1).coordinate[1];

				if (r1x > r2x) {
					double temp = r1x;
					r1x = r2x;
					r2x = temp;
				}

				if (r1y > r2y) {
					double temp = r1y;
					r1y = r2y;
					r2y = temp;
				}

				double s1x = s.get(j).coordinate[0];
				double s1y = s.get(j).coordinate[1];

				double s2x = s.get(j + 1).coordinate[0];
				double s2y = s.get(j + 1).coordinate[1];

				if (s1x > s2x) {
					double temp = s1x;
					s1x = s2x;
					s2x = temp;
				}

				if (s1y > s2y) {
					double temp = s1y;
					s1y = s2y;
					s2y = temp;
				}

				if (x >= r1x && x <= r2x && y >= r1y && y <= r2y && x >= s1x && x < s2x && y >= s1y && y <= s2y) {
					double[] temp = new double[2];
					temp[0] = x;
					temp[1] = y;
					Point tempP = new Point(temp);
					twoInt tempI = new twoInt(i, j);

					intersections.add(tempP);
					index.add(tempI);
					for (int k = 0; k <= j; k++) {
						used[k] = true;
					}

					if (intersections.size() == 1) {
						ArrayList<Point> tempPolyPoints = new ArrayList<Point>();

						for (int ii = 0; ii <= i; ii++) {
							tempPolyPoints.add(r.get(ii));
						}

						tempPolyPoints.add(tempP);

						for (int ii = j; ii >= 0; ii--) {
							tempPolyPoints.add(s.get(ii));
						}

						ArrayList<Point> tempR = new ArrayList<Point>();
						for (int ii = 0; ii <= i; ii++) {
							tempR.add(r.get(ii));
						}

						ArrayList<Point> tempS = new ArrayList<Point>();
						for (int ii = 0; ii <= j; ii++) {
							tempS.add(s.get(ii));
						}

						double lengthRSub = getLength(tempR);
						double lengthSSub = getLength(tempS);

						double weightV = (lengthRSub + lengthSSub) / (lengthR + lengthS);

						weight.add(new Double(weightV));
						result.add(new Polygon(tempPolyPoints));
					} else {
						ArrayList<Point> tempPolyPoints = new ArrayList<Point>();

						tempPolyPoints.add(intersections.get(intersections.size() - 2));

						for (int ii = index.get(index.size() - 2).x + 1; ii <= i; ii++) {
							tempPolyPoints.add(r.get(ii));
						}

						tempPolyPoints.add(tempP);

						for (int ii = j; ii >= index.get(index.size() - 2).y + 1; ii--) {
							tempPolyPoints.add(s.get(ii));
						}

						ArrayList<Point> tempR = new ArrayList<Point>();
						for (int ii = index.get(index.size() - 2).x + 1; ii <= i; ii++) {
							tempR.add(r.get(ii));
						}

						ArrayList<Point> tempS = new ArrayList<Point>();
						for (int ii = index.get(index.size() - 2).y + 1; ii <= j; ii++) {
							tempS.add(s.get(ii));
						}

						double lengthRSub = getLength(tempR);
						double lengthSSub = getLength(tempS);

						double weightV = (lengthRSub + lengthSSub) / (lengthR + lengthS);

						weight.add(new Double(weightV));

						result.add(new Polygon(tempPolyPoints));
					}
				}

			}
		}

		return result;

	}
	/*
	 * LIP, get the polygon first, then 
	 */
	private static ArrayList<PolygonNew> getPolygonNew(ArrayList<Point> r, ArrayList<Point> s) {

		ArrayList<PolygonNew> result = new ArrayList<PolygonNew>();

		weight = new ArrayList<Double>();
		double lengthR = getLength(r);
		double lengthS = getLength(s);

		ArrayList<Line> rl = getPolyline(r);
		ArrayList<Line> sl = getPolyline(s);

		ArrayList<Point> intersections = new ArrayList<Point>();
		ArrayList<twoInt> index = new ArrayList<twoInt>();

		boolean[] used = new boolean[sl.size()];
		for (int i = 0; i < used.length; i++) {
			used[i] = false;
		}

		for (int i = 0; i < rl.size(); i++) {
			for (int j = 0; j < sl.size(); j++) {
				if (used[j]) {
					continue;
				}

				Point inter = rl.get(i).getIntersection(sl.get(j));
				if (inter == null) {
					continue;
				}
				double x = inter.coordinate[0];
				double y = inter.coordinate[1];

				double r1x = r.get(i).coordinate[0];
				double r1y = r.get(i).coordinate[1];

				double r2x = r.get(i + 1).coordinate[0];
				double r2y = r.get(i + 1).coordinate[1];

				if (r1x > r2x) {
					double temp = r1x;
					r1x = r2x;
					r2x = temp;
				}

				if (r1y > r2y) {
					double temp = r1y;
					r1y = r2y;
					r2y = temp;
				}

				double s1x = s.get(j).coordinate[0];
				double s1y = s.get(j).coordinate[1];

				double s2x = s.get(j + 1).coordinate[0];
				double s2y = s.get(j + 1).coordinate[1];

				if (s1x > s2x) {
					double temp = s1x;
					s1x = s2x;
					s2x = temp;
				}

				if (s1y > s2y) {
					double temp = s1y;
					s1y = s2y;
					s2y = temp;
				}

				if (x >= r1x && x <= r2x && y >= r1y && y <= r2y && x >= s1x && x < s2x && y >= s1y && y <= s2y) {
					double[] temp = new double[2];
					temp[0] = x;
					temp[1] = y;
					Point tempP = new Point(temp);
					twoInt tempI = new twoInt(i, j);

					intersections.add(tempP);
					index.add(tempI);
					for (int k = 0; k <= j; k++) {
						used[k] = true;
					}

					if (intersections.size() == 1) {
						ArrayList<Point> up = new ArrayList<Point>();
						ArrayList<Point> low = new ArrayList<Point>();

						for (int ii = 0; ii <= i; ii++) {
							up.add(r.get(ii));
						}

						up.add(tempP);

						for (int ii = 0; ii <= j; ii++) {
							low.add(s.get(ii));
						}

						low.add(tempP);

						ArrayList<Point> tempR = new ArrayList<Point>();
						for (int ii = 0; ii <= i; ii++) {
							tempR.add(r.get(ii));
						}

						ArrayList<Point> tempS = new ArrayList<Point>();
						for (int ii = 0; ii <= j; ii++) {
							tempS.add(s.get(ii));
						}

						double lengthRSub = getLength(tempR);
						double lengthSSub = getLength(tempS);

						double weightV = (lengthRSub + lengthSSub) / (lengthR + lengthS);

						weight.add(new Double(weightV));
						result.add(new PolygonNew(up, low));
					} else {
						ArrayList<Point> up = new ArrayList<Point>();
						ArrayList<Point> low = new ArrayList<Point>();

						up.add(intersections.get(intersections.size() - 2));

						for (int ii = index.get(index.size() - 2).x + 1; ii <= i; ii++) {
							up.add(r.get(ii));
						}

						up.add(tempP);

						low.add(intersections.get(intersections.size() - 2));
						for (int ii = index.get(index.size() - 2).y + 1; ii <= j; ii++) {
							low.add(s.get(ii));
						}
						low.add(tempP);

						ArrayList<Point> tempR = new ArrayList<Point>();
						for (int ii = index.get(index.size() - 2).x + 1; ii <= i; ii++) {
							tempR.add(r.get(ii));
						}

						ArrayList<Point> tempS = new ArrayList<Point>();
						for (int ii = index.get(index.size() - 2).y + 1; ii <= j; ii++) {
							tempS.add(s.get(ii));
						}

						double lengthRSub = getLength(tempR);
						double lengthSSub = getLength(tempS);

						double weightV = (lengthRSub + lengthSSub) / (lengthR + lengthS);

						weight.add(new Double(weightV));

						result.add(new PolygonNew(up, low));
					}
				}

			}
		}
		return result;

	}

	private static ArrayList<Line> getPolyline(ArrayList<Point> r) {
		ArrayList<Line> result = new ArrayList<Line>();

		if (r.size() < 2) {
			return result;
		}

		for (int i = 0; i < r.size() - 1; i++) {
			Line tempLine = new Line(r.get(i), r.get(i + 1));
			result.add(tempLine);
		}

		return result;
	}
	/*
	 * find the point that are intersect with each other
	 */
	public static Point lineIntersect(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
		double denom = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
		if (denom == 0.0) { // Lines are parallel.
			return null;
		}
		double ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / denom;
		double ub = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / denom;
		if (ua >= 0.0f && ua <= 1.0f && ub >= 0.0f && ub <= 1.0f) {
			// Get the intersection point.
			double[] x = {(x1 + ua * (x2 - x1)), (y1 + ua * (y2 - y1))};
			return new Point(x);
		}
		return null;
	}

}

class twoInt {
	int x;
	int y;

	public twoInt(int i, int j) {
		x = i;
		y = j;
	}
}