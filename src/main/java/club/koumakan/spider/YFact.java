package club.koumakan.spider;

import java.util.function.Consumer;
import java.util.function.Function;

public class YFact {

  public static <A, B> Function<A, B> y(Function<Function<A, B>, Function<A, B>> ff) {
    return ff.apply(a -> y(ff).apply(a));
  }

  public static <T> Consumer<T> yConsumer(Function<Consumer<T>, Consumer<T>> ff) {
    return ff.apply(t -> yConsumer(ff).accept(t));
  }
}
