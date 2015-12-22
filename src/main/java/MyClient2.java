import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Created by Tao Li on 2015/12/18.
 */
public class MyClient2 {
  public static void main(String[] args) {
    SocketAddress address = new InetSocketAddress("localhost", 9999);
    try (SocketChannel channel = SocketChannel.open(address)) {
      ByteBuffer buffer = ByteBuffer.allocate(20);
      for (int i = 0; i < 20; i++) {
        buffer.put((byte) i);
        buffer.flip();
        channel.write(buffer);
        System.out.println(String.format("sent: %d", i));
        buffer.clear();
        try {
          Thread.sleep(1000 * 5);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (RuntimeException e) {
      System.err.println("unexpected error");
      e.printStackTrace();
    }
  }
}
