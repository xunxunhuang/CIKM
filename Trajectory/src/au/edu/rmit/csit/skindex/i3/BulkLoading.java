package au.edu.rmit.csit.skindex.i3;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;


/**
 * The fast bulk-loading algorithm to build index. The algorithm assumes that:
 * 1. All the data are contained in an input data file.
 * 2. The memory is large enough to hold all the input data.
 * 3. The spatial attributes are latitude and longitude.
 */
public class BulkLoading {
    private String DATASET;
    private Region region;
    private String indexFolder;

    public BulkLoading(String folder, String datafile) {
        indexFolder = folder;
        DATASET = datafile;
        region = new Region(-90, 90, -180, 180);
    }

    public static void main(String args[]) {
        if (args.length != 3) {
            System.err.println("Please specify indexFolder, datafile and bitlen");
        }
        String indexFolder = args[0];
        String datafile = args[1];
        Config.BIT_LEN = Integer.parseInt(args[2]);
        BulkLoading loader = new BulkLoading(indexFolder, datafile);

        long start = System.currentTimeMillis();
        loader.build();
        long end = System.currentTimeMillis();
        System.err.println("Second: " + ((end - start) / 1000.0f));
    }

    public HIFIndex build() {
        String line;
        HIFIndex idx = new HIFIndex(region, indexFolder);
        try {
            BufferedReader inputStream = new BufferedReader(new FileReader(
                    DATASET));
            int docID = 1;
            while ((line = inputStream.readLine()) != null) {
                if (docID > 0 && docID % 1000 == 0) {
                    System.out.println(docID);
                }

                if (docID == 99444) {
                    int debug = 1;
                }

                String parts[] = line.split(" ");
                if (parts.length % 2 != 0 || parts.length < 4) {
                    System.err.println("Error in this line : " + line);
                    continue;
                }


                HashMap<String, Integer> kwdCount = new HashMap<String, Integer>();
                for (int i = 2; i < parts.length; i += 2) {
                    String kwd = parts[i];
                    int freq = Integer.parseInt(parts[i + 1]);
                    if (kwdCount.containsKey(kwd)) {
                        kwdCount.put(kwd, kwdCount.get(kwd) + freq);
                    } else {
                        kwdCount.put(kwd, freq);
                    }
                }

                double sumeOfWeightSquare = 0;
                double weight;
                for (String kwd : kwdCount.keySet()) {
                    int freq = kwdCount.get(kwd);
                    weight = 1 + Math.log(freq);
                    sumeOfWeightSquare += weight * weight;
                }
                sumeOfWeightSquare = Math.sqrt(sumeOfWeightSquare);

                // Get the location information
                double x = Double.parseDouble(parts[0]);
                double y = Double.parseDouble(parts[1]);

                for (String kwd : kwdCount.keySet()) {
                    if (kwd.equals("con") || kwd.equals("prn")) {
                        continue;
                    }
                    int freq = kwdCount.get(kwd);
                    idx.addItem(kwd, docID, x, y, (1 + Math.log(freq)) / sumeOfWeightSquare);
                }

                docID++;

                if (docID > 100000) {
                    //break;
                }

            }
            idx.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return idx;
    }
}
