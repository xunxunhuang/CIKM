package au.edu.rmit.csit.skindex.i3;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;


/**
 * A DataFile contains a list of CellChunks. In our implementation, we want to
 * improve the storage utilization. Note that DataFile operates on CellChunk and
 * CellChunk operates on data items. Thus, we don't provide data item realted
 * functions such as addItem.
 *
 * @author Workshop
 */
public class DataFile {
    private String filename;
    private RandomAccessFile fileHandler;
    private FileChannel fc;

    private HashMap<Integer, CellChunk> chunks;
    private int maxChunkID;
    private int IO_COUNT = 0;

    public DataFile(String filename) {
        this.filename = filename;
        this.chunks = new HashMap<Integer, CellChunk>();
        try {
            fileHandler = new RandomAccessFile(filename, "rw");
            fc = fileHandler.getChannel();
            maxChunkID = (int) fileHandler.length() / CellChunk.SIZE;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addItem(CellData data, int chunkID) {
        CellChunk chunk = getChunk(chunkID);
        chunk.addItem(data);
    }


    /*
     * If the chunk is already in memory, we just return it. Otherwise, if this is
     * an existing file, we load the chunk from it.
     */
    public CellChunk getChunk(int chunkID) {
        CellChunk chunk = null;

        if (chunks.containsKey(chunkID)) {
            chunk = chunks.get(chunkID);
        } else {
            chunk = loadChunk(chunkID);
            chunks.put(chunkID, chunk);
        }

        return chunk;
    }


    public CellChunk getLastChunk() {
        return getChunk(maxChunkID - 1);
    }


    public int getChunkNum() {
        return maxChunkID;
    }


    /*
     * Create a new chunk in the data file.
     */
    public int appendChunk() {
        CellChunk chunk = new CellChunk();
        int chunkID = maxChunkID++;
        //System.err.println("\t"+chunkID);
        chunks.put(chunkID, chunk);
        return chunkID;
    }


    /*
     * Find a chunk to hold a set of data with size dataNum.
     */
    public int findNonFullChunk(int slotNum) {
        // First check the chunk
        int chunkID = ChunkEmptySlotManager.getChunkWithSlotNum(slotNum);
        if (chunkID != -1) {
            return chunkID;
        }

        // Second we check the disk chunks
        for (int i = 0; i < maxChunkID; i++) {
            if (!chunks.containsKey(i)) {
                CellChunk chunk = getChunk(i);
                if (chunk.getEmptySlotNum() >= slotNum) {
                    return i;
                }
            }
        }
        int newChunkID = appendChunk();
        ChunkEmptySlotManager.addChunkEmptySlotNum(newChunkID, Config.CELL_SIZE);
        return newChunkID;
    }


    public int getIO() {
        return IO_COUNT;
    }


    private CellChunk loadChunk(int chunkID) {
        CellChunk chunk = null;
        long pos = -1;
        try {
            pos = ((long) chunkID) * CellChunk.SIZE;
            MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, pos, CellChunk.SIZE);
            chunk = new CellChunk();
            chunk.readByteBuffer(mbb);
            IO_COUNT++;
        } catch (Exception e) {
            System.out.println("error in load chunk id" + chunkID);
            System.out.println(filename);
            e.printStackTrace();
            System.exit(0);
        }
        return chunk;
    }


    public void save() {
        try {
            int count = 1, milestone = chunks.size() / 1000;

            for (Integer chunkID : chunks.keySet()) {
                //System.err.println("save "+chunkID);
                if (count++ % milestone == 0) {
                    System.out.println(count / milestone + "/" + 1000);
                }
                saveChunk(chunkID);
            }
            fc.close();
            fileHandler.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void saveChunk(int chunkID) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(CellChunk.SIZE);
            getChunk(chunkID).writeByteBuffer(buffer);
            buffer.rewind();
            long pos = ((long) chunkID) * CellChunk.SIZE;
            fc.write(buffer, pos);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }


    public void printStat() {
        System.out.println("Total number of chunks :" + maxChunkID);
        int emptySlotNum = 0;
        for (int i = 0; i < maxChunkID; i++) {
            CellChunk chunk = getChunk(i);
            //System.out.println(chunk);

            //System.out.print(chunk.getEmptySlotNum()+" ");
            emptySlotNum += chunk.getEmptySlotNum();
        }
//		System.out.println(emptySlotNum);
//		System.out.println(Config.CELL_SIZE);
//		System.out.println(maxChunkID);
        System.out.println("\nAvg storage utilization: " + (1 - emptySlotNum * 1.0 / (Config.CELL_SIZE * maxChunkID)));
    }

    @Override
    public String toString() {
        String str = filename + ":\n";
        for (Integer chunkID : chunks.keySet()) {
            str += "chunk " + chunkID + "\n";
            str += chunks.get(chunkID).toString();
        }
        return str;
    }
}
