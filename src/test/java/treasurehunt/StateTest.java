package treasurehunt;

import org.junit.jupiter.api.Test;
import treasurehunt.model.GameGrid;
import treasurehunt.model.Position;
import treasurehunt.model.State;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class StateTest {
    @Test
    void neighborsFollowLectureOrderAndKeepTheOriginalState() {
        GameGrid grid = new GameGrid(List.of("S..", "...", "..T"));
        State center = new State(grid).moveDown().moveRight();
        List<State> neighbors = center.getPossibleNextStates();

        assertEquals(List.of('D', 'R', 'U', 'L'), neighbors.stream().map(State::getMoveFromParent).toList());
        for (State next : neighbors) {
            assertSame(grid, next.getGrid());
            assertSame(center, next.getParent());
            assertEquals(center.getGCost() + 1, next.getGCost());
        }
        assertEquals(new Position(1, 1), center.getPosition());
        assertEquals("S . .\n. . .\n. . T\n", grid.toString());
    }

    @Test
    void blockedMovesReturnNullAndGoalRebuildsItsParentChain() {
        State start = new State(new GameGrid(List.of("S.T", "#..")));

        assertFalse(start.canMoveUp());
        assertFalse(start.canMoveDown());
        assertFalse(start.canMoveLeft());
        assertTrue(start.canMoveRight());
        assertNull(start.moveUp());
        assertNull(start.moveDown());
        assertNull(start.moveLeft());
        assertNull(start.move('?'));
        assertFalse(start.isWin());

        State middle = start.moveRight();
        State goal = middle.move('R');
        assertTrue(goal.isWin());
        assertEquals(List.of(start, middle, goal), goal.reconstructPath());
        assertSame(middle, goal.getParent());
        assertSame(start, middle.getParent());
        assertNull(start.getParent());
        assertEquals("RR", goal.getMovesString());
        assertEquals(2, goal.getGCost());
    }

    @Test
    void stateIdentityUsesLocationAndTheMapInstance() {
        GameGrid grid = new GameGrid(List.of("S.T"));
        State origin = new State(grid);
        State afterLoop = origin.moveRight().moveLeft();
        State otherMap = new State(new GameGrid(List.of("S.T")));

        assertNotSame(origin, afterLoop);
        assertNotEquals(origin.getGCost(), afterLoop.getGCost());
        assertEquals(origin, afterLoop);
        assertEquals(origin.hashCode(), afterLoop.hashCode());
        assertNotEquals(origin, origin.moveRight());
        assertNotEquals(origin, otherMap);

        Set<State> visited = new HashSet<>();
        assertTrue(visited.add(origin));
        assertFalse(visited.add(afterLoop));
        assertTrue(visited.add(otherMap));
    }

    @Test
    void gridOwnsItsDataAndChecksBounds() {
        List<String> rows = new ArrayList<>(List.of("S#T"));
        GameGrid grid = new GameGrid(rows);
        rows.set(0, "S.T");
        assertTrue(grid.isWall(1, 0));
        assertFalse(grid.isFree(1, 0));
        assertFalse(grid.isFree(-1, 0));
        assertFalse(grid.isFree(3, 0));
        assertFalse(grid.isFree(0, 1));
        assertFalse(grid.isWall(0, -1));
    }
}
