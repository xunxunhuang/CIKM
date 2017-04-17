package au.edu.rmit.csit.skindex.i3;

import java.util.HashMap;


/**
 * The fast bulk-loading algorithm to build index. The algorithm assumes that:
 * 1. All the data are contained in an input data file.
 * 2. The memory is large enough to hold all the input data.
 * 3. The spatial attributes are latitude and longitude.
 * <p>
 * modified by xianyan
 */
public class I3Indexer {
    public HIFIndex idx;
    String indexFolder;
    int docID = 1;
    private Region region;

    public I3Indexer(String indexFolder) {
        this.indexFolder = indexFolder;
        region = new Region(-90, 90, -180, 180);
        idx = new HIFIndex(region, indexFolder);
    }

    public static String[] tokenize(String sentence) {
        return sentence.split("[^\\w]+");
    }

    public HIFIndex build(double lat, double lng, String tweetText) {
        String[] tokens = tokenize(tweetText);
        HashMap<String, Integer> kwdCount = new HashMap<>();
        for (String t : tokens) {
            t = t.toLowerCase();
            Integer count = kwdCount.get(t);
            if (count == null) {
                kwdCount.put(t, 1);
            } else {
                kwdCount.put(t, 1 + count);
            }
        }
        return build(lat, lng, kwdCount);
    }

    public HIFIndex build(double lat, double lng, HashMap<String, Integer> kwdCount) {
        if (docID > 0 && docID % 1000 == 0) {
            System.out.println(docID);
        }
//        if (docID == 99444) {
//            int debug = 1;
//        }
        double sumeOfWeightSquare = 0;
        double weight;
        for (String kwd : kwdCount.keySet()) {
            int freq = kwdCount.get(kwd);
            weight = 1 + Math.log(freq);
            sumeOfWeightSquare += weight * weight;
        }
        sumeOfWeightSquare = Math.sqrt(sumeOfWeightSquare);
        for (String kwd : kwdCount.keySet()) {
            if (kwd.equals("con") || kwd.equals("prn")) {
                continue;
            }
            int freq = kwdCount.get(kwd);
            idx.addItem(kwd, docID, lat, lng, (1 + Math.log(freq)) / sumeOfWeightSquare);
        }
        docID++;
        if (docID > 100000) {
            idx.flush();
        }
        return idx;
    }
}
