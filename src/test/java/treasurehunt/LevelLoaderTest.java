package treasurehunt;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import treasurehunt.loader.LevelLoader;
import treasurehunt.model.Level;
import treasurehunt.model.Position;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LevelLoaderTest {
    @Test
    void bundledResourceContainsTheFiveLectureMaps() throws IOException {
        List<Level> levels = LevelLoader.loadBundled();
        assertEquals(List.of(1, 2, 3, 4, 5),
                levels.stream().map(Level::getLevelNumber).toList());
        assertEquals(List.of("Default Map", "Open Map", "No Path", "Two Routes", "Maze"),
                levels.stream().map(Level::getLevelName).toList());
        assertEquals("S # . . . . .\n. # . # # # .\n. . . . . # .\n# # # # . # .\n"
                + ". . . . . # .\n. # # # # # .\n. . . . . . T\n",
                levels.get(4).getGameGrid().toString());
    }

    @Test
    void loadsExternalUtf8LevelsOfDifferentSizes(@TempDir Path folder) throws IOException {
        Path levelsFile = folder.resolve("custom.txt");
        Files.writeString(levelsFile, "\uFEFFLEVEL: 7\nNAME: خريطة\nS.T\n"
                + "\nLEVEL: 12\nNAME: Tall\nT #\n.\t.\n. S\n");
        List<Level> levels = LevelLoader.load(levelsFile);

        assertEquals(2, levels.size());
        assertEquals("خريطة", levels.get(0).getLevelName());
        assertEquals(3, levels.get(0).getGameGrid().getWidth());
        assertEquals(1, levels.get(0).getGameGrid().getHeight());
        assertEquals(12, levels.get(1).getLevelNumber());
        assertEquals(2, levels.get(1).getGameGrid().getWidth());
        assertEquals(3, levels.get(1).getGameGrid().getHeight());
        assertEquals(new Position(1, 2), levels.get(1).getStartPosition());
        assertEquals(new Position(0, 0), levels.get(1).getTreasurePosition());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "'S.T|..', columns",
            "'SXT', Unknown symbol",
            "'SST', more than one S",
            "'STT', more than one T",
            "'S..', exactly one S and one T",
            "'', at least one row",
            "'ST|LEVEL: 1|NAME: Duplicate|ST', Duplicate level number"
    })
    void rejectsInvalidMapsWithContext(String rows, String expectedMessage) {
        IOException error = assertThrows(IOException.class, () -> LevelLoader.parse(
                new StringReader("LEVEL: 1\nNAME: Broken\n" + rows.replace('|', '\n'))));
        assertTrue(error.getMessage().contains("Line "));
        assertTrue(error.getMessage().contains(expectedMessage), error::getMessage);
    }

    @Test
    void rejectsInvalidHeaders() {
        for (String input : List.of("", "S T", "LEVEL: -1", "LEVEL: 3\nNAME:", "LEVEL: 1")) {
            assertThrows(IOException.class, () -> LevelLoader.parse(new StringReader(input)), input);
        }
    }
}
