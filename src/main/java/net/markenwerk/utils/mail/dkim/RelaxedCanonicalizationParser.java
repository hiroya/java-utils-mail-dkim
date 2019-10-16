package net.markenwerk.utils.mail.dkim;

class RelaxedCanonicalizationParser {
    private static final byte CR = (byte)'\r';
    private static final byte LF = (byte)'\n';
    private static final byte SP = (byte)' ';
    private static final byte HT = (byte)'\t';

    enum State {
        Default, Space, CR
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

        if (s == State.Space) write(SP);
        if (s == State.CR) writeLn();

        s = State.Default;

        ByteArray res = out.result();
        ignoreEmptyLinesAtTheEnd(res);
        return res;
    }

    private void ignoreEmptyLinesAtTheEnd(ByteArray ba) {
        while(ba.endsWith(CRLFCRLF)) {
            ba.dropRight(2);
        }
        if (ba.equals(CRLF)) ba.dropRight(2);
    }

    private void accept(byte b) {
        switch (s) {
            case Default:
                onDefault(b);
                return;

            case Space:
                onSpace(b);
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

            case SP: case HT:
                s = State.Space;
                return;

            default:
                write(b);
                s = State.Default;
        }
    }

    private void onSpace(byte b) {
        // ignore space before line end
        switch(b) {
            case CR:
                s = State.CR;
                return;

            case LF:
                writeLn();
                s = State.Default;
                return;

            case SP: case HT:
                // reduce multiple spaces
                s = State.Space;
                return;

            default:
                write(SP);
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

            case SP: case HT:
                writeLn();
                s = State.Space;
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
