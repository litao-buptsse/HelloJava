import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Tao Li on 2015/12/18.
 */
public class NIOTCPServer {
  public static void main(String[] args) {
    BlockingQueue<Byte> queue = new ArrayBlockingQueue<Byte>(1000);
    final int workerThreadNum = 5;
    ExecutorService service = Executors.newFixedThreadPool(5);
    for (int i = 0; i < workerThreadNum; i++) {
      service.submit(new WorkerThread(queue));
    }
    service.shutdown();

    SocketAddress address = new InetSocketAddress("0.0.0.0", 9999);
    ServerSocketChannel serverChannel;
    Selector selector;

    try {
      serverChannel = ServerSocketChannel.open();
      serverChannel.configureBlocking(false);
      serverChannel.bind(address);
      selector = Selector.open();
      serverChannel.register(selector, SelectionKey.OP_ACCEPT);
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }

    while (true) {
      try {
        selector.select();
      } catch (IOException e) {
        e.printStackTrace();
        break;
      }

      Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
      while (iterator.hasNext()) {
        SelectionKey key = iterator.next();
        iterator.remove();

        if (key.isAcceptable()) {
          SocketChannel channel = null;
          try {
            channel = serverChannel.accept();
            System.out.println(String.format("accept new connection: %s", channel.getRemoteAddress()));
            channel.configureBlocking(false);
            SelectionKey key2 = channel.register(selector, SelectionKey.OP_READ);
            ByteBuffer buffer = ByteBuffer.allocate(10);
            key2.attach(buffer);
          } catch (IOException e) {
            e.printStackTrace();
            closeQuietly(channel);
          }
        } else if (key.isReadable()) {
          SocketChannel channel = (SocketChannel) key.channel();
          ByteBuffer buffer = (ByteBuffer) key.attachment();
          try {
            int n = channel.read(buffer);
            if (n > 0) {
              buffer.flip();

              System.out.print("received: ");
              while (buffer.hasRemaining()) {
                byte data = buffer.get();
                System.out.print((int) data + " ");
                try {
                  queue.put(data);
                } catch (InterruptedException e) {
                  e.printStackTrace();
                }
              }
              System.out.println(String.format(", from %s", channel.getRemoteAddress()));

              buffer.clear();
            } else if (n == -1) {
              // client side socket closed
              closeQuietly(channel);
            }
          } catch (IOException e) {
            key.cancel();
            closeQuietly(channel);
          }
        }
      }
    }

    try {
      serverChannel.close();
    } catch (IOException e) {
    }
  }

  private static void closeQuietly(SocketChannel channel) {
    if (channel != null) {
      try {
        channel.close();
      } catch (IOException e) {
      }
    }
  }

  private static class WorkerThread implements Runnable {
    private BlockingQueue<Byte> queue;

    public WorkerThread(BlockingQueue<Byte> queue) {
      this.queue = queue;
    }

    @Override
    public void run() {
      // do some hard work on server side
      while (true) {
        try {
          Thread.sleep(1000 * 1);
          System.out.println(String.format("[%s] finish hard work: %d", Thread.currentThread().getName(), (int) queue.take()));
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }
}