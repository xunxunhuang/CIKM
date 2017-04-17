package au.edu.rmit.csit.skindex.i3;

public class Config {
    // How to split grid cell
    public static int N = 2;

    // Each grid cell is split into N*N smaller cells
    public static int CELL_NUM = N * N;

    // The number of data items stored in each cell
    public static int CELL_SIZE = 100;


    public static int BIT_LEN = 100;

    public static double MAX_DIST = 200;

    public static double ALPHA = 0.7;

}
