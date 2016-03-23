import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by Tao Li on 2016/3/23.
 */
public class WebPageRender {
  Logger LOG = Logger.getLogger(WebPageRender.class.getName());

  private int downloadPicture(int id) {
    LOG.info("begin download picture " + id);
    sleepQuietly(5);
    LOG.info("end download picture " + id);
    return id;
  }

  private void renderText() {
    LOG.info("begin render text");
    sleepQuietly(1);
    LOG.info("end render text");
  }

  private void renderPicture(int newId) {
    LOG.info("begin render picture " + newId);
    sleepQuietly(1);
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

    waitForExecutorServiceShutdown(service, 60);
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

    waitForExecutorServiceShutdown(service, 60);
  }

  public void render3(int[] pictureIds) {
    ExecutorService service = Executors.newFixedThreadPool(5);

    IntStream.of(pictureIds).forEach(id ->
        CompletableFuture
            .supplyAsync(() -> downloadPicture(id), service)
            .thenAccept(picId -> renderPicture(picId))
    );
    renderText();

    waitForExecutorServiceShutdown(service, 60);
  }

  private static void sleepQuietly(long seconds) {
    try {
      TimeUnit.SECONDS.sleep(seconds);
    } catch (InterruptedException e) {
    }
  }

  private static void waitForExecutorServiceShutdown(ExecutorService service, long seconds) {
    service.shutdown();
    try {
      service.awaitTermination(seconds, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
    }
  }

  public static long test(Consumer<int[]> consumer, int[] pictureIds) {
    long beginTime = System.nanoTime();
    consumer.accept(pictureIds);
    long endTime = System.nanoTime();
    return TimeUnit.NANOSECONDS.toMillis(endTime - beginTime);
  }

  public static void main(String[] args) {
    int[] pictureIds = IntStream.iterate(1, n -> n + 1).limit(5).toArray();

    ExecutorService service = Executors.newFixedThreadPool(3);

    Map<String, Future<Long>> testCases = new HashMap<>();
    testCases.put("render1", service.submit(() -> test(new WebPageRender()::render1, pictureIds)));
    testCases.put("render2", service.submit(() -> test(new WebPageRender()::render2, pictureIds)));
    testCases.put("render3", service.submit(() -> test(new WebPageRender()::render3, pictureIds)));

    testCases.forEach((name, future) -> {
      try {
        System.out.println(String.format("%s took: %d ms", name, future.get()));
      } catch (Exception e) {
        System.out.println(String.format("fail to execute: %s", name));
      }
    });

    waitForExecutorServiceShutdown(service, 60);
  }
}
