package net.markenwerk.utils.mail.dkim;

import java.io.*;
import java.nio.*;

/**
 * Simple byte array representation.
 */
class ByteArray {

    private byte[] bytes;
    private int p = 0;
    ByteArray(int capacity) {
        bytes = new byte[capacity];
    }
    ByteArray(byte[] origin) {
        bytes = origin;
        p = origin.length;
    }

    void write(byte b) {
        bytes[p++] = b;
    }

    void write(byte[] b, int off, int len) {
        System.arraycopy(b, off, bytes, p, len);
        p += len;
    }

    void write(ByteArray ba) {
        write(ba.bytes, 0, ba.p);
    }

    int capacity() {
        return bytes.length;
    }

    void writeTo(OutputStream out) throws IOException {
        out.write(bytes, 0, p);
    }

    @Override
    public String toString() {
        return new String(bytes, 0, p);
    }
}
