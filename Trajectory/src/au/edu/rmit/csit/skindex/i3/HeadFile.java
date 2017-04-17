package au.edu.rmit.csit.skindex.i3;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;


/**
 * Each HeadFile contains a list of GridSummary. The hierarchy structure is :
 * HeadFile -> GridSummary -> CellSummary
 *
 * @author Workshop
 */

public class HeadFile {
    private String filename;
    private RandomAccessFile fileHandler;
    private FileChannel fc;

    private HashMap<Integer, GridSummary> gridSummaries;
    private int maxGridID;
    private int IO_COUNT = 0;

    public HeadFile(String filename) {
        this.filename = filename;
        this.gridSummaries = new HashMap<Integer, GridSummary>();

        try {
            File file = new File(filename);
            boolean exists = file.exists();
            if (!exists) {
                file.createNewFile();
            }
            fileHandler = new RandomAccessFile(filename, "rw");
            fc = fileHandler.getChannel();
            maxGridID = (int) fileHandler.length() / GridSummary.SIZE;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public GridSummary getGridSummary(int gridID) {
        GridSummary gridSummary = null;

        if (gridSummaries.containsKey(gridID)) {
            gridSummary = gridSummaries.get(gridID);
        } else {
            gridSummary = loadGridSummary(gridID);
            gridSummaries.put(gridID, gridSummary);
        }

        return gridSummary;
    }

    public int getIO() {
        return IO_COUNT;
    }

    /*
     * Create a new chunk in the data file.
     */
    public int appendGridSummary(Region region) {
        GridSummary gridSummary = new GridSummary(region);
        gridSummaries.put(maxGridID, gridSummary);
        maxGridID++;
        return maxGridID - 1;
    }

    private GridSummary loadGridSummary(int gridID) {
        GridSummary gridSummary = null;
        try {
            //System.out.println("load grid "+gridID);
            MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, gridID * GridSummary.SIZE, GridSummary.SIZE);
            gridSummary = new GridSummary();
            gridSummary.readByteBuffer(mbb);
            IO_COUNT++;
        } catch (Exception e) {
            System.out.println(filename);
            e.printStackTrace();
        }
        return gridSummary;
    }

    public void save() {
        try {
            int count = 1, milestone = gridSummaries.size() / 1000;
            for (Integer gridID : gridSummaries.keySet()) {
                if (count++ % milestone == 0) {
                    System.out.println(count / milestone + "/" + 1000);
                }
                saveGridSummary(gridID);
            }
            fc.close();
            fileHandler.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveGridSummary(int gridID) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(GridSummary.SIZE);
            getGridSummary(gridID).writeByteBuffer(buffer);
            buffer.rewind();
            fc.write(buffer, gridID * GridSummary.SIZE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public String toString() {
        String str = filename + " : with " + maxGridID + " GridSummaries\n";
        for (int i = 0; i < maxGridID; i++) {
            str += "===========================================\n";
            str += getGridSummary(i).toString();
            //System.out.println(getGridSummary(i));
            //break;
        }
        return str;
    }
}

