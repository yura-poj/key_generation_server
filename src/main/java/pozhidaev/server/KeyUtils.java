package pozhidaev.server;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.security.*;

public class KeyUtils {
    
    public static KeyPair generateRsaKeyPair(int keySize) throws GeneralSecurityException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(keySize, new SecureRandom());
        return kpg.generateKeyPair();
    }

    public static void savePrivateKeyToPEM(PrivateKey privateKey, String filePath) throws IOException {
        try (FileWriter fileWriter = new FileWriter(filePath);
             PemWriter pemWriter = new PemWriter(fileWriter)) {
            
            PemObject pemObject = new PemObject("PRIVATE KEY", privateKey.getEncoded());
            pemWriter.writeObject(pemObject);
        }
    }

    public static void savePublicKeyToPEM(PublicKey publicKey, String filePath) throws IOException {
        try (FileWriter fileWriter = new FileWriter(filePath);
             PemWriter pemWriter = new PemWriter(fileWriter)) {
            
            PemObject pemObject = new PemObject("PUBLIC KEY", publicKey.getEncoded());
            pemWriter.writeObject(pemObject);
        }
    }
}