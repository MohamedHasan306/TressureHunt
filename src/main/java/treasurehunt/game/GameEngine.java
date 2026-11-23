package treasurehunt.game;

import treasurehunt.model.Direction;
import treasurehunt.model.Level;
import treasurehunt.model.Player;
import treasurehunt.model.Position;

public final class GameEngine {
    private Level level;
    private Player player;

    public GameEngine(Level level) {
        setLevel(level);
    }

    public void setLevel(Level level) {
        this.level = level;
        player = level.newPlayer();
    }

    public Level getLevel() {
        return level;
    }

    public Position getPlayerPosition() {
        return player.getPosition();
    }

    public void resetPlayer() {
        player.resetToStart();
    }

    public boolean move(Direction direction) {
        Position target = level.getGameGrid().destination(player.getX(), player.getY(), direction);
        if (target == null) {
            return false;
        }
        player.setX(target.x());
        player.setY(target.y());
        return true;
    }

    public boolean reachedTreasure() {
        return getPlayerPosition().equals(level.getTreasurePosition());
    }
}
