package pozhidaev.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

public class Server {
    ServerConfig config;
    LinkedBlockingQueue<Main.Message> sendQueue;
    public Server(ServerConfig config, LinkedBlockingQueue<Main.Message> sendQueue) {
        this.config = config;
        this.sendQueue = sendQueue;
    }

    public void start() throws IOException, InterruptedException {
        Selector selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(config.getServerPort()));
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server started on port " + config.getServerPort());

        while (true) {
            selector.select();

            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iter = keys.iterator();

            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();

                if (key.isAcceptable()) {
                    SocketChannel client = serverChannel.accept();
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ);
                }

                if (key.isReadable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int bytesRead = client.read(buffer);

                    if (bytesRead == -1) {
                        key.cancel();
                        client.close();
                    } else {
                        buffer.flip();
                        byte[] data = new byte[buffer.limit()];
                        buffer.get(data);
                        Main.Message message = new Main.Message(new String(data), client);

                        sendQueue.put(message);
                    }
                }
            }
        }
    }
}
