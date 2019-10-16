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

    byte at(int i) { return bytes[i]; }

    void write(ByteArray ba) {
        write(ba.bytes, 0, ba.p);
    }

    int capacity() {
        return bytes.length;
    }

    int length() { return p; }

    void dropRight(int cnt) { p -= cnt; }

    ByteBuffer toByteBuffer() { return ByteBuffer.wrap(bytes, 0, p); }

    void writeTo(OutputStream out) throws IOException {
        out.write(bytes, 0, p);
    }

    byte last() { return bytes[p - 1]; }

    boolean endsWith(byte[] b) {
        if (b.length > p) return false;
        int s = p - b.length;
        for (int i = 0; i < b.length; i++) {
            if (b[i] != bytes[s + i]) return false;
        }
        return true;
    }

    boolean equals(byte[] b) {
        return b.length == p && endsWith(b);
    }

    @Override
    public String toString() {
        return new String(bytes, 0, p);
    }
}
