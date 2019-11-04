package yoshikihigo.cpanalyzer;

import java.util.List;

public class Utility {

  public static <T> T getFirst(List<T> list) {
    return (list != null && !list.isEmpty()) ? list.get(0) : null;
  }

  public static <T> T getLast(List<T> list) {
    return (list != null && !list.isEmpty()) ? list.get(list.size() - 1) : null;
  }
}
