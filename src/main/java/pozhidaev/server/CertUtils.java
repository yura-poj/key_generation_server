package pozhidaev.server;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import javax.security.auth.x500.X500Principal;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Date;

public class CertUtils {

    public static X509Certificate createCertificate(
            PublicKey subjectPublicKey,
            String subjectCN,
            PrivateKey issuerPrivateKey,
            String issuerDN
    ) throws Exception {

        Date from = new Date(System.currentTimeMillis() - 60_000);
        Date to = new Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000);

        BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());

        X500Name issuer = new X500Name(issuerDN);
        X500Name subject = new X500Name("CN=" + subjectCN);

        JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                issuer,
                serial,
                from,
                to,
                subject,
                subjectPublicKey
        );

        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
                .build(issuerPrivateKey);

        X509CertificateHolder holder = certBuilder.build(signer);

        return new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(holder);
    }
}
