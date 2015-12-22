import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Created by Tao Li on 2015/12/18.
 */
public class StreamTCPClient {
  public static boolean isOpen(Socket socket) {
    return !socket.isClosed() && socket.isConnected();
  }

  public static void main(String[] args) {
    SocketAddress address = new InetSocketAddress("localhost", 9999);
    try (Socket socket = new Socket()) {
      System.out.println(String.format("connected: %s", address));
      socket.connect(address);
      OutputStream output = socket.getOutputStream();
      for (int i = 0; i < 20; i++) {
        output.write(i);
        System.out.println(String.format("sent: %d", i));
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
