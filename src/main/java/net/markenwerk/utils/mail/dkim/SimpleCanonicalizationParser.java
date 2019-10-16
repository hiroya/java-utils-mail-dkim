package net.markenwerk.utils.mail.dkim;

class SimpleCanonicalizationParser {
    private static final byte CR = (byte)'\r';
    private static final byte LF = (byte)'\n';

    enum State {
        Default, CR
    }

    private State s = State.Default;
    private ByteArrayBackedOutputStream out;

    private static byte[] CRLF = {CR, LF};
    private static byte[] CRLFCRLF = {CR, LF, CR, LF};

    ByteArray parse(ByteArray origin) {
        out = new ByteArrayBackedOutputStream((int)(origin.length() * 1.2));
        for (int i = 0; i < origin.length(); i++) {
            byte b = origin.at(i);
            accept(b);
        }
        if (origin.length() == 0 || (origin.last() != CR && origin.last() != LF)) {
            accept(LF);
        }

        if (s == State.CR) writeLn();

        s = State.Default;

        ByteArray res = out.result();
        reduceEmptyLinesAtTheEnd(res);
        return res;
    }

    private void reduceEmptyLinesAtTheEnd(ByteArray ba) {
        while(ba.endsWith(CRLFCRLF)) {
            ba.dropRight(2);
        }
    }

    private void accept(byte b) {
        switch (s) {
            case Default:
                onDefault(b);
                return;

            case CR:
                onCR(b);
        }
    }

    private void onDefault(byte b) {
        switch(b) {
            case CR:
                s = State.CR;
                return;

            case LF:
                writeLn();
                s = State.Default;
                return;

            default:
                write(b);
                s = State.Default;
        }
    }

    private void onCR(byte b) {
        switch(b) {
            case CR:
                writeLn();
                s = State.CR;
                return;

            case LF:
                // found \r\n
                writeLn();
                s = State.Default;
                return;

            default:
                writeLn();
                write(b);
                s = State.Default;
        }
    }

    private void write(byte b) {
        out.write(b);
    }

    private void writeLn() {
        out.write(CRLF, 0, 2);
    }
}
