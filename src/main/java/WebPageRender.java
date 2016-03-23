import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by Tao Li on 2016/3/23.
 */
public class WebPageRender {
  Logger LOG = Logger.getLogger(WebPageRender.class.getName());

  private int downloadPicture(int id) {
    LOG.info("begin download picture " + id);
    CommonUtils.sleepQuietly(5);
    LOG.info("end download picture " + id);
    return id;
  }

  private void renderText() {
    LOG.info("begin render text");
    CommonUtils.sleepQuietly(1);
    LOG.info("end render text");
  }

  private void renderPicture(int newId) {
    LOG.info("begin render picture " + newId);
    CommonUtils.sleepQuietly(1);
    LOG.info("end render picture " + newId);
  }

  public void render1(int[] pictureIds) {
    ExecutorService service = Executors.newFixedThreadPool(5);

    List<Future<Integer>> futures = new ArrayList<>();
    for (int id : pictureIds) {
      futures.add(service.submit(new Callable<Integer>() {
        @Override
        public Integer call() throws Exception {
          return downloadPicture(id);
        }
      }));
    }

    renderText();

    for (Future<Integer> future : futures) {
      service.submit(new Runnable() {
        @Override
        public void run() {
          try {
            renderPicture(future.get());
          } catch (Exception e) {
          }
        }
      });
    }

    CommonUtils.waitForExecutorServiceShutdown(service, 60);
  }

  public void render2(int[] pictureIds) {
    ExecutorService service = Executors.newFixedThreadPool(5);

    // download pictures
    List<Future<Integer>> futures = IntStream.of(pictureIds)
        .mapToObj(id -> service.submit(() -> downloadPicture(id))).collect(Collectors.toList());

    // render text
    renderText();

    // render pictures
    futures.stream().forEach(future ->
        service.submit(() -> {
          try {
            renderPicture(future.get());
          } catch (Exception e) {
          }
        })
    );

    CommonUtils.waitForExecutorServiceShutdown(service, 60);
  }

  public void render3(int[] pictureIds) {
    ExecutorService service = Executors.newFixedThreadPool(5);

    IntStream.of(pictureIds).forEach(id ->
        CompletableFuture
            .supplyAsync(() -> downloadPicture(id), service)
            .thenAccept(picId -> renderPicture(picId))
    );
    renderText();

    CommonUtils.waitForExecutorServiceShutdown(service, 60);
  }

  public static void main(String[] args) {
    WebPageRender render = new WebPageRender();
    int[] pictureIds = IntStream.iterate(1, n -> n + 1).limit(5).toArray();
    long timeout = 10;

    TestUtils.test(timeout, true,
        new TestUtils.TestCase<>("render1", render::render1, pictureIds),
        new TestUtils.TestCase<>("render2", render::render2, pictureIds),
        new TestUtils.TestCase<>("render3", render::render3, pictureIds)
    );
  }
}

class TestUtils {
  public static class TestCase<T> {
    private String name;
    private Consumer<T> consumer;
    private T t;

    public TestCase(String name, Consumer<T> consumer, T t) {
      this.name = name;
      this.consumer = consumer;
      this.t = t;
    }
  }

  public static <T> long test(Consumer<T> consumer, T t) {
    long beginTime = System.nanoTime();
    consumer.accept(t);
    long endTime = System.nanoTime();
    return TimeUnit.NANOSECONDS.toMillis(endTime - beginTime);
  }

  private static <T> CompletableFuture<Void> testFure(ExecutorService service,
                                                      String name, Consumer<T> consumer, T t) {
    return CompletableFuture.supplyAsync(() -> test(consumer, t), service)
        .thenAccept(time -> System.out.println(String.format("%s took: %d ms", name, time)));
  }

  public static <T> void test(long timeout, boolean parallel, TestCase<T>... testCases) {
    ExecutorService service = Executors.newFixedThreadPool(testCases.length);

    Stream<CompletableFuture<Void>> stream = Stream.of(testCases)
        .map(testCase -> testFure(service, testCase.name, testCase.consumer, testCase.t));

    if (parallel) {
      stream = stream.parallel();
    }

    stream.forEach(future -> {
      try {
        future.get(timeout, TimeUnit.SECONDS);
      } catch (TimeoutException e) {
        System.err.println(String.format("exceed timeout %s s", timeout));
        future.cancel(true);
      } catch (ExecutionException | InterruptedException e) {
        e.printStackTrace();
        future.cancel(true);
      }
    });

    CommonUtils.waitForExecutorServiceShutdown(service, 5 + timeout * testCases.length);
  }
}

class CommonUtils {
  public static void sleepQuietly(long seconds) {
    try {
      TimeUnit.SECONDS.sleep(seconds);
    } catch (InterruptedException e) {
    }
  }

  public static void waitForExecutorServiceShutdown(ExecutorService service, long seconds) {
    service.shutdown();
    try {
      service.awaitTermination(seconds, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
    }
  }
}
