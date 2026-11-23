package treasurehunt.model;

import java.util.List;

public enum Direction {
    DOWN('D', 0, 1),
    RIGHT('R', 1, 0),
    UP('U', 0, -1),
    LEFT('L', -1, 0);

    public static final List<Direction> SEARCH_ORDER = List.of(DOWN, RIGHT, UP, LEFT);

    private final char symbol;
    private final int dx;
    private final int dy;

    Direction(char symbol, int dx, int dy) {
        this.symbol = symbol;
        this.dx = dx;
        this.dy = dy;
    }

    public static Direction fromSymbol(char symbol) {
        return switch (Character.toUpperCase(symbol)) {
            case 'D' -> DOWN;
            case 'R' -> RIGHT;
            case 'U' -> UP;
            case 'L' -> LEFT;
            default -> null;
        };
    }

    public Position from(int x, int y) {
        return new Position(x + dx, y + dy);
    }

    public char symbol() {
        return symbol;
    }
}
