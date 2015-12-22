import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * Created by Tao Li on 2015/12/18.
 */
public class MyServer2 {
  public static void main(String[] args) throws IOException {
    SocketAddress address = new InetSocketAddress("0.0.0.0", 9999);
    ServerSocketChannel serverChannel = ServerSocketChannel.open();
    serverChannel.configureBlocking(false);
    serverChannel.bind(address);

    Selector selector = Selector.open();
    serverChannel.register(selector, SelectionKey.OP_ACCEPT);
    while (true) {
      selector.select();
      Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
      while (iterator.hasNext()) {
        SelectionKey key = iterator.next();
        iterator.remove();

        if (key.isAcceptable()) {
          ServerSocketChannel server = (ServerSocketChannel) key.channel();
          SocketChannel channel = null;
          try {
            channel = server.accept();
            System.out.println(String.format("accept new connection: %s", channel.getRemoteAddress()));
            channel.configureBlocking(false);
            SelectionKey key2 = channel.register(selector, SelectionKey.OP_READ);
            ByteBuffer buffer = ByteBuffer.allocate(60);
            key2.attach(buffer);
          } catch (IOException e) {
            System.err.println("accept error");
            e.printStackTrace();
            if (channel != null) {
              try {
                channel.close();
                System.out.println(String.format("socket was closed"));
              } catch (IOException ex) {
              }
            }
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
                System.out.print((int) buffer.get() + " ");
              }
              System.out.println();
              buffer.clear();
            } else if (n == -1) {
              System.out.println(String.format("meet end of stream"));
              if (channel != null) {
                try {
                  channel.close();
                  System.out.println(String.format("socket was closed"));
                } catch (IOException e) {
                }
              }
            }
          } catch (IOException e) {
            // client close connection, ignore
          }
        }
      }
    }
  }
}