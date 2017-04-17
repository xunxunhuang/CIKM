package au.edu.rmit.csit.RkNNT;
import static org.junit.Assert.assertTrue;

import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;

import com.vividsolutions.*;
import au.edu.rmit.csit.RkNNT.mtree.*;
import au.edu.rmit.csit.RkNNT.mtree.MTree.ResultItem;
import au.edu.rmit.csit.RkNNT.mtree.MTree.RkNNT;
import au.edu.rmit.csit.RkNNT.mtree.utils.*;
import au.edu.rmit.csit.index.QuadTreeP.PointRegionQuadTreeP;
import uq.distance.*;
import uq.entities.Point;
import au.edu.rmit.csit.index.QuadTreeP;

public class main {
	/*
	 * define the distance to be ERP.
	 */
	private MTree<ArrayList<Point>> mERP = new MTree<ArrayList<Point>>(
			new DistanceFunction<ArrayList<Point>>() {
			public double calculate(ArrayList<Point> data1, ArrayList<Point> data2) {
				return ERPDistanceCalculator.getERP(data1, data2);
				}
			},
			null
		);
	/*
	 * insert trajectory into index.
	 */
	private void loadTrajectory(){
		//mERP.add(data);
		
	}
	
	private void ReverseMtree(ArrayList<Point> queryData, int limit){
		RkNNT query = mERP.getRkNNT(queryData, limit);
		query.filter(mERP.getroot(), null);
		query.refine();
		ArrayList<ResultItem> result = query.getResult();
		System.out.println(result.size());
	}
	
	/*
	 * employ trajectory counter quadtree to do pruning.
	 */
	private void ReverseQuadtree(){
		PointRegionQuadTreeP aP = new PointRegionQuadTreeP<>(0, 0, 180, 180, 4, 20);
		aP.insert(0, 0);
		aP.queryKNN(0, 1, 1);
	}
	/*
	 * Perpendicular bisector
	 */
	public void PBisector(Point2D a, Point2D b){
		
	}
	/*
     * get the intersection point of a line and rectangle
     * 
     */
    public static ArrayList<Point2D> getIntersectionPoint(Line2D line, Rectangle2D rectangle) {
        Point2D[] p = new Point2D[4];
        ArrayList<Point2D> result = new ArrayList<>();
        // Top line
        p[0] = getIntersectionPoint(line,
                        new Line2D.Double(
                        rectangle.getX(),
                        rectangle.getY(),
                        rectangle.getX() + rectangle.getWidth(),
                        rectangle.getY()));
        // Bottom line
        p[1] = getIntersectionPoint(line,
                        new Line2D.Double(
                        rectangle.getX(),
                        rectangle.getY() + rectangle.getHeight(),
                        rectangle.getX() + rectangle.getWidth(),
                        rectangle.getY() + rectangle.getHeight()));
        // Left side...
        p[2] = getIntersectionPoint(line,
                        new Line2D.Double(
                        rectangle.getX(),
                        rectangle.getY(),
                        rectangle.getX(),
                        rectangle.getY() + rectangle.getHeight()));
        // Right side
        p[3] = getIntersectionPoint(line,
                        new Line2D.Double(
                        rectangle.getX() + rectangle.getWidth(),
                        rectangle.getY(),
                        rectangle.getX() + rectangle.getWidth(),
                        rectangle.getY() + rectangle.getHeight()));
        int i=0;
        for(Point2D aPoint2d: p){
        	if(aPoint2d != null)
        		if(aPoint2d.getX()>rectangle.getMaxX() || aPoint2d.getX()<rectangle.getMinX()
        				|| aPoint2d.getY()>rectangle.getMaxY() || aPoint2d.getY()<rectangle.getMinY()){
        			p[i]=null;
        		}else{
        			result.add(p[i]);
        		}
        	i++;
        }       
        return result;
    }
    /*
     * 
     */
    public static Point2D getIntersectionPoint(Line2D lineA, Line2D lineB) {
        double x1 = lineA.getX1();
        double y1 = lineA.getY1();
        double x2 = lineA.getX2();
        double y2 = lineA.getY2();
        double x3 = lineB.getX1();
        double y3 = lineB.getY1();
        double x4 = lineB.getX2();
        double y4 = lineB.getY2();
        Point2D p = null;
        double d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        if (d != 0) {
            double xi = ((x3 - x4) * (x1 * y2 - y1 * x2) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d;
            double yi = ((y3 - y4) * (x1 * y2 - y1 * x2) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d;
            p = new Point2D.Double(xi, yi);
        }
        return p;
    }
	public static void main(String[] args) {
		Rectangle2D aRectangle2d = new Rectangle();
		aRectangle2d.setRect(0, 0, 2, 2);
		Line2D aLine2d = new Line2D.Double(-1, -1, 2, 1);
		ArrayList<Point2D> aPoint2ds = getIntersectionPoint(aLine2d, aRectangle2d);
		for(Point2D p:aPoint2ds)
			System.out.println(p.getX()+" "+p.getY());
		
		// TODO Auto-generated method stub
		MTree<Integer> mt = new MTree<Integer>(
				new DistanceFunction<Integer>() {
					@Override
					public double calculate(Integer data1, Integer data2) {
						return Math.abs(data1 - data2);
					}
				},
				null
			);
		
		for(int i=0; i<100; i++)
			mt.add(i);

		RkNNT query = mt.getRkNNT(7, 2);
		query.filter(mt.getroot(), null);
		query.refine();
		ArrayList<ResultItem> result = query.getResult();
		System.out.println(result.size());
		
		MTree<Integer>.Query query1 = mt.getNearest(102);
		//Performs a nearest-neighbor query on the M-Tree, without constraints.

		// The first iterator
		Iterator<MTree<Integer>.ResultItem> i1 = query1.iterator();
		assertTrue(i1.hasNext());
		MTree<Integer>.ResultItem ri1 = i1.next();
		System.out.println(ri1.distance+" "+ri1.data);
	}
	/*
	 * insert new trajectory
	 */
	
	/*
	 * search based on iknnt
	 */
	
}