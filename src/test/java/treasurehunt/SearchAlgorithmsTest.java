package treasurehunt;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import treasurehunt.game.SolutionPlayer;
import treasurehunt.loader.LevelLoader;
import treasurehunt.model.GameGrid;
import treasurehunt.model.Level;
import treasurehunt.model.Position;
import treasurehunt.model.State;
import treasurehunt.search.AStar;
import treasurehunt.search.BFS;
import treasurehunt.search.DFS;
import treasurehunt.search.DFSRecursive;
import treasurehunt.search.Dijkstra;
import treasurehunt.search.SearchAlgorithm;
import treasurehunt.search.SearchResult;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchAlgorithmsTest {
    private static final List<SearchAlgorithm> ALGORITHMS =
            List.of(new BFS(), new DFS(), new DFSRecursive(), new Dijkstra(), new AStar());

    @ParameterizedTest
    @CsvSource({"1,9,11,9", "2,8,24,8", "3,-1,-1,-1", "4,8,12,8", "5,16,16,20"})
    void checksAllAlgorithmsAgainstLectureMaps(int levelNumber, int shortest, int dfs, int recursive)
            throws IOException {
        Level level = LevelLoader.loadBundled().stream()
                .filter(candidate -> candidate.getLevelNumber() == levelNumber).findFirst().orElseThrow();
        List<Integer> expected = List.of(shortest, dfs, recursive, shortest, shortest);

        for (int i = 0; i < ALGORITHMS.size(); i++) {
            SearchAlgorithm algorithm = ALGORITHMS.get(i);
            SearchResult result = algorithm.solve(level.getInitialState());
            String description = algorithm.name() + " on " + level;
            assertEquals(algorithm.name(), result.getAlgorithmName());
            assertTrue(result.getExecutionTimeNanos() >= 0, description);
            assertTrue(result.getDiscoveredNodes() >= result.getExpandedNodes(), description);

            int length = expected.get(i);
            if (length < 0) {
                assertFalse(result.isFound(), description);
                assertNull(result.getGoalState(), description);
                assertTrue(result.getPath().isEmpty(), description);
                assertEquals("", result.getSolutionMoves(), description);
                assertEquals(result.getDiscoveredNodes(), result.getExpandedNodes(), description);
            } else {
                assertTrue(result.isFound(), description);
                assertEquals(length, result.getPathLength(), description);
                assertEquals(length, result.getTotalCost(), description);
                validatePathAndReplay(level, result);
            }
        }
    }

    @Test
    void recursiveDfsExploresADeadEndAndBacktracks() {
        GameGrid grid = new GameGrid(List.of("S.T", ".##"));
        SearchResult result = new DFSRecursive().solve(new State(grid));
        assertEquals(List.of(new Position(0, 0), new Position(0, 1), new Position(1, 0)),
                result.getExploredPositions());
        assertEquals("RR", result.getSolutionMoves());
        assertFalse(result.getPath().stream().anyMatch(state -> state.getPosition().equals(new Position(0, 1))));
    }

    @Test
    void repeatedSearchesPreserveLivePlayerAndGiveDeterministicResults() throws IOException {
        Level level = LevelLoader.loadBundled().get(1);
        SolutionPlayer player = new SolutionPlayer(level);
        player.play("DD");
        player.stepForward();
        Position livePosition = player.getPlayerPosition();

        for (SearchAlgorithm algorithm : ALGORITHMS) {
            SearchResult first = algorithm.solve(level.getInitialState());
            SearchResult next = algorithm.solve(level.getInitialState());
            assertEquals(first.getSolutionMoves(), next.getSolutionMoves());
            assertEquals(first.getExploredPositions(), next.getExploredPositions());
            assertEquals(livePosition, player.getPlayerPosition());
            assertSame(level.getInitialState(), next.getPath().get(0));
        }
    }

    private static void validatePathAndReplay(Level level, SearchResult result) {
        String moves = result.getSolutionMoves();
        assertTrue(moves.matches("[UDLR]+"));
        assertEquals(result.getPathLength(), moves.length());
        assertEquals(moves.length() + 1, result.getPath().size());
        assertSame(level.getInitialState(), result.getPath().get(0));
        for (int i = 0; i < moves.length(); i++) {
            State parent = result.getPath().get(i);
            State child = result.getPath().get(i + 1);
            assertSame(parent, child.getParent());
            assertEquals(child, parent.move(moves.charAt(i)));
            assertEquals(moves.charAt(i), child.getMoveFromParent());
            assertEquals(i + 1, child.getGCost());
        }
        assertSame(result.getGoalState(), result.getPath().get(moves.length()));
        assertTrue(result.getGoalState().isWin());

        SolutionPlayer replay = new SolutionPlayer(level);
        replay.play(moves);
        for (int i = 0; i < moves.length(); i++) {
            replay.tick();
        }
        assertTrue(replay.reachedTreasure());
        assertEquals(SolutionPlayer.Status.COMPLETE, replay.getStatus());
        assertEquals(result.getPathLength(), replay.getStep());
    }
}
