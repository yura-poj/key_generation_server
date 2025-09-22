package pozhidaev.server;

import java.nio.channels.SocketChannel;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {
    public static void main(String[] args) {
        try {
            Map<String, Data> dataMap = new ConcurrentHashMap<>();
            LinkedBlockingQueue<String> genQueue = new LinkedBlockingQueue<>();
            LinkedBlockingQueue<Message> sendQueue = new LinkedBlockingQueue<>();
            LinkedBlockingQueue<String> sendCheckedQueue = new LinkedBlockingQueue<>();


            ServerConfig config = new ServerConfig();
            config.loadFromCommandLine(args);

            KeyPair signingKeyPair = KeyLoader.generateAndSaveSigningKeys(
                    config.getPrivateKeyPath(),
                    config.getPublicKeyPath()
            );
            PrivateKey signingKey = signingKeyPair.getPrivate();
            System.out.println("Keys ready");

            KeyGeneration keyGeneration = new KeyGeneration(config.getIssuerDN(), signingKey, dataMap,
                    genQueue, sendCheckedQueue);

            Thread[] threadsGeneration = new Thread[config.getGenThreadNum()];

            Thread t1 = new Thread(() -> {
                for (int i = 0; i < config.getGenThreadNum(); i++) {
                    threadsGeneration[i] = new Thread(keyGeneration);
                    threadsGeneration[i].start();
                }
                for (int i = 0; i < config.getGenThreadNum(); i++) {
                    try {
                        threadsGeneration[i].join();
                    } catch (InterruptedException e) {
                        System.out.println("Ah nevermind, you was interrupted");
                    }
                }
                System.out.println("Genereations done");
            });
            KeySender keySender = new KeySender(dataMap, genQueue, sendQueue, sendCheckedQueue);
            Thread[] threadsSender = new Thread[config.getSendThreadNums()];

            Thread t2 = new Thread(() -> {
                for (int i = 0; i < config.getSendThreadNums(); i++) {
                    threadsSender[i] = new Thread(keySender);
                    threadsSender[i].start();
                }
                for (int i = 0; i < config.getSendThreadNums(); i++) {
                    try {
                        threadsSender[i].join();
                    } catch (InterruptedException e) {
                        System.out.println("Ah nevermind, you was interrupted");
                    }
                }
                System.out.println("Senders done");
            });

            t1.start();
            System.out.println("Keys generators activated");

            t2.start();
            System.out.println("Listener activated " + config.getServerPort());

            Server server = new Server(config, sendQueue);
            server.start();

        } catch (Exception e) {
            System.err.println("Error at initializing server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static class Data {
        public boolean ready;
        KeyGeneration.CertificateData certificateData;
        ConcurrentLinkedDeque<SocketChannel> list = new ConcurrentLinkedDeque<>();
    }

    public static class Message {
        String name;
        SocketChannel socketChannel;

        public Message(String s, SocketChannel client) {
            name = s;
            socketChannel = client;
        }
    }
}
