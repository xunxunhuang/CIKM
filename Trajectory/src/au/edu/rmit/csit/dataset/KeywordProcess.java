package au.edu.rmit.csit.dataset;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Map.Entry;
/*
 * transfer keyword into integer and compute the weight
 */
public class KeywordProcess {
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
	/*
	 * translate word into id
	 */
	public static void tras_word_id() {
		int cf=1;
		Hashtable<String, Integer> dic = new Hashtable<String, Integer>();
		Hashtable<String, Integer> word_id = new Hashtable<String, Integer>();
		try {
			Scanner in = new Scanner(new BufferedReader(new FileReader("/media/wangsheng/SW/Laptop/D/LA/key_id.txt")));
			while (in.hasNextLine()) {
				String str = in.nextLine();//file name
				System.out.println(str);
				String[] cols = str.split("\t");
				int id = Integer.valueOf(cols[1]);			
				word_id.put(cols[0], id);			
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println(word_id.size());
		
		try {
			Scanner in = new Scanner(new BufferedReader(new FileReader("/media/wangsheng/SW/Laptop/D/LA/doc_names.txt")));
			while (in.hasNextLine()) {
				String content = "";
				String str = in.nextLine();//file name
				try {
					String aa = "/media/wangsheng/SW/Laptop/D/LA/"+str.substring(37,str.length()).trim();
					System.out.println(aa);
					Scanner in1 = new Scanner(new BufferedReader(new FileReader(aa)));
					content += String.valueOf(cf++);
					while (in1.hasNextLine()) {
						String str1 = in1.nextLine();
						String[] cols = str1.split(" ");
						for(int i=0;i<cols.length;i++){
							if(word_id.containsKey(cols[i])){
								int id = (word_id.get(cols[i])).intValue();
								content += ","+String.valueOf(id);
							}
						}
					}
					in1.close();
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
				content+="\n";
				write("/media/wangsheng/SW/Laptop/D/LA/all_word_id.txt", content);
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}		
	}
	/*
	 * compute the weight
	 */
	static void ComputeTermWeights(String infile, String outfile)
	{
		double lmd = 0.2;		//smoothing factor
		Hashtable<String, Integer> dic = new Hashtable<String, Integer>();
		try{
			LineNumberReader lr = new LineNumberReader(new FileReader(infile));
			FileWriter fw = new FileWriter(outfile);
			PrintWriter out = new PrintWriter(fw);
			
			int totalLength = 0;
			String line = lr.readLine();
			while( line != null){
				String[] cols = line.split(",");
				for(int i=1;i<cols.length;i++){
					totalLength++;
					if(dic.containsKey(cols[i])){
						int count = (dic.get(cols[i])).intValue();
						dic.put(cols[i], new Integer(count + 1));
					}else{
						dic.put(cols[i], new Integer(1));
					}	
				} 
				line = lr.readLine();
			}			
			lr.close();
			
			lr = new LineNumberReader(new FileReader(infile));
			line = lr.readLine();
			while( line != null){
				String[] cols = line.split(",");
				System.out.println(line);
				Hashtable<String, Integer> sent = new Hashtable<String, Integer>();
				String wordID = cols[0];
				
				for(int i=1;i<cols.length;i++){
					if(sent.contains(cols[i])){
						int count = (sent.get(cols[i])).intValue();
						sent.put(cols[i], new Integer(count + 1));
					}else{
						sent.put(cols[i], 1);
					}	
				} 
				
				Iterator<Entry<String, Integer>> iter = sent.entrySet().iterator();
				String buf = "";
				while (iter.hasNext()) {
					Entry<String, Integer> entry = (Entry<String, Integer> ) iter.next();
					String word = entry.getKey();
					int count_in_sent = entry.getValue().intValue();
					int count_in_coll = dic.get(word).intValue();
					
					double weight = (1-lmd) * (double)count_in_sent / (double)(cols.length-1) + 
							lmd * (double)count_in_coll/(double) totalLength;
					weight = Math.pow(weight, count_in_sent);
					buf += word + " " + weight + ",";
				}
				if(buf.length()>0)
					buf = buf.substring(0, buf.length()-1);
				out.println(wordID + "," + buf);
				line = lr.readLine();
			}			
			lr.close();		
			out.close();
			fw.close();
					
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	/*
	 * transfer to the new similarity
	 */
    public static void pre_coodinate(){
		try {			
			Scanner in = new Scanner(
					new BufferedReader(new FileReader("/media/wangsheng/SW/Laptop/D/LA/coordinatenew.txt")));
			while (in.hasNextLine()) {
				String content = "";
				String str = in.nextLine();// file name
				System.out.println(str);
				String[] cols = str.split(" ");
				content += cols[0] + "," + cols[1] + "," + cols[2]+"\n";
				write("/media/wangsheng/SW/Laptop/D/LA/IR_coordinate.txt", content);
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) throws IOException {		
	//	tras_word_id();
	//	pre_coodinate();
		String all_word_id =  "C:/Users/e29944/workspace-java/Trajectory/Data/ETQ/NYC/all_word_id.txt";
		String output =  "C:/Users/e29944/workspace-java/Trajectory/Data/ETQ/NYC/word_weight.txt";
		ComputeTermWeights(all_word_id, output);
	}
}
