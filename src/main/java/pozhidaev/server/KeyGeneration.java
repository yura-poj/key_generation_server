package pozhidaev.server;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.*;
import java.util.HashMap;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class KeyGeneration implements Runnable {
    Map<String, Main.Data> dataMap;
    private final String issuerDN;
    private final PrivateKey signingKey;
    LinkedBlockingQueue<String> genQueue;
    LinkedBlockingQueue<String> sendCheckedQueue;
    
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public KeyGeneration(String issuerDN, PrivateKey signingKey, Map<String, Main.Data> dataMap,
                         LinkedBlockingQueue<String> genQueue, LinkedBlockingQueue<String> sendCheckedQueue) {
        this.issuerDN = issuerDN;
        this.signingKey = signingKey;
        this.dataMap = dataMap;
        this.genQueue = genQueue;
        this.sendCheckedQueue = sendCheckedQueue;
    }

    public void generateKeysAndCertificate(String subjectName) {
        try {
            KeyPair keyPair = KeyUtils.generateRsaKeyPair(2048);

            X509Certificate certificate = CertUtils.createCertificate(
                    keyPair.getPublic(),
                    subjectName,
                    signingKey,
                    issuerDN
            );

            CertificateData certData = new CertificateData(subjectName, keyPair, certificate);
            Main.Data data = dataMap.get(subjectName);
            data.certificateData = certData;
            data.ready = true;

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при генерации ключей и сертификата для " + subjectName, e);
        }
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                String name = genQueue.take();
                generateKeysAndCertificate(name);
                sendCheckedQueue.put(name);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("KeyGeneration thread interrupted");
        }
    }

    public static class CertificateData {
        private final String subjectName;
        private final KeyPair keyPair;
        private final X509Certificate certificate;

        public CertificateData(String subjectName, KeyPair keyPair, X509Certificate certificate) {
            this.subjectName = subjectName;
            this.keyPair = keyPair;
            this.certificate = certificate;
        }

        public String getSubjectName() { return subjectName; }
        public KeyPair getKeyPair() { return keyPair; }
        public X509Certificate getCertificate() { return certificate; }
        public PublicKey getPublicKey() { return keyPair.getPublic(); }
        public PrivateKey getPrivateKey() { return keyPair.getPrivate(); }
    }
}
