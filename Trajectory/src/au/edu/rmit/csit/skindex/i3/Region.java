package au.edu.rmit.csit.skindex.i3;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;


public class Region implements Serializable {
    public static final int SIZE = 4 * 8;
    private static final long serialVersionUID = 8172388781L;
    public double min_x;
    public double max_x;
    public double min_y;
    public double max_y;

    public Region() {
        min_x = Double.MAX_VALUE;
        max_x = Double.MIN_VALUE;
        min_y = Double.MAX_VALUE;
        max_y = Double.MIN_VALUE;
    }

    public Region(double min_x, double max_x, double min_y, double max_y) {
        this.min_x = min_x;
        this.max_x = max_x;
        this.min_y = min_y;
        this.max_y = max_y;
    }

    public double getArea() {
        return (max_x - min_x) * (max_y - min_y);
    }

    public boolean intersect(Region other) {
        return (this.max_x >= other.min_x && this.min_x <= other.max_x) && (this.max_y >= other.min_y && this.min_y <= other.max_y);

    }

    public boolean contain(Region other) {
        return (this.max_x >= other.max_x && this.min_x <= other.min_x) && (this.max_y >= other.max_y && this.min_y <= other.min_y);
    }

    public Region getExpand(double radius) {
        return new Region(min_x - radius, max_x + radius, min_y - radius, max_y + radius);
    }

    public boolean contains(double x, double y) {
        return x >= min_x && x <= max_x && y >= min_y && y <= max_y;
    }

    public void add(double x, double y) {
        min_x = x < min_x ? x : min_x;
        max_x = x > max_x ? x : max_x;
        min_y = y < min_y ? y : min_y;
        max_y = y > max_y ? y : max_y;
    }

    public void readByteBuffer(ByteBuffer buf) throws IOException {
        min_x = buf.getDouble();
        max_x = buf.getDouble();
        min_y = buf.getDouble();
        max_y = buf.getDouble();
    }

    public void writeByteBuffer(ByteBuffer buf) throws IOException {
        buf.putDouble(min_x);
        buf.putDouble(max_x);
        buf.putDouble(min_y);
        buf.putDouble(max_y);
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Region other = (Region) obj;
        double epsilon = 0.00001;
        if (Math.abs(min_x - other.min_x) > epsilon) {
            return false;
        }
        if (Math.abs(max_x - other.max_x) > epsilon) {
            return false;
        }
        if (Math.abs(min_y - other.min_y) > epsilon) {
            return false;
        }
        if (Math.abs(max_y - other.max_y) > epsilon) {
            return false;
        }
        return true;
    }

    public String toString() {
        return "[" + min_x + "," + max_x + "] [" + min_y + "," + max_y + "]";
    }
}
