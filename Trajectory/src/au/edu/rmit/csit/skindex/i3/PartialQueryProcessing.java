package au.edu.rmit.csit.skindex.i3;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;


public class PartialQueryProcessing {
    public static int CID = 1;
    HIFIndex idx;
    PriorityQueue<CellCandidate> candidates;
    private Vector<ResultTuple> results; // Top-k results
    private int K; // Top-k
    private long runningTime = 0;
    // For statistics
    private int _pruneByHeadFile = 0;
    private int _pruneByDataFile = 0;
    private int _pruneByTopK = 0;
    private int _candidateNum = 0;
    private long HeadFile_IO_Time = 0;
    private long DataFile_IO_Time = 0;
    public PartialQueryProcessing(String folder) {
        idx = new HIFIndex(folder);
        candidates = new PriorityQueue<CellCandidate>();
        results = new Vector<ResultTuple>();
//		idx.debug();
//		System.exit(0);
    }

    public static void main(String[] args) {
        if (args.length != 5) {
            System.err.println("please specify indexFolder, queryfile, k, alpha, bitlen");
            System.exit(0);
        }


        String indexFolder = args[0];
        String queryFile = args[1];
        int k = Integer.parseInt(args[2]);
        Config.ALPHA = Double.parseDouble(args[3]);
        Config.BIT_LEN = Integer.parseInt(args[4]);


        PartialQueryProcessing processor = new PartialQueryProcessing(indexFolder);
        //processor.debugPointCellLevel(43.07519, 5.80114 , 6);
        //System.exit(0);

        //processor.query(p, kwds, 50);


        //Config.MAX_DIST = Double.parseDouble(args[3]);


        String line;
        int queryNum = 0;
        try {
            BufferedReader inputStream = new BufferedReader(new FileReader(queryFile));
            long start = System.currentTimeMillis();
            while ((line = inputStream.readLine()) != null) {
                Vector<String> queryKwds = new Vector<String>();
                System.out.println(line);
                String[] parts = line.split(" ");
                double x = Double.parseDouble(parts[0]);
                double y = Double.parseDouble(parts[1]);
                for (int i = 2; i < parts.length; i++) {
                    queryKwds.add(parts[i]);
                }

                processor.query(new Point(x, y), queryKwds, k);
                queryNum++;
                //break;
            }
            long end = System.currentTimeMillis();
            double headIO = processor.idx.getHeadIO() * 1.0 / queryNum;
            double dataIO = processor.idx.getDataIO() * 1.0 / queryNum;
            double io = headIO + dataIO;
            System.err.println((end - start) * 1.0 / queryNum + "\t" + headIO + "\t" + dataIO + "\t" + io);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void printStat() {
        System.out.println("Prune by HeadFile :" + _pruneByHeadFile);
        System.out.println("Prune by DataFile :" + _pruneByDataFile);
        System.out.println("Prune by Topk :" + _pruneByTopK);
        System.out.println("Candidate Num :" + _candidateNum);

        System.out.println("HeadFile IO Time : " + HeadFile_IO_Time);
        System.out.println("DataFile IO Time : " + DataFile_IO_Time);
    }

    private double getSpatialScore(double dist) {
        double spatialProximity = 1 - dist / Config.MAX_DIST;
        return Config.ALPHA * spatialProximity;
    }

    private double getKwdScore(double freq) {
        /*
		double textRelevance = docTermWeight;

        return (1-alpha)*textRelevance;
        */
        double textRelevance = freq;
        return (1 - Config.ALPHA) * textRelevance;
    }

    private void insertResultTuple(ResultTuple tuple) {
        if (results.size() >= K && tuple.score <= results.get(K - 1).score) {
            return;
        }

        int index = Collections.binarySearch(results, tuple);
        if (index < 0) {
            results.add(-index - 1, tuple);
        } else {
            System.err.println("Unable to insert tuple " + tuple);
        }
    }

    private boolean pruneByHeadFile(CellCandidate parent, int cellID, Vector<Integer> queryKwds) {

        for (int k = 0; k < queryKwds.size(); k++) {
            // denseKwds contains keyword whose HeadFiles have not been explored
            // before in parent's region.
            int kwdID = queryKwds.get(k);

            if (parent.denseKwds.contains(kwdID)) {
                int gridID = parent.denseKwdGridID.get(kwdID);
                GridSummary gridSummary = idx.getGridSummary(gridID);
                CellSummary cellSummary = gridSummary.getCell(cellID);

                // The keyword must exist in this cell.
                if (cellSummary.getKwdFreq() != 0) {
                    return false;
                }
            }
        }
        return true;
    }

	
	/*
	 * Check if a cell is a candidate by only checking the information in the
	 * HeadFile. Initially, we know that parent cell is a candidate and we are
	 * now further exploring the child cells. Given a keyword at a certain cell,
	 * there are two cases: 1. Its corresponding docs have been loaded before.
	 * In other words, this is not a frequent keyword and the datafile has been
	 * read into memory and its related docs have been preserved in parent.docs.
	 * In this case, we don't need to read the HeadFile as it does not exist 2.
	 * We havn't reached its data file and its HeadFile exists. We can use the
	 * information in the HeadFile for further pruning.
	 */

    /*
     * The function does no pruning at all.
     */
    private boolean pruneByDataFile(CellCandidate parent,
                                    int cellID,
                                    Vector<Integer> queryKwds,
                                    HashSet<Integer> childDenseKwds,
                                    HashMap<Integer, Integer> childDenseKwdGridID,
                                    HashMap<Integer, Double> docKwdFreq,
                                    HashMap<Integer, Point> docLocs) {

        for (int k = 0; k < queryKwds.size(); k++) {
            int kwdID = queryKwds.get(k);
            if (parent.denseKwds.contains(queryKwds.get(k))) {
                int gridID = parent.denseKwdGridID.get(kwdID);
                GridSummary gridSummary = idx.getGridSummary(gridID);
                CellSummary cellSummary = gridSummary.getCell(cellID);

                if (!cellSummary.isDense()) {
                    if (cellSummary.getKwdFreq() != 0) {

                        Vector<CellData> allData = idx.loadFreqDocs(gridID, cellID);

                        for (CellData data : allData) {
                            if (docKwdFreq.containsKey(data.docID)) {
                                docKwdFreq.put(data.docID, docKwdFreq.get(data.docID) + data.freq);

                            } else {
                                docLocs.put(data.docID, new Point(data.x, data.y));
                                docKwdFreq.put(data.docID, data.freq);
                            }
                        }
                    }
                    childDenseKwds.remove(queryKwds.get(k));
                    childDenseKwdGridID.remove(queryKwds.get(k));
                } else {
                    childDenseKwdGridID.put(queryKwds.get(k), cellSummary.getGridID());
                }
            }
        }

        return false;
    }

    /*
     * Get the upper bound keyword score in the cell.
     * For the non-dense keywords, we have known the related docID and score.
     * For the dense keywords, it could appear in anywhere of the cell with maxFreq in CellSummary.
     * Point is whether the score of dense keywords can be aggregated with non-dense one.
     * So we need to calculate all the possible cases of partial aggregation.
     *
     * We want this upperScore as small as possible
     */
    private double getCellUpperScore(CellCandidate parent, int cellID, Vector<Integer> queryKwds, Point queryLoc) {
        double upperScore = Double.MIN_VALUE;
        Vector<AprioriElement> currentLevel = new Vector<AprioriElement>();

        for (int k = 0; k < queryKwds.size(); k++) {
            int kwdID = queryKwds.get(k);
            if (parent.denseKwds.contains(kwdID)) {
                int gridID = parent.denseKwdGridID.get(kwdID);
                GridSummary gridSummary = idx.getGridSummary(gridID);
                CellSummary cellSummary = gridSummary.getCell(cellID);
                Signature sig = cellSummary.getSignature();
                double kwdScore = getKwdScore(cellSummary.getKwdFreq());
                //System.out.println(parent.id+" "+cellID);
                //System.out.println(Grid.getCellRegion(cellID, gridSummary.getRegion()));

                AprioriElement elem = new AprioriElement(String.valueOf(k), sig, kwdScore, Grid.getCellRegion(cellID, gridSummary.getRegion()), queryLoc);
                //System.out.println(elem.spatialScore);
                currentLevel.add(elem);
                if (elem.totalScore > upperScore) {
                    upperScore = elem.totalScore;
                }
            }
        }
        if (parent.docLocs.size() > 0) {
            HashMap<Integer, Double> kwdScoreMap = (HashMap<Integer, Double>) parent.docKwdFreq.clone();
            for (Integer docID : kwdScoreMap.keySet()) {
                kwdScoreMap.put(docID, getKwdScore(kwdScoreMap.get(docID)));
            }
            AprioriElement elem = new AprioriElement(String.valueOf(queryKwds.size()), kwdScoreMap, parent.docLocs, queryLoc);
            currentLevel.add(elem);
            if (elem.totalScore > upperScore) {
                upperScore = elem.totalScore;
            }
        }
        int totalLevel = currentLevel.size();
        for (int l = 2; l <= totalLevel; l++) {
            Vector<AprioriElement> nextLevel = new Vector<AprioriElement>();
            for (int i = 0; i < currentLevel.size(); i++) {
                for (int j = i + 1; j < currentLevel.size(); j++) {
                    if (currentLevel.get(i).canMerge(currentLevel.get(j))) {
                        AprioriElement elem = currentLevel.get(i).merge(currentLevel.get(j));
                        nextLevel.add(elem);
                        if (elem.totalScore > upperScore) {
                            upperScore = elem.totalScore;
                        }
                    }
                }
            }
            currentLevel = nextLevel;
        }

        return upperScore;
    }

    private double getCellUpperScore2(CellCandidate parent, int cellID, Vector<Integer> queryKwds, Point queryLoc) {
        double kwdFreq = 0;


        //TODO: Need to check this part carefully

        for (int k = 0; k < queryKwds.size(); k++) {
            int kwdID = queryKwds.get(k);
            if (parent.denseKwds.contains(kwdID)) {
                int gridID = parent.denseKwdGridID.get(kwdID);
                GridSummary gridSummary = idx.getGridSummary(gridID);
                CellSummary cellSummary = gridSummary.getCell(cellID);
                kwdFreq += (cellSummary.getKwdFreq());

            }
        }

        // Check the keywords that are no longer dense. We use the most promising doc
        double nonDenseKwdFreq = 0;
        for (Integer docID : parent.docLocs.keySet()) {

            if (Grid.getCellId(parent.docLocs.get(docID).x, parent.docLocs.get(docID).y, parent.region) == cellID) {
                if (parent.docKwdFreq.get(docID) > nonDenseKwdFreq) {
                    nonDenseKwdFreq = parent.docKwdFreq.get(docID);
                }
            }
        }
        kwdFreq += nonDenseKwdFreq;
        double spatialScore = queryLoc.getMinDistance(Grid.getCellRegion(cellID, parent.region));
        return getKwdScore(kwdFreq) + getSpatialScore(spatialScore);
    }

    /*
     * Given a candidate cell, we explore further candidates inside. Since
     * different keywords have different distribution in that cell. Some of the
     * keywords are sparsely distributed and some of them may be very dense in
     * that cell. Thus, we need to zoom into the finest level until we can
     * retrieve the detailed information from the data file.
     */
    private void exploreCandidate(CellCandidate parent,
                                  Vector<Integer> queryKwds, Point queryLoc) {
        //System.out.println(parent);

        if (parent.denseKwds.size() == 0) {
            // all the keywords reach data level and we can calculate the
            // distance
            parent.calculateScore(queryLoc);
        } else {
            // For each child cell, we check whether it has a document contains
            // all the keywords
            for (int cellID = 0; cellID < Config.CELL_NUM; cellID++) {
                if (parent.id == 1 && cellID == 71) {
                    System.out.println("debug");
                }

                // First initialize the signature and docs for child cell candidate

                HashSet<Integer> childDenseKwds = (HashSet<Integer>) parent.denseKwds.clone();
                HashMap<Integer, Integer> childDenseKwdGridID = (HashMap<Integer, Integer>) parent.denseKwdGridID.clone();
                HashMap<Integer, Double> childDocKwdFreq = new HashMap<Integer, Double>();
                HashMap<Integer, Point> childDocLoc = new HashMap<Integer, Point>();
                if (parent.docLocs.size() > 0) {
                    for (Integer docID : parent.docLocs.keySet()) {
                        // data pruning is always more powerful than headfile pruning
                        int dataCellID = Grid.getCellId(parent.docLocs.get(docID).x, parent.docLocs.get(docID).y, parent.region);
                        if (dataCellID == cellID) {
                            childDocKwdFreq.put(docID, parent.docKwdFreq.get(docID));
                            childDocLoc.put(docID, parent.docLocs.get(docID));
                        }
                    }
                    if (childDocLoc.size() == 0) {
                        continue;
                    }
                }

                // Second, we check the cell summary information for each keyword
                if (pruneByHeadFile(parent, cellID, queryKwds)) {
                    _pruneByHeadFile++;
                    continue;
                }

                // Third, we check the upper bound score of the cell exceeds top-k
                double childUpperScore = getCellUpperScore(parent, cellID, queryKwds, queryLoc);

                //System.out.println(parent.id+"  "+cellID+"  "+childUpperScore);

                if (results.size() >= K && childUpperScore <= results.get(K - 1).score) {
                    _pruneByTopK++;
                    continue;
                }

                // Fourth, if some keywords have reached data level, we load it. Otherwise,
                // if we continue to zoom it, it does not have corresponding headfile anymore.
                pruneByDataFile(parent, cellID, queryKwds, childDenseKwds, childDenseKwdGridID, childDocKwdFreq, childDocLoc);


                //Finally, we crate a new candidate and push it into priority queue.
                Region childCellRegion = Grid.getCellRegion(cellID, parent.region);
                CellCandidate newCandidate = new CellCandidate(childDenseKwds, childDenseKwdGridID, childCellRegion, childDocKwdFreq, childDocLoc, childUpperScore);

                _candidateNum++;

                candidates.add(newCandidate);
            }
        }

    }

    private CellCandidate getRootCandidate(Vector<Integer> queryKwds) {
        HashSet<Integer> denseKwds = new HashSet<Integer>();
        HashMap<Integer, Integer> denseKwdGridID = new HashMap<Integer, Integer>();

        HashMap<Integer, Double> docKwdFreq = new HashMap<Integer, Double>();
        HashMap<Integer, Point> docLocs = new HashMap<Integer, Point>();
        for (int i = 0; i < queryKwds.size(); i++) {
            int kwdID = queryKwds.get(i);
            if (idx.isKeywordFrequent(kwdID)) {
                denseKwds.add(kwdID);
                denseKwdGridID.put(kwdID, idx.getFreqKwdGridID(kwdID));
            } else {
                Vector<CellData> items = idx.loadInFreqDocs(kwdID);
                for (CellData data : items) {
                    docKwdFreq.put(data.docID, data.freq);
                    docLocs.put(data.docID, new Point(data.x, data.y));
                }
            }
        }
        return new CellCandidate(denseKwds, denseKwdGridID, idx.getWholeRegion(), docKwdFreq, docLocs, 0);
    }

    public void debugPointCellLevel(double x, double y, int level) {
        Region region = idx.getWholeRegion();
        for (int l = 0; l < level; l++) {
            int cellID = Grid.getCellId(x, y, region);
            region = Grid.getCellRegion(cellID, region);
            System.out.print(cellID + "  =>  ");
        }
        System.out.println();
    }

    private Vector<Integer> getQueryKwdID(Vector<String> keywords) {
        Vector<Integer> queryKwds = new Vector<Integer>();
        for (int i = 0; i < keywords.size(); i++) {
            int kwdID = idx.getKwdID(keywords.get(i));
            if (idx.containKeyword(kwdID)) {
                queryKwds.add(kwdID);
            }
        }
        return queryKwds;
    }

    /**
     * We maintain a PriorityQueue ordered by the estimated score.
     *
     * @param kwds
     */
    public void query(Point queryLoc, Vector<String> keywords, int k) {
        Vector<Integer> queryKwds = getQueryKwdID(keywords);
        long start = System.currentTimeMillis();
        this.K = k;
        candidates.clear();
        results.clear();
        candidates.add(getRootCandidate(queryKwds));

        while (candidates.size() > 0) {
            CellCandidate candidate = candidates.poll();
            if (results.size() >= K && candidate.upperScore <= results.get(K - 1).score) {
                break;
            }
            exploreCandidate(candidate, queryKwds, queryLoc);
        }
        long end = System.currentTimeMillis();

        printStat();
        System.out.println("results : ");
        int resultNum = K > results.size() ? results.size() : K;
        for (int i = 0; i < resultNum; i++) {
            System.out.println(results.get(i).docID + "\t"
                    + results.get(i).score);
        }
        System.out.println("Total Time : " + (end - start));
        runningTime += (end - start);

    }

    private class CellCandidate implements Comparable<CellCandidate> {
        public int id;
        public HashSet<Integer> denseKwds;        // Indicate whether the keyword is dense in this cell
        public HashMap<Integer, Integer> denseKwdGridID;

        public Region region;                // Region of the cell
        public double upperScore;            // Upper bound of final score
        HashMap<Integer, Double> docKwdFreq;// Used to calculate socre
        HashMap<Integer, Point> docLocs;    // Used to calculate score


        public CellCandidate(
                HashSet<Integer> denseKwds,
                HashMap<Integer, Integer> denseKwdGridID,
                Region region,
                HashMap<Integer, Double> docKwdFreq,
                HashMap<Integer, Point> docLocs,
                double upperScore
        ) {
            this.denseKwds = denseKwds;
            this.denseKwdGridID = denseKwdGridID;

            this.region = region;
            this.docLocs = docLocs;
            this.docKwdFreq = docKwdFreq;
            this.upperScore = upperScore;

            this.id = QueryProcessing.CID++;
        }

        public void calculateScore(Point queryLoc) {
            for (Integer docID : docLocs.keySet()) {
                double spatialScore = getSpatialScore(queryLoc.dist(docLocs.get(docID).x, docLocs.get(docID).y));
                double kwdScore = getKwdScore(docKwdFreq.get(docID));

                //System.out.println(docID+" "+( spatialScore+kwdScore)+" "+spatialScore+" "+kwdScore);
                insertResultTuple(new ResultTuple(docID, spatialScore + kwdScore));
            }
        }

        @Override
        public String toString() {
            String str = "";
            str += "****************************\n";
            str += "id : " + id + "\n";
            str += "upperScore :" + upperScore + "\n";
            str += "Dense Keywords : ";
            for (Integer kwd : denseKwds) {
                str += kwd + "  ";
            }
            str += "\nDense keyword grid:";
            for (Integer kwd : denseKwdGridID.keySet()) {
                str += kwd + ":" + denseKwdGridID.get(kwd) + "   ";
            }
            str += "\n" + region;
            str += "\ndocs : ";
            for (Integer docID : docLocs.keySet()) {
                str += docID + " ";
            }
            str += "\n";
            str += "****************************\n";
            return str;
        }

        public int compareTo(CellCandidate other) {
            return this.upperScore <= other.upperScore ? 1 : -1;
        }
    }

    public class ResultTuple implements Comparable<ResultTuple> {
        public int docID;
        public double score;

        public ResultTuple() {
            docID = -1;
            score = Double.MIN_VALUE;
        }

        public ResultTuple(int id, double score) {
            this.docID = id;
            this.score = score;
        }

        @Override
        public String toString() {
            return docID + " " + score;
        }

        @Override
        public int compareTo(ResultTuple other) {
            return this.score <= other.score ? 1 : -1;
        }
    }

    private class AprioriElement {
        public Point queryLoc = null;
        public String label;

        public Signature sig = null;
        public Region region = null;

        public HashMap<Integer, Double> docKwdFreq = null;
        public HashMap<Integer, Point> docLocs = null;

        public double kwdScore = Double.MIN_VALUE;
        public double spatialScore = Double.MIN_VALUE;
        public double totalScore = Double.MIN_VALUE;

        public AprioriElement(String label, Signature sig, double kwdScore, Region region, Point loc) {
            this.label = label;
            this.sig = sig;
            this.region = region;

            this.queryLoc = loc;
            this.kwdScore = kwdScore;
            this.spatialScore = getSpatialScore(queryLoc.getMinDistance(region));
            this.totalScore = kwdScore + spatialScore;
        }

        public AprioriElement(String label, HashMap<Integer, Double> kwdScoreMap, HashMap<Integer, Point> locMap, Point loc) {
            this.label = label;
            this.docKwdFreq = kwdScoreMap;
            this.docLocs = locMap;

            this.queryLoc = loc;
            for (Integer docID : kwdScoreMap.keySet()) {
                double score = kwdScoreMap.get(docID) + getSpatialScore(locMap.get(docID).dist(loc.x, loc.y));
                if (score > totalScore) {
                    totalScore = score;
                }
            }
        }

        @Override
        public String toString() {
            String str = "";
            if (isCellSumary()) {
                str += label + " : " + sig;
            } else {
                for (Integer docID : docKwdFreq.keySet()) {
                    str += docID + " ";
                }
            }
            str += "\n score :" + totalScore;

            return str;
        }

        public boolean isCellSumary() {
            return sig != null;
        }

        public boolean canMerge(AprioriElement other) {
            if (label.contains(" ") && !label.startsWith(other.label.substring(0, other.label.lastIndexOf(' ')))) {
                return false;
            }

            if (this.isCellSumary() && other.isCellSumary()) {
                return sig.isIntersect(other.sig);
            }
            if (this.isCellSumary() && !other.isCellSumary()) {
                for (Integer docID : other.docLocs.keySet()) {
                    if (this.sig.contain(docID)) {
                        return true;
                    }
                }
            }
            if (!this.isCellSumary() && other.isCellSumary()) {
                for (Integer docID : this.docLocs.keySet()) {
                    if (other.sig.contain(docID)) {
                        return true;
                    }
                }
            }
            return false;
        }


        public AprioriElement merge(AprioriElement other) {

            if (this.isCellSumary() && other.isCellSumary()) {
                Signature newSig = this.sig.getIntersection(other.sig);
                double newKwdScore = this.kwdScore + other.kwdScore;
                return new AprioriElement(this.label + " " + other.label, newSig, newKwdScore, this.region, queryLoc);
            }
            if (this.isCellSumary() && !other.isCellSumary()) {
                Signature newSig = new Signature();
                for (Integer docID : other.docLocs.keySet()) {
                    newSig.add(docID);
                }
                newSig.intersects(this.sig);

                // Indeed, it is possible that the CellSummary can get a higher score than
                // merging with docs of non-dense keywords because their spatial score is
                // higher. However, here, we only consider the case of merge and get new
                // score. No matter this score is even smaller.
                HashMap<Integer, Double> freqMap = new HashMap<Integer, Double>();
                HashMap<Integer, Point> locMap = new HashMap<Integer, Point>();
                for (Integer docID : other.docLocs.keySet()) {
                    if (newSig.contain(docID)) {
                        freqMap.put(docID, other.docKwdFreq.get(docID) + this.kwdScore);
                        locMap.put(docID, other.docLocs.get(docID));
                    }
                }
                return new AprioriElement(this.label + " " + other.label, freqMap, locMap, queryLoc);

            }
            if (!this.isCellSumary() && other.isCellSumary()) {
                Signature newSig = new Signature();
                for (Integer docID : this.docLocs.keySet()) {
                    newSig.add(docID);
                }
                newSig.intersects(other.sig);


                HashMap<Integer, Double> freqMap = new HashMap<Integer, Double>();
                HashMap<Integer, Point> locMap = new HashMap<Integer, Point>();
                for (Integer docID : this.docLocs.keySet()) {
                    if (newSig.contain(docID)) {
                        freqMap.put(docID, this.docKwdFreq.get(docID) + other.kwdScore);
                        locMap.put(docID, this.docLocs.get(docID));
                    }
                }
                return new AprioriElement(this.label + " " + other.label, freqMap, locMap, queryLoc);
            }
            return null;
        }


    }
}
