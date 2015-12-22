import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Tao Li on 2015/12/18.
 */
public class StreamTCPServer {
  public static boolean isOpen(ServerSocket serverSocket) {
    return !serverSocket.isClosed() && serverSocket.isBound();
  }

  public static void main(String[] args) {
    SocketAddress address = new InetSocketAddress("0.0.0.0", 9999);
    try (ServerSocket serverSocket = new ServerSocket()) {
      serverSocket.bind(address);
      ExecutorService service = Executors.newFixedThreadPool(5);
      while (true) {
        Socket socket = null;
        try {
          socket = serverSocket.accept();
          System.out.println(String.format("accept new connection: %s", socket.getRemoteSocketAddress()));
          service.submit(new WorkerThread(socket));
        } catch (IOException e) {
          e.printStackTrace();
          closeQuietly(socket);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static class WorkerThread implements Runnable {
    private Socket socket = null;

    public WorkerThread(Socket socket) {
      this.socket = socket;
    }

    public void run() {
      try (InputStream inputStream = socket.getInputStream()) {
        int result = 0;
        while (result != -1) {
          int bytesRead = 0;
          int bytesToRead = 10;
          byte[] bytes = new byte[bytesToRead];
          while (bytesRead < bytesToRead) {
            result = inputStream.read(bytes, bytesRead, bytesToRead - bytesRead);
            if (result == -1) break;

            System.out.print(String.format("[%s] received: ", Thread.currentThread().getName()));
            for (int i = 0; i < result; i++) {
              System.out.print((int) bytes[bytesRead + i] + " ");
            }
            System.out.println(String.format(", from %s", socket.getRemoteSocketAddress()));

            // do some hard work on server side
            for (int i = 0; i < result; i++) {
              try {
                Thread.sleep(1000 * 1);
                System.out.println(String.format("[%s] finish hard work: %d", Thread.currentThread().getName(), (int) bytes[bytesRead + i]));
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
            }

            bytesRead += result;
          }
        }
        // client side socket closed
      } catch (IOException e) {
      }
    }
  }

  private static void closeQuietly(Socket socket) {
    if (socket != null) {
      try {
        socket.close();
      } catch (IOException e) {
      }
    }
  }

}

