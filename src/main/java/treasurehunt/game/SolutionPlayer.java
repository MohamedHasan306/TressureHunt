package treasurehunt.game;

import treasurehunt.model.Direction;
import treasurehunt.model.Level;
import treasurehunt.model.Position;

public final class SolutionPlayer {
    public enum Status {
        READY, PLAYING, PAUSED, COMPLETE, ERROR
    }

    private final GameEngine game;
    private String moves = "";
    private int step;
    private Status status = Status.READY;
    private String message = "Ready";

    public SolutionPlayer(Level level) {
        game = new GameEngine(level);
    }

    public void changeLevel(Level level) {
        game.setLevel(level);
        reset();
    }

    public void reset() {
        game.resetPlayer();
        moves = "";
        step = 0;
        status = Status.READY;
        message = "Ready";
    }

    public void play(String moves) {
        reset();
        this.moves = moves.replaceAll("\\p{javaWhitespace}+", "");
        if (this.moves.isEmpty()) {
            finish();
        } else {
            status = Status.PLAYING;
            message = "Playing moves";
        }
    }

    public void pause() {
        if (status == Status.PLAYING) {
            status = Status.PAUSED;
            message = "Paused";
        }
    }

    public void resume() {
        if (status == Status.PAUSED) {
            status = Status.PLAYING;
            message = "Playing moves";
        }
    }

    /** Called by the animation timer. A paused or completed replay ignores ticks. */
    public void tick() {
        if (status == Status.PLAYING) {
            advance();
        }
    }

    public void stepForward() {
        pause();
        if (status == Status.PAUSED) {
            advance();
        }
    }

    private void advance() {
        char symbol = moves.charAt(step);
        Direction direction = Direction.fromSymbol(symbol);
        if (direction == null) {
            fail("unknown direction", symbol);
            return;
        }
        if (!game.move(direction)) {
            fail("a wall or the edge of the map blocks the move", symbol);
            return;
        }
        step++;
        if (step == moves.length()) {
            finish();
        } else if (game.reachedTreasure()) {
            message = "Treasure reached";
        }
    }

    private void finish() {
        status = Status.COMPLETE;
        message = game.reachedTreasure() ? "Treasure reached!" : "Moves ended before reaching the treasure.";
    }

    private void fail(String reason, char symbol) {
        Position position = game.getPlayerPosition();
        status = Status.ERROR;
        message = "Move " + (step + 1) + " ('" + symbol + "') at ("
                + position.x() + ", " + position.y() + ") failed. " + reason + ".";
    }

    public Level getLevel() {
        return game.getLevel();
    }

    public Position getPlayerPosition() {
        return game.getPlayerPosition();
    }

    public boolean reachedTreasure() {
        return game.reachedTreasure();
    }

    public int getStep() {
        return step;
    }

    public int getMoveCount() {
        return moves.length();
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
