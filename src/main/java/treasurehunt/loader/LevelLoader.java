package treasurehunt.loader;

import treasurehunt.model.GameGrid;
import treasurehunt.model.Level;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class LevelLoader {
    private LevelLoader() {
    }

    public static List<Level> loadBundled() throws IOException {
        InputStream stream = LevelLoader.class.getResourceAsStream("/levels.txt");
        if (stream == null) {
            throw new IOException("levels.txt is missing from the application resources.");
        }
        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return parse(reader);
        }
    }

    public static List<Level> load(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return parse(reader);
        }
    }

    public static List<Level> parse(Reader source) throws IOException {
        BufferedReader reader = new BufferedReader(source);
        List<Level> levels = new ArrayList<>();
        Set<Integer> ids = new HashSet<>();
        List<String> rows = new ArrayList<>();
        int number = 0;
        String name = null;
        int headerLine = 0;
        int lineNumber = 0;
        String raw;
        while ((raw = reader.readLine()) != null) {
            lineNumber++;
            String line = (lineNumber == 1 && raw.startsWith("\uFEFF") ? raw.substring(1) : raw).strip();
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith("LEVEL:")) {
                if (number != 0) {
                    levels.add(makeLevel(number, name, rows, headerLine));
                }
                number = parseNumber(line, lineNumber);
                if (!ids.add(number)) {
                    throw error(lineNumber, "Duplicate level number " + number + ".");
                }
                headerLine = lineNumber;
                name = null;
                rows.clear();
            } else if (number == 0) {
                throw error(lineNumber, "Expected LEVEL: followed by a positive integer.");
            } else if (name == null) {
                if (!line.startsWith("NAME:") || line.substring(5).isBlank()) {
                    throw error(lineNumber, "Expected NAME: followed by a level name.");
                }
                name = line.substring(5).strip();
            } else {
                rows.add(line.replaceAll("\\s+", ""));
            }
        }

        if (number == 0) {
            throw new IOException("No levels found. Start the file with LEVEL: 1.");
        }
        levels.add(makeLevel(number, name, rows, headerLine));
        return List.copyOf(levels);
    }

    private static int parseNumber(String line, int lineNumber) throws IOException {
        try {
            int number = Integer.parseInt(line.substring(6).strip());
            if (number > 0) {
                return number;
            }
        } catch (NumberFormatException ignored) {
            // Report the same file-format error for malformed numbers.
        }
        throw error(lineNumber, "Expected a positive integer after LEVEL:.");
    }

    private static Level makeLevel(int number, String name, List<String> rows, int headerLine)
            throws IOException {
        if (name == null) {
            throw error(headerLine, "Level " + number + " is missing NAME:.");
        }
        try {
            return new Level(number, name, new GameGrid(rows));
        } catch (IllegalArgumentException e) {
            throw error(headerLine, "Level " + number + " (" + name + "). " + e.getMessage());
        }
    }

    private static IOException error(int line, String message) {
        return new IOException("Line " + line + ". " + message);
    }
}
