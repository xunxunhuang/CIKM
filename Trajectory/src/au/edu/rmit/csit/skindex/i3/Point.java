package au.edu.rmit.csit.skindex.i3;


import java.io.Serializable;


public class Point implements Serializable {
    private static final long serialVersionUID = 136898217L;

    public double x;
    public double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public String toString() {
        return "[" + x + "," + y + "]";
    }

    public double dist(double x, double y) {
        return Math.sqrt((this.x - x) * (this.x - x) + (this.y - y) * (this.y - y));
    }

    public double getMinDistance(Region r) {
        double dist_x = 0, dist_y = 0;
        if (x < r.min_x) {
            dist_x = r.min_x - x;
        } else if (x > r.max_x) {
            dist_x = x - r.max_x;
        }

        if (y < r.min_y) {
            dist_y = r.min_y - y;
        } else if (y > r.max_y) {
            dist_y = y - r.max_y;
        }

        return Math.sqrt(dist_x * dist_x + dist_y * dist_y);
    }

    @Override
    public boolean equals(Object aThat) {
        if (this == aThat) return true;
        if (!(aThat instanceof Point)) return false;
        Point that = (Point) aThat;
        return this.x == that.x && this.y == that.y;
    }

    @Override
    public int hashCode() {
        return (int) (x + y);
    }
}
