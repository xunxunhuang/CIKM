package au.edu.rmit.csit.FTSE;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.text.html.HTMLDocument.Iterator;
import uq.entities.Point;

public class LCSS {
	static double Threshold=0.01;
	
	public static void main(String[] args) {
        Point p1 = new Point(new double[]{0,0});//x,y
        Point p2 = new Point(new double[]{0,7});
        Point p3 = new Point(new double[]{0,90});
        Point p33 = new Point(new double[]{0,10});
        ArrayList<Point> r =  new ArrayList<>();
        for(int i = 0; i<1000;i++)
        r.add(p1); r.add(p2); r.add(p3);r.add(p33);
        
        Point p4 = new Point(new double[]{2,0});
        Point p5 = new Point(new double[]{2,7});
        Point p6 = new Point(new double[]{2,10});
        ArrayList<Point> s =  new ArrayList<>();
        for(int i = 0; i<1000;i++)
        s.add(p4); s.add(p5); s.add(p6);
        
        long startTime = System.nanoTime();
        double cost = getLCSS(r, s, Threshold);
        long endTime = System.nanoTime();
		System.out.println((double) (endTime - startTime));
        System.out.println("Cost: " + cost);
    }
	
	public static int getLCSS(ArrayList<Point> R, ArrayList<Point> S, double threhold){
		ArrayList<Integer> match = new ArrayList<>();
		if(R.size()>S.size()){
			ArrayList<Point> temp = R;
			R = S;
			S = temp;
		}
		int length=R.size();
		int max=0;
		match.add(0);
		for(int i=1;i< S.size();i++)
			match.add(length);
		Map<Integer, ArrayList<Integer>> L = Intersection.getIntersection(S,R,threhold);// index of cell in trajectory	
		for(int i=0;i< S.size();i++){
			int c=0;
			int temp = match.get(0);
			for(int j=0; j<L.get(i).size(); j++){
				int k = L.get(i).get(j)+1;// the index start from 1
				if(temp<k){
					while(c< match.size() && match.get(c)<k)
						c=c+1;
					if(c == match.size())
						c--;
					temp = match.get(c);
					match.set(c, k);
					if(c>max)
						max=c;
				}
			}
		}

	/*	if(max>2){
			for (Map.Entry<Integer, ArrayList<Integer>> entry : L.entrySet()) {  				  
			    System.out.println("Key = " + entry.getKey());
			    ArrayList<Integer> great =  entry.getValue();
			   	for(int i =0 ; i<great.size(); i++)
			   		System.out.println(great.get(i));
			} 
			System.out.println("the length is "+length);
			
		}*/
		return max;
	}
}
