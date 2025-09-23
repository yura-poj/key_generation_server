package pozhidaev.server;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class KeyLoader {
    
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static PrivateKey loadPrivateKeyFromPEM(String keyFilePath) throws IOException, GeneralSecurityException {
        try (FileReader fileReader = new FileReader(keyFilePath);
             PEMParser pemParser = new PEMParser(fileReader)) {
            
            Object object = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            
            if (object instanceof PEMKeyPair) {
                return converter.getPrivateKey(((PEMKeyPair) object).getPrivateKeyInfo());
            } else if (object instanceof org.bouncycastle.asn1.pkcs.PrivateKeyInfo) {
                return converter.getPrivateKey((org.bouncycastle.asn1.pkcs.PrivateKeyInfo) object);
            } else {
                throw new IllegalArgumentException("Неподдерживаемый формат приватного ключа в файле: " + keyFilePath);
            }
        }
    }

    public static PublicKey loadPublicKeyFromPEM(String keyFilePath) throws IOException, GeneralSecurityException {
        try (FileReader fileReader = new FileReader(keyFilePath);
             PEMParser pemParser = new PEMParser(fileReader)) {
            
            Object object = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            
            if (object instanceof org.bouncycastle.asn1.x509.SubjectPublicKeyInfo) {
                return converter.getPublicKey((org.bouncycastle.asn1.x509.SubjectPublicKeyInfo) object);
            } else if (object instanceof PEMKeyPair) {
                return converter.getPublicKey(((PEMKeyPair) object).getPublicKeyInfo());
            } else {
                throw new IllegalArgumentException("Неподдерживаемый формат публичного ключа в файле: " + keyFilePath);
            }
        }
    }

    public static PrivateKey loadPrivateKeyFromDER(String keyFilePath) throws IOException, GeneralSecurityException {
        byte[] keyBytes = Files.readAllBytes(Path.of(keyFilePath));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
    }

    public static PublicKey loadPublicKeyFromDER(String keyFilePath) throws IOException, GeneralSecurityException {
        byte[] keyBytes = Files.readAllBytes(Path.of(keyFilePath));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

    public static KeyPair generateAndSaveSigningKeys(String privateKeyPath, String publicKeyPath) 
            throws IOException, GeneralSecurityException {
        
        if (Files.exists(Path.of(privateKeyPath)) && Files.exists(Path.of(publicKeyPath))) {
            System.out.println("Ключи подписи уже существуют, загружаем из файлов...");
            PrivateKey privateKey = loadPrivateKeyFromPEM(privateKeyPath);
            PublicKey publicKey = loadPublicKeyFromPEM(publicKeyPath);
            return new KeyPair(publicKey, privateKey);
        }

        System.out.println("Генерируем новые ключи подписи...");
        KeyPair keyPair = KeyUtils.generateRsaKeyPair(2048);
        
        KeyUtils.savePrivateKeyToPEM(keyPair.getPrivate(), privateKeyPath);
        KeyUtils.savePublicKeyToPEM(keyPair.getPublic(), publicKeyPath);
        
        System.out.println("Ключи подписи сохранены в файлы:");
        System.out.println("  Приватный ключ: " + privateKeyPath);
        System.out.println("  Публичный ключ: " + publicKeyPath);
        
        return keyPair;
    }
}
