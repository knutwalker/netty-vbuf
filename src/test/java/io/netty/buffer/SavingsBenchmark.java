package io.netty.buffer;


import org.perf4j.LoggingStopWatch;
import org.perf4j.StopWatch;

public final class SavingsBenchmark {

  private static final class Savings {

    final double memory;

    final double time;

    private Savings(final double memory, final double time) {
      this.memory = memory;
      this.time = time;
    }

    private static String getQuantified(final double value) {
      if (value >= 0.0d) {
        return String.format("%.2f%% less", 100.0d * Math.abs(value));
      } else {
        return String.format("%.2f%% more", 100.0d * Math.abs(value + 1.0d));
      }
    }

    @Override
    public String toString() {
      return String.format("used %s memory and %s time", getQuantified(memory),
          getQuantified(time));
    }

    static Savings of(final BufferResult first, final BufferResult second) {
      final double memory = 1.0d - (double) first.memory / (double) second.memory;
      final double time = 1.0d - (double) first.time / (double) second.time;
      return new Savings(memory, time);
    }
  }

  private static class BufferResult {

    final long time;

    final int memory;

    private BufferResult(final StopWatch time, final int memory) {
      this.time = time.getElapsedTime();
      this.memory = memory;
    }
  }

  private static BufferResult makeConsumptionResult(final StopWatch stopwatch, final ByteBuf buf) {
    final int memory = buf.readableBytes();

    buf.clear();
    buf.discardReadBytes();
    buf.release();

    return new BufferResult(stopwatch, memory);
  }

  private static BufferResult testBufferConsumption(final int n, final ByteBuf buf) {
    final StopWatch stopWatch = new LoggingStopWatch();
    stopWatch.start();
    for (int i = 0; i < n; i++) {
      buf.writeLong((long) i);
    }
    stopWatch.stop("growing " + buf.getClass().getSimpleName());
    return makeConsumptionResult(stopWatch, buf);
  }

  private static BufferResult testBufferConsumption(final int n, final long value,
      final ByteBuf buf) {
    final StopWatch stopWatch = new LoggingStopWatch();
    stopWatch.start();
    for (int i = 0; i < n; i++) {
      buf.writeLong(value);
    }
    stopWatch.stop("7-bit " + buf.getClass().getSimpleName());
    return makeConsumptionResult(stopWatch, buf);
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

  public static void showSavings(final boolean warmup) {

    System.out.println(warmup ? "Just warmup... " : "Here we go!");

    final int n = 100000000;
    final long value = 64L;
    final Savings fixWithResizing = Savings.of(testBufferConsumption(n, value, variable()),
        testBufferConsumption(n, value, regular()));
    if (!warmup) {
      System.out.println("writing 7-bit long with resizing " + fixWithResizing);
    }

    final Savings growingWithResizing =
        Savings.of(testBufferConsumption(n, variable()), testBufferConsumption(n, regular()));
    if (!warmup) {
      System.out.println("writing growing long with resizing " + growingWithResizing);
    }

    final Savings fixWithoutResizing = Savings.of(testBufferConsumption(n, value, variableSized(n)),
        testBufferConsumption(n, value, regularSized(n)));
    if (!warmup) {
      System.out.println("writing 7-bit long without resizing " + fixWithoutResizing);
    }

    final Savings growingWithoutResizing = Savings.of(testBufferConsumption(n, variableSized(n)),
        testBufferConsumption(n, regularSized(n)));
    if (!warmup) {
      System.out.println("writing growing long without resizing " + growingWithoutResizing);
    }
  }

  public static void main(final String... args) {
    showSavings(true);
    showSavings(false);
  }
}
