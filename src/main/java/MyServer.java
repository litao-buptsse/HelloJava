import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Tao Li on 2015/12/18.
 */
public class MyServer {
  public boolean isOpen(ServerSocket serverSocket) {
    return !serverSocket.isClosed() && serverSocket.isBound();
  }

  public static void main(String[] args) {
    SocketAddress address = new InetSocketAddress("0.0.0.0", 9999);
    try (ServerSocket serverSocket = new ServerSocket()) {
      serverSocket.bind(address, 5);
      ExecutorService service = Executors.newFixedThreadPool(5);
      while (true) {
        try {
          Socket socket = serverSocket.accept();
          int port = socket.getPort();
          String hostName = socket.getInetAddress().getHostName();
          System.out.println(String.format("accept new connection: (%s, %d)", hostName, port));
          service.submit(new WorkerThread(socket));
        } catch (IOException e) {
          System.err.println("accept error");
          e.printStackTrace();
        } catch (RuntimeException e) {
          System.err.println("unexpected error");
          e.printStackTrace();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (RuntimeException e) {
      e.printStackTrace();
    }
  }
}

class WorkerThread implements Runnable {
  private Socket socket = null;
  private InputStream inputStream = null;
  private String hostName = null;
  private int port = -1;
  private String name = null;

  public WorkerThread(Socket socket) throws IOException {
    this.socket = socket;
    this.inputStream = socket.getInputStream();
    this.hostName = socket.getInetAddress().getHostName();
    this.port = socket.getPort();
  }

  public void run() {
    name = String.format("%s-%s-%d", Thread.currentThread().getName(), hostName, port);
    try {
      int result = 0;
      while (result != -1) {
        int bytesRead = 0;
        int bytesToRead = 10;
        byte[] input = new byte[bytesToRead];
        while (bytesRead < bytesToRead) {
          result = inputStream.read(input, bytesRead, bytesToRead - bytesRead);
          if (result == -1) break;
          bytesRead += result;
          System.out.println(String.format("result: %d, bytesRead: %d", result, bytesRead));
          try {
            Thread.sleep(1000 * 10);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        System.out.println("finish read input array: " + input);
      }
      System.out.println(String.format("[%s] meet end of stream", name));
    } catch (IOException e) {
      // client close connection, ignore
    } finally {
      try {
        socket.close();
        System.out.println(String.format("[%s] socket was closed", name));
      } catch (IOException e) {
      }
    }
  }
}
