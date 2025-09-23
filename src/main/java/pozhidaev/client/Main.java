package pozhidaev.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.security.cert.CertificateException;

public class Main {
    public static void main(String[] args) {
        try {
            ClientConfig config = new ClientConfig();
            config.parseCommandLine(args);

            System.out.println("Server: " + config.getServerHost() + ":" + config.getServerPort());
            System.out.println("Output directory: " + config.getOutputDir());

            connectAndRequestCertificate(config);
            
        } catch (Exception e) {
            System.err.println("Client error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static void connectAndRequestCertificate(ClientConfig config) throws IOException, InterruptedException, CertificateException {
        try (SocketChannel socketChannel = SocketChannel.open()) {
            socketChannel.connect(new InetSocketAddress(config.getServerHost(), config.getServerPort()));
            System.out.println("Connected successfully");

            InetSocketAddress localAddress = (InetSocketAddress) socketChannel.getLocalAddress();
            System.out.println("Client IP: " + localAddress.getAddress().getHostAddress());
            System.out.println("Client Port: " + localAddress.getPort());
            
            ByteBuffer requestBuffer = ByteBuffer.wrap(config.getSubjectName().getBytes());
            socketChannel.write(requestBuffer);
            System.out.println("Request sent");

            if (config.isCrashBeforeRead()) {
                System.out.println("Simulating error");
                System.exit(42);
            }

            if (config.getDelaySeconds() > 0) {
                System.out.println("Waiting " + config.getDelaySeconds());
                Thread.sleep(config.getDelaySeconds() * 1000L);
            }
            
            System.out.println("Reading response from server...");
            ByteBuffer responseBuffer = ByteBuffer.allocate(8192);
            StringBuilder responseData = new StringBuilder();
            
            int totalBytesRead = 0;
            while (true) {
                responseBuffer.clear();
                int bytesRead = socketChannel.read(responseBuffer);
                
                if (bytesRead == -1) {
                    break;
                }
                
                if (bytesRead > 0) {
                    totalBytesRead += bytesRead;
                    responseBuffer.flip();
                    byte[] data = new byte[responseBuffer.limit()];
                    responseBuffer.get(data);
                    responseData.append(new String(data));
                }
                
                if (responseData.toString().contains("-----END CERTIFICATE-----")) {
                    break;
                }
            }
            
            if (responseData.length() == 0) {
                return;
            }
            
            System.out.println("Parsing certificate data...");
            CertificateResponse response = CertificateResponse.parseFromString(responseData.toString());
            
            if (!response.isComplete()) {
                System.err.println("Incomplete certificate data received");
                return;
            }
            
            saveCertificateFiles(config, response);
            
            System.out.println("Certificate completed!");
        }
    }
    
    private static void saveCertificateFiles(ClientConfig config, CertificateResponse response) throws IOException, CertificateException {
        String sanitizedName = FileUtils.sanitizeFileName(config.getSubjectName());
        String outputDir = config.getOutputDir();
        
        String privateKeyPath = Paths.get(outputDir, sanitizedName + ".key").toString();
        String publicKeyPath = Paths.get(outputDir, sanitizedName + ".pub").toString();
        String certificatePath = Paths.get(outputDir, sanitizedName + ".crt").toString();
        
        FileUtils.savePrivateKey(response.getPrivateKey(), privateKeyPath);
        FileUtils.savePublicKey(response.getPublicKey(), publicKeyPath);
        FileUtils.saveCertificate(response.getCertificate(), certificatePath);
        
        System.out.println("Certificate details:");
        System.out.println("  Subject: " + response.getCertificate().getSubjectDN());
        System.out.println("  Issuer: " + response.getCertificate().getIssuerDN());
        System.out.println("  Serial Number: " + response.getCertificate().getSerialNumber());
        System.out.println("  Valid From: " + response.getCertificate().getNotBefore());
        System.out.println("  Valid To: " + response.getCertificate().getNotAfter());
        System.out.println("  Algorithm: " + response.getCertificate().getPublicKey().getAlgorithm());
    }
}
