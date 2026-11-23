package treasurehunt;

import treasurehunt.loader.LevelLoader;
import treasurehunt.model.Level;
import treasurehunt.search.AStar;
import treasurehunt.search.BFS;
import treasurehunt.search.DFS;
import treasurehunt.search.DFSRecursive;
import treasurehunt.search.Dijkstra;
import treasurehunt.search.SearchAlgorithm;
import treasurehunt.search.SearchResult;
import treasurehunt.ui.MainFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public final class Main {
    private static final List<SearchAlgorithm> ALGORITHMS =
            List.of(new BFS(), new DFS(), new DFSRecursive(), new Dijkstra(), new AStar());

    private Main() {
    }

    public static void main(String[] args) {
        boolean console = args.length > 0 && args[0].equals("--console");
        try {
            if (console) {
                runConsole(args);
            } else if (args.length != 0) {
                throw new IllegalArgumentException(usage());
            } else if (GraphicsEnvironment.isHeadless()) {
                throw new IllegalStateException("Swing needs a graphical desktop. " + usage());
            } else {
                List<Level> levels = LevelLoader.loadBundled();
                SwingUtilities.invokeLater(() -> showWindow(levels));
            }
        } catch (IOException | IllegalArgumentException | IllegalStateException e) {
            if (!console && args.length == 0 && !GraphicsEnvironment.isHeadless()) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, e.getMessage(),
                        "Treasure Hunt", JOptionPane.ERROR_MESSAGE));
            } else {
                System.err.println(e.getMessage());
                System.exit(2);
            }
        }
    }

    private static void showWindow(List<Level> levels) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ReflectiveOperationException | UnsupportedLookAndFeelException ignored) {
            // Swing's default look and feel remains available.
        }
        new MainFrame(levels, ALGORITHMS).setVisible(true);
    }

    private static void runConsole(String[] args) throws IOException {
        if (args.length > 4) {
            throw new IllegalArgumentException(usage());
        }
        List<Level> levels = args.length == 4 ? LevelLoader.load(Path.of(args[3])) : LevelLoader.loadBundled();
        int levelNumber = args.length > 1 ? Integer.parseInt(args[1]) : levels.get(0).getLevelNumber();
        Level level = levels.stream().filter(candidate -> candidate.getLevelNumber() == levelNumber).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown level " + levelNumber + "."));
        String algorithmName = args.length > 2 ? args[2] : "BFS";
        SearchAlgorithm algorithm = ALGORITHMS.stream()
                .filter(candidate -> candidate.name().equalsIgnoreCase(algorithmName))
                .findFirst().orElseThrow(() -> new IllegalArgumentException(usage()));

        SearchResult result = algorithm.solve(level.getInitialState());
        System.out.println(level + " | " + result.getAlgorithmName());
        if (result.isFound()) {
            System.out.printf("Solution: %s%nPath length: %d%nTotal cost: %d%n",
                    result.getSolutionMoves(), result.getPathLength(), result.getTotalCost());
        } else {
            System.out.println("No path to the treasure.");
        }
        System.out.printf(Locale.ROOT, "Expanded: %d%nDiscovered: %d%nTime: %.3f ms%n",
                result.getExpandedNodes(), result.getDiscoveredNodes(),
                result.getExecutionTimeNanos() / 1_000_000.0);
    }

    private static String usage() {
        return "Usage: java -jar treasure-hunt-1.0.0.jar [--console [level [algorithm [levels.txt]]]]"
                + " (BFS, \"DFS (iterative)\", \"DFS (recursive)\", Dijkstra, A*).";
    }
}
