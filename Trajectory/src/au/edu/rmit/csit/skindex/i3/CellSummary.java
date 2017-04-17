package au.edu.rmit.csit.skindex.i3;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * CellSummary contains summary information for all the data points in a grid cell.
 * It is used for pruning in the query processing.
 *
 * @author Administrator
 */
public class CellSummary implements Serializable {
    public static final int SIZE = 4 + 4 + 8 + Signature.SIZE;
    static final long serialVersionUID = 123123800L;
    private int gridID;                // If the cell is dense, it points to the next GridSummary in the headFile
    private int chunkID;            // The chunk id of the cell contents really stored.
    private double kwdfreq;            // The maximum weight of keyword  in this cell
    private Signature signature;    // Signature of all the doc id in this cell


    public CellSummary() {
        this.gridID = -1;
        this.chunkID = -1;
        kwdfreq = 0;
        signature = new Signature();
    }


    public void addItem(int docID, double freq) {
        if (freq > kwdfreq) {
            kwdfreq = freq;
        }
        signature.add(docID);
    }

    public void deleteItem(int freq, Signature newSig, Region newRegion) {
        kwdfreq -= freq;
        signature = newSig;
    }

    public void reset() {
        kwdfreq = 0;
        signature = new Signature();
    }


    public boolean isDense() {
        // If the cell id dense, its grid id will point to the next GridSummary in the next level
        return gridID != -1;
    }

    public int getGridID() {
        return gridID;
    }

    public void setGridID(int gridID) {
        this.gridID = gridID;
    }

    public int getChunkID() {
        return chunkID;
    }

    public void setChunkID(int chunkID) {
        this.chunkID = chunkID;
    }

    public Signature getSignature() {
        return signature;
    }

    public void setSignature(Signature sig) {
        signature = sig;
    }

    public double getKwdFreq() {
        return kwdfreq;
    }

    public void setKwdFreq(double freq) {
        kwdfreq = freq;
    }

    public void readByteBuffer(ByteBuffer buf) throws IOException {
        gridID = buf.getInt();
        chunkID = buf.getInt();
        kwdfreq = buf.getDouble();
        signature = new Signature();
        signature.readByteBuffer(buf);
    }

    public void writeByteBuffer(ByteBuffer buf) throws IOException {
        buf.putInt(gridID);
        buf.putInt(chunkID);
        buf.putDouble(kwdfreq);
        signature.writeByteBuffer(buf);
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CellSummary other = (CellSummary) obj;

        if (gridID != other.gridID) {
            return false;
        }
        if (chunkID != other.chunkID) {
            return false;
        }
        if (kwdfreq != other.kwdfreq) {
            return false;
        }

        if (!signature.equals(signature)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        String str = "gridID : " + gridID + "\t ";
        str += "chunkID : " + chunkID + "\t ";
        str += "kwdfreq : " + kwdfreq + "\t ";
        str += "signature : " + signature + "\n";
        return str;
    }

	/*
	public static void main(String[] args)
	{
		BitSet slots = new BitSet(Config.CELL_SIZE);
		for(int i=0;i < Config.CELL_SIZE;i++){
			slots.set(i,true);
		}
		System.out.println(slots.nextClearBit(0));
	}
	*/

}