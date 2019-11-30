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
        className = "yoshikihigo.cpanalyzer.ChangeExtractor";
        break;
      case "patterns":
        className = "yoshikihigo.cpanalyzer.ChangePatternMaker";
        break;
      case "multirepos":
        className = "yoshikihigo.cpanalyzer.ManyRepoExecutor";
        break;
      case "bugfixes":
        className = "yoshikihigo.cpanalyzer.BugFixAllMaker";
        break;
      case "latentbugs":
        className = "yoshikihigo.cpanalyzer.LatentBugExplorer";
        break;
      case "ammonia":
        className = "yoshikihigo.cpanalyzer.nh3.Ammonia";
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
      //final Throwable causeException = e.getCause();
      //causeException.printStackTrace();
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
    System.err
        .println(" multirepos: to execute \'changes\' and \'patterns\' on multiple repositories");
    System.err.println(" bugfixes: to identify bugfix-related change patterns");
    System.err.println(" latentbugs: to identify latent bugs by using identified change patterns");
    System.err.println(" ammonia: to analyze found latent bugs by using GUI");
  }
}
