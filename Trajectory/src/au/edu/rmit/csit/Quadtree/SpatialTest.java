package au.edu.rmit.csit.Quadtree;
import org.junit.Test;

import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SpatialTest {

    static List<Point> _pointList = null;

    private static void LoadPointsFromFile(String source) {
        String[] item;
        String[] lines = readAllTextFileLines(source);
        for (String line : lines) {
            item = line.split(",");
            _pointList.add(new Point(Double.parseDouble(item[2]), Double.parseDouble(item[1]), Double.parseDouble(item[0])));
        }
    }

    private static String[] readAllTextFileLines(String fileName) {
        StringBuilder sb = new StringBuilder();

        try {
            String textLine;

            BufferedReader br = new BufferedReader(new FileReader(fileName));

            while ((textLine = br.readLine()) != null) {
                sb.append(textLine);
                sb.append('\n');
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        } finally {
            if (sb.length() == 0)
                sb.append("\n");
        }
        return sb.toString().split("\n");
    }

    @Test
    public static void main(String[] args) {
    	Rectangle r1 = new Rectangle(0, 0, 100, 100);
		Line2D l1 = new Line2D.Float(0, 200, 200, 200);
		
		System.out.println("l1.intsects(r1) = " + l1.intersects(r1));
        _pointList = new ArrayList<Point>();
        URL classpathResource = Thread.currentThread().getContextClassLoader().getResource("");
        String resourcePath = classpathResource.getPath()+"points.txt";
        LoadPointsFromFile(resourcePath);
        assertEquals("Expecting 844 points",844,_pointList.size());

        //http://spatialreference.org/ref/epsg/4326/
        QuadTree qt = new QuadTree(-180.000000, -90.000000, 180.000000, 90.000000);
        for(Point pt:_pointList)
        {
            qt.set(pt.getX(), pt.getY(), pt.getValue());
        }
        Point[] points = qt.searchIntersect(-84.375,27.059,-78.75,31.952 );
        Point aPoint = new Point(-84.375, 27.059, null);
        qt.knn(aPoint, 1);
        System.out.print( Arrays.asList(points).toString());
        assertEquals(60,points.length);
    }


}
