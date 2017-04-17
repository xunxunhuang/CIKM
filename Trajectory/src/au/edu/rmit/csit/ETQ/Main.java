package au.edu.rmit.csit.ETQ;


import java.util.ArrayList;
import java.util.concurrent.ConcurrentMap;

import org.mapdb.DB;
import org.mapdb.DBMaker;

public class Main {
	public static void main(String[] args) {
		DB db = DBMaker.memoryDB().make();
		ConcurrentMap map = db.hashMap("map").make();
		map.put("something", "here");
		map.put(1,2);
		System.out.println(map.get("something"));
		
		int array[] = {-122,31,-117,36};
		String file = "/home/wangsheng/Desktop/Data/LA-standard-clean.tra";
		lcsssearch.load_standard(file,array);
		ArrayList<Point> query = new ArrayList<>();
		String[] aStrings1 = { "hello", "fuck" };
		String[] aStrings2 = { "what", "fuck" };
		Point p4 = new Point(new double[] { 2, 0 }, aStrings1);
		Point p5 = new Point(new double[] { 2, 7 }, aStrings1);
		Point p6 = new Point(new double[] { 2, 10 }, aStrings2);
		query.add(p4);
		query.add(p5);
		query.add(p6);
		lcsssearch.query_basic(query,10);
	}
}
