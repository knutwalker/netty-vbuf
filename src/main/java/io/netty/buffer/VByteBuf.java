package io.netty.buffer;

/**
 * A variable-width ByteBuf, that used a variable amount of bytes to store an int or a long.
 *
 * Integers take between 1 and 5 bytes.
 * Longs take between 1 and 9 Bytes.
 */
@SuppressWarnings("UnusedDeclaration")
public final class VByteBuf extends WrappedByteBuf {

    private static final int HIGH_BIT_1 = 0x7F;
    private static final long L_HIGH_BIT_1 = 0x7FL;
    private static final int LOW_BIT_2 = 0x80;
    private static final long L_LOW_BIT_2 = 0x80L;
    private static final int BITS_PER_BYTE = 7;

    VByteBuf(final ByteBuf buf) {
        super(buf);
    }

    @Override
    public ByteBuf setInt(final int index, final int value) {
        int i = value;
        int idx = index;
        while ((i & ~HIGH_BIT_1) != 0) {
            setByte(idx++, i & HIGH_BIT_1 | LOW_BIT_2);
            i >>>= BITS_PER_BYTE;
        }
        setByte(idx, i);
        return this;
    }

    @Override
    public ByteBuf writeInt(final int value) {
        int i = value;
        while ((i & ~HIGH_BIT_1) != 0) {
            writeByte(i & HIGH_BIT_1 | LOW_BIT_2);
            i >>>= BITS_PER_BYTE;
        }
        writeByte(i);
        return this;
    }

    @Override
    public ByteBuf setLong(final int index, final long value) {
        assert value >= 0L;
        long i = value;
        int idx = index;
        while ((i & ~L_HIGH_BIT_1) != 0L) {
            setByte(idx++, (byte) (i & L_HIGH_BIT_1 | L_LOW_BIT_2));
            i >>>= BITS_PER_BYTE;
        }
        setByte(idx, (int) i);
        return this;
    }

    @Override
    public ByteBuf writeLong(final long value) {
        assert value >= 0L;
        long i = value;
        while ((i & ~L_HIGH_BIT_1) != 0L) {
            writeByte((byte)(i & L_HIGH_BIT_1 | L_LOW_BIT_2));
            i >>>= BITS_PER_BYTE;
        }
        writeByte((int) i);
        return this;
    }

    @Override
    public int getInt(final int index) {
        int idx = index;
        byte b = getByte(idx);
        int i = b & HIGH_BIT_1;
        int shift = BITS_PER_BYTE;
        while ((b & LOW_BIT_2) != 0) {
            b = getByte(++idx);
            i |= (b & HIGH_BIT_1) << shift;
            shift += BITS_PER_BYTE;
        }
        return i;
    }

    @Override
    public int readInt() {
        byte b = readByte();
        int i = b & HIGH_BIT_1;
        int shift = BITS_PER_BYTE;
        while ((b & LOW_BIT_2) != 0) {
            b = readByte();
            i |= (b & HIGH_BIT_1) << shift;
            shift += BITS_PER_BYTE;
        }
        return i;
    }

    @Override
    public long getLong(final int index) {
        int idx = index;
        byte b = getByte(idx);
        long i = b & HIGH_BIT_1;
        int shift = BITS_PER_BYTE;
        while ((b & LOW_BIT_2) != 0) {
            b = getByte(++idx);
            i |= (b & L_HIGH_BIT_1) << shift;
            shift += BITS_PER_BYTE;
        }
        return i;
    }

    @Override
    public long readLong() {
        byte b = readByte();
        long i = b & HIGH_BIT_1;
        int shift = BITS_PER_BYTE;
        while ((b & LOW_BIT_2) != 0) {
            b = readByte();
            i |= (b & L_HIGH_BIT_1) << shift;
            shift += BITS_PER_BYTE;
        }
        return i;
    }

    public static ByteBuf wrap(final ByteBuf buf) {
        return new VByteBuf(buf);
    }
}
