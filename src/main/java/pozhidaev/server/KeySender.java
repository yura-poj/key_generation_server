package pozhidaev.server;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class KeySender implements Runnable {
    Map<String, Main.Data> dataMap;
    LinkedBlockingQueue<String> genQueue;
    LinkedBlockingQueue<Main.Message> sendQueue;
    LinkedBlockingQueue<String> sendCheckedQueue;
    public KeySender(Map<String, Main.Data> dataMap, LinkedBlockingQueue<String> genQueue, LinkedBlockingQueue<Main.Message> sendQueue, LinkedBlockingQueue<String> sendCheckedQueue) {
        this.dataMap = dataMap;
        this.genQueue = genQueue;
        this.sendQueue = sendQueue;
        this.sendCheckedQueue = sendCheckedQueue;
    }

    @Override
    public void run() {
        new Thread(this::secondSender).start();
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Main.Message message = sendQueue.take();
                Main.Data data = new Main.Data();
                if (dataMap.containsKey(message.name)) {
                    data = dataMap.get(message.name);
                    if (data.ready) {
                        send(data, message.socketChannel);
                    } else {
                        data.list.add(message.socketChannel);
                    }
                } else {
                    data.list.add(message.socketChannel);
                    genQueue.put(message.name);
                    dataMap.put(message.name, data);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("KeySender thread interrupted");
        }

    }

    private void send(Main.Data data, SocketChannel channel) {
        if (channel.isOpen()) {
            try {
                String pemData = data.certificateData.toPemFormat();
                java.nio.ByteBuffer buffer = java.nio.ByteBuffer.wrap(pemData.getBytes());
                channel.write(buffer);
                System.out.println("Send for: " + channel.getRemoteAddress());
                } catch (java.net.SocketException e) {
                System.err.println("Error writing to channel: " + e.getMessage());
            } catch (IOException e) {
                System.err.println("Error writing to channel: " + e.getMessage());
            }
        }
    }

    public void secondSender() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                String name = sendCheckedQueue.take();
                Main.Data data = dataMap.get(name);
                if (!data.ready) {
                    continue;
                }
                for (SocketChannel socketChannel : data.list) {
                    send(data, socketChannel);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("SecondSender thread interrupted");
        }
    }
}
