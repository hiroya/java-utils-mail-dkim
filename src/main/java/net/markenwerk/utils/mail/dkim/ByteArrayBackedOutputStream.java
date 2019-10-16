package net.markenwerk.utils.mail.dkim;

import java.io.*;

/**
 * Simple byte array {@link OutputStream}<br>
 * {@link ByteArrayOutputStream} isn't desirable in the performance critical code.<br>
 * {@link ByteArrayOutputStream#toByteArray} creates array copy.<br>
 * {@link ByteArrayOutputStream}'s methods are synchronized.
 */
class ByteArrayBackedOutputStream extends OutputStream {
    private ByteArray bb;
    private int size = 0;

    ByteArrayBackedOutputStream() {
        this(1024);
    }
    ByteArrayBackedOutputStream(int capacity) {
        bb = new ByteArray(Math.max(capacity, 16));
    }

    private void enlarge(int target) {
        int cap = bb.capacity();
        while(cap < target) cap <<= 1;
        ByteArray nbb = new ByteArray(cap);
        nbb.write(bb);
        bb = nbb;
    }

    @Override
    public void write(int b) {
        if (size + 1 > bb.capacity()) enlarge(size + 1);
        bb.write((byte)b);
        size++;
    }

    @Override
    public void write(byte[] b, int off, int len) {
        if (size + len > bb.capacity()) enlarge(size + len);
        bb.write(b, off, len);
        size += len;
    }

    ByteArray result() {
        return bb;
    }
}
