package pozhidaev.client;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import java.io.IOException;
import java.io.StringReader;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class CertificateResponse {
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private X509Certificate certificate;
    
    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    
    public static CertificateResponse parseFromString(String responseData) throws IOException, CertificateException {
        CertificateResponse response = new CertificateResponse();
        
        try (StringReader stringReader = new StringReader(responseData);
             PEMParser pemParser = new PEMParser(stringReader)) {
            
            JcaPEMKeyConverter keyConverter = new JcaPEMKeyConverter().setProvider("BC");
            JcaX509CertificateConverter certConverter = new JcaX509CertificateConverter().setProvider("BC");
            
            Object object;
            while ((object = pemParser.readObject()) != null) {
                if (object instanceof PEMKeyPair) {
                    PEMKeyPair keyPair = (PEMKeyPair) object;
                    response.privateKey = keyConverter.getPrivateKey(keyPair.getPrivateKeyInfo());
                    response.publicKey = keyConverter.getPublicKey(keyPair.getPublicKeyInfo());
                } else if (object instanceof org.bouncycastle.asn1.pkcs.PrivateKeyInfo) {
                    response.privateKey = keyConverter.getPrivateKey((org.bouncycastle.asn1.pkcs.PrivateKeyInfo) object);
                } else if (object instanceof org.bouncycastle.asn1.x509.SubjectPublicKeyInfo) {
                    response.publicKey = keyConverter.getPublicKey((org.bouncycastle.asn1.x509.SubjectPublicKeyInfo) object);
                } else if (object instanceof X509CertificateHolder) {
                    response.certificate = certConverter.getCertificate((X509CertificateHolder) object);
                }
            }
        }
        
        return response;
    }
    
    public PrivateKey getPrivateKey() { return privateKey; }
    public PublicKey getPublicKey() { return publicKey; }
    public X509Certificate getCertificate() { return certificate; }
    
    public boolean isComplete() {
        return privateKey != null && publicKey != null && certificate != null;
    }
}
