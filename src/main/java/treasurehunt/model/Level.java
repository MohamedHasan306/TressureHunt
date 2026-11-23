package treasurehunt.model;

public final class Level {
    private final int levelNumber;
    private final String levelName;
    private final GameGrid gameGrid;
    private final State initialState;

    public Level(int levelNumber, String levelName, GameGrid gameGrid) {
        this.levelNumber = levelNumber;
        this.levelName = levelName;
        this.gameGrid = gameGrid;
        initialState = new State(gameGrid);
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public String getLevelName() {
        return levelName;
    }

    public GameGrid getGameGrid() {
        return gameGrid;
    }

    public State getInitialState() {
        return initialState;
    }

    public Position getStartPosition() {
        return gameGrid.getStartPosition();
    }

    public Position getTreasurePosition() {
        return gameGrid.getTreasurePosition();
    }

    public Player newPlayer() {
        return new Player(getStartPosition());
    }

    @Override
    public String toString() {
        return levelNumber + ". " + levelName;
    }
}
