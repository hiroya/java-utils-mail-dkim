package net.markenwerk.utils.mail.dkim;

import java.io.OutputStream;
import java.util.Random;

import org.junit.Test;

public class SpeedTest {
	@Test
	public void test() throws Exception {
		String body = Utils.randomString(new Random(), 100000);
		for (Canonicalization c : Canonicalization.values()) {
			for (SigningAlgorithm a : SigningAlgorithm.values()) {
				for (int i = 0; i < 1000; i++) {
					DkimSigner signer = Utils.getSigner(c, a);
					DkimMessageTest.writeMessageTo(signer, body, false, new FakeOutputStream());
				}
			}
		}
	}

	static class FakeOutputStream extends OutputStream {
		@Override
		public void write(int b) {
		}

		@Override
		public void write(byte[] b, int off, int len) {
		}
	}
}
