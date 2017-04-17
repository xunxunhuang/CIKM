package au.edu.rmit.csit.FTSE;
import java.util.ArrayList;
import java.util.Map;

import au.edu.rmit.csit.reverse.precomputation;

import uq.entities.Point;

public class EDR {
	static double Threshold= 10;	
	public static void main(String[] args) {
        Point p1 = new Point(new double[]{0,0});//x,y
        Point p2 = new Point(new double[]{0,7});
        Point p3 = new Point(new double[]{0,10});
        Point p33 = new Point(new double[]{0,10});
        ArrayList<Point> r =  new ArrayList<>();
        for(int i = 0; i<1000;i++)
        r.add(p1); r.add(p2); r.add(p3);r.add(p33);
        
        Point p4 = new Point(new double[]{2,0});
        Point p5 = new Point(new double[]{2,7});
        Point p6 = new Point(new double[]{2,20});
        ArrayList<Point> s =  new ArrayList<>();
        for(int i = 0; i<1000;i++)
        s.add(p4); s.add(p5); s.add(p6);
        
        long startTime = System.nanoTime();
        double cost = getEDR(r, s);
        long endTime = System.nanoTime();
		System.out.println((double) (endTime - startTime));
        System.out.println("Cost: " + cost);
    }
	
	public static int getEDR(ArrayList<Point> R, ArrayList<Point> S){		
		ArrayList<Integer> match = new ArrayList<>();
		int length=2*R.size()+1;
		int max=0;
		match.add(0);
		for(int i=1;i< S.size()*2+1;i++)
			match.add(length);
		int c=0;
		int temp=0, temp2=0;
		Map<Integer, ArrayList<Integer>> L = Intersection.getIntersection(R,S,Threshold);// index of cell in trajectory
		for(int i=0;i< S.size();i++){			
			temp = match.get(0);
			temp2 = match.get(0);
			for(int j=0; j<L.get(i).size(); j++){
				int k = L.get(i).get(j)+1;
				if(temp<k){
					while(match.get(c)<k){
						if(temp<match.get(c)-1 && temp<S.size()-1){
							temp2= match.get(c);
							match.set(c, temp+1);
							temp = temp2;
						}else{
							temp = match.get(c);
						}
						c=c+1;
					}
					temp2 = match.get(c);
					match.set(c, temp+1);
					temp = match.get(c+1);
					if(match.get(c+1)>k)
						match.set(c+1, k);
					if(max<c+1)
						max=c+1;
					c=c+2;
				}else if(temp2<k && k<match.get(c)){
					temp2 = temp;
					temp = match.get(c);
					match.set(c, k);
					if(max<c)
						max=c;
					c=c+1;
				}
			}
			for(int j=c; j<max+1; j++){
				if(temp<match.get(j)-1 && temp<S.size()-1){
					temp2 = match.get(j);
					match.set(j, temp+1);
					temp = temp2;
					if(max<j)
						max=j;
				}else{
					temp = match.get(j);
				}
			}
		}		
		return max-(R.size()+S.size());
	}
}
