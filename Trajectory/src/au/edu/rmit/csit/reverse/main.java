package au.edu.rmit.csit.reverse;

import java.io.File;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import au.edu.rmit.csit.dataset.Brightkite;
import au.edu.rmit.csit.dataset.Foursquare;
import uq.entities.*;
import au.edu.rmit.csit.reverse.query;
public class main {

	public static void main(String[] args) {
//		Foursquare.clus_tra("NYC");
	//	Foursquare.time_convert("LA");
	/* 	int arr[] = { 10, 22, 9, 33, 21, 50, 41, 60};
        int n = arr.length;
        int sim =  precomputation.EDR(arr,n,60);
        System.out.println("\n"+sim);
        String temp = "";
		for(int c =0 ; c<sim.length; c++){
			temp += Integer.toString(sim[c])+"-";
			System.out.print(sim[c] + " ");
		}*/
	//	test_online();	
	//	test_precomputation();
		test_pruning();
	}
	public static void test_pruning() {
		Online.set_epsilon(0.01);
		Online.load_db_standard(1);
		ArrayList<Point> query1;
		Map<Integer, ArrayList<Point>> query_set = new HashMap<Integer, ArrayList<Point>>();
		// Generate the query based on the candidates
		query_set = query.generate_by_numberCandidate(1, 1, 10,
				Double.toString(Online.get_epsilon()));// 100
		ArrayList<Point> query = new ArrayList<Point>();
		Iterator<Integer> iterator = query_set.keySet().iterator();
		long startTime = System.nanoTime();
		while (iterator.hasNext()) {
			int a = iterator.next();
			query1 = query_set.get(a);
			query = new ArrayList<Point>();
			for (int p = 0; p < 10; p++) {// |Q|:3-10
				query.add(query1.get(p));
			}
		//	Online.query_quad(query, 5);
			Online.query_group_merge(query, 5);
		}
		long endTime = System.nanoTime();
		System.out.println((endTime - startTime) / 1000000000.0 / query_set.size());
	}
	/*
	 * test the online
	 */
	public static void test_precomputation() {
		double[] e = new double[] { 0.01, 0.02, 0.03, 0.04, 0.05 };
		for (int e_id = 0; e_id < 1; e_id++) {
			precomputation.set_epsilon(e[e_id]);
			String experiment = "/home/wangsheng/Desktop/Data/result_pre.txt";
			for (int ij = 1; ij < 7; ij++) {
				System.out.println("Now we are testing " + ij + " dataset");
				precomputation.load_db_standard(ij);
				ArrayList<Point> query1;
				Map<Integer, ArrayList<Point>> query_set = new HashMap<Integer, ArrayList<Point>>();
				// Generate the query based on the candidates
				query_set = query.generate_by_numberCandidate(ij, 100, 10,
						Double.toString(precomputation.get_epsilon()));// 100
				System.out.println("The query is generated");
				double[][] multiq = new double[8][4];
				double[][] multik = new double[5][4];
				/*
				 * |Q| first
				 * 
				 */
				for (int k = 5; k <= 5; k += 5) {// k: 1-25
					System.out.println("k:" + k);
					for (int i = 3; i < 11; i++) {
						ArrayList<Point> query = new ArrayList<Point>();
						Iterator<Integer> iterator = query_set.keySet().iterator();
						long startTime = System.nanoTime();
						while (iterator.hasNext()) {
							int a = iterator.next();
							query1 = query_set.get(a);
							query = new ArrayList<Point>();
							for (int p = 0; p < i; p++) {// |Q|:3-10
								query.add(query1.get(p));
							}
							precomputation.query_quad(query, k);
						}
						long endTime = System.nanoTime();
						multiq[i - 3][0] = (endTime - startTime) / 1000000000.0 / query_set.size();
						
						startTime = System.nanoTime();
						iterator = query_set.keySet().iterator();
						while (iterator.hasNext()) {
							int a = iterator.next();
							query1 = query_set.get(a);
							query = new ArrayList<Point>();
							for (int p = 0; p < i; p++) {// |Q|:3-10
								query.add(query1.get(p));
							}
							precomputation.query_group_pure(query, k);
						}
						endTime = System.nanoTime();
						multiq[i - 3][1] = (endTime - startTime) / 1000000000.0 / query_set.size();
						
						startTime = System.nanoTime();
						iterator = query_set.keySet().iterator();
						while (iterator.hasNext()) {
							int a = iterator.next();
							query1 = query_set.get(a);
							query = new ArrayList<Point>();
							for (int p = 0; p < i; p++) {// |Q|:3-10
								query.add(query1.get(p));
							}
							precomputation.query_group(query, k);
						}
						endTime = System.nanoTime();
				//		multiq[i - 3][1] = (endTime - startTime) / 1000000000.0 / query_set.size();

						startTime = System.nanoTime();
						iterator = query_set.keySet().iterator();
						while (iterator.hasNext()) {
							int a = iterator.next();
							query1 = query_set.get(a);
							query = new ArrayList<Point>();
							for (int p = 0; p < i; p++) {// |Q|:3-10
								query.add(query1.get(p));
							}
							precomputation.query_group_pruning(query, k);
						}
						endTime = System.nanoTime();
						multiq[i - 3][2] = (endTime - startTime) / 1000000000.0 / query_set.size();

						startTime = System.nanoTime();
						iterator = query_set.keySet().iterator();
						while (iterator.hasNext()) {
							int a = iterator.next();
							query1 = query_set.get(a);
							query = new ArrayList<Point>();
							for (int p = 0; p < i; p++) {// |Q|:3-10
								query.add(query1.get(p));
							}

							precomputation.query_group_merge(query, k);
						}
						endTime = System.nanoTime();
						multiq[i - 3][3] = (endTime - startTime) / 1000000000.0 / query_set.size();
					}
					String content = "";
					for (int mn = 0; mn < 4; mn++) {
						content += "m" + Integer.toString(mn + 1) + " <- c(";
						for (int pq = 0; pq < 8; pq++) {
							content += Double.toString(multiq[pq][mn]);
							if (pq < 7)
								content += ",";
						}
						content += ")\n";
					}
					Brightkite.write(experiment, content);
				}
				System.out.println();
				/*
				 * k first
				 */
				for (int i = 5; i < 6; i++) {
					System.out.println("Q:" + i);
					for (int k = 5; k <= 26; k += 5) {// k: 1-25
						ArrayList<Point> query = new ArrayList<Point>();
						Iterator<Integer> iterator = query_set.keySet().iterator();
						long startTime = System.nanoTime();
						while (iterator.hasNext()) {
							int a = iterator.next();
							query1 = query_set.get(a);
							query = new ArrayList<Point>();
							for (int p = 0; p < i; p++) {// |Q|:3-10
								query.add(query1.get(p));
							}

							precomputation.query_quad(query, k);
						}
						long endTime = System.nanoTime();
						multik[k / 5 - 1][0] = (endTime - startTime) / 1000000000.0 / query_set.size();

						startTime = System.nanoTime();
						iterator = query_set.keySet().iterator();
						while (iterator.hasNext()) {
							int a = iterator.next();
							query1 = query_set.get(a);
							query = new ArrayList<Point>();
							for (int p = 0; p < i; p++) {// |Q|:3-10
								query.add(query1.get(p));
							}
							precomputation.query_group_pure(query, k);
						}
						endTime = System.nanoTime();
						multik[k / 5 - 1][1] = (endTime - startTime) / 1000000000.0 / query_set.size();

						startTime = System.nanoTime();
						iterator = query_set.keySet().iterator();
						while (iterator.hasNext()) {
							int a = iterator.next();
							query1 = query_set.get(a);
							query = new ArrayList<Point>();
							for (int p = 0; p < i; p++) {// |Q|:3-10
								query.add(query1.get(p));
							}
							precomputation.query_group_pruning(query, k);
						}
						endTime = System.nanoTime();
						multik[k / 5 - 1][2] = (endTime - startTime) / 1000000000.0 / query_set.size();

						startTime = System.nanoTime();
						iterator = query_set.keySet().iterator();
						while (iterator.hasNext()) {
							int a = iterator.next();
							query1 = query_set.get(a);
							query = new ArrayList<Point>();
							for (int p = 0; p < i; p++) {// |Q|:3-10
								query.add(query1.get(p));
							}
							precomputation.query_group_merge(query, k);
						}
						endTime = System.nanoTime();
						multik[k / 5 - 1][3] = (endTime - startTime) / 1000000000.0 / query_set.size();
					}
					String content = "";
					for (int mn = 0; mn < 4; mn++) {
						content += "m" + Integer.toString(mn + 1) + " <- c(";
						for (int pq = 0; pq < 5; pq++) {
							content += Double.toString(multik[pq][mn]);
							if (pq < 4)
								content += ",";
						}
						content += ")\n";
					}
					Brightkite.write(experiment, content);
				}
			}
		}
	}
	public static void test_online(){
		double[] e = new double[] { 0.01};
		for (int e_id = 0; e_id < e.length; e_id++) {
			Online.set_epsilon(e[e_id]);
			String experiment = "/home/wangsheng/Desktop/Data/result_online_1.txt";
			for (int ij = 1; ij < 7; ij++) {
				System.out.println("Now we are testing " + ij + " dataset");
				Online.load_db_standard(ij);
				ArrayList<Point> query1;
				Map<Integer, ArrayList<Point>> query_set = new HashMap<Integer, ArrayList<Point>>();
				// Generate the query based on the candidates
				query_set = query.generate_by_numberCandidate(ij, 1, 10,
						Double.toString(Online.get_epsilon()));// 100
				System.out.println("The query is generated");
				double[][] multiq = new double[8][4];
				double[][] multik = new double[5][4];
				/*
				 * |Q| first
				 * 
				 */
				for (int k = 5; k <= 5; k += 5) {// k: 1-25
					System.out.println("k:" + k);
					for (int i = 3; i < 11; i++) {
						ArrayList<Point> query = new ArrayList<Point>();
						Iterator<Integer> iterator = query_set.keySet().iterator();
						long startTime = System.nanoTime();
						while (iterator.hasNext()) {
							int a = iterator.next();
							query1 = query_set.get(a);
							query = new ArrayList<Point>();
							for (int p = 0; p < i; p++) {// |Q|:3-10
								query.add(query1.get(p));
							}
							Online.query_quad(query, k);
						}
						long endTime = System.nanoTime();
						multiq[i - 3][0] = (endTime - startTime) / 1000000000.0 / query_set.size();
						
						startTime = System.nanoTime();
						iterator = query_set.keySet().iterator();
						while (iterator.hasNext()) {
							int a = iterator.next();
							query1 = query_set.get(a);
							query = new ArrayList<Point>();
							for (int p = 0; p < i; p++) {// |Q|:3-10
								query.add(query1.get(p));
							}
							Online.query_group_pure(query, k);
						}
						endTime = System.nanoTime();
						multiq[i - 3][1] = (endTime - startTime) / 1000000000.0 / query_set.size();
						
						startTime = System.nanoTime();
						iterator = query_set.keySet().iterator();
						while (iterator.hasNext()) {
							int a = iterator.next();
							query1 = query_set.get(a);
							query = new ArrayList<Point>();
							for (int p = 0; p < i; p++) {// |Q|:3-10
								query.add(query1.get(p));
							}
							Online.query_group_pure(query, k);
						}
						endTime = System.nanoTime();
				//		multiq[i - 3][1] = (endTime - startTime) / 1000000000.0 / query_set.size();

						startTime = System.nanoTime();
						iterator = query_set.keySet().iterator();
						while (iterator.hasNext()) {
							int a = iterator.next();
							query1 = query_set.get(a);
							query = new ArrayList<Point>();
							for (int p = 0; p < i; p++) {// |Q|:3-10
								query.add(query1.get(p));
							}
							Online.query_group_pruning(query, k);
						}
						endTime = System.nanoTime();
						multiq[i - 3][2] = (endTime - startTime) / 1000000000.0 / query_set.size();

						startTime = System.nanoTime();
						iterator = query_set.keySet().iterator();
						while (iterator.hasNext()) {
							int a = iterator.next();
							query1 = query_set.get(a);
							query = new ArrayList<Point>();
							for (int p = 0; p < i; p++) {// |Q|:3-10
								query.add(query1.get(p));
							}

							Online.query_group_merge(query, k);
						}
						endTime = System.nanoTime();
						multiq[i - 3][3] = (endTime - startTime) / 1000000000.0 / query_set.size();
					}
					String content = "";
					for (int mn = 0; mn < 4; mn++) {
						content += "m" + Integer.toString(mn + 1) + " <- c(";
						for (int pq = 0; pq < 8; pq++) {
							content += Double.toString(multiq[pq][mn]);
							if (pq < 7)
								content += ",";
						}
						content += ")\n";
					}
					Brightkite.write(experiment, content);
				}
				System.out.println();
				/*
				 * k first
				 */
				for (int i = 5; i < 6; i++) {
					System.out.println("Q:" + i);
					for (int k = 5; k <= 26; k += 5) {// k: 1-25
						ArrayList<Point> query = new ArrayList<Point>();
						Iterator<Integer> iterator = query_set.keySet().iterator();
						long startTime = System.nanoTime();
						while (iterator.hasNext()) {
							int a = iterator.next();
							query1 = query_set.get(a);
							query = new ArrayList<Point>();
							for (int p = 0; p < i; p++) {// |Q|:3-10
								query.add(query1.get(p));
							}

							Online.query_quad(query, k);
						}
						long endTime = System.nanoTime();
						multik[k / 5 - 1][0] = (endTime - startTime) / 1000000000.0 / query_set.size();

						startTime = System.nanoTime();
						iterator = query_set.keySet().iterator();
						while (iterator.hasNext()) {
							int a = iterator.next();
							query1 = query_set.get(a);
							query = new ArrayList<Point>();
							for (int p = 0; p < i; p++) {// |Q|:3-10
								query.add(query1.get(p));
							}
							Online.query_group_pure(query, k);
						}
						endTime = System.nanoTime();
						multik[k / 5 - 1][1] = (endTime - startTime) / 1000000000.0 / query_set.size();

						startTime = System.nanoTime();
						iterator = query_set.keySet().iterator();
						while (iterator.hasNext()) {
							int a = iterator.next();
							query1 = query_set.get(a);
							query = new ArrayList<Point>();
							for (int p = 0; p < i; p++) {// |Q|:3-10
								query.add(query1.get(p));
							}
							Online.query_group_pruning(query, k);
						}
						endTime = System.nanoTime();
						multik[k / 5 - 1][2] = (endTime - startTime) / 1000000000.0 / query_set.size();

						startTime = System.nanoTime();
						iterator = query_set.keySet().iterator();
						while (iterator.hasNext()) {
							int a = iterator.next();
							query1 = query_set.get(a);
							query = new ArrayList<Point>();
							for (int p = 0; p < i; p++) {// |Q|:3-10
								query.add(query1.get(p));
							}
							Online.query_group_merge(query, k);
						}
						endTime = System.nanoTime();
						multik[k / 5 - 1][3] = (endTime - startTime) / 1000000000.0 / query_set.size();
					}
					String content = "";
					for (int mn = 0; mn < 4; mn++) {
						content += "m" + Integer.toString(mn + 1) + " <- c(";
						for (int pq = 0; pq < 5; pq++) {
							content += Double.toString(multik[pq][mn]);
							if (pq < 4)
								content += ",";
						}
						content += ")\n";
					}
					Brightkite.write(experiment, content);
				}
			}
		}
	}
}
		