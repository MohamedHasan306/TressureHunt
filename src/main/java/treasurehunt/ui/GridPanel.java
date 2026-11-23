package treasurehunt.ui;

import treasurehunt.model.GameGrid;
import treasurehunt.model.Position;
import treasurehunt.model.State;
import treasurehunt.search.SearchResult;

import javax.swing.JPanel;
import javax.swing.Scrollable;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.HashSet;
import java.util.Set;

final class GridPanel extends JPanel implements Scrollable {
    private static final int MARGIN = 16;
    private static final int MIN_CELL = 18;
    private static final int MAX_CELL = 96;
    private static final Color WALL = new Color(46, 57, 69);
    private static final Color EMPTY = new Color(235, 239, 242);
    private static final Color PLAYER = new Color(28, 86, 202);
    private static final Color TREASURE = new Color(232, 169, 32);
    private static final Color PATH = new Color(137, 205, 156);
    private static final Color EXPLORED = new Color(179, 215, 242);

    private GameGrid grid;
    private Position player;
    private Set<Position> explored = Set.of();
    private Set<Position> path = Set.of();

    GridPanel() {
        setBackground(Color.WHITE);
        getAccessibleContext().setAccessibleName("Treasure map");
    }

    void setLevel(GameGrid grid, Position player) {
        this.grid = grid;
        this.player = player;
        setResult(null);
        revalidate();
    }

    void setPlayer(Position player) {
        this.player = player;
        repaint();
    }

    void setResult(SearchResult result) {
        explored = result == null ? Set.of() : new HashSet<>(result.getExploredPositions());
        path = new HashSet<>();
        if (result != null) {
            for (State step : result.getPath()) {
                path.add(step.getPosition());
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (grid == null) {
            return;
        }
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int cell = Math.max(MIN_CELL, Math.min(MAX_CELL, Math.min(
                    (getWidth() - 2 * MARGIN) / grid.getWidth(),
                    (getHeight() - 2 * MARGIN) / grid.getHeight())));
            int left = (getWidth() - cell * grid.getWidth()) / 2;
            int top = (getHeight() - cell * grid.getHeight()) / 2;
            Rectangle clip = g.getClipBounds();
            int firstX = Math.max(0, (clip.x - left) / cell);
            int firstY = Math.max(0, (clip.y - top) / cell);
            int lastX = Math.min(grid.getWidth() - 1, (clip.x + clip.width - left) / cell);
            int lastY = Math.min(grid.getHeight() - 1, (clip.y + clip.height - top) / cell);

            for (int y = firstY; y <= lastY; y++) {
                for (int x = firstX; x <= lastX; x++) {
                    drawCell(g, new Position(x, y), left + x * cell, top + y * cell, cell);
                }
            }
        } finally {
            g.dispose();
        }
    }

    private void drawCell(Graphics2D g, Position position, int left, int top, int size) {
        g.setColor(cellColor(position));
        g.fillRoundRect(left + 1, top + 1, size - 2, size - 2, size / 7, size / 7);
        int cx = left + size / 2;
        int cy = top + size / 2;
        if (position.equals(grid.getTreasurePosition())) {
            int radius = size / 3;
            g.setColor(TREASURE);
            g.fillPolygon(new int[]{cx, cx + radius, cx, cx - radius},
                    new int[]{cy - radius, cy, cy + radius, cy}, 4);
        } else if (position.equals(grid.getStartPosition())) {
            g.setColor(WALL);
            g.drawOval(cx - size / 5, cy - size / 5, size * 2 / 5, size * 2 / 5);
        }
        if (position.equals(player)) {
            int diameter = size / 2;
            g.setColor(Color.WHITE);
            g.fillOval(cx - diameter / 2 - 1, cy - diameter / 2 - 1, diameter + 2, diameter + 2);
            g.setColor(PLAYER);
            g.fillOval(cx - diameter / 2, cy - diameter / 2, diameter, diameter);
        }
    }

    private Color cellColor(Position position) {
        if (grid.isWall(position.x(), position.y())) {
            return WALL;
        }
        if (path.contains(position)) {
            return PATH;
        }
        return explored.contains(position) ? EXPLORED : EMPTY;
    }

    @Override
    public Dimension getPreferredSize() {
        return grid == null ? getPreferredScrollableViewportSize()
                : new Dimension(grid.getWidth() * MIN_CELL + 2 * MARGIN,
                        grid.getHeight() * MIN_CELL + 2 * MARGIN);
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return new Dimension(500, 460);
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return MIN_CELL;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return Math.max(MIN_CELL, (orientation == javax.swing.SwingConstants.HORIZONTAL
                ? visibleRect.width : visibleRect.height) - MIN_CELL);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return getParent() != null && getPreferredSize().width <= getParent().getWidth();
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return getParent() != null && getPreferredSize().height <= getParent().getHeight();
    }
}
