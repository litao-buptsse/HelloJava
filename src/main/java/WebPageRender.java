import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by Tao Li on 2016/3/23.
 */
public class WebPageRender {
  private static int downloadPicture(int id) {
    System.out.println("begin download picture " + id);
    try {
      Thread.sleep(1000 * 5);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    System.out.println("end download picture " + id);
    return id;
  }

  private static void renderText() {
    System.out.println("begin render text");
    try {
      Thread.sleep(1000 * 1);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    System.out.println("end render text");
  }

  private static void renderPicture(int newId) {
    System.out.println("begin render picture " + newId);
    try {
      Thread.sleep(1000 * 1);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    System.out.println("end render picture " + newId);
  }

  private static void f1(int[] pictureIds) {
    ExecutorService service = Executors.newFixedThreadPool(5);

    // download pictures
    List<Future<Integer>> downloadPictureTasks =
        IntStream.of(pictureIds).mapToObj(id -> service.submit(() -> downloadPicture(id))).collect(Collectors.toList());

    // render text
    renderText();

    // render pictures
    downloadPictureTasks.stream().forEach(future ->
        service.submit(() -> {
          try {
            renderPicture(future.get());
          } catch (InterruptedException e) {
            e.printStackTrace();
          } catch (ExecutionException e) {
            e.printStackTrace();
          }
        })
    );

    service.shutdown();
    try {
      service.awaitTermination(1, TimeUnit.MINUTES);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private static <Void> void test(String name, Consumer<Void> consumer) {
    long t0 = System.nanoTime();
    consumer.accept(null);
    long t1 = System.nanoTime();
    long millis = TimeUnit.NANOSECONDS.toMillis(t1 - t0);
    System.out.println(String.format("%s took: %d ms", name, millis));
  }

  public static void main(String[] args) {
    int[] pictureIds = IntStream.iterate(1, n -> n + 1).limit(5).toArray();
    test("f1", Void -> f1(pictureIds));
  }
}
