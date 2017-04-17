package au.edu.rmit.csit.skindex.i3;


import java.io.*;
import java.util.HashMap;
import java.util.Vector;

/**
 * In this bulk-loading version, we do not allow a chunk to contain different keywords.
 *
 * @author Workshop
 */


public class HIFIndex {
    private String headFileName;
    private String dataFileName;
    private String metaFileName;

    private HeadFile headFile;
    private DataFile dataFile;

    // Manage keyword id assignment
    private HashMap<String, Integer> kwdIDMap;

    // If a keyword is frequent, it will point to the root GridSummary in the headFile
    private HashMap<Integer, Integer> freqKwdGridMap;

    // If a keyword is infrequent, it will point to a Chunk in the dataFile
    private HashMap<Integer, Integer> inFreqKwdChunkMap;

    private Region wholeRegion;

    public HIFIndex(String folder) {
        headFileName = folder + "/" + "hif.head";
        dataFileName = folder + "/" + "hif.dat";
        metaFileName = folder + "/" + "hif_meta.dat";

        loadMeta();
        headFile = new HeadFile(headFileName);
        dataFile = new DataFile(dataFileName);
    }


    public HIFIndex(Region region, String folder) {
        headFileName = folder + "/" + "hif.head";
        dataFileName = folder + "/" + "hif.dat";
        metaFileName = folder + "/" + "hif_meta.dat";


        wholeRegion = region;
        kwdIDMap = new HashMap<String, Integer>();
        freqKwdGridMap = new HashMap<Integer, Integer>();
        inFreqKwdChunkMap = new HashMap<Integer, Integer>();


        headFile = new HeadFile(headFileName);
        dataFile = new DataFile(dataFileName);
    }


    /*
     * Add the item into the index. We first check whether this is a frequent or infrequent keyword.
     * Then, the item is inserted with different strategic.
     *
     */
    public void addItem(String keyword, int docID, double x, double y, double freq) {
        int kwdID = getKwdID(keyword);
        if (kwdID == 268) {
            int debug = 1;
        }
        if (isKeywordFrequent(kwdID)) {
            addFreqItem(kwdID, docID, x, y, freq);
        } else {
            addInFreqItem(kwdID, docID, x, y, freq);
        }
    }


    /*
     * To add a frequent item, since it is possible that this keyword is very frequent and we have
     * built a Index.quadtree to store it. So our first step is to find the leaf node of the Index.quadtree for
     * this keyword and insert the item into the leaf node.
     *
     * A leaf node corresponds to a summary node+a data chunk.
     */
    private void addFreqItem(int kwdID, int docID, double x, double y, double freq) {
        int gridID = freqKwdGridMap.get(kwdID);
        int cellID = Grid.getCellId(x, y, wholeRegion);
        do {
            CellSummary summary = headFile.getGridSummary(gridID).getCell(cellID);
            if (!summary.isDense()) {
                break;
            }
            summary.addItem(docID, freq);
            gridID = summary.getGridID();
            cellID = Grid.getCellId(x, y, headFile.getGridSummary(gridID).getRegion());
        } while (true);

        CellData item = new CellData(kwdID, docID, x, y, freq);
        addFreqItem(item, gridID, cellID);
    }


    /*
     * After loading the necessary summary and data chunk, we can add the item to the chunk.
     * If the chunk is not full, we can finish quickly. Otherwise, we need to split the cell
     * into four child cells, create new summary nodes.
     *
     */
    public void addFreqItem(CellData item, int gridID, int cellID) {
        GridSummary gridSummary = headFile.getGridSummary(gridID);
        CellSummary cellSummary = gridSummary.getCell(cellID);
        int chunkID = cellSummary.getChunkID();
        // It is possible that the CellSummary's chunk id has been initialized
        if (chunkID == -1) {
            chunkID = dataFile.findNonFullChunk(1);
            cellSummary.setChunkID(chunkID);
        }
        CellChunk chunk = dataFile.getChunk(chunkID);

        if (!chunk.full()) {
            // If there is empty slot, we simply insert the data and update the summary
            dataFile.addItem(item, chunkID);
            cellSummary.addItem(item.docID, item.freq);
        } else {
            // Create a new GridSummary
            Region cellRegion = Grid.getCellRegion(cellID, gridSummary.getRegion());

            int newGridID = splitCell(cellRegion, chunk, chunkID);

            // update the old CellSummary to point to the new GridSummary instead of a data chunk.
            cellSummary.setGridID(newGridID);

            // Insert the data into the new child cell
            addFreqItem(item, cellSummary.getGridID(), Grid.getCellId(item.x, item.y, cellRegion));
        }

    }


    private void addInFreqItem(int kwdID, int docID, double x, double y, double freq) {
        if (!inFreqKwdChunkMap.containsKey(kwdID)) {
            inFreqKwdChunkMap.put(kwdID, dataFile.appendChunk());
            //inFreqKwdChunkMap.put(kwdID, dataFile.findNonFullChunk(Config.CELL_SIZE/2));

        }
        int chunkID = inFreqKwdChunkMap.get(kwdID);
        CellData item = new CellData(kwdID, docID, x, y, freq);
        addInFreqItem(item, kwdID, chunkID);
    }


    private void addInFreqItem(CellData item, int kwdID, int chunkID) {
        CellChunk chunk = dataFile.getChunk(chunkID);

        if (!chunk.full()) {
            // If there is empty slot, we simply insert the data. We use dataFile as we need
            // to know add event happend in dataFile
            dataFile.addItem(item, chunkID);
            //ChunkEmptySlotManager.updateChunkEmptySlotNum(chunkID, dataFile.getChunk(chunkID).getEmptySlotNum()+1, dataFile.getChunk(chunkID).getEmptySlotNum());
        } else {
            if (chunk.sameKeyword()) {
                int newGridID = splitCell(wholeRegion, chunk, chunkID);
                int newCellID = Grid.getCellId(item.x, item.y, wholeRegion);
                addFreqItem(item, newGridID, newCellID);

                freqKwdGridMap.put(kwdID, newGridID);
                inFreqKwdChunkMap.remove(kwdID);

            } else {
                Vector<CellData> sameSourceData = chunk.fetchData(item.kwd);
                dataFile.getChunk(chunkID).deleteKwd(item.kwd);

                int newChunkID = dataFile.findNonFullChunk(sameSourceData.size() + 1);

                for (CellData data : sameSourceData) {
                    dataFile.addItem(data, newChunkID);
                }
                dataFile.addItem(item, newChunkID);
                inFreqKwdChunkMap.put(kwdID, newChunkID);

                //ChunkEmptySlotManager.updateChunkEmptySlotNum(newChunkID, dataFile.getChunk(newChunkID).getEmptySlotNum()+sameSourceData.size()+1, dataFile.getChunk(newChunkID).getEmptySlotNum());
                //ChunkEmptySlotManager.updateChunkEmptySlotNum(chunkID, dataFile.getChunk(chunkID).getEmptySlotNum()-sameSourceData.size(), dataFile.getChunk(chunkID).getEmptySlotNum());
            }
        }
    }


    /*
     * When a cell is overflow, we split it into four child cells. The original
     * cell becomes a grid.
     *
     */
    private int splitCell(Region region, CellChunk chunk, int chunkID) {
        int newGridID = headFile.appendGridSummary(region);
        GridSummary newGridSummary = headFile.getGridSummary(newGridID);
        // We leave to old chunk to the first child cell. For the others, we allocate new chunks.
        newGridSummary.getCell(0).setChunkID(chunkID);
        for (int c = 1; c < Config.CELL_NUM; c++) {
            newGridSummary.getCell(c).setChunkID(dataFile.appendChunk());
        }


        // Update the summary and data chunks in the new GridSummary.
        Vector<CellData> chunkData = chunk.fetchAllData();
        int itemKept = 0;
        for (int slotID = 0; slotID < chunkData.size(); slotID++) {
            CellData data = chunkData.get(slotID);
            int dataCellID = Grid.getCellId(data.x, data.y, region);

            newGridSummary.getCell(dataCellID).addItem(data.docID, data.freq);
            if (dataCellID != 0) {
                dataFile.addItem(data, newGridSummary.getCell(dataCellID).getChunkID());
                dataFile.getChunk(chunkID).deleteItem(slotID);
            } else {
                itemKept++;
            }

        }
        return newGridID;
    }

    // For debug
    public void locateDoc(int docID, double x, double y, int kwdID) {
        int gridID, cellID;
        gridID = getFreqKwdGridID(kwdID);
        while (true) {
            GridSummary gridSummary = headFile.getGridSummary(gridID);
            cellID = Grid.getCellId(x, y, gridSummary.getRegion());
            CellSummary cellSummary = gridSummary.getCell(cellID);
            System.out.println(gridID + "  " + cellID);
            if (!cellSummary.isDense()) {
                System.out.println(cellSummary);
                System.out.println("chunkID : " + cellSummary.getChunkID());
                System.out.println(dataFile.getChunk(cellSummary.getChunkID()));
                break;
            }
            gridID = cellSummary.getGridID();
        }
    }


    public GridSummary getGridSummary(int gridID) {
        return headFile.getGridSummary(gridID);
    }


    public boolean isKeywordFrequent(int kwdID) {
        return freqKwdGridMap.containsKey(kwdID);
    }

    public boolean containKeyword(int kwdID) {
        return freqKwdGridMap.containsKey(kwdID) || inFreqKwdChunkMap.containsKey(kwdID);
    }

    public int getFreqKwdGridID(int kwdID) {
        return freqKwdGridMap.get(kwdID);
    }

    public Region getWholeRegion() {
        return wholeRegion;
    }

    public Vector<CellData> loadInFreqDocs(int kwdID) {
        int chunkID = inFreqKwdChunkMap.get(kwdID);
        CellChunk chunk = dataFile.getChunk(chunkID);
        return chunk.fetchAllData();
    }

    public Vector<CellData> loadFreqDocs(int gridID, int cellID) {
        GridSummary gridSummary = headFile.getGridSummary(gridID);
        CellSummary cellSummary = gridSummary.getCell(cellID);
        int chunkID = cellSummary.getChunkID();
        CellChunk chunk = dataFile.getChunk(chunkID);
        return chunk.fetchAllData();
    }

    public int getKwdID(String kwd) {
        if (!kwdIDMap.containsKey(kwd)) {
            kwdIDMap.put(kwd, kwdIDMap.size() + 1);
        }
        return kwdIDMap.get(kwd);
    }

    public int getDataIO() {
        return dataFile.getIO();
    }

    public int getHeadIO() {
        return headFile.getIO();
    }

    public int getIO() {
        return dataFile.getIO() + headFile.getIO();
    }


    public void flush() {
        System.out.println("start flushing..");
        dataFile.printStat();

        System.out.println("flushing headfile..");
        headFile.save();
        System.out.println("flushing datafile..");
        dataFile.save();
        saveMeta();
    }


    @SuppressWarnings("unchecked")
    private void loadMeta() {
        wholeRegion = new Region();
        kwdIDMap = new HashMap<String, Integer>();
        freqKwdGridMap = new HashMap<Integer, Integer>();
        inFreqKwdChunkMap = new HashMap<Integer, Integer>();

        try {
            File file = new File(metaFileName);
            if (file.exists()) {
                FileInputStream f = new FileInputStream(file);
                ObjectInputStream s = new ObjectInputStream(f);

                wholeRegion = (Region) s.readObject();
                System.out.println("load keyword id map");
                kwdIDMap = (HashMap<String, Integer>) s.readObject();
                System.out.println("load frequent keyword meta");
                freqKwdGridMap = (HashMap<Integer, Integer>) s.readObject();
                System.out.println("load infrequent keyword meta");
                inFreqKwdChunkMap = (HashMap<Integer, Integer>) s.readObject();
                System.out.println("finish loading meta info.");
                s.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void saveMeta() {
        try {
            File file = new File(metaFileName);
            FileOutputStream f = new FileOutputStream(file);
            ObjectOutputStream s = new ObjectOutputStream(f);

            s.writeObject(wholeRegion);
            s.writeObject(kwdIDMap);
            s.writeObject(freqKwdGridMap);
            s.writeObject(inFreqKwdChunkMap);


            s.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String getMetaString() {
        String str = "";
        str += "Infrequent Keywords :\n";
        for (Integer kwdID : inFreqKwdChunkMap.keySet()) {
            str += kwdID + " ";
        }
        str += "\nFrequent Keywords :\n";
        for (Integer kwdID : freqKwdGridMap.keySet()) {
            str += kwdID + " ";
        }
        str += "\n";
        str += wholeRegion.toString();

        return str;
    }

    @Override
    public String toString() {
        String str = getMetaString();
        str += headFile.toString();
        str += dataFile.toString();
        return str;
    }


    //==============  For Debug Only ============//
    public void debug() {
        Vector<String> keywords = new Vector<String>();
        keywords.add("restaurant");
        double lat = 2.801995;
        double lng = 101.772084;


        for (int i = 0; i < keywords.size(); i++) {
            int kwdID = getKwdID(keywords.get(i));
            System.out.println(kwdID + " : ");
            if (!isKeywordFrequent(kwdID)) {
                Vector<CellData> items = loadInFreqDocs(kwdID);
                for (CellData item : items) {
                    System.out.println(item);
                }
            } else {
                int gridID = freqKwdGridMap.get(kwdID);
                int cellID = Grid.getCellId(lat, lng, wholeRegion);
                CellSummary summary = null;
                do {
                    summary = headFile.getGridSummary(gridID).getCell(cellID);
                    System.out.println(gridID + "\t" + cellID + "\t" + headFile.getGridSummary(gridID).getRegion());
                    if (!summary.isDense()) {
                        break;
                    }

                    gridID = summary.getGridID();
                    cellID = Grid.getCellId(lat, lng, headFile.getGridSummary(gridID).getRegion());
                } while (true);
                if (summary != null) {
                    System.out.println("chunk id : " + summary.getChunkID());
                    System.out.println(dataFile.getChunk(summary.getChunkID()));
                }
            }

        }
    }

}
