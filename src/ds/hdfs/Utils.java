package ds.hdfs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

public class Utils {
    public static Map<String, String> parseConfigFile(String filename) throws IOException {
        return Files.lines(Paths.get(filename))
                .map(line -> line.split("="))
                .collect(Collectors.toMap(
                        line -> line[0],
                        line -> line[1]
                ));
    }
}
