package net.markenwerk.utils.mail.dkim;

import java.util.Arrays;

class CanonicalizationParser {
	private static final byte CR = (byte)'\r';
	private static final byte LF = (byte)'\n';
	private static final byte SP = (byte)' ';
	private static final byte HT = (byte)'\t';

	enum State {
		Default,
		Space,  // found space sequence. this state is used if reduceSp is true
		CR      // found '\r'
	}

	private State s = State.Default;
	private ByteArrayBackedOutputStream out;

	private static byte[] CRLF = {CR, LF};

	private boolean reduceSp; // a flag whether reduces space sequence to single SP.
	private boolean removeLineEndReducedSp; // a flag whether ignore white spaces at line end.
	private boolean addCRLFIfEmpty; // a flag whether completely empty body is canonicalized as CRLF.

	CanonicalizationParser withReduceSp(boolean reduceSp) {
		this.reduceSp = reduceSp;
		return this;
	}

	CanonicalizationParser withRemoveLineEndReducedSp(boolean removeLineEndReducedSp) {
		this.removeLineEndReducedSp = removeLineEndReducedSp;
		return this;
	}

	CanonicalizationParser withAddCRLFIfEmpty(boolean addCRLFIfEmpty) {
		this.addCRLFIfEmpty = addCRLFIfEmpty;
		return this;
	}

	ByteArray parse(ByteArray origin) {
		out = new ByteArrayBackedOutputStream((int)(origin.length() * 1.2));
		for (int i = 0; i < origin.length(); i++) {
			byte b = origin.at(i);
			accept(b);
		}
		accept(LF); // terminate with CRLF

		ByteArray res = out.result();
		removeEmptyLinesAtTheEnd(res);
		if (addCRLFIfEmpty && res.length() == 0) {
			return new ByteArray(Arrays.copyOf(CRLF, CRLF.length));
		} else {
			return res;
		}
	}

	private void removeEmptyLinesAtTheEnd(ByteArray ba) {
		// ignores all empty lines at the end of the message body.
		while (ba.endsWith(CRLF) && (ba.length() == 2 || ba.at(ba.length() - 3) == LF)) {
			ba.dropRight(2);
		}
	}

	private void accept(byte b) {
		s = _accept(b);
	}

	private State _accept(byte b) {
		switch (b) {
			case CR:
				return cr();
			case LF:
				return lf();
			case SP:
			case HT:
				return sp(b);
			default:
				return chars(b);
		}
	}

	private State cr() {
		switch (s) {
			case Default:
				break;
			case CR:
				writeLn();
				break;
			case Space:
				if (!removeLineEndReducedSp) write(SP);
				break;
		}

		return State.CR;
	}

	private State lf() {
		switch (s) {
			case Default:
				writeLn();
				break;
			case CR:
				writeLn();
				break; // \r\n
			case Space:
				if (!removeLineEndReducedSp) write(SP);
				writeLn();
				break;
		}

		return State.Default;
	}

	private State sp(byte b) {
		switch (s) {
			case Default:
				break;
			case CR:
				writeLn();
				break;
			case Space:
				break;
		}
		if (reduceSp) {
			return State.Space;
		} else {
			write(b);
			return State.Default;
		}
	}

	private State chars(byte b) {
		switch (s) {
			case Default:
				write(b);
				break;
			case CR:
				writeLn();
				write(b);
				break;
			case Space:
				write(SP);
				write(b);
				break; // write single SP
		}

		return State.Default;
	}

	private void write(byte b) {
		out.write(b);
	}

	private void writeLn() {
		out.write(CRLF, 0, 2);
	}
}
