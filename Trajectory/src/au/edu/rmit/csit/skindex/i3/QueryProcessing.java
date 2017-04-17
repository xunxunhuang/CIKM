package au.edu.rmit.csit.skindex.i3;

import java.io.*;
import java.util.*;


/**
 * Scalable keyword query processing algorithm. Given a set of query keywords,
 * our goal is to find the top-k documents with highest matching score. The
 * ranking function considers both spatial and textual relevance.
 * <p>
 * During the query processing stage, we first load the header file of each
 * query keyword into memory. With the obtained summary information in the
 * header file, we propose the following three prunning rules:
 * <p>
 * Pruning rule 1 Cell id list intersection. A cell is a candidate if it
 * contains all the query keywords Pruning rule 2 For each cell, we know the
 * distance from query loc to it and its keyword frequency inside. Then, we can
 * get a upper bound score for pruning Pruning rule 3 Given a candidate cell, we
 * further use the doc id signature for pruning. We require the results are
 * documents containing all the query keywords.
 * <p>
 * The idea behind the processing algorithm is that we want to retrieve as fewer
 * cells as possible to reduce I/O cost. So we estimate the score value for each
 * cell and start from the most promising method.
 * <p>
 * The search algorithm starts from the top level of the hierarchy grid. It
 * maintains a priority queue to store the candidates. For the query keywords,
 * we check their intersection in the same grid cell. If the cell for all the
 * keywords already contain a list of documents, we simply calculate the real
 * score value. Otherwise, we need to zoom in to the lower levels until we reach
 * the cell that is not dense and get split.
 *
 * @author Workshop
 */
public class QueryProcessing {
    public static int CID = 1;
    HIFIndex idx;
    HashMap<String, Double> kwdDf;
    HashMap<Integer, Double> queryKwdDf;
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
    public QueryProcessing(String folder) {
        idx = new HIFIndex(folder);
        candidates = new PriorityQueue<CellCandidate>();
        results = new Vector<ResultTuple>();
        loadKwdDf();
    }
    public QueryProcessing(HIFIndex idx) {
        this.idx = idx;
        candidates = new PriorityQueue<CellCandidate>();
        results = new Vector<ResultTuple>();
        loadKwdDf();
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

        QueryProcessing processor = new QueryProcessing(indexFolder);


        String line;
        int queryNum = 0;
        try {
            BufferedReader inputStream = new BufferedReader(new FileReader(queryFile));
            long start = System.currentTimeMillis();
            while ((line = inputStream.readLine()) != null) {
                System.out.println(line);
                Vector<String> queryKwds = new Vector<String>();

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

    private void loadKwdDf() {
        try {
            File file = new File("keyword.df");
            if (file.exists()) {
                FileInputStream f = new FileInputStream(file);
                ObjectInputStream s = new ObjectInputStream(f);


                kwdDf = (HashMap<String, Double>) s.readObject();


                s.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printStat() {
        /*
        System.out.println("Prune by HeadFile :"+_pruneByHeadFile);
		System.out.println("Prune by DataFile :"+_pruneByDataFile);
		System.out.println("Prune by Topk :"+_pruneByTopK);
		System.out.println("Candidate Num :"+_candidateNum);

		System.out.println("HeadFile IO Time : "+HeadFile_IO_Time);
		System.out.println("DataFile IO Time : "+DataFile_IO_Time);
		*/
        System.out.println("IO Count : " + idx.getIO());
    }

    private double getSpatialScore(double dist) {

        double spatialProximity = 1 - (dist / Config.MAX_DIST);
        return Config.ALPHA * spatialProximity;
    }

    private double getKwdScore(double freq) {

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

    private boolean pruneByHeadFile(CellCandidate parent, int cellID,
                                    Vector<Integer> queryKwds, Signature childSig) {

        for (int k = 0; k < queryKwds.size(); k++) {

            // denseKwds contains keyword whose HeadFiles have not been explored
            // before in parent's region.
            int kwdID = queryKwds.get(k);
            if (parent.denseKwds.contains(queryKwds.get(k))) {
                int gridID = parent.denseKwdGridID.get(kwdID);
                GridSummary gridSummary = idx.getGridSummary(gridID);
                CellSummary cellSummary = gridSummary.getCell(cellID);

                // The keyword must exist in this cell.
                if (cellSummary.getKwdFreq() == 0) {
                    return true;
                }

                // The cell must contain a document with all the query keywords
                if (!childSig.isIntersect(cellSummary.getSignature())) {
                    return true;
                }

                childSig.intersects(cellSummary.getSignature());
            }
        }
        return false;
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

    private boolean pruneByDataFile(CellCandidate parent,
                                    int cellID,
                                    Vector<Integer> queryKwds,
                                    Signature childSig,
                                    HashSet<Integer> childDenseKwds,
                                    HashMap<Integer, Integer> childDenseKwdGridID,
                                    HashMap<Integer, Double> docKwdFreq,
                                    HashMap<Integer, Point> docLocs) {

        for (int k = 0; k < queryKwds.size(); k++) {
            int kwdID = queryKwds.get(k);
            if (parent.denseKwds.contains(kwdID)) {
                int gridID = parent.denseKwdGridID.get(kwdID);
                GridSummary gridSummary = idx.getGridSummary(gridID);
                CellSummary cellSummary = gridSummary.getCell(cellID);
                if (!cellSummary.isDense()) {
                    Vector<CellData> allData = idx.loadFreqDocs(gridID, cellID);

                    HashMap<Integer, CellData> filterData = new HashMap<Integer, CellData>();
                    HashSet<Integer> filterDocID = new HashSet<Integer>();
                    for (CellData data : allData) {
                        if (childSig.contain(data.docID)) {
                            filterData.put(data.docID, data);
                            filterDocID.add(data.docID);
                        }

                    }

                    if (childDenseKwds.size() == queryKwds.size()) {
                        for (CellData data : filterData.values()) {
                            docLocs.put(data.docID, new Point(data.x, data.y));
                            docKwdFreq.put(data.docID, data.freq);
                        }
                    } else {
                        HashSet<Integer> removeDocs = new HashSet<Integer>();
                        for (Integer docID : docKwdFreq.keySet()) {
                            if (!filterDocID.contains(docID)) {
                                removeDocs.add(docID);
                            } else {
                                docKwdFreq.put(docID, docKwdFreq.get(docID) + filterData.get(docID).freq);
                            }
                        }

                        for (Integer docID : removeDocs) {
                            docKwdFreq.remove(docID);
                            docLocs.remove(docID);
                        }


                        if (docLocs.size() == 0) {
                            return true;
                        }

                        boolean existDoc = false;
                        for (Integer docID : docLocs.keySet()) {
                            if (childSig.contain(docID)) {
                                existDoc = true;
                                break;
                            }
                        }
                        if (!existDoc) {
                            return true;
                        }
                    }
                    childDenseKwds.remove(queryKwds.get(k));
                } else {
                    childDenseKwdGridID.put(queryKwds.get(k), cellSummary.getGridID());
                }
            }
        }

        return false;
    }

    /*
     * Get the upper bound keyword score in the cell.
     */
    private double getCellKwdScore(CellCandidate parent, int cellID, Vector<Integer> queryKwds) {
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

        return getKwdScore(kwdFreq);
    }

    /*
     * Given a candidate cell, we explore further candidates inside. Since
     * different keywords have different distribution in that cell. Some of the
     * keywords are sparsely distributed and some of them may be very dense in
     * that cell. Thus, we need to zoom into the finest level until we can
     * retrieve the detailed information from the data file.
     */
    @SuppressWarnings("unchecked")
    private void exploreCandidate(CellCandidate parent, Vector<Integer> queryKwds, Point queryLoc) {
        //System.out.println(parent);

        if (parent.denseKwds.size() == 0) {
            // all the keywords reach data level and we can calculate the
            // distance
            parent.calculateScore(queryLoc);
        } else {
            // For each child cell, we check whether it has a document contains
            // all the keywords
            for (int cellID = 0; cellID < Config.CELL_NUM; cellID++) {
                if (parent.id == 1 && cellID == 27) {
                    System.out.println("debug");
                }
                // First initialize the signature and docs for child cell candidate
                HashSet<Integer> childDenseKwds = (HashSet<Integer>) parent.denseKwds.clone();
                HashMap<Integer, Integer> childDenseKwdGridID = (HashMap<Integer, Integer>) parent.denseKwdGridID.clone();
                HashMap<Integer, Double> childDocKwdFreq = new HashMap<Integer, Double>();
                HashMap<Integer, Point> childDocLoc = new HashMap<Integer, Point>();
                Signature childSig = null;

                if (parent.docLocs.size() == 0) {
                    childSig = (Signature) parent.sig.clone();
                } else {
                    childSig = new Signature();
                    for (Integer docID : parent.docLocs.keySet()) {
                        // data pruning is always more powerful than headfile pruning
                        int dataCellID = Grid.getCellId(parent.docLocs.get(docID).x, parent.docLocs.get(docID).y, parent.region);
                        if (dataCellID == cellID) {
                            childSig.add(docID);
                            childDocKwdFreq.put(docID, parent.docKwdFreq.get(docID));
                            childDocLoc.put(docID, parent.docLocs.get(docID));
                        }
                    }
                    if (childDocLoc.size() == 0) {
                        continue;
                    }
                }

                // Second, we check the cell summary information for each keyword
                if (pruneByHeadFile(parent, cellID, queryKwds, childSig)) {
                    _pruneByHeadFile++;
                    continue;
                }

                // Third, we check the upper bound score of the cell exceeds top-k
                Region childRegion = null;
                if (childDocLoc.size() != 0) {
                    childRegion = new Region();
                    for (Point loc : childDocLoc.values()) {
                        childRegion.add(loc.x, loc.y);
                    }
                } else {
                    childRegion = Grid.getCellRegion(cellID, parent.region);
                }

                double newDistScore = getSpatialScore(queryLoc
                        .getMinDistance(childRegion));
                double newKwdScore = getCellKwdScore(parent, cellID, queryKwds);
                if (results.size() >= K && newKwdScore + newDistScore <= results.get(K - 1).score) {
                    _pruneByTopK++;
                    continue;
                }

                // Fourth, if some keywords have reached data level, we load it. Otherwise,
                // if we continue to zoom it, it does not have corresponding headfile anymore.

                if (pruneByDataFile(parent, cellID, queryKwds, childSig, childDenseKwds, childDenseKwdGridID, childDocKwdFreq, childDocLoc)) {
                    _pruneByDataFile++;
                    continue;
                }


                // Fifth : after loading the data file, the childDocs are more accurate
                if (childDocLoc.size() != 0) {
                    childRegion = new Region();
                    for (Point loc : childDocLoc.values()) {
                        childRegion.add(loc.x, loc.y);
                    }
                    newDistScore = getSpatialScore(queryLoc
                            .getMinDistance(childRegion));
                    newKwdScore = getCellKwdScore(parent, cellID, queryKwds);
                    if (results.size() >= K && newKwdScore + newDistScore <= results.get(K - 1).score) {
                        _pruneByTopK++;
                        continue;
                    }
                }


                //Finally, we crate a new candidate and push it into priority queue.
                double childUpperScore = newKwdScore + newDistScore;
                Region childCellRegion = Grid.getCellRegion(cellID, parent.region);
                CellCandidate newCandidate = new CellCandidate(childDenseKwds, childDenseKwdGridID, childCellRegion, childSig, childDocKwdFreq, childDocLoc, childUpperScore);
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
        Signature sig = new Signature();
        sig.setAll();
        return new CellCandidate(denseKwds, denseKwdGridID, idx.getWholeRegion(), sig, docKwdFreq, docLocs, 0);
    }

    private void debugPointCellLevel(double x, double y, int level) {
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
    public Vector<ResultTuple> query(Point queryLoc, Vector<String> keywords, int k) {

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
        System.out.println("Total Time : " + ((end - start) / 1000.0) + "s");
        runningTime += (end - start);
        return results;
    }

    /*
     * Each CellCandidate contains all the necessary information for query
     * processing. It could be either a dense cell or a non-dense cell. If it is
     * a dense cell, it will get further split. Otherwise, we can retrieve the
     * doc information and store it first.
     *
     * Only QueryProcessing can view the contents in this class.
     */
    public class CellCandidate implements Comparable<CellCandidate> {
        public HashSet<Integer> denseKwds;        // Indicate whether the keyword is dense in this cell
        public HashMap<Integer, Integer> denseKwdGridID;
        public Region region;                // Region of the cell
        public Signature sig;                // Signature of candidate docs in the cell
        public double upperScore;            // Upper bound of final score
        int id;
        HashMap<Integer, Double> docKwdFreq;// Used to calculate socre
        HashMap<Integer, Point> docLocs;    // Used to calculate score


        public CellCandidate(
                HashSet<Integer> denseKwds,
                HashMap<Integer, Integer> denseKwdGridID,
                Region region,
                Signature sig,
                HashMap<Integer, Double> docKwdFreq,
                HashMap<Integer, Point> docLocs,
                double upperScore
        ) {
            this.denseKwds = denseKwds;
            this.denseKwdGridID = denseKwdGridID;

            this.region = region;
            this.sig = sig;
            this.docLocs = docLocs;
            this.docKwdFreq = docKwdFreq;
            this.upperScore = upperScore;
            this.id = QueryProcessing.CID++;

        }

        public void calculateScore(Point queryLoc) {
            for (Integer docID : docLocs.keySet()) {
                double spatialScore = getSpatialScore(queryLoc.dist(docLocs.get(docID).x, docLocs.get(docID).y));
                double kwdScore = getKwdScore(docKwdFreq.get(docID));
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
}
