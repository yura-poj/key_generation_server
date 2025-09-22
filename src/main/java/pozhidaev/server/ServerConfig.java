package pozhidaev.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ServerConfig {
    private static final String DEFAULT_ISSUER_DN = "CN=KeyGeneration CA,O=Example Organization,C=RU";
    private static final String DEFAULT_PRIVATE_KEY_PATH = "signing-key-private.pem";
    private static final String DEFAULT_PUBLIC_KEY_PATH = "signing-key-public.pem";
    private static final int DEFAULT_SERVER_PORT = 8080;
    private static final int DEFAULT_GEN_THREAD_NUMS = Runtime.getRuntime().availableProcessors() - 2;
    private static final int DEFAULT_SEND_THREAD_NUMS = 2;

    private String issuerDN;
    private int genThreadNum;

    public ServerConfig() {
        this.issuerDN = DEFAULT_ISSUER_DN;
        this.genThreadNum = DEFAULT_GEN_THREAD_NUMS;
    }


    public void loadFromCommandLine(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--issuer-dn":
                    if (i + 1 < args.length) {
                        this.issuerDN = args[++i];
                    }
                    break;
                case "--gen-threads":
                    if (i + 1 < args.length) {
                        try {
                            this.genThreadNum = Integer.parseInt(args[++i]);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid number for --gen-threads: " + args[i]);
                        }                    }
                    break;
            }
        }
    }

    public String getIssuerDN() { return issuerDN; }
    public String getPrivateKeyPath() { return DEFAULT_PRIVATE_KEY_PATH; }
    public String getPublicKeyPath() { return DEFAULT_PUBLIC_KEY_PATH; }
    public int getServerPort() { return DEFAULT_SERVER_PORT; }
    public int getGenThreadNum() { return genThreadNum; }
    public int getSendThreadNums() { return DEFAULT_SEND_THREAD_NUMS; }

}
