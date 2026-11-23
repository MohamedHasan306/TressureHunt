package treasurehunt.model;

public final class Player {
    private final Position startPosition;
    private int x;
    private int y;

    public Player(Position startPosition) {
        this.startPosition = startPosition;
        resetToStart();
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Position getPosition() {
        return new Position(x, y);
    }

    public void resetToStart() {
        x = startPosition.x();
        y = startPosition.y();
    }
}
