import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by Tao Li on 2015/12/18.
 */
public class NIOTCPClient {
  public static void main(String[] args) {
    SocketAddress address = new InetSocketAddress("localhost", 9999);
    try (SocketChannel channel = SocketChannel.open(address)) {
      System.out.println(String.format("connected: %s", address));
      ByteBuffer buffer = ByteBuffer.allocate(20);
      for (int i = 0; i < 20; i++) {
        buffer.put((byte) i);
        buffer.flip();
        channel.write(buffer);
        System.out.println(String.format("sent: %d", i));
        buffer.clear();
        try {
          Thread.sleep(1000 * 1);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
