package au.edu.rmit.csit.skindex.i3;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;


/**
 * CellData is data unit. It contains the following information:
 * sourceID  : refers to keyword id for infrequent keywords or global cellID for frequent
 * keyword. If it is infrequent keyword, sourceID = -kwdID; Else
 * sourceID = glocal_cellID = gridID*CellNum + local_cellID
 * docID     : the document id of the keyword
 * x         : x-coordinate of the doc location
 * y         : y-coordinate of the doc location
 * freq      : frequency of the keyword in the doc
 *
 * @author Workshop
 */
public class CellData implements Serializable, Cloneable {
    public static final int DATA_SIZE = 4 + 4 + 8 + 8 + 8;
    private static final long serialVersionUID = 12973127L;
    public int docID;
    public double x;
    public double y;
    public double freq;
    public int kwd;

    public CellData() {
        this.docID = -1;
        this.x = 0;
        this.y = 0;
        this.freq = 0;
        this.kwd = -1;
    }


    public CellData(int kwd, int docID, double x, double y, double freq) {
        this.kwd = kwd;
        this.docID = docID;
        this.x = x;
        this.y = y;
        this.freq = freq;
    }

    public void readByteBuffer(ByteBuffer buf) throws IOException {
        docID = buf.getInt();
        x = buf.getDouble();
        y = buf.getDouble();
        freq = buf.getDouble();
        kwd = buf.getInt();
    }

    public void writeByteBuffer(ByteBuffer buf) throws IOException {
        buf.putInt(docID);
        buf.putDouble(x);
        buf.putDouble(y);
        buf.putDouble(freq);
        buf.putInt(kwd);
    }

    @Override
    public int hashCode() {
        return docID;
    }


    @Override
    public boolean equals(Object obj) {
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CellData other = (CellData) obj;
        return docID == other.docID;
    }


    @Override
    public String toString() {
        return docID + "@[" + x + "," + y + "]\t" + freq + " for keyword " + kwd;
    }
}
