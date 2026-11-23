package treasurehunt.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class State {
    private final GameGrid grid;
    private final int x;
    private final int y;
    private final State parent;
    private final char moveFromParent;
    private final int gCost;

    public State(GameGrid grid) {
        this(grid, grid.getStartPosition().x(), grid.getStartPosition().y(), null, '\0', 0);
    }

    public State(GameGrid grid, int x, int y, State parent, char moveFromParent, int gCost) {
        if (!grid.isFree(x, y)) {
            throw new IllegalArgumentException("A state must occupy a walkable cell.");
        }
        this.grid = grid;
        this.x = x;
        this.y = y;
        this.parent = parent;
        this.moveFromParent = moveFromParent;
        this.gCost = gCost;
    }

    public GameGrid getGrid() {
        return grid;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Position getPosition() {
        return new Position(x, y);
    }

    public State getParent() {
        return parent;
    }

    public char getMoveFromParent() {
        return moveFromParent;
    }

    public int getGCost() {
        return gCost;
    }

    public boolean canMoveUp() {
        return canMove(Direction.UP);
    }

    public boolean canMoveDown() {
        return canMove(Direction.DOWN);
    }

    public boolean canMoveLeft() {
        return canMove(Direction.LEFT);
    }

    public boolean canMoveRight() {
        return canMove(Direction.RIGHT);
    }

    private boolean canMove(Direction direction) {
        return grid.destination(x, y, direction) != null;
    }

    public State moveUp() {
        return move(Direction.UP);
    }

    public State moveDown() {
        return move(Direction.DOWN);
    }

    public State moveLeft() {
        return move(Direction.LEFT);
    }

    public State moveRight() {
        return move(Direction.RIGHT);
    }

    /** Returns null for an invalid direction, wall, or move outside the map. */
    public State move(char symbol) {
        Direction direction = Direction.fromSymbol(symbol);
        return direction == null ? null : move(direction);
    }

    private State move(Direction direction) {
        Position target = grid.destination(x, y, direction);
        return target != null
                ? new State(grid, target.x(), target.y(), this, direction.symbol(), gCost + 1)
                : null;
    }

    public List<State> getPossibleNextStates() {
        List<State> neighbors = new ArrayList<>(4);
        for (Direction direction : Direction.SEARCH_ORDER) {
            State next = move(direction);
            if (next != null) {
                neighbors.add(next);
            }
        }
        return neighbors;
    }

    public boolean isWin() {
        return getPosition().equals(grid.getTreasurePosition());
    }

    public List<State> reconstructPath() {
        List<State> path = new ArrayList<>();
        for (State step = this; step != null; step = step.parent) {
            path.add(step);
        }
        Collections.reverse(path);
        return List.copyOf(path);
    }

    public String getMovesString() {
        StringBuilder moves = new StringBuilder();
        for (State step = this; step.parent != null; step = step.parent) {
            moves.append(step.moveFromParent);
        }
        return moves.reverse().toString();
    }

    public void printPath() {
        for (State step : reconstructPath()) {
            System.out.println(step);
        }
        String moves = getMovesString();
        System.out.printf("%d moves: %s%n", moves.length(), moves);
    }

    @Override
    public String toString() {
        return grid.render(getPosition());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof State other && grid == other.grid && x == other.x && y == other.y;
    }

    @Override
    public int hashCode() {
        return 31 * System.identityHashCode(grid) + y * grid.getWidth() + x;
    }
}
