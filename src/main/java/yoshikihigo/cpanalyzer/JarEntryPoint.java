package yoshikihigo.cpanalyzer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class JarEntryPoint {

  public static void main(final String[] args) {


    if (0 == args.length) {
      printUsage();
      System.exit(0);
    }

    final String[] realArgs = Arrays.copyOfRange(args, 1, args.length);
    String className = null;
    switch (args[0]) {
      case "changes":
        className = "ChangeExtractor";
        break;
      case "patterns":
        className = "ChangePatternMaker";
        break;
      default:
        System.err.println("invalid name:" + args[0]);
        printUsage();
        System.exit(0);
    }

    try {
      final Class<?> mainClass = Class.forName(className);
      final Method mainMethod = mainClass.getMethod("main", String[].class);
      mainMethod.invoke(null, new Object[] {realArgs});

    } catch (final ClassNotFoundException e) {
      System.err.println("unknown class Name: " + className);
      System.exit(1);
    } catch (final NoSuchMethodException e) {
      System.err.println("main method was not found in class");
      System.exit(1);
    } catch (final InvocationTargetException e) {
      System.err.println("An exception was thrown by invoked main method");
      System.exit(1);
    } catch (final IllegalAccessException e) {
      System.err.println("failed to access main method");
      System.exit(1);
    }
  }

  private static void printUsage() {
    System.err.println("One the following names must be specified as the first argument.");
    System.err.println(" changes: to extract changes from a repository");
    System.err.println(" patterns: to make change patterns from the extracted changes");
  }
}
