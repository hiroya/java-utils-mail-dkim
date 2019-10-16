package net.markenwerk.utils.mail.dkim;

import java.io.*;
import java.nio.*;
import java.util.*;

import com.sun.mail.iap.ByteArray;
import javax.mail.*;
import javax.mail.internet.*;
import static org.junit.Assert.assertArrayEquals;
import org.junit.Test;

public class SpeedTest {

    private enum Cannon {
        simple(Canonicalization.SIMPLE),
        relaxed(Canonicalization.RELAXED);

        Canonicalization cannon;
        Cannon(Canonicalization cannon) {
            this.cannon = cannon;
        }
    }

    private enum Algo {
        sha256(SigningAlgorithm.SHA256_WITH_RSA),
        sha1(SigningAlgorithm.SHA1_WITH_RSA);

        SigningAlgorithm algo;
        Algo(SigningAlgorithm algo) {
            this.algo = algo;
        }
    }

	@Test
	public void test() throws Exception {
        String body = Utils.randomString(100000);
        for (Cannon c : Cannon.values()) {
            for (Algo a : Algo.values()) {
                for (int i = 0; i < 1000; i++) {
                    DkimSigner signer = mkSigner(c.cannon, a.algo);
                    writeMsg(signer, body);
                }
            }
        }
	}

	private static DkimSigner mkSigner(Canonicalization canonicalization, SigningAlgorithm algorithm) throws Exception {
		// signer
		DkimSigner signer = new DkimSigner("example.com", "dkim1", new File("private_key.pk8"));
		signer.setHeaderCanonicalization(canonicalization);
		signer.setBodyCanonicalization(canonicalization);
		signer.setLengthParam(true);
		signer.setSigningAlgorithm(algorithm);
		signer.setZParam(false);
		signer.setCheckDomainKey(false);

		return signer;
	}

	private static void writeMsg(DkimSigner signer, String body) throws Exception {
		// Session
		Properties properties=new Properties();
		properties.setProperty("mail.smtp.host", "exapmle.com");
		properties.setProperty("mail.from", "foo@exapmle.com");
		properties.setProperty("mail.smtp.from", "exapmle.com");
		Session session=Session.getDefaultInstance(properties);
		// Message
		MimeMessage message = new MimeMessage(session) {
            @Override // bind "Message-ID"
            protected void updateMessageID() throws MessagingException {
                super.updateMessageID();
                String msgId = getHeader("Message-ID")[0];
                String addr = msgId.substring(1, msgId.length() - 1);
                int i = addr.lastIndexOf('@');
                this.setHeader("Message-ID", "<msgid"+addr.substring(i)+">");
            }
        };
        message.setSentDate(new Date((long)1e9)); // to bind "t" parameter, set constant date as "Signature Timestamp"
		message.setRecipient(Message.RecipientType.TO, new InternetAddress("test@exapmle.com"));
		message.setSubject("Title");
		message.setFrom("support@example.com");
		message.setText(body, "US-ASCII", "plain");
		message.setHeader("Content-Transfer-Encoding", "7bit");
		message.setHeader("Content-Type", "text/plain; charset=\"US-ASCII\"");
		message.saveChanges();

        signer.writeTo(message, new FakeOutputStream());
	}

	static class FakeOutputStream extends  OutputStream {
        @Override
        public void write(int b) throws IOException {
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
        }
    }
}
