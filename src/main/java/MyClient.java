import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Created by Tao Li on 2015/12/18.
 */
public class MyClient {
  public static boolean isOpen(Socket socket) {
    return !socket.isClosed() && socket.isConnected();
  }

  public static void main(String[] args) {
    SocketAddress address = new InetSocketAddress("localhost", 9999);
    try (Socket socket = new Socket()) {
      socket.connect(address);
      OutputStream output = socket.getOutputStream();
      for (int i = 0; i < 20; i++) {
        output.write(i);
        System.out.println(String.format("sent: %d", i));
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
