package au.edu.rmit.csit.skindex.i3;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Vector;


/**
 * A CellChunk contains all the related document information in a grid cell.
 * We can consider it as a vector of CellData. The number of CellChunk in
 * the index file is fixed as N*N. The number of items in a chunk is als
 * fixed as _LIMIT.
 * <p>
 * TODO: In the disk based version, we may need to maintain a bitmap to see
 * which slots are empty.
 *
 * @author Workshop
 */
public class CellChunk implements Serializable {
    private static final long serialVersionUID = 1238218723L;
    public static int SIZE = CellData.DATA_SIZE * Config.CELL_SIZE;

    private CellData[] slots;
    private int nextEmptySlot;

    public CellChunk() {
        slots = new CellData[Config.CELL_SIZE];
        for (int slotID = 0; slotID < Config.CELL_SIZE; slotID++) {
            slots[slotID] = null;
        }
        updateEmptySlot();
    }

    public void addItem(CellData data) {
        slots[nextEmptySlot] = data;
        updateEmptySlot();
    }

    public void deleteItem(int slotID) {
        slots[slotID] = null;
        updateEmptySlot();
    }

    public void clear() {
        for (int i = 0; i < slots.length; i++) {
            slots[i] = null;
        }
        updateEmptySlot();
    }

    public boolean full() {
        return nextEmptySlot == -1;
    }


    public int getEmptySlotNum() {
        int num = 0;
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == null) {
                num++;
            }
        }
        return num;
    }

    public Vector<CellData> fetchAllData() {
        Vector<CellData> alldata = new Vector<CellData>();
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] != null) {
                alldata.add(slots[i]);
            }
        }
        return alldata;
    }

    public Vector<CellData> fetchData(int kwd) {
        Vector<CellData> alldata = new Vector<CellData>();
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] != null && slots[i].kwd == kwd) {
                alldata.add(slots[i]);
            }
        }
        return alldata;
    }

    public void deleteKwd(int kwd) {
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] != null && slots[i].kwd == kwd) {
                slots[i] = null;
            }
        }
        updateEmptySlot();
    }

    public boolean sameKeyword() {
        int kwd = -1;
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] != null) {
                if (kwd == -1) {
                    kwd = slots[i].kwd;
                } else {
                    if (kwd != slots[i].kwd) {
                        return false;
                    }
                }
            }

        }
        return true;
    }

    public void readByteBuffer(ByteBuffer buf) throws IOException {
        for (int cellID = 0; cellID < Config.CELL_SIZE; cellID++) {
            CellData data = new CellData();
            data.readByteBuffer(buf);

            if (data.docID != -1) {
                slots[cellID] = data;
            } else {
                slots[cellID] = null;
            }
        }
        updateEmptySlot();
    }

    private void updateEmptySlot() {
        nextEmptySlot = -1;
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == null) {
                nextEmptySlot = i;
                break;
            }
        }
    }

    public void writeByteBuffer(ByteBuffer buf) throws IOException {
        for (int i = 0; i < Config.CELL_SIZE; i++) {
            CellData data = slots[i];
            if (data == null) {
                data = new CellData();
            }
            data.writeByteBuffer(buf);
        }
    }

    @Override
    public String toString() {
        String str = "";
        str += "---------------------------------------\n";
        for (int i = 0; i < Config.CELL_SIZE; i++) {
            if (slots[i] != null) {
                str += slots[i] + "\n";
            }
        }
        str += "---------------------------------------\n";
        return str;
    }
}
