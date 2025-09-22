package pozhidaev.server;

import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class KeySender implements Runnable {
    Map<String, Main.Data> dataMap;
    LinkedBlockingQueue genQueue;
    LinkedBlockingQueue sendQueue;
    LinkedBlockingQueue sendCheckedQueue;
    public KeySender(Map<String, Main.Data> dataMap, LinkedBlockingQueue genQueue, LinkedBlockingQueue sendQueue, LinkedBlockingQueue sendCheckedQueue) {
        this.dataMap = dataMap;
        this.genQueue = genQueue;
        this.sendQueue = sendQueue;
        this.sendCheckedQueue = sendCheckedQueue;
    }

    @Override
    public void run() {
        new Thread(){
            while(true) {

            }
        }.start();
        try {
            while (true) {
                Main.Message message = (Main.Message) sendQueue.take();
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
            throw new RuntimeException(e);
        }

    }

    private void send(Main.Data data, SocketChannel channel) {
        if (channel.isOpen()) {
            channel.write(data.certificateData.toString())
        }
    }

    public void secondSender() {
        while (true) {
            String name = sendCheckedQueue.take();
            Main.Data data = dataMap.get(name);
            if (!data.ready) {
                continue;
            }
            for (SocketChannel socketChannel : data.list) {
                send(data, socketChannel);
            }
        }
    }
}
