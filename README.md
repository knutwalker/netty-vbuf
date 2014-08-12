netty-vbuf
==========

Variable length encoding ByteBuf implementation.

[![Build Status](https://travis-ci.org/knutwalker/netty-vbuf.svg?branch=master)](https://travis-ci.org/knutwalker/netty-vbuf)
[![Coverage Status](https://img.shields.io/coveralls/knutwalker/netty-vbuf.svg)](https://coveralls.io/r/knutwalker/netty-vbuf)

Overview
--------

This is a `ByteBuf` implementation, that's using [variable length encoding](http://en.wikipedia.org/wiki/Variable-length_quantity) for `int`s and `long`s to save memory consumption and eventually cpu time.


## Get it

### from Maven Central

    <dependency>
        <groupId>de.knutwalker</groupId>
        <artifactId>netty-vbuf</artifactId>
        <version>0.1.3</version>
    </dependency>


### from release binary

Download the [netty-vbuf-0.1.3.jar](https://github.com/knutwalker/netty-vbuf/releases/download/v0.1.3/netty-vbuf-0.1.3.jar) of the [latest release](https://github.com/knutwalker/netty-vbuf/releases/tag/v0.1.3)
and place it in your classpath.

### from source

Clone this repo and run `mvn package -DskipTests` and then add the `target/netty-vbuf-0.1.4-SNAPSHOT.jar` jar to your classpath.


## Use it

```java
import io.netty.buffer.VByteBuf;
...
ByteBuf vBuf = VByteBuf.wrap(existingByteBuf);
```

The methods `set`, `get`, `read`, and `write` for both, `Int` and `Long` are reimplemented, all others delegate to the wrapped buffer.


## Profit

Variable length encoding used the highest bit of each byte to encode whether the next byte
 belongs to the current number. This allows for an int to be encoded in 1 to 5 bytes.
If you're encoding primarily small numbers, using a variable length encoding can drastically save memory consumption.

These are some examples for the different encodings


int        | regular encoding                    | variable length encoding                     | memory savings
----------:|-------------------------------------|:---------------------------------------------|----------------
42         | 00000000 00000000 00000000 00101010 | 00101010                                     | 75%
1337       | 00000000 00000000 00000101 00111001 | 00001010 10111001                            | 50%
134217728  | 00001000 00000000 00000000 00000000 | 01000000 10000000 10000000 10000000          | 0%
2147483647 | 01111111 11111111 11111111 11111111 | 00000111 11111111 11111111 11111111 11111111 | -25% (loss)


If you write many small numbers to a ByteBuf, that isn't over sized and needs to resize once in a while,
using variable length encoding can reduce the number of costly memory copy operations and therefore reduce used cpu time.

Otherwise, the encoding adds a runtime overhead, mostly due to the repeated boundary checking for each byte, where the regular encoding would just check once for all bytes.

These are some possible savings, when writing 100M (continuously increasing) longs:

         writing 7-bit long with resizing used 87.50% less memory and 95.14% less time
       writing growing long with resizing used 50.26% less memory and 69.69% less time
      writing 7-bit long without resizing used 87.50% less memory and 31.83% less time
    writing growing long without resizing used 50.26% less memory and 242.82% more time

These are just `nanoTime`s, so no real, useful benchmarks, but they give a hint in the right direction.
