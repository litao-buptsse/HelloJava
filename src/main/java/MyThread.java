import java.util.concurrent.*;

/**
 * Created by Tao Li on 2015/12/21.
 */
class MyCallableThread implements Callable<String> {

  @Override
  public String call() throws Exception {
    return "hello, world";
  }
}

public class MyThread {
  public static void main(String[] args) throws ExecutionException, InterruptedException {
    ExecutorService service = Executors.newFixedThreadPool(2);
    MyCallableThread task = new MyCallableThread();
    Future<String> future = service.submit(task);
    System.out.println(future.get());
  }
}
