package au.edu.rmit.csit.dataset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.POS;

public class Foursquare_keyword {
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
	public static String dealString(String str1) {
		str1 = str1.replace("\"", "");
		str1 = str1.replace(".", " ");
		str1 = str1.replace("?", " ");
		str1 = str1.replace(",", " ");
		str1 = str1.replace("!", " ");
		str1 = str1.replace("-", " ");
		str1 = str1.replace("@", " ");
		str1 = str1.replace("$", " ");
		str1 = str1.replace("%", " ");
		str1 = str1.replace("^", " ");
		str1 = str1.replace("*", " ");
		str1 = str1.replace("(", " ");
		str1 = str1.replace(")", " ");
		str1 = str1.replace("+", " ");
		str1 = str1.replace("=", " ");
		str1 = str1.replace(":", " ");
		str1 = str1.replace("~", " ");
		str1 = str1.replace("<", " ");
		str1 = str1.replace(">", " ");
		str1 = str1.replace("_", " ");
		str1 = str1.replace("[", " ");
		str1 = str1.replace("]", " ");
		str1 = str1.replace("{", " ");
		str1 = str1.replace("}", " ");
		str1 = str1.replace("'", " ");
		str1 = str1.replace("/", " ");
		str1 = str1.replace("\\", " ");
		str1 = str1.replace(";", " ");
		str1 = str1.replace("&", " ");
		str1 = str1.replace("#", " ");
		str1 = str1.toLowerCase();
		return str1;
	}
	public static boolean isContainChinese(String str) {

		Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
		Matcher m = p.matcher(str);
		if (m.find()) {
			return true;
		}
		return false;
	}

	public static boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(str);
		if (!isNum.matches()) {
			return false;
		}
		return true;
	}
	/*
	 * code to location
	 */
	public static Map<String, Map<Float, Float>> StringtoLoc() throws FileNotFoundException{//find the latitude and longitude of stirng
		Map<String, Map<Float, Float>> a; 
		Map<Float, Float> b;
		a = new HashMap<String, Map<Float, Float>>();
		try {
			Scanner in = new Scanner(new BufferedReader(new FileReader("d:/LA/LA-Venues.txt")));
			while (in.hasNextLine()) {
				String str = in.nextLine();
				String strr = str.trim();
				String[] abc = strr.split("\t");
				String word = abc[0];	
				float locx= Float.valueOf(abc[2]);
				float locy= Float.valueOf(abc[3]);
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
	public static void IndexofTerm(int num) throws IOException {

		int wordCount = 0;
		int line = 1;
		float x = 0 ,y = 0;
		String path = "C:/Program Files (x86)/WordNet/2.1" + File.separator + "dict";
		URL url = new URL("file", null, path);
		// construct the dictionary object and open it
		IDictionary dict = new Dictionary(url);
		dict.open(); // ´ò¿ª´Êµä
		Map<String, Map<Float, Float>> locmap;
		Map<String, Integer> count_key = new HashMap<String,Integer>();
		int i=0;
		File file = new File("D:/LA/coordinate.txt");	// store the x and y;
		FileOutputStream fop = new FileOutputStream(file);
		if (!file.exists()) {
			file.createNewFile();
		}
		file = new File("D:/LA/trajectory.txt");	// store the x and y;
		fop = new FileOutputStream(file);
		if (!file.exists()) {
			file.createNewFile();
		}
		try {
			Scanner in = new Scanner(new BufferedReader(new FileReader("d:/LA/LA-Tips.txt")));
			locmap = StringtoLoc();
			while (in.hasNextLine() && num-->0) {
				int count = 0;
				String str = in.nextLine();
				String strr = str.trim();
				String[] abc = strr.split("\t");
				line = Integer.valueOf(abc[0]);//the identifier of trajectory
				//System.out.println(line);
				while (count < abc.length) {
					String str1 = abc[count];					
					if(str1.equals("null")){// the location and text is among the "null"									
						String loc = abc[count-1];
						String coordinate = String.valueOf(i)+" ";
						Map<Float, Float> locxy = locmap.get(loc);//get the latitude
						Iterator<Float> itr = locxy.keySet().iterator();
				        while(itr.hasNext())
				        {
				        	x = (float) itr.next();
				        	y = locxy.get(x);
				        }			        
						str1 = abc[count+1];																		
						String content = "";
						coordinate += String.valueOf(x) + " " + String.valueOf(y) + "\n";
						//System.out.println(coordinate);
						if (str1.charAt(0) == '"' && str1.charAt(1) != '4' && str1.charAt(1) != '3') {
							str1 = dealString(str1);
							StringTokenizer token = new StringTokenizer(str1);
							while (token.hasMoreTokens()) {
								wordCount++;
								String word = token.nextToken(";, ?.!:\\\''\n");							
								if (isNumeric(word) || isContainChinese(word)) {
									continue;
								}
								IIndexWord idxWord = dict.getIndexWord(word, POS.NOUN);
								if (idxWord == null) {
									continue;
								}
								content += word+" ";//to form new sentence								
								if(!count_key.containsKey(word)){
									count_key.put(word, 1);
								}else{
									int aa = count_key.get(word)+1;
									count_key.put(word, aa);
								}
							}
						}
					}
					count++;					
				}				
			}
			dict.close();
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	public static void attach_tags(){
		
	}
}
