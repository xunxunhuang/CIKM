package au.edu.rmit.csit.skindex.RCA;

import java.util.ArrayList;
import java.util.NavigableSet;
import java.util.Vector;
import org.mapdb.DB;

import uq.entities.Point;

public class Gridindex {
	
	public static DB db;
	
	public static int resolution = 256;    // set resolution here, power(2,8)

	// LA	
	public static double global_x1 = 32.0;		// set border x1 here
	public static double global_y1 = -120.0;		// set border x1 here
	public static double global_x2 = 35.0;		// set border x1 here
	public static double global_y2 = -117.0;		// set border x1 here
	
	//NYC
/*	public static double global_x1 = 39.0;		// set border x1 here
	public static double global_y1 = -75.0;		// set border x1 here
	public static double global_x2 = 42.0;		// set border x1 here
	public static double global_y2 = -73.0;		// set border x1 here
	*/

    
    /*
	 * find all the cells covered by the query rectangle.
	 */
	public static ArrayList<Integer> cover_cells_threshold(double x, double y, double threshold){
		ArrayList<Integer> cover_region = new ArrayList<Integer>();
		double minx = x-threshold;
		double miny = y-threshold;
		double maxx = x+threshold;
		double maxy = y+threshold;
		//get the distance 
		int minClat = (int) ((minx-global_y1)/((global_y2-global_y1)/resolution));
		int minClng = (int) ((miny-global_x1)/((global_x2-global_x1)/resolution));
		int maxClat = (int) ((maxx-global_y1)/((global_y2-global_y1)/resolution));
		int maxClng = (int) ((maxy-global_x1)/((global_x2-global_x1)/resolution));
		int i,j;
		for(i = minClat; i<=maxClat; i++){
			for(j=minClng; j<=maxClng; j++){
				int cellid = get_cellid_level(i,j,resolution);
				cover_region.add(cellid);				
			}
		}
		return cover_region;
	}
	/*
	 * find all the cells covered by the query rectangle.
	 */
	public static ArrayList<Integer> cover_cells_threshold_nine(double x, double y, double threshold, int resolution, double x1, double y1, double x2, double y2){
		ArrayList<Integer> cover_region = new ArrayList<Integer>();
		double minx = x-threshold;
		double miny = y-threshold;
		double maxx = x+threshold;
		double maxy = y+threshold;
		//get the distance 
		int minClat = (int) ((minx-y1)/((y2-y1)/resolution));
		int minClng = (int) ((miny-x1)/((x2-x1)/resolution));
		int maxClat = (int) ((maxx-y1)/((y2-y1)/resolution));
		int maxClng = (int) ((maxy-x1)/((x2-x1)/resolution));
		int i,j;
		for(i = minClat; i<=maxClat; i++){
			for(j=minClng; j<=maxClng; j++){
				int cellid = get_cellid_level(i,j,resolution);
				cover_region.add(cellid);				
			}
		}
		return cover_region;
	}
	
	public static int ComputeCellid(double lat, double lng){
		int Clat = (int) ((lat-global_y1)/((global_y2-global_y1)/resolution));
		int Clng = (int) ((lng-global_x1)/((global_x2-global_x1)/resolution));
		return Clng*resolution + Clat+1;
	}
	public static int ComputeCellid(double lat, double lng, int resolution1){// used in the icde2013
		int Clat = (int) ((lat-global_y1)/((global_y2-global_y1)/resolution1));
		int Clng = (int) ((lng-global_x1)/((global_x2-global_x1)/resolution1));
		return Clng*resolution1 + Clat+1;
	}
	public static int get_cellid_level(int cla, int clg, int num){
		int m=0;
		int result = 0;
		for(;num>1 ;num/=2){
			m++;
		}
		int[] value = new int[2*m];		
		for(int i = m-1; i>=0 ; i--){
			if((int)(cla/Math.pow(2,i)) == 1){
				value[i*2] = 1;
				cla  = (int) (cla - Math.pow(2,i));
				
			}
			else if((int)(cla / Math.pow(2,i)) == 0){
				value[i*2] = 0;
			}
			if((int)(clg/Math.pow(2,i)) == 1){
				value[i*2+1] = 1;
				clg  = (int) (clg - Math.pow(2,i));
			}
			else if((int)(clg / Math.pow(2,i)) == 0){
				value[i*2+1] = 0;
			}
		}
		for(int i = 0 ; i < 2*m; i++){
			result += value[i] * Math.pow(2, i);
		}
		result += 1;	
		return result;
	}
	/*
	 * z-order curve
	 */
	public static int ComputeCellid1(double lat, double lng, int num){
		int cla = (int) ((lat-global_y1)/((global_y2-global_y1)/resolution));
		int clg = (int) ((lng-global_x1)/((global_x2-global_x1)/resolution));
		int m=0;
		int result = 0;
		for(;num>1 ;num/=2){
			m++;
		}
		int[] value = new int[2*m];		
		for(int i = m-1; i>=0 ; i--){
			if((int)(cla/Math.pow(2,i)) == 1){
				value[i*2] = 1;
				cla  = (int) (cla - Math.pow(2,i));
				
			}
			else if((int)(cla / Math.pow(2,i)) == 0){
				value[i*2] = 0;
			}
			if((int)(clg/Math.pow(2,i)) == 1){
				value[i*2+1] = 1;
				clg  = (int) (clg - Math.pow(2,i));
			}
			else if((int)(clg / Math.pow(2,i)) == 0){
				value[i*2+1] = 0;
			}
		}
		for(int i = 0 ; i < 2*m; i++){
			result += value[i] * Math.pow(2, i);
		}
		result += 1;
		//we need to compute the binary value first, then we can merge them.		
		return result;
	}
	/*
	 * z-order curve
	 */
	public static int ComputeCellid_nine(double lat, double lng, int num, double x1, double y1, double x2, double y2){
		int cla = (int) ((lat-y1)/((y2-y1)/num));
		int clg = (int) ((lng-x1)/((x2-x1)/num));
		int m=0;
		int result = 0;
		for(;num>1 ;num/=2){
			m++;
		}
		int[] value = new int[2*m];		
		for(int i = m-1; i>=0 ; i--){
			if((int)(cla/Math.pow(2,i)) == 1){
				value[i*2] = 1;
				cla  = (int) (cla - Math.pow(2,i));
				
			}
			else if((int)(cla / Math.pow(2,i)) == 0){
				value[i*2] = 0;
			}
			if((int)(clg/Math.pow(2,i)) == 1){
				value[i*2+1] = 1;
				clg  = (int) (clg - Math.pow(2,i));
			}
			else if((int)(clg / Math.pow(2,i)) == 0){
				value[i*2+1] = 0;
			}
		}
		for(int i = 0 ; i < 2*m; i++){
			result += value[i] * Math.pow(2, i);
		}
		result += 1;
		//we need to compute the binary value first, then we can merge them.		
		return result;
	}
	public static double point_distance(double x1, double x2, double y1, double y2) {
		return Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
	}
	public static double compute_distance(int result, int num, double x, double y){//z-order curve
		int m=0;
		int a = num;
		for(;num>1 ;num/=2){
			m++;
		}		
		int[] value = new int[2*m];
		int lat_1 = 0;
		int lng_1 = 0;		
		
		result -= 1;
		for(int i = 2*m-1 ; i >=0; i--){
			if((int)(result/Math.pow(2, i))==1){
				value[i] = 1;
				result = (int)(result - Math.pow(2, i));
			}
			else{
				value[i] = 0;
			}
		}		
		for(int i = m-1; i>=0 ; i--){
			lat_1 += (int) (value[2*i]*Math.pow(2, i));
			lng_1 += (int) (value[2*i+1]*Math.pow(2, i));
		}
		
		double distance;
		int x_grid = lat_1;
		int y_grid = lng_1;
			
		double cell_low_x = x_grid*((Gridindex.global_x2-Gridindex.global_x1)/a)+Gridindex.global_x1;
		double cell_low_y = y_grid*((Gridindex.global_y2-Gridindex.global_y1)/a)+Gridindex.global_y1;
		double cell_high_x = cell_low_x + (Gridindex.global_x2-Gridindex.global_x1)/a;
		double cell_high_y = cell_low_y + (Gridindex.global_y2-Gridindex.global_y1)/a;
		if(x < cell_low_x){
			if(y < cell_low_y){
				distance = point_distance(x,cell_low_x, y, cell_low_y);
			}else if(y>cell_high_y){
				distance = point_distance(x,cell_low_x, y, cell_high_y);
			}
			else{
				distance = Math.abs(x-cell_low_x);
			}
		}else if(x>cell_high_x){
			if(y < cell_low_y){
				distance = point_distance(x,cell_high_x, y, cell_low_y);
			}else if(y>cell_high_y){
				distance = point_distance(x,cell_high_x, y, cell_high_y);
			}
			else{
				distance = Math.abs(x-cell_high_x);
			}
		}
		else{
			if(y < cell_low_y){
				distance = cell_low_y - y;
			}else if(y>cell_high_y){
				distance = y - cell_high_y;
			}
			else{
				distance = 0;
			}
		}
		return distance;
	}
	
	public static Vector<Integer> ComputeCellids(double x1, double y1, double x2, double y2){
		Vector<Integer> results = new Vector<Integer>();
		int MinCid = ComputeCellid(y1, x1);
		int MaxCid = ComputeCellid(y2, x2);
		int XMaxYMinCid = ComputeCellid(y1, x2);
		int XMinYMaxCid = ComputeCellid(y2, x1);
		int Div = XMaxYMinCid - MinCid;
		System.out.println("MinCid: " + MinCid + "  MaxCid: " + MaxCid + "  XMaxYMinCid: " + XMaxYMinCid + "  XMinYMaxCid: " + XMinYMaxCid);
		for(int i=MinCid; i<XMinYMaxCid; i+=Gridindex.resolution){
			for(int j=i; j<=i+Div; j++){
				results.add(j);
			}
		}
		System.out.println(results);
		return results;
	}
}