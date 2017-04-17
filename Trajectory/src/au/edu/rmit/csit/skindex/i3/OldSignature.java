package au.edu.rmit.csit.skindex.i3;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;


public class OldSignature implements Serializable, Cloneable {
    private static final long serialVersionUID = 172368217L;
    public static int SIZE = 8;
    private long sig = 0L;

    public OldSignature() {
        sig = 0L;
    }

    public OldSignature(Long sid) {
        sig = sid;
    }

    public static void main(String[] args) {
        OldSignature sig = new OldSignature();
        sig.add(351);
        sig.add(255);
        sig.add(78);
        sig.add(3);
        System.out.println(sig);
        ByteBuffer buf = ByteBuffer.allocate(OldSignature.SIZE);
        try {
            sig.writeByteBuffer(buf);
            OldSignature newSig = new OldSignature();
            buf.rewind();
            newSig.readByteBuffer(buf);
            System.out.println(newSig);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setAll() {
        sig = ~0L;
    }

    public boolean contain(int id) {
        return (sig & (1L << id % 64)) != 0;
    }

    public void add(int id) {
        sig |= (1L << id % 64);
    }

    public void reset() {
        sig = 0L;
    }

    public boolean isIntersect(OldSignature other) {
        return (sig & other.sig) != 0L;
    }

    public void intersects(OldSignature other) {
        sig &= other.sig;
    }

    public OldSignature getIntersection(OldSignature other) {
        return new OldSignature(sig & other.sig);
    }

    @Override
    public Object clone() {
        return new OldSignature(sig);
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OldSignature other = (OldSignature) obj;

        return sig == other.sig;
    }

    public void readByteBuffer(ByteBuffer buf) throws IOException {
        sig = buf.getLong();
    }

    public void writeByteBuffer(ByteBuffer buf) throws IOException {
        buf.putLong(sig);
    }

    @Override
    public String toString() {
        String str = "";
        long value = 1;
        for (int i = 0; i < 64; i++) {
            if ((sig & value) != 0) {
                str += i + " ";
            }
            value <<= 1;
        }
        return str;
    }
}
