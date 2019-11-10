package yoshikihigo.cpanalyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import org.apache.commons.cli.Option;
import yoshikihigo.cpanalyzer.nh3.Ammonia;

public class AllExecution {

  private static String CONFIG = new String("config.txt");

  public static void main(final String[] args) {

    if (1 == args.length) {
      CONFIG = args[0];
    } else if (1 < args.length) {
      System.err.println("Please run this program with only a single argument.");
      System.err.println("The argument must be a path of configuration file.");
      System.exit(0);
    }

    final Map<String, String> configs = getConfigs();
    final String[] arguments = getArguments(configs);
    ChangeExtractor.main(arguments);
    ChangePatternMaker.main(arguments);
    Ammonia.main(arguments);
  }

  public static Map<String, String> getConfigs() {
    final List<String> optionNames = CPAConfig.getOptions()
        .stream()
        .map(o -> o.getLongOpt())
        .collect(Collectors.toList());
    final Map<String, String> configs = new HashMap<>();
    try {
      final List<String> lines = Files.lines(Paths.get(CONFIG))
          .collect(Collectors.toList());
      for (int i = 0; i < lines.size(); i++) {
        final String line = lines.get(i);

        if (line.startsWith("#") || isBlankLine(line)) {
          continue;
        }

        final StringTokenizer tokenizer = new StringTokenizer(line, "= ");
        if (0 == tokenizer.countTokens()) {
          System.out.print("line ");
          System.out.print(Integer.toString(i + 1));
          System.out.println(": no configuration name.");
          System.exit(0);
        } else if (1 == tokenizer.countTokens()) {
          continue;
        } else if (2 < tokenizer.countTokens()) {
          System.out.print("line ");
          System.out.print(Integer.toString(i + 1));
          System.out.println(": format is invalid.");
          System.exit(0);
        }

        final String configName = tokenizer.nextToken();
        if (optionNames.stream()
            .noneMatch(n -> n.equals(configName))) {
          System.out.print("line ");
          System.out.print(Integer.toString(i + 1));
          System.out.print(": ");
          System.out.print(configName);
          System.out.print(" is undefined.");
          System.exit(0);
        }

        if (configs.containsKey(configName)) {
          System.out.print("line ");
          System.out.print(Integer.toString(i + 1));
          System.out.print(": ");
          System.out.print(configName);
          System.out.print(" is duplicatedly specified in ");
          System.out.print(CONFIG);
          System.out.println(".");
          System.exit(0);
        }

        final String configValue = tokenizer.nextToken();
        configs.put(configName, configValue);
      }

    } catch (final IOException e) {
      e.printStackTrace();
    }

    return configs;
  }

  public static String[] getArguments(final Map<String, String> configs) {

    final List<String> arguments = new ArrayList<>();
    for (final Entry<String, String> entry : configs.entrySet()) {
      final String name = entry.getKey();
      final String value = entry.getValue();

      final Option option = CPAConfig.OPTIONS.getOption(name);
      if (null == option) {
        continue;
      }

      if (!option.hasArg()) {
        if (value.equals("YES")) {
          arguments.add("--" + name);
        }
      }

      else {
        arguments.add("--" + name);
        arguments.add(value);
      }
    }

    return arguments.toArray(new String[0]);
  }

  private static boolean isBlankLine(final String line) {
    for (int i = 0; i < line.length(); i++) {
      final char c = line.charAt(i);
      if ((' ' != c) && ('\t' != c)) {
        return false;
      }
    }
    return true;
  }
}
