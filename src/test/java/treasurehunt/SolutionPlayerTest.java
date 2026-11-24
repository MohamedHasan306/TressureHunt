package treasurehunt;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import treasurehunt.game.SolutionPlayer;
import treasurehunt.game.SolutionPlayer.Status;
import treasurehunt.model.Direction;
import treasurehunt.model.GameGrid;
import treasurehunt.model.Level;
import treasurehunt.model.Position;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SolutionPlayerTest {
    private final Level level = new Level(1, "Hall", new GameGrid(List.of("S..T", ".##.")));

    @Test
    void movesDirectlyAndCountsOnlyLegalStepsUntilTreasure() {
        SolutionPlayer player = new SolutionPlayer(level);
        player.moveManually(Direction.UP);
        assertEquals(level.getStartPosition(), player.getPlayerPosition());
        assertEquals(0, player.getStep());

        player.moveManually(Direction.RIGHT);
        player.moveManually(Direction.DOWN);
        assertEquals(new Position(1, 0), player.getPlayerPosition());
        assertEquals(1, player.getStep());
        assertTrue(player.getMessage().contains("wall"));

        player.moveManually(Direction.RIGHT);
        player.moveManually(Direction.RIGHT);
        assertTrue(player.reachedTreasure());
        assertEquals(3, player.getStep());
        assertTrue(player.getMessage().contains("Treasure reached!"));
        player.moveManually(Direction.LEFT);
        assertEquals(level.getTreasurePosition(), player.getPlayerPosition());
        assertEquals(3, player.getStep());

        player.reset();
        assertEquals(level.getStartPosition(), player.getPlayerPosition());
        assertEquals(0, player.getStep());
        player.moveManually(Direction.DOWN);
        assertEquals(new Position(0, 1), player.getPlayerPosition());
    }

    @Test
    void takingManualControlClearsTheReplayAndKeepsTheCurrentPosition() {
        SolutionPlayer player = new SolutionPlayer(level);
        player.play("RRR");
        player.tick();
        player.pause();

        player.moveManually(Direction.RIGHT);
        assertEquals(Status.MANUAL, player.getStatus());
        assertEquals(0, player.getMoveCount());
        assertEquals(1, player.getStep());
        assertEquals(new Position(2, 0), player.getPlayerPosition());
        player.resume();
        player.tick();
        player.stepForward();
        assertEquals(new Position(2, 0), player.getPlayerPosition());

        player.play("RRR");
        assertEquals(level.getStartPosition(), player.getPlayerPosition());
        assertEquals(Status.PLAYING, player.getStatus());
    }

    @Test
    void playsPausesStepsResumesAndResets() {
        SolutionPlayer player = new SolutionPlayer(level);
        player.play("r r r");
        player.tick();
        player.pause();
        player.tick();
        assertEquals(1, player.getStep());
        assertEquals(Status.PAUSED, player.getStatus());
        player.stepForward();
        assertEquals(new Position(2, 0), player.getPlayerPosition());
        player.tick();
        assertEquals(2, player.getStep());
        player.resume();
        player.tick();

        assertEquals(Status.COMPLETE, player.getStatus());
        assertEquals(3, player.getStep());
        assertTrue(player.reachedTreasure());
        assertEquals("Treasure reached!", player.getMessage());

        player.reset();
        player.tick();
        assertEquals(Status.READY, player.getStatus());
        assertEquals(level.getStartPosition(), player.getPlayerPosition());
        assertEquals(0, player.getStep());
        assertEquals(0, player.getMoveCount());
    }

    @ParameterizedTest
    @CsvSource({"RX,unknown direction", "RD,a wall", "RU,the edge"})
    void stopsAtFirstInvalidMoveAndReportsItsIndexAndPosition(String moves, String detail) {
        SolutionPlayer player = new SolutionPlayer(level);
        player.play(moves);
        player.tick();
        player.tick();
        player.tick();

        assertEquals(Status.ERROR, player.getStatus());
        assertEquals(1, player.getStep());
        assertEquals(new Position(1, 0), player.getPlayerPosition());
        assertTrue(player.getMessage().contains("Move 2"));
        assertTrue(player.getMessage().contains("(1, 0)"));
        assertTrue(player.getMessage().contains(detail));
    }

    @Test
    void changingTheLevelDuringReplayDiscardsItsMoves() {
        SolutionPlayer player = new SolutionPlayer(level);
        player.play("RRR");
        player.tick();
        Level next = new Level(2, "New map", new GameGrid(List.of("TS")));
        player.changeLevel(next);

        player.tick();
        player.stepForward();
        assertSame(next, player.getLevel());
        assertEquals(Status.READY, player.getStatus());
        assertEquals(next.getStartPosition(), player.getPlayerPosition());
        assertEquals(0, player.getMoveCount());

        player.play("L");
        player.tick();
        assertTrue(player.reachedTreasure());
    }

    @Test
    void endingWithoutTheTreasureIsReported() {
        SolutionPlayer player = new SolutionPlayer(level);
        player.play("");
        assertEquals(Status.COMPLETE, player.getStatus());
        assertFalse(player.reachedTreasure());
        assertTrue(player.getMessage().contains("before reaching"));
    }
}
