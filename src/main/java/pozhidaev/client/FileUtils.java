package pozhidaev.client;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class FileUtils {
    
    public static void savePrivateKey(PrivateKey privateKey, String filePath) throws IOException {
        createDirectoryIfNotExists(filePath);
        
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("-----BEGIN PRIVATE KEY-----\n");
            String encoded = Base64.getEncoder().encodeToString(privateKey.getEncoded());
            
            for (int i = 0; i < encoded.length(); i += 64) {
                int end = Math.min(i + 64, encoded.length());
                writer.write(encoded.substring(i, end) + "\n");
            }
            
            writer.write("-----END PRIVATE KEY-----\n");
        }
    }
    
    public static void saveCertificate(X509Certificate certificate, String filePath) throws IOException, CertificateEncodingException {
        createDirectoryIfNotExists(filePath);
        
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("-----BEGIN CERTIFICATE-----\n");
            String encoded = Base64.getEncoder().encodeToString(certificate.getEncoded());
            
            for (int i = 0; i < encoded.length(); i += 64) {
                int end = Math.min(i + 64, encoded.length());
                writer.write(encoded.substring(i, end) + "\n");
            }
            
            writer.write("-----END CERTIFICATE-----\n");
        }
    }
    
    public static void savePublicKey(PublicKey publicKey, String filePath) throws IOException {
        createDirectoryIfNotExists(filePath);
        
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("-----BEGIN PUBLIC KEY-----\n");
            String encoded = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            
            for (int i = 0; i < encoded.length(); i += 64) {
                int end = Math.min(i + 64, encoded.length());
                writer.write(encoded.substring(i, end) + "\n");
            }
            
            writer.write("-----END PUBLIC KEY-----\n");
        }
    }
    
    private static void createDirectoryIfNotExists(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Path parentDir = path.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
    }
    
    public static String sanitizeFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
