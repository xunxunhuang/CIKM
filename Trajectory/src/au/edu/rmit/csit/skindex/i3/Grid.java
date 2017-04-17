package au.edu.rmit.csit.skindex.i3;

/**
 * GridCell is used as an approximate representation of data's
 * spatial information. It has one parameter to set the granularity.
 *
 * @author Workshop
 */
public class Grid {
    public static int _N = Config.N;

    public static int getCellId(double x, double y, Region region) {
        double x_seg = (region.max_x - region.min_x) / _N;
        double y_seg = (region.max_y - region.min_y) / _N;

        int xIdx = (int) ((x - region.min_x) / x_seg);
        int yIdx = (int) ((y - region.min_y) / y_seg);

        yIdx = (yIdx == _N ? yIdx - 1 : yIdx);
        xIdx = (xIdx == _N ? xIdx - 1 : xIdx);

        //System.out.println("    "+xIdx+" "+yIdx);
        return yIdx * _N + xIdx;
    }


    public static Region getCellRegion(int id, Region region) {
        double x_seg = (region.max_x - region.min_x) / _N;
        double y_seg = (region.max_y - region.min_y) / _N;

        int xIdx = (int) (id % _N);
        int yIdx = (int) (id / _N);

        double min_x = region.min_x + xIdx * x_seg;
        double max_x = region.min_x + (xIdx + 1) * x_seg;
        double min_y = region.min_y + yIdx * y_seg;
        double max_y = region.min_y + (yIdx + 1) * y_seg;

        return new Region(min_x, max_x, min_y, max_y);
    }

}
