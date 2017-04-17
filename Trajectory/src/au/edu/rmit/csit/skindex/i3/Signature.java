package au.edu.rmit.csit.skindex.i3;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.BitSet;


public class Signature implements Serializable, Cloneable {
    private static final long serialVersionUID = 172368217L;
    public static int SIZE = Config.BIT_LEN / 8 + 1;
    private BitSet sig;

    public Signature() {
        sig = new BitSet(Config.BIT_LEN);
    }

    public Signature(BitSet newSig) {
        sig = newSig;
    }

    public void setAll() {
        sig.set(0, Config.BIT_LEN, true);
    }

    public boolean contain(int id) {
        return sig.get(id % Config.BIT_LEN);
    }

    public void add(int id) {
        sig.set(id % Config.BIT_LEN);
    }

    public void reset() {
        sig.clear();
    }

    public boolean isIntersect(Signature other) {
        return sig.intersects(other.sig);
    }

    public void intersects(Signature other) {
        sig.and(other.sig);

    }

    public Signature getIntersection(Signature other) {
        BitSet newSig = (BitSet) sig.clone();
        newSig.and(other.sig);
        return new Signature(newSig);
    }

    @Override
    public Object clone() {
        return new Signature(sig);
    }


    @Override
    public boolean equals(Object obj) {
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Signature other = (Signature) obj;

        return sig == other.sig;
    }

    // Returns a bitset containing the values in bytes.
    // The byte-ordering of bytes must be big-endian which means the most significant bit is in element 0.
    public void readByteBuffer(ByteBuffer buf) {
        byte[] bytes = new byte[SIZE];
        buf.get(bytes);
        sig = new BitSet(Config.BIT_LEN);
        for (int i = 0; i < Config.BIT_LEN; i++) {
            if ((bytes[SIZE - i / 8 - 1] & (1 << (i % 8))) > 0) {
                sig.set(i);
            }
        }
    }

    // Returns a byte array of at least length 1.
    // The most significant bit in the result is guaranteed not to be a 1
    // (since BitSet does not support sign extension).
    // The byte-ordering of the result is big-endian which means the most significant bit is in element 0.
    // The bit at index 0 of the bit set is assumed to be the least significant bit.
    public void writeByteBuffer(ByteBuffer buf) {
        byte[] bytes = new byte[SIZE];
        for (int i = 0; i < sig.length(); i++) {
            if (sig.get(i)) {
                bytes[SIZE - i / 8 - 1] |= 1 << (i % 8);
            }
        }
        buf.put(bytes);
    }


    @Override
    public String toString() {
        return sig.toString();
    }


}
