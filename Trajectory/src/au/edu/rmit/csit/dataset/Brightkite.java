package au.edu.rmit.csit.dataset;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import javax.swing.text.AbstractDocument.Content;

import au.edu.rmit.csit.index.Gridindex;
import uq.entities.Point;
import au.edu.rmit.csit.Quadtree.*;
import au.edu.rmit.csit.reverse.*;
/*
 * clean dataset
 */
public class Brightkite {
	public static void write(String fileName, String content) {   
        RandomAccessFile randomFile = null;  
        try {       
            randomFile = new RandomAccessFile(fileName, "rw");     
            long fileLength = randomFile.length();     
            randomFile.seek(fileLength);     
            randomFile.writeBytes(content);      
        } catch (IOException e) {     
            e.printStackTrace();     
        } finally{  
            if(randomFile != null){  
                try {  
                    randomFile.close();  
                } catch (IOException e) {  
                    e.printStackTrace();  
                }  
            }
        }
    }
	public static void range(){
		String file = "/media/wangsheng/SW/Data/Stanford-SNAP/Brightkite_totalCheckins_58227.txt";
		String out = "/media/wangsheng/SW/Data/Stanford-SNAP/LA.txt";
		double minx =91;
		double maxx = -91;
		double miny = 181;
		double maxy = -181;
		try {
			Scanner in = new Scanner(new BufferedReader(new FileReader(file)));
			int k = 0;
			while (in.hasNextLine()) {
				int count = 0;
				String str = in.nextLine();
				String strr = str.trim();
				String[] abc = strr.split("\t");				
				if(abc.length>4){
				int line = Integer.valueOf(abc[0]);// the identifier of
				double x = Double.valueOf(abc[2]);
				double y = Double.valueOf(abc[3]);
				int order = 1;							
			/*	if (y > Gridindex.global_y1 && y < Gridindex.global_y2 && x > Gridindex.global_x1
						&& x < Gridindex.global_x2) {// out of scope
					write(out, str+"\n");
				}*/
				if(x<90 && x>-90){
				if(x>maxx)
					maxx = x;
				else if(x<minx)
					minx = x;
				}
				if(y>maxy)
					maxy = y;
				else if(y<miny)
					miny = y;
				}				
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println(minx+"\t"+miny);
		System.out.println(maxx+"\t"+maxy);
	}	
	/*
	 * filte_repetitive points.
	 */
	public static void filter_repetive(String file, String out) {
		String xy = "";
		int id_temp = -1;
		Map<Integer, Integer> tra_null = new HashMap<>();
		try {
			Scanner in = new Scanner(new BufferedReader(new FileReader(file)));
			while (in.hasNextLine()) {
				String str = in.nextLine();
				String strr = str.trim();
				String[] abc = strr.split("\t");
				if (abc.length > 4) {
					int tra_id = Integer.valueOf(abc[0]);
					tra_null.put(tra_id, 0);
					if ((xy != "" && !abc[4].equals(xy)) || tra_id != id_temp) {
				//		System.out.println(xy+"\t"+abc[4]);
						write(out, str+"\n");
					}
					xy = abc[4];
					id_temp = tra_id;
				}				
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println(tra_null.size());	
	}
	/*
	 *extract a longest one day
	 */
	public static void extract(String file, String out){
		String xy = "";
		int tra_id_temp = -1;
		Map<String, Integer> day_count = new HashMap<>();
		Map<Integer, String> tra_day = new HashMap<>();
		Map<Integer, Integer> tra_null = new HashMap<>();
		try {
			Scanner in = new Scanner(new BufferedReader(new FileReader(file)));
			while (in.hasNextLine()) {
				int count = 0;
				String str = in.nextLine();
				String strr = str.trim();
				String[] abc = strr.split("\t");
				int tra_id = Integer.valueOf(abc[0]);
				tra_null.put(tra_id, 0);
				if (abc.length > 4) {
					String aString[] = abc[1].split("T");					
					if(tra_id == tra_id_temp){
						int d_count=1;
						if(day_count.containsKey(aString[0])){
							d_count = day_count.get(aString[0])+1;
						}
						day_count.put(aString[0], d_count);
					}else {
						if (!day_count.isEmpty()) {
							day_count = precomputation.sortByValue(day_count);							
							Iterator<String> iterator = day_count.keySet().iterator();
							String aString2 = iterator.next();
						//	if(tra_id_temp==1){
						//		System.out.println(day_count.get(aString2));
								System.out.println(tra_id_temp + " " + aString2 + " "+ day_count.get(aString2));
						//	}
							if(day_count.get(aString2)>1)
								tra_day.put(tra_id_temp, aString2);// get the day with most points							
						}
						day_count = new HashMap<>();
						day_count.put(aString[0], 1);
					}					
				}
				tra_id_temp = tra_id;
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}		
		try {
			Scanner in = new Scanner(new BufferedReader(new FileReader(file)));
			while (in.hasNextLine()) {
				int count = 0;
				String str = in.nextLine();
				String strr = str.trim();
				String[] abc = strr.split("\t");
				int tra_id = Integer.valueOf(abc[0]);
				tra_null.put(tra_id, 0);
				if (abc.length > 4) {
					String aString[] = abc[1].split("T");					
					if(tra_day.containsKey(tra_id)){
						String aString2 = tra_day.get(tra_id);
						if(aString2 == aString[0]){
							write(out, str+"\n");
						}
					}
				}
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println(tra_day.size() + "\n"+tra_null.size());
	}
	/*
	 *divide one trajectory into many trajectories by day, and choose the longest one
	 */
	public static void filter_onlyone(String file, String out){
		String xy = "";
		int tra_id_temp = -1;
		Map<String, Integer> day_count = new HashMap<>();
		Map<Integer, String> tra_day = new HashMap<>();
		Map<Integer, Map<String, Integer>> tra_day_count = new HashMap<>();
		Map<Integer, Integer> tra_null = new HashMap<>();
		try {
			Scanner in = new Scanner(new BufferedReader(new FileReader(file)));
			while (in.hasNextLine()) {
				int count = 0;
				String str = in.nextLine();
				String strr = str.trim();
				String[] abc = strr.split("\t");
				int tra_id = Integer.valueOf(abc[0]);
				tra_null.put(tra_id, 0);
				if (abc.length > 4) {
					String aString[] = abc[1].split("T");					
					if(tra_id == tra_id_temp){
						int d_count=1;
						if(day_count.containsKey(aString[0])){
							d_count = day_count.get(aString[0])+1;
						}
						day_count.put(aString[0], d_count);
					}else {
						if (!day_count.isEmpty()) {
							day_count = precomputation.sortByValue(day_count);// sort by the count descendly.
							tra_day_count.put(tra_id_temp, day_count);						
						}
						day_count = new HashMap<>();
						day_count.put(aString[0], 1);
					}					
				}
				tra_id_temp = tra_id;
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}		
		try {
			Scanner in = new Scanner(new BufferedReader(new FileReader(file)));
			while (in.hasNextLine()) {
				int count = 0;
				String str = in.nextLine();
				String strr = str.trim();
				String[] abc = strr.split("\t");
				int tra_id = Integer.valueOf(abc[0]);
				tra_null.put(tra_id, 0);				
				if (abc.length > 4) {
					String aString[] = abc[1].split("T");					
					if(tra_day_count.containsKey(tra_id)){
						day_count = tra_day_count.get(tra_id);
						if(day_count.containsKey(aString[0])){
							int count1 = day_count.get(aString[0]);
							if(count1>1){// delete those days which only contains one points.
								write(out, str+"\n");
							}
						}
					}
				}
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println(tra_day.size() + "\n"+tra_null.size());
	}
	/*
	 * divide one trajectory into many sub-trajectory by day
	 */
	public static void divide_by_day(String file, String out) {
		String string_temp ="";
		int i=0;
		try {
			Scanner in = new Scanner(new BufferedReader(new FileReader(file)));
			while (in.hasNextLine()) {
				int count = 0;
				String str = in.nextLine();
				String strr = str.trim();
				String[] abc = strr.split("\t");
				int tra_id = Integer.valueOf(abc[0]);			
				if (abc.length > 4) {
					String aString[] = abc[1].split("T");
					if(!string_temp.equals(aString[0])){
						i++;
					}
					String content = Integer.toString(i)+"\t";
					for(int i1=1; i1<abc.length; i1++)
						content += abc[i1]+"\t";
					content+="\n";
					write(out, content);					
					string_temp = aString[0];
				}
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println(i);
	}
	/*
	 * transfer to standard
	 */
	public static void transfer_standard(String file, String out) {
		int id_temp = -1;
		Map<Integer, Integer> tra_null = new HashMap<>();
		String content = "";
		int count = 0;
		try {
			Scanner in = new Scanner(new BufferedReader(new FileReader(file)));
			while (in.hasNextLine()) {
				String str = in.nextLine();
				String strr = str.trim();
				String[] abc = strr.split("\t");
				if (abc.length > 4) {
					int tra_id = Integer.valueOf(abc[0]);
					tra_null.put(tra_id, 0);					
					if (tra_id != id_temp && id_temp != -1) {
						write(out, abc[0]+"\t"+ Integer.toString(count)+"\t"+content+"\n");
						count=1;
						content = abc[2] +"," +abc[3]+";";						
					}else{
						content += abc[2] +"," +abc[3]+";";
						count++;
					}
					id_temp = tra_id;
				}				
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println(tra_null.size());
	}
	/*
	 * load the dataset of 
	 */
	public static QuadTree load_db_brightkite(String file, Map<Integer, ArrayList<Point>> trajectory_db, QuadTree qt){
		//divide by a week.
		qt = new QuadTree(-180, -90, 180, 90);
		ArrayList<Point> trajectory = new ArrayList<>();
		int line = 0;
		double x = 0, y = 0;
		int temp_c =line;
		try {
			Scanner in = new Scanner(new BufferedReader(new FileReader(file)));
			while (in.hasNextLine()) {
				String str = in.nextLine();
				String strr = str.trim();
				String[] abc = strr.split("\t");
				if (abc.length > 4) {
					line = Integer.valueOf(abc[0]);// the identifier of
					x = Double.valueOf(abc[2]);
					y = Double.valueOf(abc[3]);
					int order = 1;
					if (y > -179.824219 && y < 179.999 && x > -89.999 && x < 89.999999) {
						Point a = new Point(new double[] { x, y });
						if (line == temp_c)
							order++;
						else
							order = 1;						
						String temp = Integer.toString(line) + "_" + Integer.toString(order);
						qt.set(y, x, temp);
						if (trajectory_db.containsKey(line)) {
							trajectory = trajectory_db.get(line);
						} else {
							trajectory = new ArrayList<>();
						}
						trajectory.add(a);
						trajectory_db.put(line, trajectory);
					}
				}
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println("ads"+qt.getCount());
		System.out.println(trajectory_db.size());
		return qt;
	}
	/*
	 * divide one to many.
	 */
	public void creat_new_dat_set() {
		
	}
	public static void tra_clus(){
		
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	//	load_db_brightkite("/media/wangsheng/SW/Data/Stanford-SNAP/Brightkite_totalCheckins_58227.txt");
	//	filter_repetive();
		String file = "/media/wangsheng/SW/Data/Stanford-SNAP/Gowalla_totalCheckins_196585.txt";
		file =  "/media/wangsheng/SW/Data/Stanford-SNAP/Brightkite_totalCheckins_58227.txt";
	//	filter_repetive(file);
	//	extract();
	//	transfer_standard("/home/wangsheng/Desktop/brightkite_filte.txt");
		
	/*	file = "/home/wangsheng/Desktop/Data/extra/brightkite_filte.txt";
		filter_onlyone(file);
		divide_by_day("/home/wangsheng/Desktop/brightkite_filte_only_oneinoneday.txt");
		transfer_standard("/home/wangsheng/Desktop/brightkite_divive_by_day.txt");*/
	/*	file = "/home/wangsheng/Desktop/Data/extra/Gowalla_filte.txt";
		filter_onlyone(file, "/home/wangsheng/Desktop/Data/extra/Gowalla_filte_only_oneinoneday.txt");
		divide_by_day("/home/wangsheng/Desktop/Data/extra/Gowalla_filte_only_oneinoneday.txt", "/home/wangsheng/Desktop/Data/extra/Gowalla_divive_by_day.txt");
		transfer_standard("/home/wangsheng/Desktop/Data/extra/Gowalla_divive_by_day.txt", "/home/wangsheng/Desktop/Data/extra/Gowalla_filte_standard_divide.txt");*/
		
		file = "/home/wangsheng/Desktop/Data/extra/brightkite_filte.txt";
		filter_onlyone(file, "/home/wangsheng/Desktop/Data/extra/brightkite_filte_only_oneinoneday.txt");
		divide_by_day("/home/wangsheng/Desktop/Data/extra/brightkite_filte_only_oneinoneday.txt", "/home/wangsheng/Desktop/Data/extra/brightkite_divive_by_day.txt");
		transfer_standard("/home/wangsheng/Desktop/Data/extra/brightkite_divive_by_day.txt", "/home/wangsheng/Desktop/Data/extra/brightkite_filte_standard_divide.txt");
	}
}
