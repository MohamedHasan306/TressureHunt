package treasurehunt.model;

import java.util.List;

public final class GameGrid {
    private final int width;
    private final int height;
    private final char[][] grid;
    private final Position startPosition;
    private final Position treasurePosition;

    public GameGrid(List<String> rows) {
        if (rows.isEmpty() || rows.get(0).isEmpty()) {
            throw new IllegalArgumentException("The map must have at least one row and one column.");
        }
        width = rows.get(0).length();
        height = rows.size();
        grid = new char[height][];
        Position start = null;
        Position treasure = null;
        for (int y = 0; y < height; y++) {
            String row = rows.get(y);
            if (row.length() != width) {
                throw new IllegalArgumentException("Map row " + (y + 1) + " has " + row.length()
                        + " columns. Expected " + width + ".");
            }
            grid[y] = row.toCharArray();
            for (int x = 0; x < width; x++) {
                switch (grid[y][x]) {
                    case 'S' -> {
                        if (start != null) {
                            throw new IllegalArgumentException("The map contains more than one S.");
                        }
                        start = new Position(x, y);
                    }
                    case 'T' -> {
                        if (treasure != null) {
                            throw new IllegalArgumentException("The map contains more than one T.");
                        }
                        treasure = new Position(x, y);
                    }
                    case '#', '.' -> { }
                    default -> throw new IllegalArgumentException("Unknown symbol '" + grid[y][x]
                            + "' at map row " + (y + 1) + ", column " + (x + 1) + ".");
                }
            }
        }
        if (start == null || treasure == null) {
            throw new IllegalArgumentException("The map needs exactly one S and one T.");
        }
        startPosition = start;
        treasurePosition = treasure;
    }

    public boolean inBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public boolean isFree(int x, int y) {
        return inBounds(x, y) && grid[y][x] != '#';
    }

    public boolean isWall(int x, int y) {
        return inBounds(x, y) && grid[y][x] == '#';
    }

    public Position destination(int x, int y, Direction direction) {
        Position target = direction.from(x, y);
        return isFree(target.x(), target.y()) ? target : null;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Position getStartPosition() {
        return startPosition;
    }

    public Position getTreasurePosition() {
        return treasurePosition;
    }

    public String render(Position playerPosition) {
        StringBuilder result = new StringBuilder();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x > 0) {
                    result.append(' ');
                }
                boolean playerHere = playerPosition != null
                        && playerPosition.x() == x && playerPosition.y() == y;
                result.append(playerHere ? 'P' : grid[y][x]);
            }
            result.append('\n');
        }
        return result.toString();
    }

    @Override
    public String toString() {
        return render(null);
    }
}
