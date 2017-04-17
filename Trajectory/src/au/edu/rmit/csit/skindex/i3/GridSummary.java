package au.edu.rmit.csit.skindex.i3;

import java.io.IOException;
import java.nio.ByteBuffer;


/**
 * A Grid Summary contains N*N CellSummaries and these summaries are stored
 * sequentially in the disk file so that we can load a GridSummary into memory
 * each time.
 *
 * @author Administrator
 */
public class GridSummary {
    private static final int C_NUM = Config.CELL_NUM;
    public static final int SIZE = C_NUM * CellSummary.SIZE + Region.SIZE;
    private Region region;
    private CellSummary[] summaries;

    public GridSummary() {
        this.region = new Region();
        summaries = new CellSummary[C_NUM];
        for (int i = 0; i < C_NUM; i++) {
            summaries[i] = new CellSummary();
        }
    }

    public GridSummary(Region region) {
        this.region = region;
        summaries = new CellSummary[C_NUM];
        for (int i = 0; i < C_NUM; i++) {
            summaries[i] = new CellSummary();
        }
    }

    public CellSummary getCell(int cellID) {
        return summaries[cellID];
    }

    public Region getRegion() {
        return region;
    }


    public void readByteBuffer(ByteBuffer buf) throws IOException {
        region.readByteBuffer(buf);
        for (int i = 0; i < C_NUM; i++) {
            summaries[i].readByteBuffer(buf);
        }
    }

    public void writeByteBuffer(ByteBuffer buf) throws IOException {
        region.writeByteBuffer(buf);
        for (int i = 0; i < C_NUM; i++) {
            summaries[i].writeByteBuffer(buf);
        }
    }

    @Override
    public String toString() {
        String str = "\n" + region.toString() + "\n";
        for (int i = 0; i < C_NUM; i++) {
            str += summaries[i].toString();
        }
        return str;
    }
}
