package io.netty.buffer;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import org.junit.Assert;
import org.junit.Test;


public final class VByteBufTest extends RandomizedTest {

    private static void testExactIntUsage(final int minInt, final int maxInt, final int expectedBytes) {
        final ByteBuf vBuf = VByteBuf.wrap(Unpooled.buffer());

        final int number = randomIntBetween(minInt, maxInt);

        vBuf.writeInt(number);

        Assert.assertEquals(
                "should have only written " + expectedBytes + " byte(s)",
                expectedBytes, vBuf.readableBytes());
        Assert.assertEquals(
                "should have been able to read complete int",
                number, vBuf.readInt());

        vBuf.clear();
        vBuf.setInt(0, number);

        Assert.assertEquals(
                "should have been able to read complete int",
                number, vBuf.getInt(0));
    }

    private static void testExactLongUsage(final long minLong, final long maxLong, final int expectedBytes) {
        final ByteBuf vBuf = VByteBuf.wrap(Unpooled.buffer());

        final long number = randomIntBetween((int) minLong, (int) maxLong);

        vBuf.writeLong(number);

        Assert.assertEquals(
                "should have only written " + expectedBytes + " byte(s)",
                expectedBytes, vBuf.readableBytes());
        Assert.assertEquals(
                "should have been able to read complete int",
                number, vBuf.readLong());

        vBuf.clear();
        vBuf.setLong(0, number);

        Assert.assertEquals(
                "should have been able to read complete int",
                number, vBuf.getLong(0));
    }

    private static void testShiftedLongUsage(final long minLong, final long maxLong, final int expectedBytes) {
        final ByteBuf vBuf = VByteBuf.wrap(Unpooled.buffer());

        final int minInt = (int) (minLong >>> 32);
        final int maxInt = (int) (maxLong >>> 32);

        final long shortNumber = randomIntBetween(minInt, maxInt);
        final long number = shortNumber << 32L | Integer.MAX_VALUE;

        vBuf.writeLong(number);

        Assert.assertEquals(
                "should have only written " + expectedBytes + " byte(s)",
                expectedBytes, vBuf.readableBytes());
        Assert.assertEquals(
                "should have been able to read complete int",
                number, vBuf.readLong());

        vBuf.clear();
        vBuf.setLong(0, number);

        Assert.assertEquals(
                "should have been able to read complete int",
                number, vBuf.getLong(0));
    }

    @Test
    @Repeat(iterations = 100)
    public void useOneByteFor7BitInts() {
        testExactIntUsage(0, 127, 1);
    }

    @Test
    @Repeat(iterations = 100)
    public void useTwoBytesFor14BitInts() {
        testExactIntUsage(128, 16383, 2);
    }

    @Test
    @Repeat(iterations = 100)
    public void useThreeBytesFor21BitInts() {
        testExactIntUsage(16384, 2097151, 3);
    }

    @Test
    @Repeat(iterations = 100)
    public void useFourBytesFor28BitInts() {
        testExactIntUsage(2097152, 268435455, 4);
    }

    @Test
    @Repeat(iterations = 100)
    public void useFiveBytesFor32BitInts() {
        testExactIntUsage(268435456, Integer.MAX_VALUE, 5);
    }


    @Test
    @Repeat(iterations = 100)
    public void useOneByteFor7BitLongs() {
        testExactLongUsage(0L, 127L, 1);
    }

    @Test
    @Repeat(iterations = 100)
    public void useTwoBytesFor14BitLongs() {
        testExactLongUsage(128L, 16383L, 2);
    }

    @Test
    @Repeat(iterations = 100)
    public void useThreeBytesFor21BitLongs() {
        testExactLongUsage(16384L, 2097151L, 3);
    }

    @Test
    @Repeat(iterations = 100)
    public void useFourBytesFor28BitLongs() {
        testExactLongUsage(2097152L, 268435455L, 4);
    }

    @Test
    @Repeat(iterations = 100)
    public void useFiveBytesFor35BitLongs() {
        testShiftedLongUsage(268435456L, 34359738367L, 5);
    }

    @Test
    @Repeat(iterations = 100)
    public void useSixBytesFor42BitLongs() {
        testShiftedLongUsage(34359738368L, 4398046511103L, 6);
    }

    @Test
    @Repeat(iterations = 100)
    public void useSevenBytesFor49BitLongs() {
        testShiftedLongUsage(4398046511104L, 562949953421311L, 7);
    }

    @Test
    @Repeat(iterations = 100)
    public void useEightBytesFor56BitLongs() {
        testShiftedLongUsage(562949953421312L, 72057594037927935L, 8);
    }

    @Test
    @Repeat(iterations = 100)
    public void useNineBytesFor63BitLongs() {
        testShiftedLongUsage(72057594037927936L, Long.MAX_VALUE, 9);
    }
}
