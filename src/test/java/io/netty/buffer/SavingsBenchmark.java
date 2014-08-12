package io.netty.buffer;


public final class SavingsBenchmark  {

    private static class Savings {
        final double memory;
        final double time;

        private Savings(final double memory, final double time) {
            this.memory = memory;
            this.time = time;
        }

        private double getMemory() {
            return 100 * Math.abs(memory);
        }

        private double getTime() {
            return 100 * Math.abs(time);
        }

        private static String getQuantifier(final double value) {
            return value >= 0 ? "less" : "more";
        }

        @Override
        public String toString() {
            return String.format("used %.2f%% %s memory", getMemory(), getQuantifier(memory)) +
                   String.format(" and %.2f%% %s time", getTime(), getQuantifier(time));
        }

        static Savings of(final BufferResult first, final BufferResult second) {
            final double memory = 1 - (double) first.memory / (double) second.memory;
            final double time = 1 - (double) first.time / (double) second.time;
            return new Savings(memory, time);
        }
    }

    private static class BufferResult {
        final long time;
        final int memory;

        private BufferResult(final long time, final int memory) {
            this.time = time;
            this.memory = memory;
        }
    }

    private static BufferResult makeConsumptionResult(final long start, final ByteBuf buf) {
        final long time = System.nanoTime() - start;
        final int memory = buf.readableBytes();

        buf.clear();
        buf.discardReadBytes();
        buf.release();

        return new BufferResult(time, memory);
    }

    private static BufferResult testBufferConsumption(final int n, final ByteBuf buf) {
        final long start = System.nanoTime();
        for (int i = 0; i < n; i++) {
            buf.writeLong((long) i);
        }
        return makeConsumptionResult(start, buf);
    }

    private static BufferResult testBufferConsumption(final int n, final long value, final ByteBuf buf) {
        final long start = System.nanoTime();
        for (int i = 0; i < n; i++) {
            buf.writeLong(value);
        }
        return makeConsumptionResult(start, buf);
    }

    private static ByteBuf regular() {
        return Unpooled.buffer();
    }
    private static ByteBuf regularSized(final int size) {
        return Unpooled.buffer(size * Long.SIZE);
    }
    private static ByteBuf variable() {
        return VByteBuf.wrap(regular());
    }
    private static ByteBuf variableSized(final int size) {
        return VByteBuf.wrap(regularSized(size));
    }

    public static void showSavings() {
        final int n = 100000000;

        System.out.println("     writing 7-bit long with resizing " + Savings.of(
                testBufferConsumption(n, 64, variable()),
                testBufferConsumption(n, 64, regular())));

        System.out.println("   writing growing long with resizing " + Savings.of(
                testBufferConsumption(n, variable()),
                testBufferConsumption(n, regular())));


        System.out.println("  writing 7-bit long without resizing " + Savings.of(
                testBufferConsumption(n, 64, variableSized(n)),
                testBufferConsumption(n, 64, regularSized(n))));

        System.out.println("writing growing long without resizing " + Savings.of(
                testBufferConsumption(n, variableSized(n)),
                testBufferConsumption(n, regularSized(n))));
    }

    public static void main(final String... args) {
        showSavings();
    }
}
