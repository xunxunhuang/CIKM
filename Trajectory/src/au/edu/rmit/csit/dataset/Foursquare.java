package au.edu.rmit.csit.dataset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.swing.text.AbstractDocument.Content;

import au.edu.rmit.csit.Quadtree.QuadTree;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.POS;
/*
 * clean the dataset
 * 1. remove 
 */
public class Foursquare {
	/*
	 * code to locations
	 */
	public static void main(String[] args) throws IOException {
	//	clus_tra("LA");
	//	time_convert("LA");
		String word_id_file = "C:/Users/e29944/workspace-java/Trajectory/Data/ETQ/NYC/word_id_file.txt";
		String point_coordinate ="C:/Users/e29944/workspace-java/Trajectory/Data/ETQ/NYC/point_coordinate.txt"; 
		String point_trajectory = "C:/Users/e29944/workspace-java/Trajectory/Data/ETQ/NYC/point_trajectory.txt";
		String all_word_id =  "C:/Users/e29944/workspace-java/Trajectory/Data/ETQ/NYC/all_word_id.txt";
		String query =  "C:/Users/e29944/workspace-java/Trajectory/Data/ETQ/NYC/10.txt";
		String query_id =  "C:/Users/e29944/workspace-java/Trajectory/Data/ETQ/NYC/query.txt";
	//	convert_standard("NYC", word_id_file, point_coordinate, point_trajectory, all_word_id);
	//	transfer_query(word_id_file, query, query_id);
	//	enlarge_trajectory_length("/home/wangsheng/Desktop/Data/NYC-standard-clean.tra", "/home/wangsheng/Desktop/Data/NYC-standard-clean-length2.tra", 2);
	//	enlarge_trajectory_count("/home/wangsheng/Desktop/Data/NYC-standard-clean.tra", "/home/wangsheng/Desktop/Data/NYC-standard-clean-size2.tra", 2);
	//	divide_segment("F:/Data/Clean Data/LA-standard-clean.tra", "F:/Data/Clean Data/LA-segment.tra");
		divide_segment("F:/Data/Clean Data/NYC-standard-clean.tra", "F:/Data/Clean Data/NYC-segment.tra");
	}
	public static void divide_segment(String file, String output){
		int counter = 0;
		try {
			Scanner in = new Scanner(new BufferedReader(new FileReader(file)));
			while (in.hasNextLine()) {
				String str = in.nextLine();
				String strr = str.trim();
				String[] abc = strr.split("\t");
				String word = abc[2];
				String[] points = word.split(";");
				for(int i = 0; i<points.length-1; i++){
					Brightkite.write(output, counter++ +" " + points[i]+";"+points[i+1]+"\n");
				}
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static Map<String, Map<Float, Float>> StringtoLoc(String file) throws FileNotFoundException {// find
		Map<String, Map<Float, Float>> a;
		Map<Float, Float> b;
		a = new HashMap<String, Map<Float, Float>>();
		try {
			Scanner in = new Scanner(new BufferedReader(new FileReader(file)));
			while (in.hasNextLine()) {
				String str = in.nextLine();
				String strr = str.trim();
				String[] abc = strr.split("\t");
				String word = abc[0];
				float locx = Float.valueOf(abc[2]);
				float locy = Float.valueOf(abc[3]);
				if (!a.containsKey(word)) {
					b = new HashMap<Float, Float>();
					b.put(locx, locy);
					a.put(word, b);
				}
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return a;
	}
	public static boolean isNumeric(String str) {
		return str.matches("-?\\d+(\\.\\d+)?"); // match a number with optional
												// '-' and decimal.
	}

	public static void clus_tra(String name) {
		String file = "/media/wangsheng/SW/Laptop/D/NYC/NYC-Tips.txt";
		double minx, miny;
		Map<String, Map<Float, Float>> locmap;
		int line;
		double x, y;
		if (name == "LA") {
			minx = 32.0;
			miny = -120;
		} else {
			minx = 39;
			miny = -75;
		}
		try {
			Scanner in = new Scanner(new BufferedReader(new FileReader(file)));
			String venues = "/media/wangsheng/SW/Laptop/D/NYC/NYC-Venues.txt";
			locmap = StringtoLoc(venues);
			int k = 0;
			String whole = "";
			while (in.hasNextLine()) {
				
				int count = 0;
				String str = in.nextLine();
				String strr = str.trim();
				String[] abc = strr.split("\t");
				if (isNumeric(abc[0]))
					line = Integer.valueOf(abc[0]);
				else
					continue;
				String xy = "";
				int number =0;
				while (count < abc.length) {
					String str1 = abc[count];
					if (str1.equals("null")) {// the location and text is among
						String loc = abc[count - 1];
						Map<Float, Float> locxy = locmap.get(loc);// get the
						for (Map.Entry<Float, Float> entry : locxy.entrySet()) {  
							x = entry.getKey();
							y = entry.getValue();
							if (y > miny && y < miny + 3 && x > minx && x < minx + 3) {
								xy += Double.toString(x) + " " + Double.toString(y) + " ";
								number++;
							}
						}
					}
					count++;
				}
				if(number!=0){
					xy = Integer.toString(k)+" "+Integer.toString(number)+" "+xy+"\n";
					Brightkite.write("/home/wangsheng/Desktop/NYC-clus.tra", xy);
					k++;
				}
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	public static void time_convert(String name) {
		String file = "/media/wangsheng/SW/Laptop/D/LA/LA-Tips.txt";
		double minx, miny;
		Map<String, Map<Float, Float>> locmap;
		int line;
		double x, y;
		if (name == "LA") {
			minx = 32.0;
			miny = -120;
		} else {
			minx = 39;
			miny = -75;
		}
		int ac =0;
		try {
			Scanner in = new Scanner(new BufferedReader(new FileReader(file)));
			String venues = "/media/wangsheng/SW/Laptop/D/LA/LA-Venues.txt";
			locmap = StringtoLoc(venues);
			int k = 0;
			String whole = "";
			String aString = "";
			while (in.hasNextLine()) {
				
				int count = 0;
				String str = in.nextLine();
				String strr = str.trim();
				String[] abc = strr.split("\t");
				if (isNumeric(abc[0]))
					line = Integer.valueOf(abc[0]);
				else
					continue;
				Date tDate = null;
				int tem=0;
				int time_c =0;
				
				while (count < abc.length) {
					String str1 = abc[count];
					if (str1.equals("null")) {// the location and text is among
						String loc = abc[count - 1];
					//	if (abc.length>count+3) {
							long time = Long.valueOf(abc[count + 2])*1000;//transfer the timestamp to code.
							Date aDate = new java.util.Date(time);
					//	}
						Map<Float, Float> locxy = locmap.get(loc);// get the
						for (Map.Entry<Float, Float> entry : locxy.entrySet()) {  
							x = entry.getKey();
							y = entry.getValue();
							String xy = "";
							if (y > miny && y < miny + 3 && x > minx && x < minx + 3) {
								xy += abc[0]+ "\t"+aDate.toString()+"\t"+Double.toString(x) + "\t" + Double.toString(y)+"\n";
								SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
							//	System.out.println(fmt.format(tDate));
								time_c ++;
								if(tem == line && tDate!=null && !fmt.format(tDate).equals(fmt.format(aDate))){
									xy = "\n"+xy;
									if(time_c>3)
										ac++;
									time_c = 0;
								}
								if(tem != line){
									xy = "\n"+xy;
									if(time_c>1)
										ac++;
									time_c = 0;									
								}
							}
							Brightkite.write("/home/wangsheng/Desktop/LA-time.txt", xy);
						}
						tDate = aDate;
						tem = line;
					}
					count++;
				}
				
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println(ac);
	}
	/*
	 * 
	 */
	public static void convert_standard(String name, String word_id_file, String point_coordinate, 
			String point_trajectory, String all_word_id) throws IOException {
		String file = "C:/Users/e29944/workspace-java/Trajectory/Data/ETQ/"+name+"/"+name+"-Tips.txt";
		double minx, miny;
		Map<String, Map<Float, Float>> locmap;
		int line;
		double x, y;
		String path = "C:/Program Files (x86)/WordNet/2.1" + File.separator + "dict";
		URL url = new URL("file", null, path);
		IDictionary dict = new Dictionary(url);
		dict.open();
		Map<String, Integer> word_id = new HashMap<>();
		Map<Integer,Map<Integer, Integer>> point_term_count = new HashMap<>();
		int id = 1;
		int point_id=0;
		if (name == "LA") {
			minx = 32.0;
			miny = -120;
		} else {
			minx = 39;
			miny = -75;
		}
		try {
			Scanner in = new Scanner(new BufferedReader(new FileReader(file)));
			String venues = "C:/Users/e29944/workspace-java/Trajectory/Data/ETQ/"+name+"/"+name+"-Venues.txt";
			locmap = StringtoLoc(venues);
			int k = 0;
			String whole = "";
			while (in.hasNextLine()) {
				System.out.println(k++);
				int count = 0;
				String str = in.nextLine();
				String strr = str.trim();
				String[] abc = strr.split("\t");
				if (isNumeric(abc[0]))
					line = Integer.valueOf(abc[0]);
				else
					continue;
				String xy = "";
				int number =0;
				while (count < abc.length) {
					String str1 = abc[count];
					if (str1.equals("null")) {// the location and text is among
						String loc = abc[count - 1];						
						if (!loc.equals(whole)) {
							Map<Float, Float> locxy = locmap.get(loc);// get the
							for (Map.Entry<Float, Float> entry : locxy.entrySet()) {
								x = entry.getKey();
								y = entry.getValue();
								if (y > miny && y < miny + 3 && x > minx && x < minx + 3) {
									String content = "";
									point_id++;
									content += Integer.toString(point_id);
								//	xy += Double.toString(x) + "," + Double.toString(y) + ",";
									Brightkite.write(point_coordinate, Integer.toString(point_id)+","
											+Double.toString(x) + "," + Double.toString(y)+"\n");
									Brightkite.write(point_trajectory, Integer.toString(point_id)+" "
											+Integer.toString(line)+"\n");
								//	String string = abc[count+1];
								/*	System.out.println(string);
									string = Foursquare_keyword.dealString(string);
									StringTokenizer token = new StringTokenizer(string);
									while (token.hasMoreTokens()) {
										String word = token.nextToken(";, ?.!:\\\''\n");						
										if (isNumeric(word) || Foursquare_keyword.isContainChinese(word)) {
											continue;
										}
										IIndexWord idxWord = dict.getIndexWord(word, POS.NOUN);
										if (idxWord == null) {
											continue;
										}
										xy += word+" ";//to form new sentence
										if(!word_id.containsKey(word)){
											word_id.put(word, id);											
											Brightkite.write(word_id_file, word+"\t"+Integer.toString(id)+"\n");
											content += ","+String.valueOf(id);
											id++;
										}else{
											int temp_id = word_id.get(word);
											content += ","+String.valueOf(temp_id);
										}
									}
									xy+=";";
									content+="\n";
									Brightkite.write(all_word_id, content);
									number++;							*/		
								}
							}							
						}
						whole = loc;
					}
					count++;
				}
			/*	if(number>1){
					xy = abc[0]+"\t"+Integer.toString(number)+"\t"+xy+"\n";
					Brightkite.write("C:/Users/e29944/workspace-java/Trajectory/Data/ETQ/"+name+"/"+name+"-standard-clean.tra", xy);
					k++;
				}*/
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	public static void transfer_query(String all_word, String query, String query_id) {	
		Map<String, Integer> word_id = new HashMap();
		try {
			Scanner in = new Scanner(new BufferedReader(new FileReader(all_word)));
			while (in.hasNextLine()) {
				String str = in.nextLine();
				String strr = str.trim();
				String[] abc = strr.split("\t");
				word_id.put(abc[0], Integer.valueOf(abc[1]));
			}			
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			Scanner in = new Scanner(new BufferedReader(new FileReader(query)));
			while (in.hasNextLine()) {
				String str = in.nextLine();
				String strr = str.trim();
				String[] a = strr.split(";");
				String[] abc = a[1].split(" ");
				String content = a[0];
				for(int i=0; i<abc.length; i++){
					content += ","+Integer.toString(word_id.get(abc[i]));
				}
				content +="\n";
				Brightkite.write(query_id, content);
			}			
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	/*
	 * enlarge the trajectory by length, the input file should be on the standard file
	 */
	public static void enlarge_trajectory_length(String file, String out, int m) {
		try {
			Scanner in = new Scanner(new BufferedReader(new FileReader(file)));
			int k = 0;
			String whole = "";
			while (in.hasNextLine()) {				
				int count = 0;
				String str = in.nextLine();
				String strr = str.trim();
				String[] abc = strr.split("\t");
				String content = "";
				for(int i=0; i<m ; i++){
					content +=abc[2];
				}
				content = abc[0]+"\t"+Integer.toString(Integer.valueOf(abc[1])*m)+"\t"+content+"\n";
				Brightkite.write(out, content);
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	/*
	 * enlarge the trajectory by count, the input file should be on the standard file.
	 */
	public static void enlarge_trajectory_count(String file, String out, int m) {
		int id =0;
		try {
			Scanner in = new Scanner(new BufferedReader(new FileReader(file)));
			while (in.hasNextLine()) {
				String str = in.nextLine();
				String strr = str.trim();
				String[] abc = strr.split("\t");
				for(int i = 0; i<m; i++){
					String content = Integer.toString(id) + "\t" + abc[1] + "\t" + abc[2]+"\n";
					Brightkite.write(out, content);
					id++;
				}
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
