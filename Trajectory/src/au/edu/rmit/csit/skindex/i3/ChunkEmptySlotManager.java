package au.edu.rmit.csit.skindex.i3;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;


public class ChunkEmptySlotManager {
    // key is empty slot number. value is the set of chunks with this number of empty slots
    private static HashMap<Integer, HashSet<Integer>> chunkSlotMap = new HashMap<Integer, HashSet<Integer>>();

    @SuppressWarnings("unchecked")
    private static HashMap<Integer, HashSet<Integer>> cloneMap() {
        HashMap<Integer, HashSet<Integer>> newMap = new HashMap<Integer, HashSet<Integer>>();

        for (Integer emptySlotNum : chunkSlotMap.keySet()) {
            newMap.put(emptySlotNum, (HashSet<Integer>) chunkSlotMap.get(emptySlotNum).clone());
        }
        return newMap;
    }

    /*
     * When a new chunk is loaded, add its empty slot number into chunkSlotMap
     */
    public static void addChunkEmptySlotNum(int chunkID, int emptySlotNum) {
        if (!chunkSlotMap.containsKey(emptySlotNum)) {
            chunkSlotMap.put(emptySlotNum, new HashSet<Integer>());
        }
        chunkSlotMap.get(emptySlotNum).add(chunkID);
    }


    public static void deleteChunkEmptySlotNum(int chunkID, int emptySlotNum) throws IOException {
        if (!chunkSlotMap.get(emptySlotNum).contains(chunkID)) {
            throw new IOException("the chunk " + chunkID + " with " + emptySlotNum + " empty slots are not found");
        }
        chunkSlotMap.get(emptySlotNum).remove(chunkID);
    }

    /*
     * Update the empty slot number of a chunk.
     */
    public static void updateChunkEmptySlotNum(int chunkID, int oldEmptySlotNum, int newEmptySlotNum) {
        try {
            deleteChunkEmptySlotNum(chunkID, oldEmptySlotNum);
            addChunkEmptySlotNum(chunkID, newEmptySlotNum);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static int getChunkWithSlotNum(int slotNum) {

        for (Integer emptySlotNum = slotNum; emptySlotNum < Config.CELL_SIZE; emptySlotNum++) {
            if (chunkSlotMap.containsKey(emptySlotNum) && chunkSlotMap.get(emptySlotNum).size() > 0) {
                for (Integer chunkID : chunkSlotMap.get(emptySlotNum)) {
                    return chunkID;
                }
            }
        }
        return -1;
    }

    /*
     * sourceCount means how many slot needed for a sourceID
     * sourceChunk means which new chunk this source should be assigned to
     */
    @SuppressWarnings("unchecked")
    public static boolean canHost(int oldChunk, HashMap<Integer, Integer> sourceCount, HashMap<Integer, Integer> sourceChunk) {
        boolean canHost = true;
        HashMap<Integer, HashSet<Integer>> tmpSlotMap = cloneMap();

        for (Integer sourceID : sourceCount.keySet()) {
            int slotNum = sourceCount.get(sourceID);
            boolean find = false;
            for (Integer emptySlotNum : tmpSlotMap.keySet()) {
                if (emptySlotNum >= slotNum && tmpSlotMap.get(emptySlotNum).size() > 0) {
                    for (Integer chunkID : tmpSlotMap.get(emptySlotNum)) {
                        if (chunkID != oldChunk) {
                            tmpSlotMap.get(emptySlotNum).remove(chunkID);
                            sourceChunk.put(sourceID, chunkID);
                            if (!tmpSlotMap.containsKey(emptySlotNum - slotNum)) {
                                tmpSlotMap.put(emptySlotNum - slotNum, new HashSet<Integer>());
                            }
                            find = true;
                            break;
                        }
                    }
                }
                if (find) {
                    break;
                }
            }

            if (!find) {
                canHost = false;
                break;
            }
        }

        return canHost;
    }

    public static HashMap<Integer, HashSet<Integer>> getChunkSlotMap() {
        return chunkSlotMap;
    }

    public static HashSet<Integer> getChunks(int emptySlotNum) {
        return chunkSlotMap.get(emptySlotNum);
    }

    public static HashMap<Integer, Integer> getChunkSlots() {
        HashMap<Integer, Integer> slotNumMap = new HashMap<Integer, Integer>();
        for (Integer emptySlotNum : chunkSlotMap.keySet()) {
            for (Integer chunkID : chunkSlotMap.get(emptySlotNum)) {
                slotNumMap.put(chunkID, emptySlotNum);
            }
        }
        return slotNumMap;
    }

    public static void print() {
        for (Integer emptySlotNum : chunkSlotMap.keySet()) {
            HashSet<Integer> chunks = getChunks(emptySlotNum);
            if (chunks != null && chunks.size() > 0) {
                System.out.print(emptySlotNum + " ( size " + chunks.size() + ") : ");
                for (Integer chunkID : chunks) {
                    System.out.print(" " + chunkID);
                }
                System.out.println();
            }
        }
    }

    public static int getChunkNum() {
        int num = 0;
        for (Integer emptySlotNum : chunkSlotMap.keySet()) {
            num += getChunks(emptySlotNum).size();
        }
        return num;
    }
}
